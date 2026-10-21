package com.lilyly.app
import org.junit.Assert.*
import org.junit.Test
class ReadingPassagesTest {
    private val book=LibraryBook(id="book",title="Moon Garden",author="Willow",format="EPUB",units=4,collection="Apothecary")
    private val passage=BookNote(id="a",bookId="book",position=2,quote="Silver leaves",date="2026-10-01")
    private val note=BookNote(id="b",bookId="book",position=1,note="Remember the roots",date="2026-10-02")
    @Test fun findsWritingAndSeparateSourceFieldsIgnoringCaseAndSpace() {
        for(query in listOf(" moon ","WILLOW","apothecary")) assertEquals(2,readingPassages(listOf(note,passage),listOf(book),query,"Everything").size)
        assertEquals(listOf(passage),readingPassages(listOf(note,passage),listOf(book),"silver","Everything"))
        assertEquals(listOf(note),readingPassages(listOf(note,passage),listOf(book),"roots","Everything"))
        assertTrue(readingPassages(listOf(note),listOf(book),"GardenWillow","Everything").isEmpty())
    }
    @Test fun filtersUseActualSavedContentAndComposeWithSearch() {
        assertEquals(listOf(passage),readingPassages(listOf(note,passage),listOf(book),"","Passages"))
        assertEquals(listOf(note),readingPassages(listOf(note,passage),listOf(book),"","Notes"))
        assertTrue(readingPassages(listOf(note,passage),listOf(book),"silver","Notes").isEmpty())
        val both=passage.copy(note="My thoughts")
        assertEquals(listOf(both),readingPassages(listOf(both),listOf(book),"","Notes"))
    }
    @Test fun missingSourcesPreserveSearchableWritingAndStableOrder() {
        val rows=readingPassages(listOf(passage,note),emptyList(),"","Everything")
        assertEquals(listOf(note,passage),rows)
        assertEquals(listOf(passage),readingPassages(rows,emptyList(),"leaves","Passages"))
        assertEquals(2,passage.position)
    }
}
