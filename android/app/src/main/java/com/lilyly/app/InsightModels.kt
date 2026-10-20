package com.lilyly.app

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

val insightSources=listOf("Sleep","Sanctuary","Cycle","Dreams")
data class InsightPoint(val date:String,val value:Double)
data class InsightGroup(val label:String,val points:List<InsightPoint>) {
    val mean:Double get()=points.map {it.value}.average()
}
data class InsightComparison(val title:String,val metric:String,val groups:List<InsightGroup>)
data class InsightReport(val comparisons:List<InsightComparison>,val symbols:List<Pair<String,Int>>,val dates:Int)
fun insightNumber(value:Double)=String.format(Locale.ROOT,"%.1f",value)
/** Exploratory same-wake-date comparisons only. No imputed ratings or causal/diagnostic inference. */
fun personalInsights(sleeps:List<SleepRecord>,mind:List<MentalCheckIn>,cycles:List<CycleLog>,enabled:Set<String>,days:Int,today:LocalDate):InsightReport {
    val start=today.minusDays(days.coerceIn(1,365).toLong()-1)
    fun included(raw:String)=runCatching {LocalDate.parse(raw).let {it>=start && it<=today}}.getOrDefault(false)
    val sleepRows=if("Sleep" in enabled)sleeps.filter {included(it.date) && sleepMinutes(it.bedtime,it.wakeTime)!=null} else emptyList()
    val rest=insightRest(sleepRows)
    val checkIns=if("Sanctuary" in enabled)mind.filter {it.detailedRatings && included(it.dateTime.take(10))} else emptyList()
    fun ratings(get:(MentalCheckIn)->Int)=checkIns.filter {get(it) in 0..10}.groupBy {it.dateTime.take(10)}.mapValues {(_,v)->v.map {get(it)}.average()}
    val cycleRows=if("Cycle" in enabled)cycles.filter {it.ratingsRecorded && included(it.date) && it.pain in 0..10} else emptyList()
    val pain=cycleRows.groupBy {it.date}.mapValues {(_,v)->v.map {it.pain}.average()}
    fun compare(title:String,metric:String,values:Map<String,Double>):InsightComparison? {
        val matched=values.mapNotNull {(date,value)->rest[date]?.let {Triple(date,value,it)}}
        val shorter=matched.filter {it.third<360}.map {InsightPoint(it.first,it.second)}.sortedBy {it.date}
        val longer=matched.filter {it.third>=420}.map {InsightPoint(it.first,it.second)}.sortedBy {it.date}
        if(shorter.size<5 || longer.size<5)return null
        return InsightComparison(title,metric,listOf(InsightGroup("Under 6h in bed",shorter),InsightGroup("At least 7h in bed",longer)))
    }
    val comparisons=listOfNotNull(compare("Rest & anxiety","Anxiety",ratings {it.anxiety}),compare("Rest & mood","Mood",ratings {it.mood}),compare("Rest & energy","Energy",ratings {it.energy}),compare("Rest & pain","Pain",pain))
    val dreamRows=if("Dreams" in enabled)sleeps.filter {included(it.date) && (it.dream.isNotBlank() || it.symbols.isNotEmpty())} else emptyList()
    val dates=(rest.keys + checkIns.map {it.dateTime.take(10)} + cycleRows.map {it.date} + dreamRows.map {it.date}).toSet().size
    return InsightReport(comparisons,recurringDreamSymbols(dreamRows),dates)
}

/** Valid non-overlapping local-time records, summed by waking date. */
internal fun insightRest(rows:List<SleepRecord>):Map<String,Long> {
    return rows.groupBy {it.date}.mapNotNull {(date,rows) ->
        val sorted=rows.sortedBy {it.bedtime}
        val overlaps=sorted.zipWithNext().any {(a,b)->LocalDateTime.parse(a.wakeTime)>LocalDateTime.parse(b.bedtime)}
        val minutes=rows.sumOf {it.durationMinutes}
        if(overlaps || minutes>1440) null else date to minutes
    }.toMap()
}
