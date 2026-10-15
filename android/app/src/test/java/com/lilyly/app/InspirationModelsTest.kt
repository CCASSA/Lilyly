package com.lilyly.app
import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
class InspirationModelsTest {
    @Test fun linksAreExplicitSafeHttpsReferences() {
        assertEquals("https://www.pinterest.com/pin/123/",inspirationUrl(" https://www.pinterest.com/pin/123/ "))
        listOf("javascript:alert(1)","file:///private/photo","https://name:secret@example.org/path","http://example.org","https://example.org:23/path","not a link").forEach {assertNull(inspirationUrl(it))}
    }
    @Test fun oldPagesAndPiecesStillLoad() {
        assertEquals("",JournalEntry.fromJson(JSONObject("{\"title\":\"Old\"}")).sourceUrl)
        assertEquals("",pagePieces("[{\"id\":\"old\",\"kind\":\"text\",\"content\":\"Keep me\"}]").single().sourceCredit)
    }
    @Test fun imageAndLinkCardsKeepTheirAttributionOnCanvas() {
        val card=JournalEntry(section="Inspiration",title="Leaves",imageUri="file:///local/leaf.png",sourceUrl="https://example.org/art",sourceCredit="Artist")
        val piece=inspirationPiece(card)
        assertEquals("photo",piece.kind);assertEquals(card.imageUri,piece.content)
        val restored=pagePieces(piecesJson(listOf(piece))).single()
        assertEquals(card.sourceUrl,restored.sourceUrl);assertEquals("Artist",restored.sourceCredit)
        assertEquals("text",inspirationPiece(card.copy(imageUri="",body="A thought")).kind)
        assertEquals(card.sourceUrl,JournalEntry.fromJson(card.toJson()).sourceUrl)
    }
    @Test fun collectionsFavoritesAndSearchStayIndependent() {
        val card=JournalEntry(section="Inspiration",title="Moon",notebook="Celestial",sourceCredit="Artist",favorite=true)
        assertTrue(matchesInspiration(card,"artist","Celestial",true))
        assertFalse(matchesInspiration(card,"artist","Botanical",true))
        assertFalse(matchesInspiration(card.copy(section="Journal"),"","",false))
        assertFalse(matchesInspiration(card.copy(favorite=false),"","",true))
    }
}
