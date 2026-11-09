package com.lilyly.app
import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test
class CycleHistoryTest {
    private val today=LocalDate.parse("2024-03-07")
    private fun row(day:String,p:Boolean=false)=CycleLog(date=day,period=p)
    @Test fun missingDaysStayMissingAndLatestIsOpen() {
        val chapters=cycleChapters(listOf(row("2024-01-31",true),row("2024-02-02",true),row("2024-02-29"),row("2024-03-01",true)),today)
        assertEquals(listOf(7L,30L),chapters.map {it.days})
        assertTrue(chapters.first().ongoing);assertFalse(chapters.last().ongoing)
        assertEquals(3,chapters.last().logs.size);assertEquals(2,chapters.last().bleedingDays)
        assertEquals("2024-02-29",chapters.last().through.toString())
    }
    @Test fun futureMalformedAndSpottingAloneCannotOpenHistory() {
        assertTrue(cycleChapters(listOf(row("bad",true),row("2024-03-08",true),row("2024-03-06").copy(spotting=true)),today).isEmpty())
    }
    @Test fun correctingAnOldStartRegroupsRecordsWithoutDiscardingNotes() {
        val earlier=row("2024-01-01",true); val second=row("2024-02-01",true).copy(notes="retained")
        assertEquals(2,cycleChapters(listOf(second,earlier),today).size)
        val corrected=cycleChapters(listOf(second.copy(period=false),earlier),today).single()
        assertEquals("retained",corrected.logs.last().second.notes);assertEquals(1,corrected.bleedingDays)
    }
}
