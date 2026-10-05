package com.lilyly.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

data class SleepRecord(
    val id: String = UUID.randomUUID().toString(), val bedtime: String, val wakeTime: String,
    val quality: String = "", val interruptions: Int = 0, val wakingEnergy: String = "",
    val dream: String = "", val dreamMood: String = "", val symbols: Set<String> = emptySet(),
    val people: String = "", val places: String = "", val nightmare: Boolean = false,
    val lucid: Boolean = false, val notes: String = "", val journalId: String = ""
) {
    val date: String get() = wakeTime.take(10)
    val durationMinutes: Long get() = sleepMinutes(bedtime,wakeTime) ?: 0
    fun toJson() = JSONObject().put("id",id).put("bedtime",bedtime).put("wakeTime",wakeTime).put("quality",quality)
        .put("interruptions",interruptions).put("wakingEnergy",wakingEnergy).put("dream",dream).put("dreamMood",dreamMood)
        .put("symbols",JSONArray(symbols.toList())).put("people",people).put("places",places).put("nightmare",nightmare)
        .put("lucid",lucid).put("notes",notes).put("journalId",journalId)
    companion object { fun fromJson(o: JSONObject) = SleepRecord(o.getString("id"),o.getString("bedtime"),o.getString("wakeTime"),o.optString("quality"),o.optInt("interruptions"),o.optString("wakingEnergy"),o.optString("dream"),o.optString("dreamMood"),o.optJSONArray("symbols")?.let { a -> (0 until a.length()).map { a.getString(it) }.toSet() } ?: emptySet(),o.optString("people"),o.optString("places"),o.optBoolean("nightmare"),o.optBoolean("lucid"),o.optString("notes"),o.optString("journalId")) }
}
/** Local wall-clock duration; timezone changes and DST cannot be inferred from a manual log. */
fun sleepMinutes(bedtime: String, wakeTime: String): Long? = runCatching {
    Duration.between(LocalDateTime.parse(bedtime),LocalDateTime.parse(wakeTime)).toMinutes().takeIf { it in 1..1440 }
}.getOrNull()
fun sleepDurationLabel(minutes: Long) = "${minutes/60}h ${minutes%60}m"
fun recurringDreamSymbols(records: List<SleepRecord>): List<Pair<String,Int>> = records.flatMap { it.symbols.map(String::trim).filter(String::isNotEmpty).map(String::lowercase).toSet() }
    .groupingBy { it }.eachCount().filterValues { it>=2 }.toList().sortedWith(compareByDescending<Pair<String,Int>> { it.second }.thenBy { it.first })

data class PersonalPattern(val title: String, val detail: String)
/** Descriptive within-person summaries. Never fill missing observations or infer a cause. */
fun sleepPatterns(sleeps: List<SleepRecord>, checkIns: List<MentalCheckIn>, includeMind: Boolean): List<PersonalPattern> {
    val nights=sleeps.groupBy { it.date }.mapValues { (_,records) -> records.sumOf { it.durationMinutes }.coerceAtMost(1440) }
    val result=mutableListOf<PersonalPattern>()
    if(nights.size>=7) {
        val mean=nights.values.average().toLong()
        result.add(PersonalPattern("Your recorded rest", "Across ${nights.size} recorded dates: ${sleepDurationLabel(mean)} on average. This describes logged time in bed, not measured time asleep."))
    }
    if(includeMind) {
        val mood=checkIns.filter { it.detailedRatings }.groupBy { it.dateTime.take(10) }.mapValues { (_,v) -> v.map { it.anxiety }.average() }
        val matched=nights.mapNotNull { (date,minutes) -> mood[date]?.let { minutes to it } }
        // Require usable observations in both groups, not merely many check-ins on one date.
        val shorter=matched.filter { it.first<360 };val longer=matched.filter { it.first>=420 }
        if(shorter.size>=5 && longer.size>=5) {
            result.add(PersonalPattern("Rest & anxiety, side by side", "On ${shorter.size} logged dates with under 6h of rest, your average recorded anxiety was ${"%.1f".format(java.util.Locale.ROOT,shorter.map { it.second }.average())}/10. On ${longer.size} dates with at least 7h, it was ${"%.1f".format(java.util.Locale.ROOT,longer.map { it.second }.average())}/10. Same-date association only; many other factors may matter."))
        }
    }
    return result
}
