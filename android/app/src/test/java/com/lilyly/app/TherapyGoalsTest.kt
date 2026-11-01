package com.lilyly.app

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONArray
import org.json.JSONObject

class TherapyGoalsTest {
    private val goal=TherapyGoal(id="goal",title="Make space to rest",intention="A gentle intention")
    private val reflection=GoalReflection(id="reflection",recordedAt="2026-10-29T23:50:00+02:00",state="Resting",body="Taking this slowly")
    @Test fun goalReflectionsRoundTripAndDuplicateSavesDoNotMultiply() {
        val saved=keepGoalReflection(goal,reflection)
        assertEquals("Resting",saved.state)
        assertEquals(saved,TherapyGoal.fromJson(saved.toJson()))
        assertEquals(saved,keepGoalReflection(saved,reflection))
    }
    @Test fun removingLatestReflectionRestoresPriorStateAndPreservesGoal() {
        val resting=keepGoalReflection(goal,reflection)
        val reached=keepGoalReflection(resting,reflection.copy(id="second",state="Reached"))
        assertEquals("Reached",reached.state)
        assertEquals(resting,removeGoalReflection(reached,"second"))
        assertEquals(goal,removeGoalReflection(resting,"reflection"))
        assertEquals("Growing",goal.state)
    }
    @Test fun oldSessionTextRemainsSeparateFromNewGoalLinks() {
        val old=TherapyNote.fromJson(JSONObject().put("goals","Older freeform goals"))
        assertTrue(old.goalIds.isEmpty());assertEquals("Older freeform goals",old.goals)
        val linked=old.copy(goalIds=setOf("goal"))
        assertEquals(linked,TherapyNote.fromJson(linked.toJson()))
        assertEquals(old.goals,linked.copy(goalIds=emptySet()).goals)
    }
    @Test fun backupMergeRetainsCurrentGoalAndIndependentIncomingGoal() {
        fun snapshot(goals:List<TherapyGoal>)=mapOf("therapyGoals" to JSONArray(goals.map {it.toJson()}).toString())
        val current=keepGoalReflection(goal,reflection)
        val other=goal.copy(id="other",title="Another intention")
        val result=mergeSnapshots(snapshot(listOf(current)),snapshot(listOf(goal,other)))
        val rows=JSONArray(result.getValue("therapyGoals"))
        assertEquals(current,TherapyGoal.fromJson(rows.getJSONObject(0)))
        assertEquals(other,TherapyGoal.fromJson(rows.getJSONObject(1)))
        validateSnapshot(mapOf("therapy" to JSONArray(listOf(TherapyNote(goals="legacy").toJson())).toString()))
    }
    @Test fun malformedGoalBackupsAreRejectedBeforeRestore() {
        assertTrue(runCatching {TherapyGoal.fromJson(goal.copy(title=" ").toJson())}.isFailure)
        assertTrue(runCatching {keepGoalReflection(goal,reflection.copy(state="diagnosed"))}.isFailure)
        assertTrue(runCatching {keepGoalReflection(goal,reflection.copy(recordedAt="broken"))}.isFailure)
        assertTrue(runCatching {validateSnapshot(mapOf("therapyGoals" to JSONArray().put(goal.copy(title="").toJson()).toString()))}.isFailure)
    }
    @Test fun datedReflectionJoinsTherapyDayWithoutInventedProgressScore() {
        val rows=dayThreads(emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),goals=listOf(keepGoalReflection(goal,reflection)))
        val row=rows.single()
        assertEquals("2026-10-29",row.date);assertEquals("Therapy",row.category)
        assertEquals("Resting\nTaking this slowly",row.detail)
        assertTrue(dayThreads(emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),emptyList(),goals=listOf(goal)).isEmpty())
    }
}
