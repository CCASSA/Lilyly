package com.lilyly.app
import org.junit.Assert.*
import org.junit.Test
class InkModelsTest {
    @Test fun legacyCoordinatesArePreservedUntilViewBoxIsKnown() {
        val d=readInk("[[{\"x\":43,\"y\":77},{\"x\":99,\"y\":103}]]")
        assertEquals(0f,d.width);assertEquals(43f,d.strokes.single().points.first().x)
        assertEquals(d,readInk(inkJson(d)))
    }
    @Test fun toolsPressureAndDimensionsSurviveRoundTrip() {
        val d=InkDocument(1000f,1300f,listOf(InkStroke(listOf(InkPoint(5f,6f,.4f),InkPoint(80f,90f,1.2f)),0xFF842E4D,8f),InkStroke(listOf(InkPoint(20f,50f)),0xFFAD7C26,30f,true)))
        assertEquals(d,readInk(inkJson(d)))
    }
    @Test fun eraserHitsSegmentBetweenWidelySpacedPoints() {
        val s=InkStroke(listOf(InkPoint(0f,0f),InkPoint(100f,100f)),width=2f)
        assertTrue(inkHits(s,InkPoint(50f,50f),4f))
        assertFalse(inkHits(s,InkPoint(50f,85f),4f))
        assertTrue(inkHits(InkStroke(listOf(InkPoint(3f,4f))),InkPoint(3f,4f),1f))
    }
    @Test fun malformedDataIsSafeAndUnsupportedVersionDoesNotRender() {
        assertTrue(readInk("broken").strokes.isEmpty())
        assertTrue(readInk("{\"version\":99}").strokes.isEmpty())
        assertTrue(readInk(clearInk()).strokes.isEmpty())
        assertTrue(readInk("[[{\"x\":\"NaN\",\"y\":4}]]").strokes.single().points.isEmpty())
    }
    @Test fun inkPieceAndPageRoundTripKeepIndependentLayers() {
        val ink=inkJson(InkDocument(500f,700f,listOf(InkStroke(listOf(InkPoint(30f,40f))))))
        val page=JournalEntry(inkJson=ink,canvasJson=piecesJson(listOf(PagePiece(kind="ink",content=ink))))
        val restored=JournalEntry.fromJson(page.toJson()).copy(inkJson=clearInk())
        assertEquals(ink,pagePieces(restored.canvasJson).single().content)
        assertEquals("[]",restored.inkJson)
    }
}
