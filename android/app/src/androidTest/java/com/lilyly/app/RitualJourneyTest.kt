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
class RitualJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun keepRitualEditReflectionAndOpenSameGrimoirePage() {
        lateinit var store:AppStore
        rule.runOnUiThread {store=AppStore(rule.activity);rule.activity.setContent {LilylyApp(store)}}
        rule.onNodeWithText("More").performClick()
        rule.onNodeWithText("Ritual room · spells & intentions").performClick()
        rule.onNodeWithText("A quiet beginning").performClick()
        rule.onNodeWithText("Ritual title").performTextReplacement("My threshold ritual")
        rule.onNodeWithText("Keep ritual").performClick()
        rule.runOnUiThread {val page=AppStore(rule.activity).journalEntries.single {it.title=="My threshold ritual"};assertTrue(RitualDetails.fromJson(page.ritualJson).intention.contains("beginning"))}
        rule.onNodeWithText("My threshold ritual").performScrollTo().performClick()
        rule.onNodeWithText("Reflection").performClick()
        rule.onNodeWithText("What I noticed afterward").performTextInput("A moment of quiet")
        rule.onNodeWithTag("ritual-editor").performScrollToNode(hasText("Keep ritual"))
        rule.onNodeWithText("Keep ritual").performClick()
        rule.onNodeWithText("My threshold ritual").performScrollTo()
        rule.waitForIdle()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"rituals.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.onNodeWithText("Open grimoire page").performScrollTo().performClick()
        rule.onNodeWithText("Page title").assertExists()
        rule.runOnUiThread {val pages=AppStore(rule.activity).journalEntries.filter {it.title=="My threshold ritual"};assertEquals(1,pages.size);assertEquals("A moment of quiet",RitualDetails.fromJson(pages.single().ritualJson).reflection)}
    }
}
