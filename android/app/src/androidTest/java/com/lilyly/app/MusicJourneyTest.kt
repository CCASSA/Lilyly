package com.lilyly.app

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MusicJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun retiredIntegrationPreservesPageMemoriesAndClearsCredentials() {
        lateinit var store:AppStore
        val raw=musicJson(listOf(MusicAttachment("https://open.spotify.com/track/1234567890123456789012","A song I remember","Saved title","Saved artist")))
        val page=JournalEntry(title="Retained memory",musicJson=raw)
        rule.runOnUiThread {
            SecurePreferences(rule.activity,"lilyly_spotify").put("tokens","synthetic retired token")
            store=AppStore(rule.activity)
            assertTrue(rule.activity.getSharedPreferences("lilyly_spotify",Context.MODE_PRIVATE).all.isEmpty())
            store.upsertJournal(page)
            rule.activity.setContent {LilylyTheme(true) {JournalEditorScreen(store,page.id,"Journal",{}, {})}}
        }
        try {
            rule.onNodeWithTag("journal-editor").performScrollToNode(hasText("A song I remember"))
            rule.onNodeWithText("A song I remember").assertIsDisplayed()
            rule.onNodeWithText("Attach Spotify music").assertDoesNotExist()
            rule.onNodeWithText("Open in Spotify").assertDoesNotExist()
            rule.onNodeWithContentDescription("Save").performClick()
            rule.runOnUiThread {assertEquals(raw,AppStore(rule.activity).journalEntries.single {it.id==page.id}.musicJson)}
        } finally {rule.runOnUiThread {store.deleteJournal(page.id)}}
    }
}
