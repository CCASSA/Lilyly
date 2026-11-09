package com.lilyly.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedInspirationScreen(store:AppStore,incoming:SharedInspiration?,problem:String?,onClose:()->Unit,onKept:()->Unit) {
    val context=LocalContext.current
    val job=remember {androidx.lifecycle.ViewModelProvider(context as androidx.activity.ComponentActivity)[SharedInspirationViewModel::class.java]}
    val batchId=rememberSaveable {UUID.randomUUID().toString()}
    var title by rememberSaveable {mutableStateOf(incoming?.title.orEmpty())}
    var body by rememberSaveable {mutableStateOf(incoming?.text.orEmpty())}
    var source by rememberSaveable {mutableStateOf(sharedInspirationSource(incoming?.text.orEmpty()))}
    var credit by rememberSaveable {mutableStateOf("")}
    var collection by rememberSaveable {mutableStateOf("")}
    val busy=job.busy
    var discard by remember {mutableStateOf(false)}
    var message by remember {mutableStateOf("")}
    LaunchedEffect(job.kept) {if(job.kept)onKept()}
    val close={if(!busy){if(incoming!=null)discard=true else onClose()}}
    BackHandler(onBack=close)
    val keyboard=LocalSoftwareKeyboardController.current
    val focus=LocalFocusManager.current
    Scaffold(modifier=Modifier.imePadding(),topBar={TopAppBar(title={Text("A spark from elsewhere")},navigationIcon={TextButton(onClick=close,enabled=!busy){Text("Close")}})},bottomBar={
        if(incoming!=null) Surface {Column {
        if((message+job.message).isNotBlank()) Text(message.ifBlank {job.message},color=MaterialTheme.colorScheme.error,modifier=Modifier.padding(horizontal=20.dp))
        Button(enabled=!busy,onClick={
            keyboard?.hide();focus.clearFocus()
            val valid=runCatching {sharedInspirationEntries(batchId,title,body,source,credit,collection,incoming.images)}
            if(valid.isFailure)message=valid.exceptionOrNull()?.message ?: "Check the details above."
            else {
                message=""
                job.keep(store,batchId,title,body,source,credit,collection,incoming.images)
            }
        },modifier=Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)){Text(if(busy) "Keeping your inspiration…" else "Keep inspiration")}}}
    }) {padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=20.dp).testTag("shared-inspiration"),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(bottom=24.dp)) {
            if(incoming==null) item {Text(problem ?: "This share could not be opened.");Text("Nothing was added to your collection.")}
            else {
                item {Text("Bring it into your world",style=MaterialTheme.typography.headlineMedium);Text("Review before keeping. Images are copied locally; web links are not fetched. Keep the creator's credit when you know it.")}
                items(incoming.images) {uri ->AsyncImage(uri,"Shared image preview",Modifier.fillMaxWidth().height(180.dp),contentScale=ContentScale.Fit)}
                if(incoming.images.size>1)item {Text("${incoming.images.size} images · one card for each, with the details below")}
                item {OutlinedTextField(title,{title=it},label={Text("Inspiration title")},enabled=!busy,modifier=Modifier.fillMaxWidth())}
                item {OutlinedTextField(body,{body=it},label={Text("Shared words or a thought")},enabled=!busy,modifier=Modifier.fillMaxWidth())}
                item {OutlinedTextField(source,{source=it},label={Text("Source link (HTTPS, optional)")},enabled=!busy,modifier=Modifier.fillMaxWidth())}
                item {OutlinedTextField(credit,{credit=it},label={Text("Creator or source credit")},enabled=!busy,modifier=Modifier.fillMaxWidth().testTag("share-credit"))}
                item {OutlinedTextField(collection,{collection=it},label={Text("Collection")},enabled=!busy,modifier=Modifier.fillMaxWidth().testTag("share-collection"),keyboardOptions=KeyboardOptions(imeAction=ImeAction.Done),keyboardActions=KeyboardActions(onDone={keyboard?.hide();focus.clearFocus()}))}
                item {Text("Up to 8 images, 25 MB each. Titles and collection names: 160 characters; words: 20,000; credit: 1,000.",style=MaterialTheme.typography.bodySmall)}
            }
            if(busy)item {LinearProgressIndicator(Modifier.fillMaxWidth())}
        }
    }
    if(discard) AlertDialog(onDismissRequest={discard=false},title={Text("Leave this inspiration?")},text={Text("Nothing will be added to Lilyly. The original stays in the app you shared from.")},confirmButton={TextButton(onClick=onClose){Text("Leave share")}},dismissButton={TextButton(onClick={discard=false}){Text("Keep reviewing")}})
}
