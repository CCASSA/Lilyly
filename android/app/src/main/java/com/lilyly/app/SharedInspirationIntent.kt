package com.lilyly.app

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

fun readSharedInspiration(intent:Intent):SharedInspiration {
    require(intent.action==Intent.ACTION_SEND || intent.action==Intent.ACTION_SEND_MULTIPLE) {"This is not a supported share."}
    val type=intent.type.orEmpty()
    require(type=="text/plain" || type.startsWith("image/")) {"Share plain text, a web link or images."}
    val multiple=intent.action==Intent.ACTION_SEND_MULTIPLE
    require(!multiple || type.startsWith("image/")) {"Multiple shares must contain images."}
    val images=if(type.startsWith("image/")) {
        val streams=if(multiple) IntentCompat.getParcelableArrayListExtra(intent,Intent.EXTRA_STREAM,Uri::class.java).orEmpty()
        else listOfNotNull(IntentCompat.getParcelableExtra(intent,Intent.EXTRA_STREAM,Uri::class.java))
        val uris=if(streams.isNotEmpty())streams else intent.clipData?.let {clip ->
            require(clip.itemCount<=8) {"Share up to eight images at a time."}
            (0 until clip.itemCount).mapNotNull {clip.getItemAt(it).uri}
        }.orEmpty()
        require(uris.isNotEmpty()) {"The sending app did not provide a readable image."}
        require(multiple || uris.size==1) {"Choose Share multiple to send more than one image."}
        uris.map {it.toString()}
    } else emptyList()
    return checkedSharedInspiration(intent.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty(),intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString().orEmpty(),images)
}
