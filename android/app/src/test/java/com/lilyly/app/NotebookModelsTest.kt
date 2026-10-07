package com.lilyly.app
import org.junit.Test
import org.junit.Assert.*
class NotebookModelsTest {
    @Test fun templateCreatesIndependentPageAndElementIdsWhileKeepingAttachments() {
        val piece=PagePiece(kind="photo",content="content://owned/photo")
        val template=JournalEntry(id="source",section="Templates",title="A layout",notebook="Old",favorite=true,canvasJson=piecesJson(listOf(piece)),imageUri="content://owned/legacy",inkJson="[]")
        val page=freshPage(template,"Grimoire","My book")
        assertNotEquals(template.id,page.id);assertEquals("Grimoire",page.section);assertEquals("My book",page.notebook);assertFalse(page.favorite)
        assertNotEquals(piece.id,pagePieces(page.canvasJson).single().id)
        assertEquals(piece.content,pagePieces(page.canvasJson).single().content);assertEquals(template.imageUri,page.imageUri)
        assertEquals("source",template.id)
    }
    @Test fun curatedTemplatesHaveUniqueIdsAndPortablePaper() {
        val pages=builtInPages();assertEquals(4,pages.map {it.id}.distinct().size)
        assertTrue(pages.all {it.paper in listOf("Lined","Parchment","Botanical","Midnight")})
    }
}
