package com.lilyly.app
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
class PhaseInsightsTest {
    private val start=LocalDate.of(2026,7,1)
    private val today=start.plusDays(60)
    private val logs=listOf(0L,28L,56L).map {CycleLog(date=start.plusDays(it).toString(),period=true)}
    private val checks=(0L..55L).map {MentalCheckIn(dateTime=start.plusDays(it).toString()+"T12:00",mood=if(it%28<5)3 else 7,anxiety=4,energy=6,detailedRatings=true)}
    private fun report(rows:List<MentalCheckIn> = checks,cycles:List<CycleLog> = logs,sources:Set<String> = setOf("Cycle","Sanctuary"),prefs:CyclePreferences = CyclePreferences(),days:Int=90)=phaseInsights(emptyList(),rows,cycles,sources,days,today,prefs)
    @Test fun completedCyclesGroupDistinctDatesWithDailyMeans() {
        val result=report(checks+checks.first().copy(id="extra",mood=9)).comparisons.first()
        assertEquals(4,result.groups.size)
        assertEquals(10,result.groups.first().points.size)
        assertEquals(2,result.groups.first().cycles)
        assertEquals(3.3,result.groups.first().mean,0.001)
    }
    @Test fun sourcesAndExplicitRatingsAreRequired() {
        assertTrue(report(sources=setOf("Sanctuary")).comparisons.isEmpty())
        assertTrue(report(sources=setOf("Cycle")).comparisons.isEmpty())
        assertTrue(report(checks.map {it.copy(detailedRatings=false)}).comparisons.isEmpty())
        assertTrue(report(checks.map {it.copy(mood=99,energy=-1,anxiety=-1)}).comparisons.isEmpty())
    }
    @Test fun unfinishedCyclesFutureStartsAndRepeatedDatesDoNotQualify() {
        assertTrue(report(cycles=logs.take(2)).comparisons.isEmpty())
        assertTrue(report(cycles=logs.take(2)+logs.last().copy(date=today.plusDays(1).toString())).comparisons.isEmpty())
        assertTrue(report(List(30){checks.first().copy(id=it.toString())}).comparisons.isEmpty())
        assertTrue(report(days=5).comparisons.isEmpty())
    }
    @Test fun uncertainContextsAreSuppressed() {
        assertTrue(report(prefs=CyclePreferences(mode="Pregnancy")).comparisons.isEmpty())
        assertTrue(report(prefs=CyclePreferences(contraception="Hormonal")).comparisons.isEmpty())
        assertTrue(report(cycles=logs.take(2)+logs.last().copy(date=start.plusDays(45).toString())).comparisons.isEmpty())
        assertTrue(report(prefs=CyclePreferences(lutealLength=25)).comparisons.isEmpty())
    }
    @Test fun sleepUsesHoursAndExcludesOverlaps() {
        val nights=checks.map {val wake=LocalDate.parse(it.dateTime.take(10)).atTime(8,0);SleepRecord(bedtime=wake.minusHours(8).toString(),wakeTime=wake.toString())}
        fun rest(rows:List<SleepRecord>,sources:Set<String>)=phaseInsights(rows,emptyList(),logs,sources,90,today,CyclePreferences())
        assertEquals(8.0,rest(nights,setOf("Cycle","Sleep")).comparisons.single().groups.first().mean,0.001)
        assertTrue(rest(nights+nights.map {it.copy(id="overlap-${it.id}")},setOf("Cycle","Sleep")).comparisons.isEmpty())
        assertTrue(rest(nights,setOf("Cycle")).comparisons.isEmpty())
    }
    @Test fun painRequiresRecordedFlagAndUsesCycleOptIn() {
        val pain=checks.map {CycleLog(date=it.dateTime.take(10),pain=4,ratingsRecorded=true)}
        assertEquals("Pain",report(rows=emptyList(),cycles=logs+pain,sources=setOf("Cycle")).comparisons.single().metric)
        assertTrue(report(rows=emptyList(),cycles=logs+pain.map {it.copy(ratingsRecorded=false)},sources=setOf("Cycle")).comparisons.isEmpty())
    }
}
