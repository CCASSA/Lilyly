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
class InsightsJourneyTest {
    private var previous=emptyMap<String,String>()
    @org.junit.After fun restoreFixture() {if(previous.isNotEmpty())rule.runOnUiThread {SecurePreferences(rule.activity).writeBatch(previous)}}
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun chooseSourcesInspectEvidenceAndKeepChoices() {
        lateinit var store:AppStore
        var opened=""
        val today=LocalDate.now()
        val sleeps=(1..10).map {i -> val wake=today.minusDays(i.toLong()).atTime(8,0);SleepRecord(id="insight-sleep-$i",bedtime=wake.minusHours(if(i<=5)5 else 8).toString(),wakeTime=wake.toString())}
        rule.runOnUiThread {
            val secure=SecurePreferences(rule.activity)
            previous=mapOf("sleep" to secure.get("sleep","[]"),"mental" to secure.get("mental","[]"),"settings" to secure.get("settings","{}"))
            secure.writeBatch(mapOf("sleep" to "[]","mental" to "[]"))
            store=AppStore(rule.activity)
            sleeps.forEachIndexed {i,r ->store.saveSleep(r);store.addMentalCheckIn(MentalCheckIn(id="insight-mind-$i",dateTime=r.date+"T12:00",anxiety=if(i<5)8 else 2,detailedRatings=true))}
            store.updateInsightPreferences(emptySet(),30)
            rule.activity.setContent {LilylyTheme(true) {InsightsScreen(store,{}, {opened=it})}}
        }
        rule.onNodeWithText("Choose insight data").performClick()
        rule.onAllNodes(isToggleable())[0].performClick()
        rule.onAllNodes(isToggleable())[1].performClick()
        rule.onNodeWithText("Done").performClick()
        rule.onNodeWithTag("insights").performScrollToNode(hasText("Rest & anxiety"))
        rule.onNodeWithText("Rest & anxiety").assertIsDisplayed()
        rule.onAllNodesWithText("View recorded days")[0].performScrollTo().performClick()
        rule.onNodeWithTag("insights").performScrollToNode(hasText(sleeps.first().date+" · 8.0/10"))
        rule.onNodeWithText(sleeps.first().date+" · 8.0/10").performScrollTo().performClick()
        rule.runOnUiThread {assertEquals(sleeps.first().date,opened);assertEquals(setOf("Sleep","Sanctuary"),AppStore(rule.activity).insightData)}
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"insights.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.runOnUiThread {
            sleeps.forEach {store.deleteSleep(it.id)}
            store.mentalCheckIns.removeAll {it.id.startsWith("insight-mind-")}
            store.updateInsightPreferences(emptySet(),90)
        }
    }
}
