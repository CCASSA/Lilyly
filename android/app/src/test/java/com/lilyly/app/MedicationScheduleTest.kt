package com.lilyly.app
import org.junit.Test
import org.junit.Assert.*
import org.json.JSONObject
import java.time.LocalDateTime
class MedicationScheduleTest {
    private val now=LocalDateTime.parse("2026-10-07T09:00")
    @Test fun legacyRecordsKeepScheduleAndDoNotEnableReminders() {
        val med=Medication.fromJson(JSONObject("{\"id\":\"old\",\"name\":\"Original\",\"time\":\"08:00\"}"))
        assertEquals("08:00",med.time);assertFalse(med.reminders);assertEquals(-1,med.remaining)
        assertEquals("",MedicationLog.fromJson(JSONObject("{\"status\":\"Taken\"}")).scheduledFor)
    }
    @Test fun multipleTimesAreValidatedDeduplicatedAndOrdered() {
        assertTrue(validMedicationTimes("20:00, 08:00, 08:00"));assertEquals(2,medicationTimes("20:00,08:00,08:00").size)
        assertFalse(validMedicationTimes("24:00"));assertFalse(validMedicationTimes("08:00,wrong"))
    }
    @Test fun nextReminderSkipsLoggedSlotsAndUsesTomorrow() {
        val med=Medication(id="a",time="08:00,20:00",reminders=true)
        assertEquals(now.withHour(20),nextMedicationReminder(listOf(med),emptyList(),now))
        val log=MedicationLog(medicationId="a",scheduledFor="2026-10-07T20:00",status="Skipped")
        assertEquals(LocalDateTime.parse("2026-10-08T08:00"),nextMedicationReminder(listOf(med),listOf(log),now))
        assertFalse(pendingMedicationReminder(listOf(med),listOf(log),now.withHour(20)))
    }
    @Test fun archivedPrnAndDisabledNeverSchedule() {
        val base=Medication(reminders=true)
        assertNull(nextMedicationReminder(listOf(base.copy(active=false),base.copy(asNeeded=true),base.copy(reminders=false)),emptyList(),now))
    }
    @Test fun newFieldsRoundTripWithoutDiscardingNotes() {
        val med=Medication(asNeeded=true,remaining=12,refillAt=3,reason="keep")
        assertEquals(med,Medication.fromJson(med.toJson()))
        val log=MedicationLog(notes="Personal note",scheduledFor="2026-10-07T08:00")
        assertEquals(log,MedicationLog.fromJson(log.toJson()))
    }
}
