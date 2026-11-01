package com.lilyly.app

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/** Retain existing page memories without exposing the retired service controls. */
@Composable
fun LegacyMusicNotes(raw:String) {
    val saved=musicAttachments(raw)
    if(saved.isNotEmpty()) Column {
        Text("Saved music notes",style=MaterialTheme.typography.titleMedium)
        saved.forEach { item ->
            if(item.title.isNotBlank()) Text(item.title)
            if(item.creator.isNotBlank()) Text(item.creator)
            if(item.label.isNotBlank()) Text(item.label)
            Text(item.url,style=MaterialTheme.typography.bodySmall)
        }
    }
}
