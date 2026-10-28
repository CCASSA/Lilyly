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
/** Checkpoints replace the same interval; only newly elapsed seconds enter the total. */
fun keepReadingSession(book:LibraryBook,session:ReadingSession):LibraryBook {
    if(session.seconds<=0 || session.id.isBlank())return book
    if(session.firstPosition !in 0 until book.units || session.lastPosition !in 0 until book.units)return book
    if(runCatching {OffsetDateTime.parse(session.startedAt);OffsetDateTime.parse(session.endedAt)}.isFailure)return book
    val previous=book.sessions.firstOrNull {it.id==session.id}
    if(previous!=null && (previous.startedAt!=session.startedAt || previous.firstPosition!=session.firstPosition || session.seconds<previous.seconds))return book
    if(previous==session)return book
    val delta=session.seconds-(previous?.seconds ?: 0L)
    if(book.readingSeconds>Long.MAX_VALUE-delta)return book
    return book.copy(readingSeconds=book.readingSeconds+delta,
        sessions=if(previous==null)book.sessions+session else book.sessions.map {if(it.id==session.id)session else it})
}
fun removeReadingSession(book:LibraryBook,id:String):LibraryBook {
    val session=book.sessions.firstOrNull {it.id==id} ?: return book
    return book.copy(readingSeconds=(book.readingSeconds-session.seconds).coerceAtLeast(0),sessions=book.sessions.filterNot {it.id==id})
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
    private var sessionId=""
    fun begin(at:Int) {if(!active) {active=true;started=tick();stamp=now();position=at;sessionId=UUID.randomUUID().toString()}}
    fun checkpoint(at:Int):ReadingSession? {
        if(!active)return null
        val seconds=((tick()-started)/1_000_000_000).coerceAtLeast(0)
        return if(seconds==0L)null else ReadingSession(id=sessionId,startedAt=stamp,endedAt=now(),seconds=seconds,firstPosition=position,lastPosition=at)
    }
    fun pause(at:Int):ReadingSession? {
        val result=checkpoint(at)
        active=false
        return result
    }
}
