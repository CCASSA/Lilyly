package com.lilyly.app
import org.junit.Assert.*
import org.junit.Test
class SharedInspirationTest {
    @Test fun textStaysIntactAndFirstHttpsSourceIsSuggested() {
        val text="A fern study https://example.org/art. More words"
        assertEquals(text,checkedSharedInspiration("Fern",text,emptyList()).text)
        assertEquals("https://example.org/art",sharedInspirationSource(text))
        assertEquals("",sharedInspirationSource("http://example.org javascript:alert(1)"))
    }
    @Test fun onlyGrantedContentStyleImagesAreAccepted() {
        assertEquals(listOf("content://gallery/1"),checkedSharedInspiration("","",listOf("content://gallery/1","content://gallery/1")).images)
        listOf("file:///private/secret","https://remote/image","content:///missing","content://name:secret@gallery/image").forEach {uri ->assertTrue(runCatching {checkedSharedInspiration("","",listOf(uri))}.isFailure)}
    }
    @Test fun emptyAndOversizedPayloadsAreRejectedWithoutTruncation() {
        assertTrue(runCatching {checkedSharedInspiration("title","",emptyList())}.isFailure)
        assertTrue(runCatching {checkedSharedInspiration("x".repeat(161),"body",emptyList())}.isFailure)
        assertTrue(runCatching {checkedSharedInspiration("","x".repeat(20001),emptyList())}.isFailure)
        assertTrue(runCatching {checkedSharedInspiration("","",(1..9).map {"content://gallery/$it"})}.isFailure)
    }
    @Test fun multipleCardsKeepIndependentImagesAndSharedCredit() {
        val rows=sharedInspirationEntries("batch","Leaves","A thought","https://example.org/art","Artist","Botanical",listOf("file:///one","file:///two"))
        assertEquals(listOf("batch-0","batch-1"),rows.map {it.id})
        assertEquals(listOf("Leaves · 1","Leaves · 2"),rows.map {it.title})
        assertTrue(rows.all {it.sourceCredit=="Artist" && it.notebook=="Botanical"})
        assertEquals("file:///two",rows[1].imageUri)
        assertEquals("Artist",inspirationPiece(rows[0]).sourceCredit)
    }
    @Test fun textOnlyCardRoundTripsWithStableIdentity() {
        val a=sharedInspirationEntries("batch","","Words","","","",emptyList()).single()
        assertEquals("A little inspiration",a.title)
        assertEquals(a,JournalEntry.fromJson(a.toJson()))
        assertEquals(a.id,sharedInspirationEntries("batch","Edited","Words","","","",emptyList()).single().id)
    }
    @Test fun unsafeEditedSourceAndExcessCreditCannotBeSaved() {
        assertTrue(runCatching {sharedInspirationEntries("id","","Words","javascript:alert(1)","","",emptyList())}.isFailure)
        assertTrue(runCatching {sharedInspirationEntries("id","","Words","","x".repeat(1001),"",emptyList())}.isFailure)
    }
}
