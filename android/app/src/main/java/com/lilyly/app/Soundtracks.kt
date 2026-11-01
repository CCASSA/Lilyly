package com.lilyly.app

import org.json.JSONObject

val soundtrackSpaces = mapOf("reading" to "Reading Nook", "sanctuary" to "Sanctuary")
fun uniqueMusic(items: List<MusicAttachment>): List<MusicAttachment> = items.mapNotNull { item ->
    spotifyLink(item.url)?.let { item.copy(url=it.url) }
}.distinctBy { it.url }
fun readSoundtracks(settings: JSONObject): Map<String,String> {
    val rooms=settings.optJSONObject("soundtracks") ?: return emptyMap()
    return soundtrackSpaces.keys.filter { rooms.has(it) }.associateWith {
        musicJson(uniqueMusic(musicAttachments(rooms.optString(it,"[]"))).take(12))
    }
}
