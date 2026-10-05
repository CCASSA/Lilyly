package com.lilyly.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import org.json.JSONObject

class CycleEngineTest {
    private val start = LocalDate.of(2026, 9, 1)
    private val prefs = CyclePreferences()
    @Test fun missingBleedingDaysDoNotBecomeExtraPeriods() {
        val pattern = cyclePattern(listOf(start, start.plusDays(2), start.plusDays(4), start.plusDays(29)), prefs)
        assertEquals(listOf(start, start.plusDays(29)), pattern.starts)
        assertEquals(29, pattern.length)
    }
    @Test fun overdueCycleIsNeverSilentlyRolledForward() {
        val pattern = cyclePattern(listOf(start), prefs)
        val day = cycleDayState(start.plusDays(45), pattern, prefs)
        assertEquals(46, day.day)
        assertFalse(day.fertile)
        assertFalse(day.predictedPeriod)
        assertEquals("Cycle continuing", day.phase)
    }
    @Test fun noHistoryAndPregnancyDoNotInventCycles() {
        assertNull(cycleDayState(start, cyclePattern(emptyList(), prefs), prefs).day)
        assertNull(cycleDayState(start, cyclePattern(listOf(start), prefs), prefs.copy(mode = "Pregnancy")).day)
    }
    @Test fun irregularAndHormonalContextsSuppressFertility() {
        val irregular = cyclePattern(listOf(start, start.plusDays(20), start.plusDays(60)), prefs)
        assertTrue(irregular.irregular)
        assertFalse(cycleDayState(start.plusDays(74), irregular, prefs).fertile)
        assertFalse(cycleDayState(start.plusDays(13), cyclePattern(listOf(start), prefs), prefs.copy(contraception = "Hormonal")).fertile)
    }
    @Test fun historicalDatesUseTheirOwnPeriodStart() {
        val pattern = cyclePattern(listOf(start, start.plusDays(28)), prefs)
        assertEquals(5, cycleDayState(start.plusDays(4), pattern, prefs).day)
        assertNull(cycleDayState(start.minusDays(1), pattern, prefs).day)
    }
    @Test fun manualLengthCanOverrideHistory() {
        val manual = prefs.copy(length = 32, useHistory = false)
        assertEquals(32, cyclePattern(listOf(start, start.plusDays(28)), manual).length)
    }
    @Test fun existingCycleJsonKeepsNotesAndDefaultsNewFieldsSafely() {
        val old = CycleLog.fromJson(JSONObject("""{"id":"old","date":"2026-09-01","notes":"keep me","symptoms":"custom symptom","flow":"Heavy","period":true}"""))
        assertEquals("keep me", old.notes)
        assertFalse(old.ratingsRecorded)
        assertTrue(old.selections.isEmpty())
        val updated = old.copy(selections = setOf("Anxious", "My own tag"))
        val restored = CycleLog.fromJson(updated.toJson())
        assertEquals(updated, restored)
    }
    @Test fun journalMigrationKeepsLegacyWritingAndNewCanvas() {
        val old = JournalEntry.fromJson(JSONObject("""{"id":"page","body":"old writing","imageUri":"content://owned/photo","inkJson":"[]"}"""))
        assertEquals("old writing", old.body)
        assertEquals("[]", old.canvasJson)
        val piece = PagePiece(content = "A pressed thought", x = .2f, rotation = 12f)
        val updated = old.copy(canvasJson = piecesJson(listOf(piece)), favorite = true, notebook = "My grimoire")
        val restored = JournalEntry.fromJson(updated.toJson())
        assertEquals(updated, restored)
        assertEquals(piece, pagePieces(restored.canvasJson).single())
    }
    @Test fun quickCheckInDoesNotInventNumericObservations() {
        val quick = MentalCheckIn(feelings = setOf("Calm", "Tired"), detailedRatings = false)
        val restored = MentalCheckIn.fromJson(quick.toJson())
        assertFalse(restored.detailedRatings)
        assertEquals(quick.feelings, restored.feelings)
        assertTrue(MentalCheckIn.fromJson(JSONObject("""{"mood":7}""")).detailedRatings)
    }

}
