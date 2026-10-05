package com.lilyly.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.util.UUID

data class LibraryBook(
    val id: String = UUID.randomUUID().toString(), val title: String, val author: String = "",
    val format: String, val units: Int, val position: Int = 0, val favorite: Boolean = false,
    val bookmarks: Set<Int> = emptySet(), val addedAt: String = LocalDateTime.now().toString(),
    val lastReadAt: String = "", val readingSeconds: Long = 0, val completed: Boolean = false
) {
    fun toJson() = JSONObject().put("id",id).put("title",title).put("author",author).put("format",format).put("units",units)
        .put("position",position).put("favorite",favorite).put("bookmarks",JSONArray(bookmarks.toList())).put("addedAt",addedAt)
        .put("lastReadAt",lastReadAt).put("readingSeconds",readingSeconds).put("completed",completed)
    companion object { fun fromJson(o: JSONObject): LibraryBook {
        val units = o.getInt("units").coerceAtLeast(1)
        return LibraryBook(o.getString("id"),o.getString("title"),o.optString("author"),o.getString("format"),units,
            o.optInt("position").coerceIn(0,units-1),o.optBoolean("favorite"),o.optJSONArray("bookmarks")?.let { a -> (0 until a.length()).map { a.getInt(it) }.filter { it in 0 until units }.toSet() } ?: emptySet(),
            o.optString("addedAt"),o.optString("lastReadAt"),o.optLong("readingSeconds"),o.optBoolean("completed"))
    } }
}
data class BookNote(val id: String = UUID.randomUUID().toString(), val bookId: String, val position: Int,
    val quote: String = "", val note: String = "", val date: String = LocalDateTime.now().toString(), val journalId: String = "") {
    fun toJson() = JSONObject().put("id",id).put("bookId",bookId).put("position",position).put("quote",quote).put("note",note).put("date",date).put("journalId",journalId)
    companion object { fun fromJson(o: JSONObject) = BookNote(o.getString("id"),o.getString("bookId"),o.optInt("position"),o.optString("quote"),o.optString("note"),o.optString("date"),o.optString("journalId")) }
}
data class ReadingUnit(val title: String, val text: String)
