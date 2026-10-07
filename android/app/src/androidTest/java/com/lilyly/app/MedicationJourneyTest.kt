package com.lilyly.app
import android.app.NotificationManager
import android.content.Intent
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
class MedicationJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun correctScheduledEntryWithoutDuplicatingItAndKeepRefillCount() {
        lateinit var store:AppStore
        rule.runOnUiThread {
            store=AppStore(rule.activity)
            store.addMedication(Medication(id="medication-fixture",name="Test cabinet entry",time="08:00",remaining=4,refillAt=5))
            rule.activity.setContent {LilylyTheme(true) {MedicationTab(store)}}
        }
        rule.onNodeWithTag("medication-cabinet").performScrollToNode(hasText("08:00 · Not recorded"))
        rule.onNodeWithText("08:00 · Not recorded").performClick()
        rule.onNodeWithText("Skipped").performClick()
        rule.onNodeWithText("Save entry").performClick()
        rule.onNodeWithText("08:00 · Skipped").performClick()
        rule.onNodeWithText("Taken").performClick()
        rule.onNodeWithText("Save entry").performClick()
        rule.runOnUiThread {
            val loaded=AppStore(rule.activity)
            val logs=loaded.medicationLogs.filter {it.medicationId=="medication-fixture" && it.scheduledFor==LocalDate.now().atTime(8,0).toString()}
            assertEquals(1,logs.size);assertEquals("Taken",logs.single().status)
            assertEquals(4,loaded.medications.first {it.id=="medication-fixture"}.remaining)
        }
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"medication.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
    }
    @Test fun reminderPostsPrivateGenericNotificationWithoutLoggingADose() {
        val context=rule.activity
        val automation=InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.executeShellCommand("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS").use {android.os.ParcelFileDescriptor.AutoCloseInputStream(it).readBytes()}
        val store=AppStore(context)
        val before=store.medicationLogs.size
        val at=LocalDate.now().atTime(8,0)
        store.addMedication(Medication(id="notification-fixture",name="Private fixture name",dose="private dose",time="08:00",reminders=true))
        try {
            MedicationReminderReceiver().onReceive(context,Intent("com.lilyly.MEDICATION").putExtra("scheduled",at.toString()))
            val notification=context.getSystemService(NotificationManager::class.java).activeNotifications.first {it.id==410}.notification
            assertEquals("A moment for your care",notification.extras.getString("android.title"))
            assertFalse(notification.extras.toString().contains("Private fixture name"))
            assertEquals(before,AppStore(context).medicationLogs.size)
        } finally {
            store.addMedication(store.medications.first {it.id=="notification-fixture"}.copy(active=false))
            context.getSystemService(NotificationManager::class.java).cancel(410)
        }
    }
}
