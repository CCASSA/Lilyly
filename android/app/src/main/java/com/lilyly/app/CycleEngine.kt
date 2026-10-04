package com.lilyly.app

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/** Pure date calculations; never advances an unconfirmed period into a new cycle. */
data class CyclePreferences(
    val length: Int = 28,
    val periodLength: Int = 5,
    val lutealLength: Int = 14,
    val pmsDays: Int = 5,
    val mode: String = "Cycle",
    val contraception: String = "None",
    val useHistory: Boolean = true
)

data class CyclePattern(val starts: List<LocalDate>, val length: Int, val irregular: Boolean, val samples: Int)

fun cyclePattern(dates: List<LocalDate>, preferences: CyclePreferences): CyclePattern {
    val sorted = dates.distinct().sorted()
    // A missing daily log must not turn each bleeding day into a new period.
    val starts = sorted.filterIndexed { i, d -> i == 0 || ChronoUnit.DAYS.between(sorted[i - 1], d) > 10 }
    val intervals = starts.zipWithNext().map { (a, b) -> ChronoUnit.DAYS.between(a, b).toInt() }.takeLast(6)
    val plausible = intervals.filter { it in 15..90 }
    val length = if (preferences.useHistory && plausible.isNotEmpty()) plausible.average().roundToInt() else preferences.length
    val irregular = intervals.any { it !in 15..90 } || (plausible.size > 1 && plausible.max() - plausible.min() > 7)
    return CyclePattern(starts, length, irregular, plausible.size)
}

data class CycleDayState(val day: Int?, val phase: String, val predictedPeriod: Boolean = false, val fertile: Boolean = false, val ovulation: Boolean = false)

fun cycleDayState(date: LocalDate, pattern: CyclePattern, preferences: CyclePreferences): CycleDayState {
    if (preferences.mode == "Pregnancy") return CycleDayState(null, "Pregnancy mode")
    val start = pattern.starts.lastOrNull { !it.isAfter(date) } ?: return CycleDayState(null, "Awaiting a period log")
    val elapsed = ChronoUnit.DAYS.between(start, date).toInt()
    val day = elapsed + 1
    val predicted = elapsed in pattern.length until pattern.length + preferences.periodLength
    if (elapsed >= pattern.length) return CycleDayState(day, if (predicted) "Estimated period" else "Cycle continuing", predicted)
    val ovulationDay = (pattern.length - preferences.lutealLength).coerceAtLeast(preferences.periodLength + 1)
    val showFertility = !pattern.irregular && preferences.contraception != "Hormonal"
    val phase = when {
        day <= preferences.periodLength -> "Menstrual phase"
        day < ovulationDay - 1 -> "Follicular phase"
        day <= ovulationDay + 1 -> "Ovulatory phase"
        else -> "Luteal phase"
    }
    return CycleDayState(day, phase, fertile = showFertility && day in (ovulationDay - 5)..(ovulationDay + 1), ovulation = showFertility && day == ovulationDay)
}
