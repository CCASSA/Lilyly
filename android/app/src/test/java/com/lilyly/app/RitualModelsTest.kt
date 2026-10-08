package com.lilyly.app
import org.junit.Test
import org.junit.Assert.*
import org.json.JSONObject
class RitualModelsTest {
    @Test fun legacyJournalAndRitualMetadataRoundTripWithoutLosingArtwork() {
        assertEquals("",JournalEntry.fromJson(JSONObject().put("title","Old spell")).ritualJson)
        val r=RitualDetails("Rest","Paper","Write", "2026-10-09","2026-10-10","Waxing","Quieter")
        val page=JournalEntry(title="A practice",imageUri="content://photo",body="My own words",ritualJson=r.toJson())
        val restored=JournalEntry.fromJson(page.toJson())
        assertEquals(r,RitualDetails.fromJson(restored.ritualJson));assertEquals(page.body,restored.body);assertEquals(page.imageUri,restored.imageUri)
    }
    @Test fun freshPracticeKeepsPlanButNotPreviousExperience() {
        val old=JournalEntry(title="Rest",body="Private memory",ritualJson=RitualDetails("Rest","Paper","Write","2026-10-01","2026-10-02","Full","Felt calm").toJson())
        val fresh=repeatRitual(old);val details=RitualDetails.fromJson(fresh.ritualJson)
        assertNotEquals(old.id,fresh.id);assertEquals("Rest",details.intention);assertEquals("Write",details.steps)
        assertEquals("",details.completedDate);assertEquals("",details.reflection);assertEquals("",fresh.body)
        assertEquals("Felt calm",RitualDetails.fromJson(old.ritualJson).reflection)
    }
}
