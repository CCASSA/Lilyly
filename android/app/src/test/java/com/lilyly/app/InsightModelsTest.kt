package com.lilyly.app
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
class InsightModelsTest {
    private val today=LocalDate.of(2026,10,13)
    private val nights=(1..10).map {i -> val wake=today.minusDays(i.toLong()).atTime(8,0);SleepRecord(bedtime=wake.minusHours(if(i<=5)5 else 8).toString(),wakeTime=wake.toString())}
    private val checks=nights.mapIndexed {i,r -> MentalCheckIn(dateTime=r.date+"T12:00",anxiety=if(i<5)8 else 2,mood=5,energy=6,detailedRatings=true)}
    @Test fun matchedGroupsUseObservedDailyAverages() {
        val report=personalInsights(nights,checks+checks.first().copy(id="another",anxiety=4),emptyList(),setOf("Sleep","Sanctuary"),30,today)
        val anxiety=report.comparisons.first()
        assertEquals(5,anxiety.groups.first().points.size)
        assertEquals(7.6,anxiety.groups.first().mean,0.001)
        assertEquals(2.0,anxiety.groups.last().mean,0.001)
    }
    @Test fun optOutDefaultsAndInsufficientDatesDoNotInventResults() {
        assertTrue(personalInsights(nights,checks,emptyList(),emptySet(),30,today).comparisons.isEmpty())
        assertTrue(personalInsights(nights,checks.map {it.copy(detailedRatings=false)},emptyList(),setOf("Sleep","Sanctuary"),30,today).comparisons.isEmpty())
        assertTrue(personalInsights(nights.take(9),checks,emptyList(),setOf("Sleep","Sanctuary"),30,today).comparisons.isEmpty())
    }
    @Test fun duplicateRatingsCannotSubstituteForDistinctDates() {
        val rows=(1..30).map {checks.first().copy(id=it.toString())}
        assertTrue(personalInsights(nights,rows,emptyList(),setOf("Sleep","Sanctuary"),30,today).comparisons.isEmpty())
    }
    @Test fun overlappingSleepAndOutOfWindowDataAreExcluded() {
        val duplicate=nights.first().copy(id="overlap")
        assertTrue(personalInsights(nights+duplicate,checks,emptyList(),setOf("Sleep","Sanctuary"),30,today).comparisons.isEmpty())
        assertEquals(0,personalInsights(nights,checks,emptyList(),setOf("Sleep","Sanctuary"),30,today.plusDays(100)).dates)
    }
    @Test fun cyclePainRequiresExplicitRatingsAndDreamOptIn() {
        val cycles=nights.map {CycleLog(date=it.date,pain=3,ratingsRecorded=true)}
        assertEquals("Rest & pain",personalInsights(nights,emptyList(),cycles,setOf("Sleep","Cycle"),30,today).comparisons.single().title)
        assertTrue(personalInsights(nights,emptyList(),cycles.map {it.copy(ratingsRecorded=false)},setOf("Sleep","Cycle"),30,today).comparisons.isEmpty())
        val dreams=nights.take(2).map {it.copy(symbols=setOf("Moon"," moon "))}
        assertEquals(listOf("moon" to 2),personalInsights(dreams,emptyList(),emptyList(),setOf("Dreams"),30,today).symbols)
        assertTrue(personalInsights(dreams,emptyList(),emptyList(),setOf("Sleep"),30,today).symbols.isEmpty())
    }
}
