package com.lilyly.app

import java.net.URI

fun inspirationUrl(raw:String):String? = runCatching {
    val u=URI(raw.trim())
    require(u.scheme=="https" && !u.host.isNullOrBlank() && u.userInfo==null && (u.port==-1 || u.port==443))
    u.toASCIIString()
}.getOrNull()
fun inspirationPiece(card:JournalEntry)=PagePiece(
    kind=if(card.imageUri.isNotBlank()) "photo" else "text",
    content=card.imageUri.ifBlank {listOf(card.title,card.body).filter {it.isNotBlank()}.joinToString("\n")},
    sourceUrl=inspirationUrl(card.sourceUrl).orEmpty(),sourceCredit=card.sourceCredit,
    y=.22f,width=.65f
)
fun matchesInspiration(card:JournalEntry,query:String,collection:String,favorites:Boolean)=
    card.section=="Inspiration" && (collection.isBlank() || card.notebook==collection) && (!favorites || card.favorite) &&
    listOf(card.title,card.body,card.tags,card.notebook,card.sourceCredit).joinToString(" ").contains(query,true)
