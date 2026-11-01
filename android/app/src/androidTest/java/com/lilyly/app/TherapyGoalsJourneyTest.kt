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
class TherapyGoalsJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private var previous=emptyMap<String,String>()
    @org.junit.After fun restoreFixture() {if(previous.isNotEmpty())rule.runOnUiThread {SecurePreferences(rule.activity).writeBatch(previous)}}
    @Test fun nurtureGoalLinkSessionKeepJournalAndRemoveWithoutLosingSession() {
        lateinit var store:AppStore
        var goalId=""
        var pageId=""
        rule.runOnUiThread {
            val secure=SecurePreferences(rule.activity)
            previous=listOf("therapyGoals","therapy","journal").associateWith {secure.get(it,"[]")}
            secure.writeBatch(previous.keys.associateWith {"[]"})
            store=AppStore(rule.activity)
            store.addTherapyNote(TherapyNote(id="goal-session",title="A gentle session",goals="Keep these older words"))
            rule.activity.setContent {LilylyTheme(true){TherapyTab(store,{})}}
        }
        rule.onNodeWithText("My growing things").performClick()
        rule.onNodeWithText("Plant a goal").performClick()
        rule.onNodeWithText("Keep intention").assertIsNotEnabled()
        rule.onNodeWithText("Goal title").performTextInput("Make space to rest")
        rule.onNodeWithText("Why this matters to me").performTextInput("One quiet evening at a time")
        rule.onNodeWithText("Why this matters to me").performImeAction()
        rule.onNodeWithText("Keep intention").performClick()
        rule.onNodeWithText("Pause & reflect").performClick()
        rule.onNodeWithText("Resting").performClick()
        rule.onNodeWithText("What I want to remember").performTextInput("I made room for a quieter evening")
        rule.onNodeWithText("What I want to remember").performImeAction()
        rule.onNodeWithText("Keep reflection").performClick()
        rule.runOnUiThread {
            val goal=AppStore(rule.activity).therapyGoals.single();goalId=goal.id
            assertEquals("Resting",goal.state);assertEquals(1,goal.reflections.size)
            val page=store.journalTherapyGoal(goal);pageId=page.id
            store.upsertJournal(page.copy(body="My independent journal words"))
            assertEquals(page.id,store.journalTherapyGoal(store.therapyGoals.single()).id)
        }
        rule.onNodeWithTag("therapy-goals").performScrollToNode(hasText("Back to my goals"))
        rule.onNodeWithText("Back to my goals").performClick()
        rule.onNodeWithText("Back to sessions").performClick()
        rule.onNodeWithTag("therapy-room").performScrollToNode(hasText("Open session"))
        rule.onNodeWithText("Open session").performClick()
        rule.onNodeWithText("Connect goals").performScrollTo().performClick()
        rule.onNodeWithTag("link-goal-$goalId").performScrollTo().performClick()
        rule.onNodeWithText("Keep session").performClick()
        rule.runOnUiThread {assertEquals(setOf(goalId),AppStore(rule.activity).therapyNotes.single().goalIds)}
        rule.onNodeWithTag("therapy-room").performScrollToNode(hasText("My growing things"))
        rule.onNodeWithText("My growing things").performClick()
        rule.onNodeWithTag("therapy-goals").performScrollToNode(hasText("Open goal"))
        rule.onNodeWithText("Open goal").performClick()
        rule.onNodeWithTag("therapy-goals").performScrollToNode(hasText("I made room for a quieter evening"))
        rule.onNodeWithText("I made room for a quieter evening").assertIsDisplayed()
        rule.waitForIdle()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(rule.activity.filesDir,"therapy-goals.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.onNodeWithTag("therapy-goals").performScrollToNode(hasText("Remove goal"))
        rule.onNodeWithText("Remove goal").performClick()
        rule.onNodeWithText("Keep goal").performClick()
        rule.runOnUiThread {assertEquals(1,AppStore(rule.activity).therapyGoals.size)}
        rule.onNodeWithText("Remove goal").performClick()
        rule.onNodeWithText("Remove goal and reflections").performClick()
        rule.runOnUiThread {
            val reloaded=AppStore(rule.activity)
            assertTrue(reloaded.therapyGoals.isEmpty())
            assertEquals("Keep these older words",reloaded.therapyNotes.single().goals)
            assertTrue(reloaded.therapyNotes.single().goalIds.isEmpty())
            assertEquals("My independent journal words",reloaded.journalEntries.first {it.id==pageId}.body)
        }
    }
}
