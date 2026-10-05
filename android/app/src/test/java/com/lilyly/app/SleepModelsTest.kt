package com.lilyly.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class SleepModelsTest {
    @Test fun crossesMidnightAndRejectsReversedOrImpossibleTimes() {
        assertEquals(465L,sleepMinutes("2026-10-01T23:15","2026-10-02T07:00"))
        assertNull(sleepMinutes("2026-10-02T07:00","2026-10-01T23:15"))
        assertNull(sleepMinutes("2026-10-01T07:00","2026-10-03T07:00"))
        assertNull(sleepMinutes("invalid","2026-10-03T07:00"))
    }
    @Test fun dreamDetailsRoundTripWithoutLosingTheJournalLink() {
        val r=SleepRecord(bedtime="2026-10-01T23:00",wakeTime="2026-10-02T07:00",dream="A garden",symbols=setOf("Moon","Garden"),lucid=true,nightmare=false,journalId="page")
        assertEquals(r,SleepRecord.fromJson(r.toJson()))
        assertEquals(listOf("garden" to 2,"moon" to 2),recurringDreamSymbols(listOf(r,r.copy(id="other",symbols=setOf("moon","garden")))))
    }
    @Test fun doesNotInventPatternsFromMissingRatingsOrSmallSamples() {
        val sleeps=(0..9).map { i -> val day=LocalDate.of(2026,9,1).plusDays(i.toLong());SleepRecord(bedtime=day.atTime(0,0).toString(),wakeTime=day.atTime(if(i<5) 5 else 8,0).toString()) }
        val quick=sleeps.map { MentalCheckIn(dateTime=it.wakeTime,detailedRatings=false) }
        assertEquals(1,sleepPatterns(sleeps,quick,true).size)
        val detailed=quick.map { it.copy(detailedRatings=true) }
        assertEquals(2,sleepPatterns(sleeps,detailed,true).size)
        assertEquals(1,sleepPatterns(sleeps,detailed,false).size)
        assertTrue(sleepPatterns(sleeps.take(3),detailed,true).isEmpty())
    }
}
