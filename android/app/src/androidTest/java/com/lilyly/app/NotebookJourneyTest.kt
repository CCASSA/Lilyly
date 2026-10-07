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
class NotebookJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun bindEmptyBookCreateTemplatePageAndKeepLayoutTemplate() {
        lateinit var store:AppStore
        rule.runOnUiThread {store=AppStore(rule.activity);rule.activity.setContent {LilylyApp(store)}}
        rule.onNodeWithText("Journal").performClick()
        rule.onNodeWithText("Bind a notebook").performClick()
        rule.onNodeWithText("Notebook name").performTextInput("Moon keepsakes")
        rule.onNodeWithText("Celestial").performClick()
        rule.onNodeWithText("Keep notebook").performClick()
        rule.runOnUiThread {assertEquals("Celestial",AppStore(rule.activity).notebookCovers[notebookKey("Journal","Moon keepsakes")])}
        rule.mainClock.advanceTimeBy(500);rule.waitForIdle()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"notebooks.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.onNodeWithText("Create from a template").performClick()
        rule.onNodeWithText("Use A little of today").performClick()
        rule.onNodeWithTag("journal-editor").performScrollToNode(hasText("Save as reusable template"))
        rule.onNodeWithText("Save as reusable template").performClick()
        rule.runOnUiThread {
            val restored=AppStore(rule.activity)
            val page=restored.journalEntries.single {it.notebook=="Moon keepsakes"}
            assertTrue(page.body.contains("One small kindness"))
            val template=restored.journalEntries.first {it.section=="Templates" && it.title==page.title}
            assertNotEquals(page.id,template.id);assertEquals(page.body,template.body)
        }
    }
}
