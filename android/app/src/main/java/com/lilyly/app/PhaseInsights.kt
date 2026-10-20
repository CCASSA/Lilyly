package com.lilyly.app

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class PhaseGroup(val label:String,val points:List<InsightPoint>,val cycles:Int) {
    val mean get()=points.map {it.value}.average()
}
data class PhaseComparison(val metric:String,val unit:String,val groups:List<PhaseGroup>)
data class PhaseReport(val comparisons:List<PhaseComparison>,val explanation:String)

/** Descriptive calendar grouping only; no inferred hormones, confirmed ovulation or causation. */
fun phaseInsights(sleeps:List<SleepRecord>,mind:List<MentalCheckIn>,logs:List<CycleLog>,
    enabled:Set<String>,days:Int,today:LocalDate,prefs:CyclePreferences):PhaseReport {
    fun empty(reason:String)=PhaseReport(emptyList(),reason)
    if("Cycle" !in enabled)return empty("Choose Cycle with Sanctuary or Sleep to explore estimated phase comparisons. Cycle alone can include explicitly recorded pain.")
    if(prefs.mode=="Pregnancy" || prefs.contraception=="Hormonal")return empty("Phase comparisons are paused for pregnancy mode or hormonal contraception because calendar phase estimates may not fit this context.")
    val first=today.minusDays(days.coerceIn(1,365).toLong()-1)
    fun date(raw:String)=runCatching {LocalDate.parse(raw)}.getOrNull()
    fun included(raw:String)=date(raw)?.let {it>=first && it<=today}==true
    val starts=cyclePattern(logs.filter {it.period}.mapNotNull {date(it.date)}.filter {it<=today},prefs).starts
    val intervals=starts.zipWithNext().filter {(a,b)->b>first && a<=today}
    val lengths=intervals.map {(a,b)->ChronoUnit.DAYS.between(a,b).toInt()}
    if(lengths.any {it !in 15..90} || (lengths.size>1 && lengths.max()-lengths.min()>7))
        return empty("The logged cycle intervals vary too much for this calendar-based comparison. Your daily records remain available in the calendar.")
    val eligible=intervals.zip(lengths).filter {(_,length)->length-prefs.lutealLength>prefs.periodLength+1}
    fun classify(raw:String):Pair<String,String>? {
        val day=date(raw)?:return null
        val interval=eligible.firstOrNull {(range,_)->day>=range.first && day<range.second}?:return null
        return cycleDayState(day,CyclePattern(listOf(interval.first.first),interval.second,false,1),prefs).phase to interval.first.first.toString()
    }
    val phases=listOf("Menstrual phase","Follicular phase","Ovulatory phase","Luteal phase")
    fun comparison(metric:String,unit:String,values:Map<String,Double>):PhaseComparison? {
        val matched=values.mapNotNull {(day,value)->classify(day)?.let {Triple(InsightPoint(day,value),it.first,it.second)}}
        val groups=phases.mapNotNull {phase ->
            val rows=matched.filter {it.second==phase}
            val cycles=rows.map {it.third}.distinct().size
            if(rows.size<5 || cycles<2)null else PhaseGroup("Estimated ${phase.lowercase()}",rows.map {it.first}.sortedBy {it.date},cycles)
        }
        return if(groups.size>=2)PhaseComparison(metric,unit,groups) else null
    }
    val checks=if("Sanctuary" in enabled)mind.filter {it.detailedRatings && included(it.dateTime.take(10))} else emptyList()
    fun ratings(get:(MentalCheckIn)->Int)=checks.filter {get(it) in 0..10}.groupBy {it.dateTime.take(10)}.mapValues {(_,rows)->rows.map {get(it)}.average()}
    val pain=logs.filter {it.ratingsRecorded && included(it.date) && it.pain in 0..10}.groupBy {it.date}.mapValues {(_,rows)->rows.map {it.pain}.average()}
    val rest=if("Sleep" in enabled)insightRest(sleeps.filter {included(it.date) && sleepMinutes(it.bedtime,it.wakeTime)!=null}).mapValues {it.value/60.0} else emptyMap()
    val comparisons=listOfNotNull(comparison("Mood","/10",ratings {it.mood}),comparison("Anxiety","/10",ratings {it.anxiety}),comparison("Energy","/10",ratings {it.energy}),comparison("Pain","/10",pain),comparison("Time in bed"," h",rest))
    return PhaseReport(comparisons,if(comparisons.isEmpty())
        "Each comparison needs at least two phases, each with 5 recorded dates across 2 completed logged cycles. Unfinished cycles and unrecorded ratings are excluded. There is no need to log more than feels right."
        else "Daily averages from completed logged cycles only. Differences describe these records; they do not show that a cycle phase caused a change.")
}
