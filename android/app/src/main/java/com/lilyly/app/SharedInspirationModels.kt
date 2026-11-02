package com.lilyly.app

import java.net.URI

/** Values received from another app are untrusted and bounded before review. */
data class SharedInspiration(val title:String="",val text:String="",val images:List<String> = emptyList())
fun checkedSharedInspiration(title:String,text:String,images:List<String>):SharedInspiration {
    require(title.length<=160) {"The shared title is too long. Share a shorter title or just the image."}
    require(text.length<=20000) {"Share up to 20,000 characters at a time."}
    require(images.size<=8) {"Share up to eight images at a time."}
    images.forEach {raw ->
        val uri=URI(raw)
        require(uri.scheme=="content" && !uri.authority.isNullOrBlank() && uri.userInfo==null) {"Share images through your gallery or another Android app."}
    }
    require(text.isNotBlank() || images.isNotEmpty()) {"This share did not include text or an image."}
    return SharedInspiration(title,text,images.distinct())
}
fun sharedInspirationSource(text:String):String = Regex("https://[^\\s<>\"']+").findAll(text)
    .mapNotNull {inspirationUrl(it.value.trimEnd('.',',',';',')',']','}'))}.firstOrNull().orEmpty()
fun sharedInspirationEntries(batchId:String,title:String,body:String,source:String,credit:String,collection:String,images:List<String>):List<JournalEntry> {
    require(batchId.isNotBlank() && title.length<=160 && body.length<=20000 && credit.length<=1000 && collection.length<=160)
    require(images.size<=8)
    val url=if(source.isBlank()) "" else requireNotNull(inspirationUrl(source)) {"Use a full HTTPS source link."}
    require(images.isNotEmpty() || body.isNotBlank() || url.isNotBlank()) {"Add a thought, image or source to keep."}
    val count=maxOf(1,images.size)
    return (0 until count).map {index -> JournalEntry(
        id="$batchId-$index",section="Inspiration",
        title=title.trim().ifBlank {"A little inspiration"}+(if(count>1) " · ${index+1}" else ""),
        body=body,imageUri=images.getOrElse(index){""},sourceUrl=url,sourceCredit=credit.trim(),notebook=collection.trim()
    )}
}
