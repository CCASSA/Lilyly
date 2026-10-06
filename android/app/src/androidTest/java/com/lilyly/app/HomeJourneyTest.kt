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
class HomeJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun dailyHomeOpensSavedBookAndKeepsPersonalization() {
        lateinit var store:AppStore
        val context=rule.activity
        val id="home-book-fixture"
        val directory=BookFiles(context).directory(id).apply {mkdirs()}
        File(directory,"text.json").writeText("[{\"title\":\"Your saved place\",\"text\":\"A little world between the pages.\"}]")
        rule.runOnUiThread {
            store=AppStore(context)
            store.personalizeHome("Garden guest",homeSectionNames.toSet())
            store.saveBook(LibraryBook(id=id,title="A Book for Home",format="EPUB",units=1,lastReadAt="2099-01-01T12:00"))
            rule.activity.setContent {LilylyApp(store)}
        }
        rule.onNodeWithTag("daily-home").assertExists()
        val shot=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(context.filesDir,"home.png").outputStream().use {shot.compress(Bitmap.CompressFormat.PNG,100,it)};shot.recycle()
        rule.onNodeWithTag("daily-home").performScrollToNode(hasText("Continue reading"))
        rule.onNodeWithText("Continue reading").performClick()
        rule.waitUntil(10000) {rule.onAllNodesWithText("A little world between the pages.").fetchSemanticsNodes().isNotEmpty()}
        rule.onNodeWithText("A little world between the pages.").assertIsDisplayed()
        rule.onNodeWithContentDescription("Back to shelves").performClick()
        rule.onNodeWithContentDescription("Back").performClick()
        rule.onNodeWithText("Home").performClick()
        rule.onNodeWithTag("daily-home").performScrollToNode(hasText("Make Home your own"))
        rule.onNodeWithText("Make Home your own").performClick()
        rule.onNodeWithText("Rhythm").performClick()
        rule.onNodeWithText("Done").performClick()
        rule.runOnUiThread {
            val loaded=AppStore(context)
            assertEquals("Garden guest",loaded.greetingName)
            assertFalse("Rhythm" in loaded.homeSections)
            store.personalizeHome("",homeSectionNames.toSet())
            store.removeBook(id)
        }
    }
}
