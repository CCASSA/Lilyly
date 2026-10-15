package com.lilyly.app
import android.graphics.Bitmap
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
@RunWith(AndroidJUnit4::class)
class InspirationJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun saveReferenceThenPlaceItOnPageWithSource() {
        lateinit var store:AppStore
        val page=JournalEntry(title="Inspired page")
        var cardId=""
        rule.runOnUiThread {store=AppStore(rule.activity);rule.activity.setContent {LilylyTheme(true){InspirationScreen(store,{})}}}
        rule.onNodeWithText("Add inspiration").performClick()
        rule.onNodeWithText("Inspiration title").performScrollTo().performTextInput("Pressed fern study")
        rule.onNodeWithText("Source link (HTTPS, optional)").performScrollTo().performTextInput("https://example.org/fern")
        rule.onNodeWithText("Creator or source credit").performScrollTo().performTextInput("Garden artist")
        rule.onNodeWithText("Collection").performScrollTo().performTextInput("Botanical studies")
        rule.onNodeWithText("Keep inspiration").performScrollTo().performClick()
        rule.onNodeWithText("Pressed fern study").assertIsDisplayed()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"inspiration.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.runOnUiThread {
            val card=AppStore(rule.activity).journalEntries.single {it.section=="Inspiration" && it.title=="Pressed fern study"}
            cardId=card.id;assertEquals("Botanical studies",card.notebook)
            store.upsertJournal(page);rule.activity.setContent {LilylyTheme(true){JournalEditorScreen(store,page.id,"Journal",{},{})}}
        }
        rule.onNodeWithTag("journal-editor").performScrollToNode(hasText("+ Inspiration"))
        rule.onNodeWithText("+ Inspiration").performClick()
        rule.onNodeWithText("Pressed fern study").performClick()
        rule.onNodeWithContentDescription("Save").performClick()
        rule.runOnUiThread {
            store.deleteJournal(cardId)
            val saved=AppStore(rule.activity).journalEntries.single {it.id==page.id}
            val piece=pagePieces(saved.canvasJson).single()
            assertEquals("https://example.org/fern",piece.sourceUrl);assertEquals("Garden artist",piece.sourceCredit)
            assertTrue(piece.content.contains("Pressed fern study"));store.deleteJournal(page.id)
        }
    }
    @Test fun localImportSurvivesSourceRemovalAndProtectsSharedImage() {
        val original=File(rule.activity.cacheDir,"inspiration-fixture.png")
        val bitmap=Bitmap.createBitmap(2400,200,Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.rgb(80,110,65))
        original.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        val files=InspirationFiles(rule.activity)
        val uri=files.importImage(Uri.fromFile(original))
        original.delete()
        val decoded=android.graphics.BitmapFactory.decodeFile(Uri.parse(uri).path)
        assertTrue(decoded.width<=2048);decoded.recycle()
        val store=AppStore(rule.activity)
        val page=JournalEntry(title="Shared import",canvasJson=piecesJson(listOf(PagePiece(kind="photo",content=uri))))
        rule.runOnUiThread {store.upsertJournal(page)}
        files.removeIfUnused(uri,store);assertTrue(File(Uri.parse(uri).path!!).exists())
        rule.runOnUiThread {store.deleteJournal(page.id)}
        files.removeIfUnused(uri,store);assertFalse(File(Uri.parse(uri).path!!).exists())
    }
}
