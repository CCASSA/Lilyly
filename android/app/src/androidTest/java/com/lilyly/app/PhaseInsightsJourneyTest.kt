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
class PhaseInsightsJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private var previous=emptyMap<String,String>()
    @org.junit.After fun restoreFixture() {if(previous.isNotEmpty())rule.runOnUiThread {SecurePreferences(rule.activity).writeBatch(previous)}}
    @Test fun phaseEvidenceOpensDateAndRespectsSourceChoice() {
        lateinit var store:AppStore
        var opened=""
        val start=LocalDate.now().minusDays(60)
        rule.runOnUiThread {
            val secure=SecurePreferences(rule.activity)
            previous=mapOf("cycle" to secure.get("cycle","[]"),"mental" to secure.get("mental","[]"),"settings" to secure.get("settings","{}"))
            secure.writeBatch(mapOf("cycle" to "[]","mental" to "[]"))
            store=AppStore(rule.activity)
            store.updateCyclePreferences(CyclePreferences())
            listOf(0L,28L,56L).forEach {store.upsertCycle(CycleLog(date=start.plusDays(it).toString(),period=true))}
            (0L..55L).forEach {store.addMentalCheckIn(MentalCheckIn(dateTime=start.plusDays(it).toString()+"T12:00",mood=6,detailedRatings=true))}
            store.updateInsightPreferences(setOf("Cycle","Sanctuary"),90)
            rule.activity.setContent {LilylyTheme(true){InsightsScreen(store,{}, {opened=it})}}
        }
        rule.onNodeWithTag("insights").performScrollToNode(hasText("View mood phase dates"))
        rule.onNodeWithText("View mood phase dates").performScrollTo().performClick()
        rule.onNodeWithTag("insights").performScrollToNode(hasText("$start · 6.0/10"))
        rule.onNodeWithText("$start · 6.0/10").performScrollTo().performClick()
        rule.runOnUiThread {assertEquals(start.toString(),opened);assertEquals(setOf("Cycle","Sanctuary"),AppStore(rule.activity).insightData)}
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"phase-insights.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.runOnUiThread {store.updateInsightPreferences(setOf("Sanctuary"),90)}
        rule.onNodeWithText("Mood across phases").assertDoesNotExist()
    }
}
