package com.lilyly.app
import org.junit.Test
import org.junit.Assert.*
class WakingTemperatureTest {
    @Test fun oldRecordsStayUnknownAndMeasurementRoundTripsWithoutCreatingARating() {
        val old=CycleLog().toJson().apply {remove("wakingTemperature")}
        val restored=CycleLog.fromJson(old)
        assertEquals("",restored.wakingTemperature)
        val measured=CycleLog.fromJson(restored.copy(wakingTemperature="36.40").toJson())
        assertEquals("36.40",measured.wakingTemperature);assertFalse(measured.ratingsRecorded)
    }
    @Test fun rejectsPartialNonfiniteAndWrongUnits() {
        assertTrue(validWakingTemperature("36,45"))
        listOf("","36.","NaN","98.6","0","36.123").forEach {assertFalse(validWakingTemperature(it))}
    }
}
