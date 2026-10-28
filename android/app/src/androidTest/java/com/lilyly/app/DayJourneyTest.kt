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
import org.junit.After
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate
@RunWith(AndroidJUnit4::class)
class DayJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private var previousReading=emptyMap<String,String>()
    @After fun restoreReadingFixture() {if(previousReading.isNotEmpty())rule.runOnUiThread {SecurePreferences(rule.activity).writeBatch(previousReading)}}
    @After fun cleanUpSampleNight() { rule.runOnUiThread {AppStore(rule.activity).deleteSleep("day-journey-night")} }
    @Test fun gatherDateHideThreadPersistChoiceAndOpenActualPage() {
        lateinit var store:AppStore
        val today=LocalDate.now()
        rule.runOnUiThread {
            store=AppStore(rule.activity)
            store.addMentalCheckIn(MentalCheckIn(dateTime=today.toString()+"T09:00",feelings=setOf("Quietly hopeful"),detailedRatings=false))
            store.saveSleep(SleepRecord(id="day-journey-night",bedtime=today.minusDays(1).toString()+"T23:00",wakeTime=today.toString()+"T07:00",quality="Restful"))
            store.upsertJournal(JournalEntry(title="A thread worth keeping",body="My connected day",createdAt=today.toString()+"T10:00"))
            rule.activity.setContent {LilylyApp(store)}
        }
        rule.onNodeWithText("Gather the threads of a day").performClick()
        rule.onNodeWithTag("day-threads").performScrollToNode(hasText("Quietly hopeful"))
        rule.onNodeWithText("Quietly hopeful").assertIsDisplayed()
        rule.onNodeWithTag("day-threads").performScrollToNode(hasText("8h 0m of recorded rest"))
        rule.onNodeWithText("8h 0m of recorded rest").assertIsDisplayed()
        rule.waitForIdle()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"day-threads.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.runOnUiThread {store.updateDaySections(store.daySections-"Sleep");assertFalse("Sleep" in AppStore(rule.activity).daySections)}
        rule.onNodeWithTag("day-threads").performScrollToNode(hasText("Open page · A thread worth keeping"))
        rule.onNodeWithText("Open page · A thread worth keeping").performClick()
        rule.onNodeWithText("Page title").assertExists()
        rule.runOnUiThread {store.updateDaySections(dayThreadCategories.toSet())}
    }
    @Test fun readingThreadOpensRecordedPlaceAndVisibilityChoicePersists() {
        lateinit var store:AppStore
        var opened=""
        val today=LocalDate.now().toString()
        rule.runOnUiThread {
            val secure=SecurePreferences(rule.activity)
            previousReading=mapOf("books" to secure.get("books","[]"),"settings" to secure.get("settings","{}"))
            secure.writeBatch(mapOf("books" to "[]"))
            store=AppStore(rule.activity)
            store.updateDaySections(setOf("Reading"))
            val session=ReadingSession(startedAt=today+"T20:00:00+02:00",endedAt=today+"T20:10:00+02:00",seconds=600,firstPosition=0,lastPosition=3)
            store.saveBook(LibraryBook(id="day-reading",title="The Moonlit Garden",format="EPUB",units=8,readingSeconds=600,sessions=listOf(session)))
            rule.activity.setContent {LilylyTheme(true) {DayScreen(store,initialDate=today,onBack={},onPage={},onSpace={_,_->},onBook={id,at->opened="$id:$at"})}}
        }
        rule.onNodeWithTag("day-threads").performScrollToNode(hasText("Return to this reading place"))
        rule.onNodeWithText("Return to this reading place").performClick()
        rule.runOnUiThread {assertEquals("day-reading:3",opened)}
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"day-reading.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.onNodeWithTag("day-threads").performScrollToNode(hasText("Choose threads"))
        rule.onNodeWithText("Choose threads").performClick()
        rule.onNodeWithTag("thread-Reading").performScrollTo().performClick()
        rule.onNodeWithText("Done").performClick()
        rule.onNodeWithText("All threads are hidden. Choose the ones you want to see.").assertExists()
        rule.runOnUiThread {
            val reloaded=AppStore(rule.activity)
            assertFalse("Reading" in reloaded.daySections)
            assertEquals(1,reloaded.books.single().sessions.size)
        }
    }
}
