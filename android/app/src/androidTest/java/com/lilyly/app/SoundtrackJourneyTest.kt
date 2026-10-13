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
class SoundtrackJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun reuseJournalMusicInRoomEditCaptionAndKeepSource() {
        lateinit var store:AppStore
        val song=MusicAttachment("https://open.spotify.com/playlist/1234567890123456789012","Moonlit reading")
        val page=JournalEntry(title="A musical memory",musicJson=musicJson(listOf(song)))
        rule.runOnUiThread {
            store=AppStore(rule.activity);store.upsertJournal(page);store.updateSoundtrack("reading","[]")
            rule.activity.setContent {LilylyTheme(true) {SoundtrackButton(store,"reading")}}
        }
        rule.onNodeWithTag("soundtrack-reading").performClick()
        rule.onNodeWithText("Choose saved music").performClick()
        rule.onNodeWithText("Find saved music").performTextInput("Moonlit")
        rule.onNodeWithText("Moonlit reading").performClick()
        rule.onNodeWithText("Edit caption").performScrollTo().performClick()
        rule.onNodeWithText("Personal caption").performTextReplacement("One more chapter")
        rule.onNodeWithText("Keep caption").performClick()
        rule.onNodeWithText("One more chapter").assertIsDisplayed()
        rule.runOnUiThread {
            val restored=AppStore(rule.activity)
            assertEquals("One more chapter",musicAttachments(restored.soundtracks.getValue("reading")).single().label)
            assertEquals("Moonlit reading",musicAttachments(restored.journalEntries.single {it.id==page.id}.musicJson).single().label)
        }
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"soundtrack.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.onNodeWithText("Remove").performScrollTo().performClick()
        rule.runOnUiThread {assertTrue(musicAttachments(AppStore(rule.activity).soundtracks.getValue("reading")).isEmpty());store.deleteJournal(page.id)}
    }
}
