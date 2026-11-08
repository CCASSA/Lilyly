package com.lilyly.app

import org.junit.Assert.*
import org.junit.Test
import java.time.*

class LunarAlmanacTest {
    @Test fun referenceNewMoonFallsOnDifferentLocalDates() {
        val from=LocalDate.of(2000,1,6)
        val utc=lunarTurns(from,from.plusDays(2),ZoneOffset.UTC).single()
        val east=lunarTurns(from,from.plusDays(2),ZoneId.of("Pacific/Auckland")).single()
        assertEquals(LunarTurn(from,"New moon"),utc)
        assertEquals(utc.copy(date=from.plusDays(1)),east)
    }
    @Test fun joiningMonthsDoesNotDropOrDoubleCountTurnsAcrossDst() {
        val a=LocalDate.of(2025,3,1);val b=a.plusMonths(1);val c=b.plusMonths(1)
        val zone=ZoneId.of("Europe/London")
        val all=lunarTurns(a,c,zone)
        assertEquals(8,all.size)
        assertEquals(all,lunarTurns(a,b,zone)+lunarTurns(b,c,zone))
        assertTrue(all.zipWithNext().all { (first,next) -> next.date.toEpochDay()-first.date.toEpochDay() in 6L..9L })
    }
    @Test fun moonPageKeepsReflectionDateAndCreationTimeSeparateAcrossRestore() {
        val date=LocalDate.of(2000,1,6)
        val draft=moonReflectionDraft(date,ZoneOffset.UTC)
        val page=JournalEntry.fromJson(draft.toJson())
        assertEquals(date.toString(),page.reflectionDate)
        assertNotEquals(date.toString(),page.createdAt.take(10))
        assertTrue(pagePieces(page.canvasJson).single().content.contains(date.toString()))
        val thread=dayThreads(emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),listOf(page),emptyList(),emptyList()).single()
        assertEquals(page.id,thread.pageId)
        assertEquals(date.toString(),thread.date)
        val old=page.toJson().apply {remove("reflectionDate")}
        assertTrue(JournalEntry.fromJson(old).reflectionDate.isBlank())
    }
    @Test fun historicalCyclesRemainOrderedAndPositive() {
        val from=LocalDate.of(1995,12,1)
        val phases=lunarTurns(from,from.plusDays(31),ZoneOffset.UTC).map {it.phase}
        assertEquals(4,phases.distinct().size)
        assertTrue(lunarFraction(Instant.parse("1995-12-01T12:00:00Z")) in 0.0..1.0)
    }
}
