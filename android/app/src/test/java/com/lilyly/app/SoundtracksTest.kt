package com.lilyly.app
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
class SoundtracksTest {
    private val song=MusicAttachment("https://open.spotify.com/track/1234567890123456789012","Quiet pages")
    @Test fun catalogueCanonicalizesAndDoesNotDuplicate() {
        assertEquals(listOf(song),uniqueMusic(listOf(song,song.copy(url= song.url+"?si=share"),song.copy(url="https://evil.test/song"))))
    }
    @Test fun oldSettingsAndInvalidRoomDataAreSafe() {
        assertTrue(readSoundtracks(JSONObject()).isEmpty())
        assertEquals("[]",readSoundtracks(JSONObject().put("soundtracks",JSONObject().put("reading","broken")))["reading"])
    }
    @Test fun roomsSurviveBackupMergeWithoutOverwritingCurrentChoices() {
        val current=JSONObject().put("soundtracks",JSONObject().put("reading",musicJson(listOf(song))))
        val incoming=JSONObject().put("soundtracks",JSONObject().put("reading","[]").put("sanctuary",musicJson(listOf(song.copy(label="Rest")))))
        val restored=readSoundtracks(JSONObject(mergeSnapshots(mapOf("settings" to current.toString()),mapOf("settings" to incoming.toString())).getValue("settings")))
        assertEquals("Quiet pages",musicAttachments(restored.getValue("reading")).single().label)
        assertEquals("Rest",musicAttachments(restored.getValue("sanctuary")).single().label)
    }
}
