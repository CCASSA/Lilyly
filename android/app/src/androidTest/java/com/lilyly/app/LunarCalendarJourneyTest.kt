package com.lilyly.app

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LunarCalendarJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun leapMonthSelectionOpensExactDay() {
        var opened=""
        rule.runOnUiThread {
            val store=AppStore(rule.activity)
            rule.activity.setContent {LilylyTheme(store.darkTheme,store.themeName,store.typeStyle) {
                MagicalCalendarScreen(store,"2024-01-31",onBack={},onDay={opened=it},onJournal={})
            }}
        }
        rule.onNodeWithContentDescription("Next month").performClick()
        rule.onNodeWithContentDescription("2024-02-29",substring=true).assertIsSelected()
        rule.onNodeWithContentDescription("2024-02-14",substring=true).performClick()
        rule.onNodeWithTag("moon-almanac").performScrollToNode(hasText("Open this day’s threads"))
        rule.onNodeWithText("Open this day’s threads").performClick()
        rule.runOnIdle {assertEquals("2024-02-14",opened)}
    }
}
