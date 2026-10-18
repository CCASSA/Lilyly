package com.lilyly.app

import org.json.JSONArray
import org.json.JSONObject

fun readingCopySnapshot(entries:List<JournalEntry>):String=JSONArray(entries.map {it.toJson()}).toString()
fun readingCopyEntries(raw:String):List<JournalEntry> {
    val a=JSONArray(raw)
    return (0 until a.length()).map {JournalEntry.fromJson(a.getJSONObject(it))}
}
fun readingCopyElementText(piece:PagePiece):String = if(piece.kind=="tarot") {
    runCatching {val o=JSONObject(piece.content);listOf(o.optString("position"),o.optString("name"),if(o.optBoolean("reversed"))"Reversed" else "Upright").filter {it.isNotBlank()}.joinToString(" · ")}.getOrDefault(piece.content)
} else piece.content
fun readingCopyFilename(title:String):String=title.replace(Regex("[\\\\/:*?\"<>|\\p{Cntrl}]"),"-").trim().take(80).ifBlank {"Lilyly-reading-copy"}+".pdf"
