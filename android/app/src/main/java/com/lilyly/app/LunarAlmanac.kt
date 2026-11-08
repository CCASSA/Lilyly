package com.lilyly.app

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.floor
import kotlin.math.roundToLong

// Retains Lilyly's mean-cycle estimate. The reference time is UTC, not local wall time.
private val reference = Instant.parse("2000-01-06T18:14:00Z").epochSecond
private const val cycleSeconds = 29.53058867 * 86400
private val quarters = listOf("New moon", "First quarter", "Full moon", "Last quarter")
private fun cycles(at: Instant) = (at.epochSecond - reference) / cycleSeconds
fun lunarFraction(at: Instant): Double = cycles(at).let { it - floor(it) }

fun moonPhaseName(now: LocalDateTime, zone: ZoneId = ZoneId.systemDefault()): String {
    val phase = lunarFraction(now.atZone(zone).toInstant())
    return when {
        phase < .03 || phase > .97 -> quarters[0]
        phase < .22 -> "Waxing crescent"
        phase < .28 -> quarters[1]
        phase < .47 -> "Waxing gibbous"
        phase < .53 -> quarters[2]
        phase < .72 -> "Waning gibbous"
        phase < .78 -> quarters[3]
        else -> "Waning crescent"
    }
}

data class LunarTurn(val date: LocalDate, val phase: String)

// Boundaries are local midnights; adding days to dates handles daylight-saving changes.
fun lunarTurns(from: LocalDate, until: LocalDate, zone: ZoneId): List<LunarTurn> {
    val start = from.atStartOfDay(zone).toInstant()
    val end = until.atStartOfDay(zone).toInstant()
    val first = floor(cycles(start) * 4).toLong()
    val last = floor(cycles(end) * 4).toLong() + 1
    return (first..last).mapNotNull { n ->
        val moment = Instant.ofEpochSecond(reference + (n * (cycleSeconds / 4)).roundToLong())
        if (moment < start || moment >= end) null
        else LunarTurn(moment.atZone(zone).toLocalDate(), quarters[Math.floorMod(n, 4L).toInt()])
    }
}
