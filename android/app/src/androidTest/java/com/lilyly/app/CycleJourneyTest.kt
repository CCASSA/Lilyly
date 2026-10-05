package com.lilyly.app

import android.graphics.Bitmap
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        rule.onNodeWithText("Return to today").performScrollTo()
        screenshot("cycle-calendar.png")
        rule.onNodeWithText("Log today").performScrollTo().performClick()
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
        rule.onNodeWithText("New page").performClick()
        rule.onNodeWithText("Page title").performTextInput("Emulator scrapbook")
        rule.onNodeWithText("+ Words").performClick()
        rule.onNodeWithTag("journal-editor").performScrollToNode(hasText("Selected text"))
        rule.onNodeWithText("Selected text").performTextReplacement("A little piece of today")
        rule.onNodeWithText("Drag an element to move it. Select it to edit, resize or turn it.").performScrollTo()
        screenshot("scrapbook.png")
        rule.onNodeWithContentDescription("Save").performClick()
        rule.runOnUiThread {
            val reloaded = AppStore(rule.activity)
            assertTrue(reloaded.mentalCheckIns.any { "Calm" in it.feelings && !it.detailedRatings })
            val page = reloaded.journalEntries.first { it.title == "Emulator scrapbook" }
            assertTrue(pagePieces(page.canvasJson).any { it.content == "A little piece of today" })
        }
    }

    private fun screenshot(name: String) {
        rule.waitForIdle()
        val image = rule.onAllNodes(isRoot()).onLast().captureToImage().asAndroidBitmap()
        File(rule.activity.filesDir, name).outputStream().use { image.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
