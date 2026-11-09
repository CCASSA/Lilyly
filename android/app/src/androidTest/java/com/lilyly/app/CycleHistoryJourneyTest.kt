package com.lilyly.app

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CycleHistoryJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun opensTheLoggedDateAndSavesACorrection() {
        val day=LocalDate.now().minusDays(1)
        rule.runOnUiThread {
            val store=AppStore(rule.activity)
            store.upsertCycle(CycleLog(date=day.toString(),period=true,flow="Light",notes="Keep my original note"))
            rule.activity.setContent { LilylyTheme(true) {CycleScreen(store)} }
        }
        rule.onNodeWithText("Cycle history").performClick()
        rule.onNodeWithText("Latest recorded cycle").performClick()
        rule.onNodeWithText("Edit "+day.format(DateTimeFormatter.ofPattern("d MMM yyyy"))).performClick()
        rule.onNodeWithText(day.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))).assertIsDisplayed()
        rule.onNodeWithContentDescription("Calm").performClick()
        rule.onNodeWithText("Save this day").performClick()
        rule.waitForIdle()
        rule.runOnIdle {
            val saved=AppStore(rule.activity).cycleLogs.single {it.date==day.toString()}
            assertTrue(saved.notes=="Keep my original note" && "Calm" in saved.selections)
        }
    }
}
