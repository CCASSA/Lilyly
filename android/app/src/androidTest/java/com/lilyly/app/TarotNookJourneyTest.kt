package com.lilyly.app

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class TarotNookJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()

    @Test fun revealReadingAndLinkItToAPersistentGrimoirePage() {
        lateinit var store: AppStore
        var linked=""
        rule.runOnUiThread {
            store=AppStore(rule.activity)
            rule.activity.setContent { LilylyTheme { TarotScreen(store,{}, {linked=it.id}) } }
        }
        rule.onNodeWithTag("tarot-screen").performScrollToNode(hasText("Shuffle & lay the cards"))
        rule.onNodeWithText("Shuffle & lay the cards").performClick()
        rule.onNodeWithContentDescription("Face-down card. Tap to reveal").performClick()
        screenshot("tarot.png")
        rule.onNodeWithTag("tarot-screen").performScrollToNode(hasText("Keep this reading"))
        rule.onNodeWithText("Keep this reading").performClick()
        rule.onNodeWithText("Make a grimoire page").performClick()
        rule.runOnUiThread {
            val reloaded=AppStore(rule.activity)
            val reading=reloaded.tarotReadings.first { it.journalId==linked }
            assertEquals(1,drawnCards(reading.drawsJson).size)
            val page=reloaded.journalEntries.first { it.id==linked }
            assertEquals("tarot",pagePieces(page.canvasJson).single().kind)
            assertEquals(linked,store.journalReading(reading).id)
        }
    }

    @Test fun importEpubAndPdfThenReadBookmarkAndKeepPassage() {
        val context=rule.activity
        val epub=File(context.cacheDir,"fixture.epub")
        ZipOutputStream(epub.outputStream()).use { z ->
            fun entry(name:String,value:String) {z.putNextEntry(ZipEntry(name));z.write(value.toByteArray());z.closeEntry()}
            entry("META-INF/container.xml","""<container xmlns="urn:oasis:names:tc:opendocument:xmlns:container"><rootfiles><rootfile full-path="book.opf"/></rootfiles></container>""")
            entry("book.opf","""<package xmlns="http://www.idpf.org/2007/opf"><metadata xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:title>The Moonlit Garden</dc:title><dc:creator>Lilyly test fixture</dc:creator></metadata><manifest><item id="a" href="one.xhtml" media-type="application/xhtml+xml"/><item id="b" href="two.xhtml" media-type="application/xhtml+xml"/></manifest><spine><itemref idref="a"/><itemref idref="b"/></spine></package>""")
            entry("one.xhtml","<html><head><title>The gate</title></head><body><p>The garden waited in the moonlight.</p></body></html>")
            entry("two.xhtml","<html><head><title>The path</title></head><body><p>She followed the silver leaves home.</p></body></html>")
        }
        val files=BookFiles(context)
        val imported=files.import(Uri.fromFile(epub))
        assertEquals(2,imported.units)
        val pdf=File(context.cacheDir,"fixture.pdf")
        PdfDocument().use { doc ->
            repeat(2) { i -> val page=doc.startPage(PdfDocument.PageInfo.Builder(400,600,i+1).create());page.canvas.drawText("A quiet page ${i+1}",30f,80f,android.graphics.Paint().apply {textSize=20f});doc.finishPage(page) }
            pdf.outputStream().use { doc.writeTo(it) }
        }
        val importedPdf=files.import(Uri.fromFile(pdf))
        assertEquals(2,importedPdf.units)
        files.pdf(importedPdf.id,1,300).let { assertTrue(it.width>0);it.recycle() }
        rule.runOnUiThread {
            val store=AppStore(context);store.saveBook(imported);store.saveBook(importedPdf)
            rule.activity.setContent { LilylyTheme { BookshelfScreen(store,{}, {}) } }
        }
        rule.onNodeWithText("Celestial").performClick()
        screenshot("bookshelf.png")
        rule.onNodeWithTag("book-${imported.id}").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("The garden waited in the moonlight.",substring=true).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("Next").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("She followed the silver leaves home.",substring=true).fetchSemanticsNodes().isNotEmpty() }
        screenshot("reader.png")
        rule.onNodeWithText("Keep this passage").performClick()
        rule.onNodeWithText("Your thoughts").performTextInput("A line to remember")
        rule.onNodeWithText("Keep passage").performClick()
        rule.onNodeWithText("Tools").performClick()
        rule.onNodeWithText("Bookmark").performClick()
        rule.runOnUiThread {
            val reloaded=AppStore(context)
            val saved=reloaded.books.first { it.id==imported.id }
            assertEquals(1,saved.position);assertTrue(1 in saved.bookmarks)
            assertEquals("Celestial",reloaded.nookTheme)
            val note=reloaded.bookNotes.first { it.bookId==imported.id }
            assertTrue(note.quote.contains("silver leaves"));assertEquals("A line to remember",note.note)
            val page=reloaded.journalBookNote(note)
            assertTrue(AppStore(context).journalEntries.any { it.id==page.id && it.body.contains("silver leaves") })
        }
    }
    private fun screenshot(name:String) {
        rule.waitForIdle();InstrumentationRegistry.getInstrumentation().waitForIdleSync();Thread.sleep(350)
        val bitmap=checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(rule.activity.filesDir,name).outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
    }
}
