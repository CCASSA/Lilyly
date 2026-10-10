package com.lilyly.app
import org.junit.Assert.*
import org.junit.Test
class SpotifyModelsTest {
    @Test fun pkceMatchesRfc7636() {assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",pkceChallenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"))}
    @Test fun linksAreCanonicalAndRestricted() {
        val id="1234567890123456789012"
        assertEquals("https://open.spotify.com/track/$id",spotifyLink("https://open.spotify.com/intl-en/track/$id?si=secret")?.url)
        assertEquals("playlist",spotifyLink("spotify:playlist:$id")?.kind)
        listOf("http://open.spotify.com/track/$id","https://open.spotify.com.evil.test/track/$id","https://evil@open.spotify.com/track/$id","https://open.spotify.com/album/$id","https://open.spotify.com/track/short").forEach {assertNull(spotifyLink(it))}
    }
    @Test fun callbackRejectsWrongStateHostReplayAndExpiry() {
        val redirect="https://music.example.org/spotify/callback"
        assertTrue(validSpotifyCallback("$redirect?code=a&state=expected",redirect,"expected",1000,2000))
        assertFalse(validSpotifyCallback("$redirect?code=a&state=wrong",redirect,"expected",1000,2000))
        assertFalse(validSpotifyCallback("$redirect?state=expected&state=expected",redirect,"expected",1000,2000))
        assertFalse(validSpotifyCallback("$redirect?state=expected",redirect,"expected",1000,700001))
        assertFalse(validSpotifyCallback("https://evil.test/spotify/callback?state=expected",redirect,"expected",1000,2000))
    }
    @Test fun attachmentsPreserveCaptionAndMetadata() {
        val item=MusicAttachment("https://open.spotify.com/track/1234567890123456789012","My memory","Title","Artist")
        assertEquals(listOf(item),musicAttachments(musicJson(listOf(item))))
        assertEquals(emptyList<MusicAttachment>(),musicAttachments("invalid"))
    }
}
