package com.lilyly.app

import java.time.LocalDateTime
import java.time.LocalTime

/** Daily wall-clock times. Legacy single-time values remain supported. */
fun medicationTimes(value:String):List<LocalTime> = value.split(',').mapNotNull {
    runCatching {LocalTime.parse(it.trim())}.getOrNull()
}.distinct().sorted()
fun validMedicationTimes(value:String):Boolean = value.isNotBlank() && value.split(',').all {
    Regex("[0-2][0-9]:[0-5][0-9]").matches(it.trim()) && runCatching {LocalTime.parse(it.trim())}.isSuccess
}
fun nextMedicationReminder(medications:List<Medication>,logs:List<MedicationLog>,now:LocalDateTime):LocalDateTime? =
    medications.filter {it.active && it.reminders && !it.asNeeded}.flatMap {med ->
        (0L..1L).flatMap {day -> medicationTimes(med.time).map {now.toLocalDate().plusDays(day).atTime(it)} }
            .filter {at -> at.isAfter(now) && logs.none {it.medicationId==med.id && it.scheduledFor==at.toString()} }
    }.minOrNull()
fun pendingMedicationReminder(medications:List<Medication>,logs:List<MedicationLog>,at:LocalDateTime):Boolean = medications.any { med ->
    med.active && med.reminders && !med.asNeeded && at.toLocalTime() in medicationTimes(med.time) && logs.none {it.medicationId==med.id && it.scheduledFor==at.toString()}
}
