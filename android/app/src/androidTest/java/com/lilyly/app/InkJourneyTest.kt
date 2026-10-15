package com.lilyly.app
import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.ui.geometry.Offset
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
class InkJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun writeUndoErasePlaceAndReopenPage() {
        lateinit var store:AppStore
        val page=JournalEntry(title="A little handwritten magic")
        rule.runOnUiThread {
            store=AppStore(rule.activity);store.upsertJournal(page)
            rule.activity.setContent {LilylyTheme(dark=true) {JournalEditorScreen(store,page.id,"Journal",{}, {})}}
        }
        rule.onNodeWithText("Write / draw").performClick()
        fun surface(){rule.onNodeWithTag("journal-editor").performScrollToNode(hasTestTag("ink-surface"))}
        fun tap(label:String){
            rule.onNodeWithTag("journal-editor").performScrollToNode(hasText(label))
            rule.onNodeWithText(label).performClick()
        }
        surface()
        rule.onNodeWithTag("ink-surface").performTouchInput {swipe(Offset(width*.2f,height*.25f),Offset(width*.7f,height*.55f),400)}
        tap("Undo ink")
        rule.onNodeWithText("Redo ink").assertIsEnabled()
        tap("Redo ink")
        tap("Erase stroke")
        rule.onNodeWithText("Erase stroke").assertIsSelected()
        surface()
        rule.onNodeWithTag("ink-surface").performTouchInput {click(Offset(width*.45f,height*.4f))}
        tap("Undo ink")
        tap("Highlighter")
        rule.onNodeWithText("Highlighter").assertIsSelected()
        surface()
        rule.onNodeWithTag("ink-surface").performTouchInput {swipe(Offset(width*.2f,height*.65f),Offset(width*.75f,height*.65f),400)}
        rule.waitForIdle()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"ink-studio.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        tap("Place ink on scrapbook")
        rule.onNodeWithContentDescription("Save").performClick()
        rule.runOnUiThread {
            val restored=AppStore(rule.activity).journalEntries.single {it.id==page.id}
            val ink=readInk(restored.inkJson)
            assertEquals(2,ink.strokes.size)
            assertTrue("Saved view box",ink.width>0)
            assertTrue("Selected highlighter persisted",ink.strokes.last().highlighter)
            val piece=pagePieces(restored.canvasJson).single {it.kind=="ink"}
            assertEquals(ink,readInk(piece.content))
            store.deleteJournal(page.id)
        }
    }
}
