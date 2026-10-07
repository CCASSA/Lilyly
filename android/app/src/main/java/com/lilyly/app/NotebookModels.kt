package com.lilyly.app

import java.time.LocalDateTime
import java.util.UUID

val notebookCoverNames=listOf("Botanical","Celestial","Wine","Parchment")
fun notebookKey(section:String,name:String)="$section::$name"
fun freshPage(source:JournalEntry,section:String,notebook:String):JournalEntry {
    val now=LocalDateTime.now().toString()
    return source.copy(id=UUID.randomUUID().toString(),section=section,notebook=notebook,favorite=false,createdAt=now,updatedAt=now,
        canvasJson=piecesJson(pagePieces(source.canvasJson).map {it.copy(id=UUID.randomUUID().toString())}))
}
fun builtInPages()=listOf(
    JournalEntry(id="daily-template",title="A little of today",body="What stayed with me\n\nWhat I need\n\nOne small kindness",paper="Lined"),
    JournalEntry(id="ritual-template",title="An intention kept",body="My intention\n\nWhat I gathered\n\nMy practice\n\nWhat I noticed afterward",paper="Botanical"),
    JournalEntry(id="dream-template",title="Through the dream door",body="What I remember\n\nPeople, places and symbols\n\nHow it felt to wake",paper="Midnight"),
    JournalEntry(id="scrapbook-template",title="A pressed memory",paper="Parchment",canvasJson=piecesJson(listOf(PagePiece(kind="sticker",content="❦",x=.6f,y=.7f,width=.25f),PagePiece(content="A memory to keep",x=.12f,y=.4f))))
)
