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
class ReadingHistoryJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private var previous=emptyMap<String,String>()
    @org.junit.After fun restoreFixture() {if(previous.isNotEmpty())rule.runOnUiThread {SecurePreferences(rule.activity).writeBatch(previous)}}
    @Test fun datedSessionsKeepLegacyTimeAndOpenTheirRecordedPlace() {
        var opened=""
        rule.runOnUiThread {
            val secure=SecurePreferences(rule.activity)
            previous=mapOf("books" to secure.get("books","[]"))
            secure.writeBatch(mapOf("books" to "[]"))
            val store=AppStore(rule.activity)
            val session=ReadingSession(startedAt="2026-10-22T20:00:00+02:00",endedAt="2026-10-22T20:12:00+02:00",seconds=720,firstPosition=0,lastPosition=3)
            store.saveBook(keepReadingSession(LibraryBook(id="history-fixture",title="The Moonlit Garden",author="Lilyly fixture",format="EPUB",units=8,readingSeconds=300),session))
            val reloaded=AppStore(rule.activity)
            assertEquals(1020L,reloaded.books.single().readingSeconds)
            assertEquals(1,reloaded.books.single().sessions.size)
            rule.activity.setContent {LilylyTheme(true){ReadingHistoryScreen(store,{}, {id,position->opened="$id:$position"})}}
        }
        rule.onNodeWithText("5 min from earlier versions has no dated session history.").assertExists()
        rule.onNodeWithTag("reading-history").performScrollToNode(hasText("Return to this place"))
        rule.onNodeWithText("Return to this place").performScrollTo().performClick()
        rule.runOnUiThread {assertEquals("history-fixture:3",opened)}
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"reading-history.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
    }
}
