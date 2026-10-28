package com.lilyly.app
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
@RunWith(AndroidJUnit4::class)
class LibraryOrganizationJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private var previous=emptyMap<String,String>()
    private var importedId:String?=null
    @org.junit.After fun restoreFixture() {
        rule.runOnUiThread {
            if(previous.isNotEmpty())SecurePreferences(rule.activity).writeBatch(previous)
            importedId?.let {BookFiles(rule.activity).directory(it).deleteRecursively()}
        }
    }
    @Test fun organizeImportedBookFilterShelfAndPreserveProgress() {
        val pdf=File(rule.activity.cacheDir,"shelf-fixture.pdf")
        val doc=PdfDocument()
        try {
            repeat(2) {i ->
                val page=doc.startPage(PdfDocument.PageInfo.Builder(400,600,i+1).create())
                page.canvas.drawText("A shelf fixture",20f,60f,android.graphics.Paint().apply {textSize=20f})
                doc.finishPage(page)
            }
            pdf.outputStream().use {doc.writeTo(it)}
        } finally {doc.close()}
        val imported=BookFiles(rule.activity).importBook(Uri.fromFile(pdf)).copy(position=1,bookmarks=setOf(0),readingSeconds=120)
        importedId=imported.id
        rule.runOnUiThread {
            val secure=SecurePreferences(rule.activity)
            previous=mapOf("books" to secure.get("books","[]"),"bookNotes" to secure.get("bookNotes","[]"))
            secure.writeBatch(mapOf("books" to "[]","bookNotes" to "[]"))
            val store=AppStore(rule.activity)
            store.saveBook(imported)
            store.saveBookNote(BookNote(bookId=imported.id,position=1,note="Keep this note"))
            rule.activity.setContent {LilylyTheme(true){BookshelfScreen(store,{}, {},imported.id)}}
        }
        // A checkpoint must reach encrypted storage while the reader is still open.
        rule.waitUntil(40_000) {AppStore(rule.activity).books.single().sessions.isNotEmpty()}
        rule.onNodeWithText("Tools").performClick()
        rule.onNodeWithText("Book details & shelf").performScrollTo().performClick()
        rule.onNodeWithText("Book title").performTextReplacement("The Herbal Shelf")
        rule.onNodeWithText("Shelf name").performTextInput("Apothecary")
        rule.onNodeWithText("Shelf name").performImeAction()
        rule.waitForIdle()
        rule.onNode(isToggleable()).performScrollTo().performClick()
        rule.onNodeWithText("Keep book details").performClick()
        rule.onNodeWithContentDescription("Back to shelves").performClick()
        rule.onNodeWithText("Browse shelves").performScrollTo().performClick()
        rule.onNodeWithTag("library-browser").performScrollToNode(hasText("Finished"))
        rule.onNodeWithText("Finished").performClick()
        rule.onNodeWithTag("library-browser").performScrollToNode(hasText("Apothecary"))
        rule.onNodeWithText("Apothecary").performClick()
        rule.onNodeWithTag("library-browser").performScrollToNode(hasText("Return to my shelves"))
        rule.onNodeWithText("Return to my shelves").performClick()
        rule.onNodeWithTag("bookshelf").performScrollToNode(hasTestTag("book-${imported.id}"))
        rule.onNodeWithTag("book-${imported.id}").assertIsDisplayed()
        rule.runOnUiThread {
            val restored=AppStore(rule.activity)
            val book=restored.books.single()
            assertEquals("The Herbal Shelf",book.title)
            assertEquals("Apothecary",book.collection)
            assertTrue(book.completed)
            assertEquals(1,book.position)
            assertEquals(setOf(0),book.bookmarks)
            assertTrue(book.readingSeconds>=120)
            assertEquals(1,book.sessions.size)
            assertEquals(1,book.sessions.first().firstPosition)
            assertEquals(1,book.sessions.last().lastPosition)
            assertEquals(120L+book.sessions.sumOf {it.seconds},book.readingSeconds)
            assertEquals("Keep this note",restored.bookNotes.single().note)
        }
    }
}
