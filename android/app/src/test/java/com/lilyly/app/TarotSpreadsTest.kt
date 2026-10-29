package com.lilyly.app
import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import kotlin.random.Random

class TarotSpreadsTest {
    private val spread=TarotSpread("personal","My garden",listOf("Root","Rain","Light","Bloom","Release","Carry forward"))
    @Test fun customSpreadsRoundTripWhileOlderSettingsRemainEmpty() {
        assertTrue(readTarotSpreads(JSONObject()).isEmpty())
        assertEquals(listOf(spread),readTarotSpreads(JSONObject().put("tarotSpreads",tarotSpreadsJson(listOf(spread)))))
    }
    @Test fun invalidRowsCannotOverrideBuiltinsOrBreakOtherTemplates() {
        val rows=tarotSpreadsJson(listOf(spread)).put("broken","not an object").put("quiet",spread.copy(id="quiet").toJson())
        assertEquals(listOf(spread),readTarotSpreads(JSONObject().put("tarotSpreads",rows)))
        assertNotNull(tarotSpreadError(spread.copy(positions=emptyList())))
        assertNotNull(tarotSpreadError(spread.copy(positions=List(7){"Position"})))
        assertNotNull(tarotSpreadError(spread.copy(positions=listOf(" "))))
        assertNotNull(tarotSpreadError(spread.copy(name="")))
        assertNotNull(tarotSpreadError(spread.copy(name="x".repeat(81))))
        assertNotNull(tarotSpreadError(spread.copy(positions=listOf("x".repeat(121)))))
    }
    @Test fun sixCardsKeepPositionOrderWithoutRepeatingCards() {
        val cards=drawTarot(spread.positions,false,Random(27))
        assertEquals(spread.positions,cards.map {it.position})
        assertEquals(6,cards.map {it.name}.toSet().size)
        assertTrue(cards.none {it.reversed})
    }
    @Test fun backupMergeKeepsCurrentSpreadAndAddsOtherTemplates() {
        fun settings(rows:List<TarotSpread>)=mapOf("settings" to JSONObject().put("tarotSpreads",tarotSpreadsJson(rows)).toString())
        val other=spread.copy(id="other",name="Another")
        val merged=mergeSnapshots(settings(listOf(spread)),settings(listOf(spread.copy(name="Older name"),other)))
        val restored=readTarotSpreads(JSONObject(merged.getValue("settings")))
        assertEquals(setOf(spread,other),restored.toSet())
    }
    @Test fun readingsKeepTheirOwnLabelsAfterTemplateChanges() {
        val reading=TarotReading(spread=spread.name,drawsJson=drawTarot(spread.positions,true,Random(1)).json())
        val revised=spread.copy(name="New garden",positions=listOf("A different question"))
        assertNotEquals(revised.name,reading.spread)
        assertEquals(spread.positions,drawnCards(TarotReading.fromJson(reading.toJson()).drawsJson).map {it.position})
    }
}
