package com.lilyly.app
import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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
class MusicJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun attachSaveReopenAndRemoveMusic() {
        lateinit var store:AppStore
        val page=JournalEntry(title="Music journey")
        rule.runOnUiThread {
            store=AppStore(rule.activity);store.upsertJournal(page)
            rule.activity.setContent {LilylyTheme(true) {JournalEditorScreen(store,page.id,"Journal",{}, {})}}
        }
        rule.onNodeWithTag("journal-editor").performScrollToNode(hasText("Attach Spotify music"))
        rule.onNodeWithText("Attach Spotify music").performClick()
        rule.onNodeWithText("Spotify song or playlist link").performTextInput("https://open.spotify.com/track/1234567890123456789012?si=discard")
        rule.onNodeWithText("Personal caption (optional)").performTextInput("A song for this page")
        rule.onNodeWithText("Add to page").performClick()
        rule.onNodeWithContentDescription("Save").performClick()
        rule.runOnUiThread {
            val saved=AppStore(rule.activity).journalEntries.single {it.id==page.id}
            assertEquals("A song for this page",musicAttachments(saved.musicJson).single().label)
            assertFalse(saved.musicJson.contains("discard"))
            rule.activity.setContent {LilylyTheme(true) {key("reopened") {JournalEditorScreen(AppStore(rule.activity),page.id,"Journal",{}, {})}}}
        }
        rule.onNodeWithTag("journal-editor").performScrollToNode(hasText("A song for this page"))
        rule.onNodeWithText("A song for this page").assertIsDisplayed()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"music.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.onNodeWithText("Remove").performScrollTo().performClick()
        rule.onNodeWithContentDescription("Save").performClick()
        rule.runOnUiThread {assertTrue(musicAttachments(AppStore(rule.activity).journalEntries.single {it.id==page.id}.musicJson).isEmpty());store.deleteJournal(page.id)}
    }
}
