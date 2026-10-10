package com.lilyly.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SpotifySession(context:Context) {
    private val secure=SecurePreferences(context.applicationContext,"lilyly_spotify")
    val configured get()=BuildConfig.SPOTIFY_CLIENT_ID.matches(Regex("[a-fA-F0-9]{32}")) && !BuildConfig.SPOTIFY_REDIRECT_HOST.endsWith(".invalid")
    val redirect get()="https://${BuildConfig.SPOTIFY_REDIRECT_HOST}${BuildConfig.SPOTIFY_REDIRECT_PATH}"
    val linked get()=secure.get("tokens").isNotBlank()
    val hasCallback get()=secure.get("callback").isNotBlank()
    companion object {private val lock=Any()}
    fun disconnect()=synchronized(lock) {secure.put("revision",oauthRandom());secure.put("tokens","");secure.put("pending","");secure.put("callback","")}
    fun begin():Intent = synchronized(lock) {
        check(configured) {"Spotify developer setup is needed for this build."}
        val verifier=oauthRandom();val state=oauthRandom()
        secure.put("pending",JSONObject().put("verifier",verifier).put("state",state).put("started",System.currentTimeMillis()).toString());secure.put("callback","")
        Intent(Intent.ACTION_VIEW,Uri.parse("https://accounts.spotify.com/authorize?"+spotifyForm(mapOf("client_id" to BuildConfig.SPOTIFY_CLIENT_ID,"response_type" to "code","redirect_uri" to redirect,"state" to state,"code_challenge_method" to "S256","code_challenge" to pkceChallenge(verifier)))))
    }
    fun acceptCallback(raw:String):Boolean = synchronized(lock) {
        val p=runCatching {JSONObject(secure.get("pending"))}.getOrNull() ?: return@synchronized false
        if(!configured || !validSpotifyCallback(raw,redirect,p.optString("state"),p.optLong("started"),System.currentTimeMillis()))return@synchronized false
        secure.put("callback",raw);true
    }
    suspend fun complete()=withContext(Dispatchers.IO) {
        val pending=synchronized(lock) {
            val raw=secure.get("callback");val p=JSONObject(secure.get("pending","{}"))
            check(validSpotifyCallback(raw,redirect,p.optString("state"),p.optLong("started"),System.currentTimeMillis())) {"This sign-in expired. Please connect again."}
            secure.put("callback","");secure.put("pending","")
            Triple(raw,p.optString("verifier"),secure.get("revision"))
        }
        val uri=Uri.parse(pending.first)
        check(uri.getQueryParameter("error")==null) {"Spotify sign-in was cancelled. You can try again."}
        val code=uri.getQueryParameter("code");check(!code.isNullOrBlank()) {"Spotify did not return an authorization code."}
        val result=request("https://accounts.spotify.com/api/token",mapOf("grant_type" to "authorization_code","code" to code,"redirect_uri" to redirect,"client_id" to BuildConfig.SPOTIFY_CLIENT_ID,"code_verifier" to pending.second))
        saveTokens(result,pending.third)
    }
    private fun saveTokens(o:JSONObject,revision:String,oldRefresh:String="")=synchronized(lock) {
        check(secure.get("revision")==revision) {"Spotify was disconnected."}
        check(o.optString("access_token").isNotBlank()) {"Spotify returned no access token."}
        val refresh=o.optString("refresh_token").ifBlank {oldRefresh};check(refresh.isNotBlank()) {"Spotify returned no refresh token. Please connect again."}
        secure.put("tokens",JSONObject().put("access",o.getString("access_token")).put("refresh",refresh).put("expires",System.currentTimeMillis()+o.optLong("expires_in",3600)*1000).toString())
    }
    suspend fun resolve(link:SpotifyLink):MusicAttachment=withContext(Dispatchers.IO) {
        val revision=secure.get("revision")
        var token=runCatching {JSONObject(secure.get("tokens"))}.getOrNull() ?: error("Connect Spotify in Settings first, or keep this as a link.")
        if(token.optLong("expires")<System.currentTimeMillis()+60000) {
            val result=request("https://accounts.spotify.com/api/token",mapOf("grant_type" to "refresh_token","refresh_token" to token.getString("refresh"),"client_id" to BuildConfig.SPOTIFY_CLIENT_ID))
            saveTokens(result,revision,token.getString("refresh"));token=JSONObject(secure.get("tokens"))
        }
        val o=request("https://api.spotify.com/v1/${if(link.kind=="track") "tracks" else "playlists"}/${link.id}",bearer=token.getString("access"))
        check(secure.get("revision")==revision) {"Spotify was disconnected."}
        val creator=if(link.kind=="track") o.optJSONArray("artists")?.let {a ->(0 until a.length()).map {a.getJSONObject(it).optString("name")}.joinToString(", ")}.orEmpty() else o.optJSONObject("owner")?.optString("display_name").orEmpty()
        MusicAttachment(link.url,title=o.optString("name"),creator=creator)
    }
    private fun request(url:String,form:Map<String,String>?=null,bearer:String?=null):JSONObject {
        val c=URL(url).openConnection() as HttpURLConnection
        try {
            c.connectTimeout=15000;c.readTimeout=15000;c.instanceFollowRedirects=false
            if(bearer!=null)c.setRequestProperty("Authorization","Bearer $bearer")
            if(form!=null){c.requestMethod="POST";c.doOutput=true;c.setRequestProperty("Content-Type","application/x-www-form-urlencoded");c.outputStream.use {it.write(spotifyForm(form).toByteArray(Charsets.UTF_8))}}
            val status=c.responseCode
            check(status in 200..299) {when(status){400,401 -> "Spotify authorization expired or was rejected. Disconnect and connect again.";403 -> "Spotify has not granted this account access. Check developer access and account eligibility.";404 -> "This Spotify item is unavailable to this account.";429 -> "Spotify is busy. Please try again later.";else -> "Spotify could not complete the request ($status)."}}
            return JSONObject(c.inputStream.bufferedReader().use {it.readText()})
        } finally {c.disconnect()}
    }
}
