package com.lilyly.app
import android.graphics.Bitmap
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
class TherapyJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun prepareEditPracticeAndLinkWithoutDuplicatingOrOverwritingJournal() {
        lateinit var store:AppStore
        rule.runOnUiThread {store=AppStore(rule.activity);rule.activity.setContent {LilylyApp(store,openMedication=true)}}
        rule.onNodeWithText("Therapy").performScrollTo().performClick()
        rule.onNodeWithText("Prepare a session").performClick()
        rule.onNodeWithText("Session title").performTextReplacement("A quiet session fixture")
        rule.onNodeWithText("What I want to bring").performTextInput("A thought to remember")
        rule.onNodeWithText("After").performClick()
        rule.onNodeWithText("What I'm working toward").performTextInput("Make room to rest")
        rule.onNodeWithText("Practice").performClick()
        rule.onNodeWithText("Homework or something to try").performTextInput("Write a gentle page")
        rule.onNodeWithText("Keep session").performClick()
        rule.onNodeWithTag("therapy-room").performScrollToNode(hasText("Open session"))
        rule.onNodeWithText("Open session").performClick()
        rule.onNodeWithText("What mattered / what I learned").performTextInput("Something I want to keep")
        rule.onNodeWithText("Keep session").performClick()
        rule.runOnUiThread {
            val reloaded=AppStore(rule.activity)
            val note=reloaded.therapyNotes.single {it.title=="A quiet session fixture"}
            assertEquals("Make room to rest",note.goals);assertEquals("Something I want to keep",note.after)
            store.addTherapyNote(note.copy(homeworkDone=true))
            assertTrue(AppStore(rule.activity).therapyNotes.first {it.id==note.id}.homeworkDone)
        }
        rule.onNodeWithTag("therapy-room").performScrollToNode(hasText("Make a journal page"))
        rule.mainClock.advanceTimeBy(600);rule.waitForIdle()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"therapy.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.onNodeWithText("Make a journal page").performClick()
        rule.runOnUiThread {
            val note=store.therapyNotes.single {it.title=="A quiet session fixture"}
            val page=store.journalEntries.first {it.id==note.journalId}
            assertTrue(page.body.contains("Make room to rest"))
            store.upsertJournal(page.copy(body="My independent journal edits"))
            assertEquals(page.id,store.journalTherapy(note).id)
            assertEquals("My independent journal edits",AppStore(rule.activity).journalEntries.first {it.id==page.id}.body)
        }
    }
}
