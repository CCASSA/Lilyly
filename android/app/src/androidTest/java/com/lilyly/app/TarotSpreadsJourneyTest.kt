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
class TarotSpreadsJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private var previous=emptyMap<String,String>()
    @org.junit.After fun restoreFixture() {if(previous.isNotEmpty())rule.runOnUiThread {SecurePreferences(rule.activity).writeBatch(previous)}}
    @Test fun createEditDrawAndRemoveTemplatePreservesReadingAndPage() {
        lateinit var store:AppStore
        val labels=listOf("Root","Rain","Light","Bloom","Release","Carry forward")
        rule.runOnUiThread {
            val secure=SecurePreferences(rule.activity)
            previous=listOf("settings","tarot","journal").associateWith {secure.get(it,if(it=="settings") "{}" else "[]")}
            secure.writeBatch(mapOf("settings" to "{}","tarot" to "[]","journal" to "[]"))
            store=AppStore(rule.activity)
            rule.activity.setContent {LilylyTheme(true){TarotScreen(store,{}, {})}}
        }
        rule.onNodeWithTag("tarot-screen").performScrollToNode(hasText("My spreads"))
        rule.onNodeWithText("My spreads").performClick()
        rule.onNodeWithText("Create a spread").performClick()
        rule.onNodeWithText("Keep spread").assertIsNotEnabled()
        rule.onNodeWithText("Spread name").performTextInput("My garden")
        rule.onNodeWithText("Card positions").performTextReplacement(labels.joinToString("\n"))
        rule.onNodeWithText("Card positions").performImeAction()
        rule.onNodeWithText("Keep spread").performClick()
        rule.onNodeWithTag("personal-spreads").performScrollToNode(hasText("Edit spread"))
        rule.onNodeWithText("Edit spread").performScrollTo().performClick()
        rule.onNodeWithText("Spread name").performTextReplacement("My moon garden")
        rule.onNodeWithText("Card positions").performImeAction()
        rule.onNodeWithText("Keep spread").performClick()
        rule.runOnUiThread {assertEquals(labels,AppStore(rule.activity).tarotSpreads.single().positions)}
        rule.onNodeWithTag("personal-spreads").performScrollToNode(hasText("Use My moon garden"))
        rule.onNodeWithText("Use My moon garden").performScrollTo().performClick()
        rule.onNodeWithTag("tarot-screen").performScrollToNode(hasText("Shuffle & lay the cards"))
        rule.onNodeWithText("Shuffle & lay the cards").performClick()
        repeat(6) {i ->
            rule.onNodeWithTag("tarot-screen").performScrollToNode(hasTestTag("draw-card-$i"))
            rule.onNodeWithTag("draw-card-$i").performScrollTo().performClick()
        }
        rule.waitForIdle()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"personal-spread.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.onNodeWithTag("tarot-screen").performScrollToNode(hasText("Keep this reading"))
        rule.onNodeWithText("Keep this reading").performClick()
        var pageId=""
        rule.runOnUiThread {
            val saved=AppStore(rule.activity).tarotReadings.single()
            assertEquals("My moon garden",saved.spread)
            assertEquals(labels,drawnCards(saved.drawsJson).map {it.position})
            assertEquals(6,drawnCards(saved.drawsJson).map {it.name}.toSet().size)
            pageId=store.journalReading(saved).id
            rule.activity.setContent {LilylyTheme(true){TarotSpreadsScreen(store,{}, {})}}
        }
        rule.onNodeWithTag("personal-spreads").performScrollToNode(hasText("Remove spread"))
        rule.onNodeWithText("Remove spread").performScrollTo().performClick()
        rule.onNodeWithText("Keep spread").performClick()
        rule.runOnUiThread {assertEquals(1,AppStore(rule.activity).tarotSpreads.size)}
        rule.onNodeWithText("Remove spread").performClick()
        rule.onNodeWithText("Remove template").performClick()
        rule.runOnUiThread {
            val reloaded=AppStore(rule.activity)
            assertTrue(reloaded.tarotSpreads.isEmpty())
            assertEquals(labels,drawnCards(reloaded.tarotReadings.single().drawsJson).map {it.position})
            assertTrue(reloaded.journalEntries.any {it.id==pageId})
        }
    }
}
