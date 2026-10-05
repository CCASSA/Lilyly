package com.lilyly.app

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import kotlin.random.Random

class TarotReadingTest {
    @Test fun deckContains78UniqueCardsAndDrawsNeverRepeat() {
        assertEquals(78,tarotDeck.map { it.name }.toSet().size)
        val all=drawTarot((1..78).map { "$it" },true,Random(42))
        assertEquals(78,all.map { it.name }.toSet().size)
        assertEquals(all,drawnCards(all.json()))
        assertTrue(drawTarot(listOf("One","Two","Three"),false).none { it.reversed })
    }
    @Test fun oldReadingsKeepTheirWritingAndNewSpreadsRoundTrip() {
        val old=TarotReading.fromJson(JSONObject("""{"id":"old","cards":"my physical deck","notes":"keep this"}"""))
        assertEquals("keep this",old.notes)
        assertTrue(drawnCards(old.drawsJson).isEmpty())
        val new=old.copy(drawsJson=drawTarot(listOf("Care"),true).json(),journalId="page",context="moon")
        assertEquals(new,TarotReading.fromJson(new.toJson()))
    }
    @Test fun readingPositionBookmarksAndNotesRoundTrip() {
        val book=LibraryBook(title="Fixture",format="EPUB",units=5,position=3,bookmarks=setOf(0,3),readingSeconds=91)
        assertEquals(book,LibraryBook.fromJson(book.toJson()))
        val note=BookNote(bookId=book.id,position=3,quote="A passage",journalId="linked")
        assertEquals(note,BookNote.fromJson(note.toJson()))
        assertEquals(4,LibraryBook.fromJson(book.toJson().put("position",999)).position)
    }
}
