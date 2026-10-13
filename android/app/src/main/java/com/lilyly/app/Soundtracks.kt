package com.lilyly.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.json.JSONObject

val soundtrackSpaces = mapOf("reading" to "Reading Nook", "sanctuary" to "Sanctuary")
fun uniqueMusic(items: List<MusicAttachment>): List<MusicAttachment> = items.mapNotNull { item ->
    spotifyLink(item.url)?.let { item.copy(url=it.url) }
}.distinctBy { it.url }
fun readSoundtracks(settings: JSONObject): Map<String,String> {
    val rooms=settings.optJSONObject("soundtracks") ?: return emptyMap()
    return soundtrackSpaces.keys.filter { rooms.has(it) }.associateWith {
        musicJson(uniqueMusic(musicAttachments(rooms.optString(it,"[]"))).take(12))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundtrackButton(store:AppStore, space:String, modifier:Modifier=Modifier) {
    var open by rememberSaveable {mutableStateOf(false)}
    val name=soundtrackSpaces.getValue(space)
    val count=musicAttachments(store.soundtracks[space] ?: "[]").size
    OutlinedButton(onClick={open=true},modifier=modifier.testTag("soundtrack-$space")) {
        Text(if(count==0) "♫  Add a soundtrack" else "♫  Soundtrack · $count")
    }
    if(open) ModalBottomSheet(onDismissRequest={open=false},sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=24.dp).padding(bottom=32.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Text(name,style=MaterialTheme.typography.headlineLarge)
            Text("A little music to settle into this space. Changes are saved as you make them. Open a card to listen in Spotify.")
            MusicAttachmentsPanel(
                raw=store.soundtracks[space] ?: "[]", title="Your soundtrack",
                saveHint="This soundtrack is saved automatically on this device.", addLabel="Keep in this space",
                savedMusic=store.savedMusic(),
                onChanged={store.updateSoundtrack(space,it)}
            )
            TextButton(onClick={open=false}) {Text("Done")}
        }
    }
}

@Composable
fun SavedMusicPicker(items:List<MusicAttachment>, onChoose:(MusicAttachment)->Unit, onClose:()->Unit) {
    var query by rememberSaveable {mutableStateOf("")}
    AlertDialog(onDismissRequest=onClose,title={Text("Music you've kept")},text={
        Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(query,{query=it},label={Text("Find saved music")},singleLine=true)
            val visible=items.filter {(it.title+" "+it.creator+" "+it.label).contains(query,true)}
            androidx.compose.foundation.lazy.LazyColumn(Modifier.heightIn(max=340.dp)) {
                if(visible.isEmpty()) item {Text("No saved music matches. Attach a Spotify link to begin.")}
                items(visible.size,key={visible[it].url}) {i ->
                    val item=visible[i]
                    TextButton(onClick={onChoose(item)},modifier=Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth()) {
                            Text(item.title.ifBlank {item.label.ifBlank {"Spotify ${spotifyLink(item.url)?.kind}"}},style=MaterialTheme.typography.titleMedium)
                            if(item.creator.isNotBlank()) Text(item.creator,style=MaterialTheme.typography.bodySmall)
                            if(item.title.isNotBlank() && item.label.isNotBlank()) Text(item.label,style=MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    },confirmButton={TextButton(onClick=onClose){Text("Close")}})
}
