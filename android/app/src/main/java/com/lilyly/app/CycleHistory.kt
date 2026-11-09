package com.lilyly.app

import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal data class CycleChapter(
    val start:LocalDate, val through:LocalDate, val ongoing:Boolean,
    val logs:List<Pair<LocalDate,CycleLog>>
) {
    val days:Long get()=ChronoUnit.DAYS.between(start,through)+1
    val bleedingDays:Int get()=logs.count { it.second.period }
}

// Reuse the wheel's grouping rule. Missing dates, projections and spotting alone
// must never create a period start or count as observed bleeding days.
internal fun cycleChapters(records:List<CycleLog>,today:LocalDate):List<CycleChapter> {
    val logs=records.mapNotNull { row -> runCatching {LocalDate.parse(row.date)}.getOrNull()?.let {it to row} }
        .filter {it.first<=today}.distinctBy {it.first}.sortedBy {it.first}
    val starts=cyclePattern(logs.filter {it.second.period}.map {it.first},CyclePreferences()).starts
    var cursor=0
    return starts.mapIndexed {index,start ->
        val next=starts.getOrNull(index+1)
        val through=next?.minusDays(1) ?: today
        while(cursor<logs.size && logs[cursor].first<start) cursor++
        val begin=cursor
        while(cursor<logs.size && logs[cursor].first<=through) cursor++
        CycleChapter(start,through,next==null,logs.subList(begin,cursor))
    }.asReversed()
}
