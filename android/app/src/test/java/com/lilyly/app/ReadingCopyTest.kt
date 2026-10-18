package com.lilyly.app
import org.junit.Assert.*
import org.junit.Test
class ReadingCopyTest {
    @Test fun snapshotFreezesSelectionOrderAndDoesNotIncludeOtherRecords() {
        val a=JournalEntry(title="First",body="Original",notebook="Moon")
        val b=JournalEntry(title="Second",body="Private caption",sourceCredit="Artist")
        val snapshot=readingCopySnapshot(listOf(b,a));a.body="Changed after picker opened"
        val decoded=readingCopyEntries(snapshot)
        assertEquals(listOf(b.id,a.id),decoded.map {it.id});assertEquals("Original",decoded[1].body)
        assertEquals("Artist",decoded[0].sourceCredit)
    }
    @Test fun filenamesCannotIntroduceDirectoriesOrControlCharacters() {
        assertEquals("Moon-notes--.pdf",readingCopyFilename("Moon/notes\n:"))
        assertEquals("Lilyly-reading-copy.pdf",readingCopyFilename("  "))
        assertTrue(readingCopyFilename("x".repeat(200)).length<=84)
    }
    @Test fun tarotReferencesBecomeReadableWithoutExposingJson() {
        assertEquals("Reflection · The Star · Reversed",readingCopyElementText(PagePiece(kind="tarot",content="{\"position\":\"Reflection\",\"name\":\"The Star\",\"reversed\":true}")))
        assertEquals("A thought",readingCopyElementText(PagePiece(content="A thought")))
    }
}
