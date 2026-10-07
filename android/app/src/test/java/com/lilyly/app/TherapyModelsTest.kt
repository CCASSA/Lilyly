package com.lilyly.app
import org.junit.Test
import org.junit.Assert.*
import org.json.JSONObject
class TherapyModelsTest {
    @Test fun existingSessionKeepsItsWordsAndDefaultsNewFields() {
        val old=TherapyNote.fromJson(JSONObject("{\"id\":\"old\",\"before\":\"Keep my words\",\"homework\":\"A practice\",\"nextAppointment\":\"Next Tuesday\"}"))
        assertEquals("Keep my words",old.before);assertEquals("Next Tuesday",old.nextAppointment)
        assertEquals("",old.goals);assertFalse(old.homeworkDone);assertEquals("",old.journalId)
    }
    @Test fun questionsGoalsAndPracticeStateRoundTrip() {
        val note=TherapyNote(goals="A personal goal",questions="A question",homework="A practice",homeworkDone=true,journalId="linked")
        assertEquals(note,TherapyNote.fromJson(note.toJson()))
    }
}
