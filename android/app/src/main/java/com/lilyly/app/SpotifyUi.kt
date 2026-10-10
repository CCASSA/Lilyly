package com.lilyly.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyScreen(onBack:()->Unit) {
    val context=LocalContext.current
    val session=remember {SpotifySession(context)}
    var linked by remember {mutableStateOf(session.linked)}
    var busy by remember {mutableStateOf(false)}
    var message by remember {mutableStateOf("")}
    LaunchedEffect(Unit) {
        if(session.hasCallback) {
            busy=true
            try {session.complete();message="Spotify is connected on this device."}
            catch(e:CancellationException){throw e}
            catch(e:Exception){message="Could not finish connecting. Please try again and check developer access."}
            finally {linked=session.linked;busy=false}
        }
    }
    Scaffold(topBar={TopAppBar(title={Text("A soundtrack for your world")},navigationIcon={TextButton(onClick=onBack){Text("Back")}})}) {padding ->
        Column(Modifier.padding(padding).padding(24.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(20.dp)) {
            Text("♫",style=MaterialTheme.typography.displayLarge,color=MaterialTheme.colorScheme.primary)
            Text("Spotify & music",style=MaterialTheme.typography.headlineLarge)
            Text("Keep a song beside a memory. Give a ritual its own playlist. Add a Spotify song or playlist link from any journal page.")
            Text(if(linked) "Connected on this device" else "Your music links work without signing in.",style=MaterialTheme.typography.titleMedium)
            if(!session.configured) Text("Account linking needs developer setup in this build. Spotify links can already be saved and opened; fetching titles and artists requires a configured connection.")
            if(linked) Button(onClick={session.disconnect();linked=false;message="Connection removed. Your page attachments are still here."},enabled=!busy){Text("Disconnect Spotify")}
            else Button(onClick={try {context.startActivity(session.begin())}catch(e:Exception){message="Could not open sign-in. Check that a browser is available."}},enabled=session.configured && !busy){Text("Connect Spotify")}
            if(busy) CircularProgressIndicator()
            if(message.isNotBlank()) Text(message)
            HorizontalDivider()
            Text("Private pages, personal soundtracks",style=MaterialTheme.typography.titleLarge)
            Text("Lilyly sends Spotify authentication requests and the music item you ask it to look up. Your journal, health data and personal captions are not sent. Access tokens stay encrypted on this device and are excluded from Lilyly exports.")
            Text("Music opens in Spotify. Lilyly does not download or stream audio. Disconnecting clears this device’s connection; access can also be revoked in your Spotify account settings.")
        }
    }
}

@Composable
fun MusicAttachmentsPanel(raw:String,onChanged:(String)->Unit) {
    val context=LocalContext.current
    val session=remember {SpotifySession(context)}
    val scope=rememberCoroutineScope()
    val currentRaw by rememberUpdatedState(raw)
    val change by rememberUpdatedState(onChanged)
    var adding by rememberSaveable {mutableStateOf(false)}
    var url by rememberSaveable {mutableStateOf("")}
    var caption by rememberSaveable {mutableStateOf("")}
    var message by remember {mutableStateOf("")}
    var busy by remember {mutableStateOf(false)}
    val attachments=musicAttachments(raw)
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Music for this page",style=MaterialTheme.typography.titleLarge)
        attachments.forEach {item ->
            Surface(color=MaterialTheme.colorScheme.secondaryContainer,shape=MaterialTheme.shapes.large) {
                Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text("♫  SPOTIFY · ${if(spotifyLink(item.url)?.kind=="track") "SONG" else "PLAYLIST"}",style=MaterialTheme.typography.labelMedium)
                    Text(item.title.ifBlank {item.label.ifBlank {"Your ${if(spotifyLink(item.url)?.kind=="track") "song" else "playlist"}"}},style=MaterialTheme.typography.titleLarge)
                    if(item.creator.isNotBlank()) Text(item.creator)
                    if(item.label.isNotBlank() && item.title.isNotBlank()) Text(item.label,style=MaterialTheme.typography.bodyMedium)
                    Row {
                        TextButton(onClick={try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(item.url)))}catch(e:Exception){message="No app is available to open this link."}}){Text("Open in Spotify")}
                        TextButton(onClick={onChanged(musicJson(attachments.filterNot {it.url==item.url}))},enabled=!busy){Text("Remove")}
                    }
                    if(session.linked) TextButton(enabled=!busy,onClick={
                        busy=true;message=""
                        scope.launch {
                            try {
                                val result=session.resolve(requireNotNull(spotifyLink(item.url)))
                                change(musicJson(musicAttachments(currentRaw).map {if(it.url==item.url)result.copy(label=it.label) else it}))
                            } catch(e:CancellationException){throw e}
                            catch(e:Exception){message="Could not fetch music details. Your link is safe. Check your connection or reconnect Spotify in Settings."}
                            finally {busy=false}
                        }
                    }) {Text("Fetch details from Spotify")}
                }
            }
        }
        if(attachments.isEmpty()) Text("A song for a memory, a playlist for a ritual. Add a link and a personal caption.")
        OutlinedButton(onClick={adding=true;message=""},enabled=attachments.size<12 && !busy){Text("Attach Spotify music")}
        if(message.isNotBlank()) Text(message)
        if(busy) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
    if(adding) AlertDialog(onDismissRequest={adding=false},title={Text("A little soundtrack")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value=url,onValueChange={url=it},label={Text("Spotify song or playlist link")},singleLine=true)
        OutlinedTextField(value=caption,onValueChange={caption=it},label={Text("Personal caption (optional)")})
        Text("Use Share → Copy link in Spotify. Save your page to keep the attachment.")
        if(message.isNotBlank()) Text(message)
    }},confirmButton={TextButton(onClick={
        val link=spotifyLink(url)
        if(link==null) message="Paste a full open.spotify.com song or playlist link."
        else if(attachments.any {it.url==link.url}) message="That music is already on this page."
        else {onChanged(musicJson(attachments+MusicAttachment(link.url,label=caption.trim())));url="";caption="";adding=false;message=""}
    }){Text("Add to page")}},dismissButton={TextButton(onClick={adding=false}){Text("Cancel")}})
}
