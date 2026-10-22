package com.lilyly.app
import org.junit.Assert.*
import org.junit.Test
import org.json.JSONArray
class ReadingSessionsTest {
    private val start="2026-10-22T20:00:00+02:00"
    private val end="2026-10-22T20:10:00+02:00"
    private val session=ReadingSession(id="session",startedAt=start,endedAt=end,seconds=600,firstPosition=0,lastPosition=3)
    private val book=LibraryBook(id="book",title="The quiet garden",format="EPUB",units=5,readingSeconds=100)
    @Test fun legacyTotalsRemainAndSessionsRoundTrip() {
        assertTrue(LibraryBook.fromJson(book.toJson().apply {remove("sessions")}).sessions.isEmpty())
        val saved=keepReadingSession(book,session)
        assertEquals(700L,saved.readingSeconds)
        assertEquals(saved,LibraryBook.fromJson(saved.toJson()))
    }
    @Test fun duplicateAndInvalidSessionsDoNotInflateTotals() {
        val saved=keepReadingSession(book,session)
        assertEquals(saved,keepReadingSession(saved,session))
        assertEquals(book,keepReadingSession(book,session.copy(seconds=0)))
        assertEquals(book,keepReadingSession(book,session.copy(lastPosition=5)))
    }
    @Test fun malformedBackupRowsCannotBreakHistory() {
        val rows=JSONArray().put(session.toJson()).put(session.toJson()).put(session.toJson().put("startedAt","broken")).put(session.toJson().put("seconds",-1)).put("bad")
        assertEquals(listOf(session),readSessions(rows,5))
    }
    @Test fun duplicateBeginAndPauseCountOnlyOneForegroundInterval() {
        var nanos=0L
        val timer=ReadingSessionTimer({start},{nanos})
        timer.begin(0);nanos=2_000_000_000;timer.begin(1);nanos=5_000_000_000
        val recorded=timer.pause(3)!!
        assertEquals(5L,recorded.seconds);assertEquals(0,recorded.firstPosition);assertEquals(3,recorded.lastPosition)
        assertNull(timer.pause(3))
    }
    @Test fun timeInBackgroundIsExcludedAndClockChangesDoNotAffectDuration() {
        var nanos=0L
        var wall=start
        val timer=ReadingSessionTimer({wall},{nanos})
        timer.begin(0);nanos=3_000_000_000
        assertEquals(3L,timer.pause(1)!!.seconds)
        nanos=600_000_000_000;timer.begin(1);nanos+=2_000_000_000;wall="2026-10-21T20:00:00-04:00"
        val next=timer.pause(2)!!
        assertEquals(2L,next.seconds);assertEquals(wall,next.endedAt)
    }
    @Test fun subSecondVisitsDoNotCreateEmptySessions() {
        var nanos=0L
        val timer=ReadingSessionTimer({start},{nanos})
        timer.begin(0);nanos=999_000_000
        assertNull(timer.pause(0))
        assertEquals("0 sec",readingDuration(0));assertEquals("59 sec",readingDuration(59));assertEquals("1 min",readingDuration(60));assertEquals("1 hr 1 min",readingDuration(3660))
    }
}
