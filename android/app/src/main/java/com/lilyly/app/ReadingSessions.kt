package com.lilyly.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.OffsetDateTime
import java.util.UUID

data class ReadingSession(val id:String=UUID.randomUUID().toString(),val startedAt:String,val endedAt:String,
    val seconds:Long,val firstPosition:Int,val lastPosition:Int) {
    fun toJson()=JSONObject().put("id",id).put("startedAt",startedAt).put("endedAt",endedAt).put("seconds",seconds).put("firstPosition",firstPosition).put("lastPosition",lastPosition)
}
fun readSessions(array:JSONArray?,units:Int):List<ReadingSession> = if(array==null) emptyList() else
    (0 until array.length()).mapNotNull {index ->runCatching {
        val o=array.getJSONObject(index)
        val row=ReadingSession(o.getString("id"),o.getString("startedAt"),o.getString("endedAt"),o.getLong("seconds"),o.getInt("firstPosition"),o.getInt("lastPosition"))
        require(row.id.isNotBlank() && row.seconds>0 && row.firstPosition in 0 until units && row.lastPosition in 0 until units)
        OffsetDateTime.parse(row.startedAt);OffsetDateTime.parse(row.endedAt)
        row
    }.getOrNull()}.distinctBy {it.id}
fun keepReadingSession(book:LibraryBook,session:ReadingSession):LibraryBook {
    if(session.seconds<=0 || book.sessions.any {it.id==session.id})return book
    if(session.firstPosition !in 0 until book.units || session.lastPosition !in 0 until book.units)return book
    return book.copy(readingSeconds=book.readingSeconds+session.seconds,sessions=book.sessions+session)
}
fun readingDuration(seconds:Long):String {
    val value=seconds.coerceAtLeast(0)
    return when {value<60 -> "$value sec";value<3600 -> "${value/60} min";else -> "${value/3600} hr ${value%3600/60} min"}
}
/** Monotonic elapsed time; wall-clock changes cannot invent or subtract reading time. */
class ReadingSessionTimer(private val now:()->String={OffsetDateTime.now().toString()},private val tick:()->Long={System.nanoTime()}) {
    private var active=false
    private var started=0L
    private var stamp=""
    private var position=0
    fun begin(at:Int) {if(!active) {active=true;started=tick();stamp=now();position=at}}
    fun pause(at:Int):ReadingSession? {
        if(!active)return null
        active=false
        val seconds=((tick()-started)/1_000_000_000).coerceAtLeast(0)
        return if(seconds==0L)null else ReadingSession(startedAt=stamp,endedAt=now(),seconds=seconds,firstPosition=position,lastPosition=at)
    }
}
