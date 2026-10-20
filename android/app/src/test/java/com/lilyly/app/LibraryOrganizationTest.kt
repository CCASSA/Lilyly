package com.lilyly.app
import org.junit.Assert.*
import org.junit.Test
class LibraryOrganizationTest {
    private val unopened=LibraryBook(id="a",title="Ash",author="Willow",format="EPUB",units=10,addedAt="2026-10-01",collection="Rituals")
    private val reading=unopened.copy(id="b",title="Birch",author="Ash",lastReadAt="2026-10-20",position=4,bookmarks=setOf(2),readingSeconds=90,collection="")
    private val finished=unopened.copy(id="c",title="Cedar",completed=true,favorite=true,addedAt="2026-10-03",collection="Dreams")
    private val books=listOf(finished,reading,unopened)
    @Test fun oldBooksDefaultToUnfiledAndNewFieldsRoundTrip() {
        val old=reading.toJson().apply {remove("collection")}
        assertEquals("",LibraryBook.fromJson(old).collection)
        val edited=reading.copy(title="New title",collection="Dreams")
        val restored=LibraryBook.fromJson(edited.toJson())
        assertEquals(edited,restored)
        assertEquals(4,restored.position)
        assertEquals(setOf(2),restored.bookmarks)
        assertEquals(90L,restored.readingSeconds)
    }
    @Test fun readingStagesAreExclusiveAndCompletionTakesPrecedence() {
        assertEquals(listOf("a"),librarySelection(books,status="Unopened").map {it.id})
        assertEquals(listOf("b"),librarySelection(books,status="Reading").map {it.id})
        assertEquals(listOf("c"),librarySelection(books,status="Finished").map {it.id})
    }
    @Test fun filtersComposeWithoutChangingRecords() {
        assertEquals(listOf(finished),librarySelection(books,query="  dreAMS ",favorites=true,status="Finished",collection="Dreams"))
        assertTrue(librarySelection(books,favorites=true,collection="Rituals").isEmpty())
        assertEquals(listOf(reading),librarySelection(books,collection=""))
        assertEquals(listOf("Dreams","Rituals"),libraryCollections(books+unopened))
        assertEquals(4,reading.position)
    }
    @Test fun stableOrderingMatchesSelectedCriterion() {
        assertEquals(listOf("a","b","c"),librarySelection(books,sort="Title").map {it.id})
        assertEquals(listOf("b","a","c"),librarySelection(books,sort="Author").map {it.id})
        assertEquals(listOf("c","a","b"),librarySelection(books,sort="Recently added").map {it.id})
        assertEquals("b",librarySelection(books).first().id)
    }
    @Test fun searchMatchesSeparateFieldsWithoutInventingBoundaryMatches() {
        assertEquals(setOf("a","b"),librarySelection(books,query="ash").map {it.id}.toSet())
        assertTrue(librarySelection(books,query="AshWillow").isEmpty())
        assertEquals(books.size,librarySelection(books,query="  ").size)
    }
}
