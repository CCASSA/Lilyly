package com.lilyly.app

import java.net.URI
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import org.json.JSONArray
import org.json.JSONObject

data class SpotifyLink(val kind:String,val id:String) {val url:String get()="https://open.spotify.com/$kind/$id"}
fun spotifyLink(raw:String):SpotifyLink? = runCatching {
    val uri=URI(raw.trim())
    val bits=if(uri.scheme=="spotify") uri.schemeSpecificPart.split(':') else {
        require(uri.scheme=="https" && uri.host=="open.spotify.com" && uri.userInfo==null && uri.port==-1)
        uri.path.trim('/').split('/').let {if(it.firstOrNull()?.startsWith("intl-")==true)it.drop(1) else it}
    }
    require(bits.size==2 && bits[0] in listOf("track","playlist") && Regex("[A-Za-z0-9]{22}").matches(bits[1]))
    SpotifyLink(bits[0],bits[1])
}.getOrNull()
data class MusicAttachment(val url:String,val label:String="",val title:String="",val creator:String="") {
    fun toJson()=JSONObject().put("url",url).put("label",label).put("title",title).put("creator",creator)
}
fun musicAttachments(raw:String):List<MusicAttachment> = runCatching {val a=JSONArray(raw);(0 until a.length()).map {a.getJSONObject(it)}.mapNotNull {o -> spotifyLink(o.optString("url"))?.let {MusicAttachment(it.url,o.optString("label"),o.optString("title"),o.optString("creator"))}}}.getOrDefault(emptyList())
fun musicJson(items:List<MusicAttachment>)=JSONArray(items.map {it.toJson()}).toString()
fun pkceChallenge(verifier:String)=Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)))
fun oauthRandom():String {val bytes=ByteArray(32);SecureRandom().nextBytes(bytes);return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)}
fun spotifyForm(fields:Map<String,String>)=fields.entries.joinToString("&") {URLEncoder.encode(it.key,"UTF-8")+"="+URLEncoder.encode(it.value,"UTF-8")}
fun validSpotifyCallback(raw:String,redirect:String,state:String,started:Long,now:Long):Boolean = runCatching {
    val u=URI(raw);val expected=URI(redirect)
    val pairs=(u.rawQuery ?: "").split('&').map {it.split('=',limit=2)}
    val states=pairs.filter {it[0]=="state"}.map {java.net.URLDecoder.decode(it.getOrElse(1){""},"UTF-8")}
    u.scheme=="https" && u.host==expected.host && u.path==expected.path && u.port==expected.port && u.userInfo==null && u.fragment==null && state.isNotBlank() && states==listOf(state) && now-started in 0..600000
}.getOrDefault(false)
