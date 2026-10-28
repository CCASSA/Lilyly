package com.lilyly.app
import org.junit.Test
import org.junit.Assert.*
class DayThreadsTest {
    private fun threads(mind:List<MentalCheckIn> = emptyList(),sleeps:List<SleepRecord> = emptyList(),doses:List<MedicationLog> = emptyList(),pages:List<JournalEntry> = emptyList())=dayThreads(emptyList(),mind,sleeps,emptyList(),doses,pages,emptyList(),emptyList())
    @Test fun missingRatingsAreNotInventedAndCrossMidnightSleepUsesWakeDate() {
        val rows=threads(mind=listOf(MentalCheckIn(dateTime="2026-10-09T10:00",feelings=setOf("Calm"),detailedRatings=false)),sleeps=listOf(SleepRecord(bedtime="2026-10-08T23:00",wakeTime="2026-10-09T07:00")))
        assertEquals(setOf("2026-10-09"),rows.map {it.date}.toSet());assertEquals("Calm",rows.first {it.category=="Sanctuary"}.detail)
        assertTrue(rows.first {it.category=="Sleep"}.title.contains("8h 0m"))
    }
    @Test fun pagesUseCreationDateExcludeTemplatesAndDosesUseLogDate() {
        val rows=threads(pages=listOf(JournalEntry(createdAt="2026-10-08T14:00",updatedAt="2026-10-09T12:00"),JournalEntry(section="Templates")),doses=listOf(MedicationLog(dateTime="2026-10-09T00:10",scheduledFor="2026-10-08T23:00",status="Late")))
        assertEquals(2,rows.size);assertEquals("2026-10-08",rows.first {it.category=="Pages"}.date)
        assertEquals("2026-10-09",rows.first {it.category=="Medication"}.date)
        assertTrue(rows.first {it.category=="Medication"}.detail.startsWith("Late"))
        assertTrue(threads().isEmpty())
    }
    @Test fun readingUsesRecordedStartDateAndKeepsBookLinkWithoutInventingOldSessions() {
        val session=ReadingSession(id="night",startedAt="2026-10-27T23:55:00-04:00",endedAt="2026-10-28T00:05:00-04:00",seconds=600,firstPosition=0,lastPosition=2)
        val book=LibraryBook(id="moon-book",title="The Moonlit Garden",format="EPUB",units=4,readingSeconds=900,sessions=listOf(session))
        fun records(books:List<LibraryBook>)=dayThreads(emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),books)
        val row=records(listOf(book)).single()
        assertEquals("2026-10-27",row.date);assertEquals("Reading",row.category)
        assertEquals(book.id,row.bookId);assertEquals(2,row.bookPosition)
        assertTrue(row.detail.contains("10 min"));assertTrue(row.detail.contains("-04:00"))
        assertTrue(records(listOf(book.copy(sessions=emptyList()))).isEmpty())
        assertTrue(records(listOf(book.copy(sessions=listOf(session.copy(startedAt="bad"))))).isEmpty())
    }
}
