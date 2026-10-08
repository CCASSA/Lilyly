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
class ThemeJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun chooseAtmosphereAndTypeThenReloadWithoutChangingPages() {
        lateinit var store:AppStore
        var pages=0
        rule.runOnUiThread {
            store=AppStore(rule.activity);pages=store.journalEntries.size
            rule.activity.setContent {LilylyApp(store)}
        }
        rule.onNodeWithContentDescription("Theme").performClick()
        rule.onNodeWithText("Celestial").performClick()
        rule.onNodeWithText("Letters").performClick()
        rule.runOnUiThread {
            val restored=AppStore(rule.activity)
            assertEquals("Celestial",restored.themeName);assertEquals("Letters",restored.typeStyle)
            assertTrue(restored.darkTheme);assertEquals(pages,restored.journalEntries.size)
        }
        rule.waitForIdle()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"themes.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.runOnUiThread {store.personalizeTheme("Cottage Witch","Clear")}
        rule.waitForIdle()
        rule.runOnUiThread {val restored=AppStore(rule.activity);assertFalse(restored.darkTheme);assertEquals("Clear",restored.typeStyle);store.personalizeTheme("Lilyly","Storybook")}
    }
}
