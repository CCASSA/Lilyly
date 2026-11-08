package com.lilyly.app

import java.time.LocalDate
import java.time.ZoneId

fun moonReflectionDraft(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): JournalEntry {
    val phase=moonPhaseName(date.atTime(12,0),zone)
    return JournalEntry(
        title="$phase · $date", tags="moon", reflectionDate=date.toString(),
        canvasJson=piecesJson(listOf(PagePiece(kind="text",content="$date\n$phase · approximate")))
    )
}
