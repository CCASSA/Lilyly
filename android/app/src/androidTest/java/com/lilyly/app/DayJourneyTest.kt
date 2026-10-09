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
import java.time.LocalDate
@RunWith(AndroidJUnit4::class)
class DayJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun gatherDateHideThreadPersistChoiceAndOpenActualPage() {
        lateinit var store:AppStore
        val today=LocalDate.now()
        rule.runOnUiThread {
            store=AppStore(rule.activity)
            store.addMentalCheckIn(MentalCheckIn(dateTime=today.toString()+"T09:00",feelings=setOf("Quietly hopeful"),detailedRatings=false))
            store.saveSleep(SleepRecord(bedtime=today.minusDays(1).toString()+"T23:00",wakeTime=today.toString()+"T07:00",quality="Restful"))
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
        rule.runOnUiThread {store.setDaySections(store.daySections-"Sleep");assertFalse("Sleep" in AppStore(rule.activity).daySections)}
        rule.onNodeWithTag("day-threads").performScrollToNode(hasText("Open page · A thread worth keeping"))
        rule.onNodeWithText("Open page · A thread worth keeping").performClick()
        rule.onNodeWithText("Page title").assertExists()
        rule.runOnUiThread {store.setDaySections(dayThreadCategories.toSet())}
    }
}
