package com.lilyly.app

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CycleJourneyTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun logSymptomAndRecoverItFromEncryptedStorage() {
        // Synthetic fixture exists only in the emulator, never in the shipped app.
        rule.runOnUiThread {
            val store = AppStore(rule.activity)
            val today = LocalDate.now()
            listOf(44L, 15L).forEach { ago ->
                (0L..3L).forEach { offset -> store.upsertCycle(CycleLog(date = today.minusDays(ago).plusDays(offset).toString(), period = true, flow = "Medium")) }
            }
            rule.activity.setContent { LilylyApp(store) }
        }
        rule.onNodeWithText("Cycle", useUnmergedTree = true).performClick()
        rule.onNodeWithText("Your own rhythm").assertIsDisplayed()
        screenshot("cycle-wheel.png")
        rule.onNode(hasScrollToIndexAction()).performScrollToIndex(1)
        screenshot("cycle-calendar.png")
        rule.onNode(hasScrollToIndexAction()).performScrollToIndex(0)
        rule.onNodeWithText("Log today").performClick()
        rule.onNodeWithText("Choose whatever fits").assertIsDisplayed()
        rule.onNodeWithContentDescription("Calm").performClick()
        screenshot("cycle-log.png")
        rule.onNodeWithText("Save this day").performClick()
        rule.runOnUiThread {
            val reloaded = AppStore(rule.activity)
            assertTrue("Selected symptom must survive encrypted persistence", "Calm" in reloaded.cycleLogs.first { it.date == LocalDate.now().toString() }.selections)
        }
    }

    @Test fun sanctuaryAndScrapbookPersistTogether() {
        rule.onNodeWithText("Sanctuary", useUnmergedTree = true).performClick()
        rule.onNodeWithText("Calm").performClick()
        screenshot("sanctuary.png")
        rule.onNodeWithText("Save check-in").assertIsDisplayed().performClick()
        rule.onNodeWithText("Journal", useUnmergedTree = true).performClick()
        rule.onNodeWithText("New page", useUnmergedTree = true).performClick()
        rule.onNodeWithText("Page title").performTextInput("Emulator scrapbook")
        rule.onNodeWithText("+ Words").performClick()
        rule.onNodeWithTag("journal-editor").performScrollToNode(hasText("Selected text"))
        rule.onNodeWithText("Selected text").performTextReplacement("A little piece of today")
        rule.onNodeWithContentDescription("Save").performClick()
        rule.runOnUiThread {
            (rule.activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).hideSoftInputFromWindow(rule.activity.window.decorView.windowToken, 0)
        }
        rule.onNodeWithText("❦ Emulator scrapbook").performClick()
        rule.onNodeWithText("A little piece of today").assertIsDisplayed()
        screenshot("scrapbook.png")
        rule.runOnUiThread {
            val reloaded = AppStore(rule.activity)
            assertTrue(reloaded.mentalCheckIns.any { "Calm" in it.feelings && !it.detailedRatings })
            val page = reloaded.journalEntries.first { it.title == "Emulator scrapbook" }
            assertTrue(pagePieces(page.canvasJson).any { it.content == "A little piece of today" })
        }
    }

    private fun screenshot(name: String) {
        rule.waitForIdle()
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        Thread.sleep(350) // Allow the Android compositor to present the settled window.
        val image = checkNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(rule.activity.filesDir, name).outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
