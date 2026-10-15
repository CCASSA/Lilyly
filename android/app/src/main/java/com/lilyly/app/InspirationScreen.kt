package com.lilyly.app

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspirationScreen(store:AppStore,onBack:()->Unit) {
    var query by rememberSaveable {mutableStateOf("")}
    var collection by rememberSaveable {mutableStateOf("")}
    var favorites by rememberSaveable {mutableStateOf(false)}
    var editing by rememberSaveable {mutableStateOf<String?>(null)}
    var creating by rememberSaveable {mutableStateOf(false)}
    BackHandler(onBack=onBack)
    val cards=store.journalEntries.filter {it.section=="Inspiration"}
    Scaffold(topBar={TopAppBar(title={Text("Gathered inspiration")},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back")}})},floatingActionButton={ExtendedFloatingActionButton(onClick={creating=true;editing=null}) {Text("Add inspiration")}}) {padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=20.dp)) {
            Text("A cabinet of little wonders",style=MaterialTheme.typography.headlineMedium)
            Text("Keep an image, a source, a spark. Bring it into a page when the moment feels right.",style=MaterialTheme.typography.bodyMedium)
            OutlinedTextField(query,{query=it},label={Text("Find inspiration")},modifier=Modifier.fillMaxWidth())
            LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                item {FilterChip(favorites,{favorites=!favorites},label={Text("Favorites")})}
                item {FilterChip(collection.isEmpty(),{collection=""},label={Text("All collections")})}
                items(cards.map {it.notebook}.filter {it.isNotBlank()}.distinct().sorted()) {name->FilterChip(collection==name,{collection=name},label={Text(name)})}
            }
            val visible=cards.filter {matchesInspiration(it,query,collection,favorites)}
            if(visible.isEmpty()) Text(if(cards.isEmpty()) "Your cabinet is waiting. Add an image from your device or keep a source link." else "No cards match these filters.",Modifier.padding(vertical=24.dp))
            LazyVerticalGrid(GridCells.Fixed(2),Modifier.weight(1f).testTag("inspiration-grid"),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(bottom=110.dp)) {
                items(visible,key={it.id}) {card->InspirationTile(card){editing=card.id;creating=false}}
            }
        }
    }
    if(creating || editing!=null) InspirationEditor(store,store.journalEntries.firstOrNull {it.id==editing},onClose={creating=false;editing=null})
}

@Composable
private fun InspirationTile(card:JournalEntry,onChoose:()->Unit) {
    Surface(shape=MaterialTheme.shapes.large,color=MaterialTheme.colorScheme.surfaceVariant,modifier=Modifier.fillMaxWidth().clickable(onClick=onChoose)) {
        Column {
            if(card.imageUri.isNotBlank()) AsyncImage(card.imageUri,card.title,Modifier.fillMaxWidth().height(160.dp),contentScale=ContentScale.Crop)
            else Text("❦",style=MaterialTheme.typography.displayMedium,color=MaterialTheme.colorScheme.primary,modifier=Modifier.padding(20.dp))
            Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Text(card.title.ifBlank {"A little inspiration"},style=MaterialTheme.typography.titleMedium)
                if(card.body.isNotBlank()) Text(card.body,maxLines=3,style=MaterialTheme.typography.bodySmall)
                if(card.sourceCredit.isNotBlank()) Text(card.sourceCredit,maxLines=2,style=MaterialTheme.typography.labelSmall)
                if(card.notebook.isNotBlank()) Text(card.notebook,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspirationPicker(store:AppStore,onChoose:(JournalEntry)->Unit,onClose:()->Unit) {
    var query by rememberSaveable {mutableStateOf("")}
    ModalBottomSheet(onDismissRequest=onClose,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("Bring a little wonder into this page",style=MaterialTheme.typography.headlineSmall)
            OutlinedTextField(query,{query=it},label={Text("Find inspiration")},modifier=Modifier.fillMaxWidth())
            val cards=store.journalEntries.filter {matchesInspiration(it,query,"",false)}
            if(cards.isEmpty()) Text("No matching inspiration. Keep a card in More → Inspiration, then choose it here.",Modifier.padding(vertical=20.dp))
            LazyVerticalGrid(GridCells.Fixed(2),Modifier.heightIn(max=430.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)) {
                items(cards,key={it.id}) {card -> InspirationTile(card){onChoose(card)}}
            }
            TextButton(onClick=onClose){Text("Close")}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InspirationEditor(store:AppStore,original:JournalEntry?,onClose:()->Unit) {
    val context=LocalContext.current
    val files=remember {InspirationFiles(context)}
    val scope=rememberCoroutineScope()
    var json by rememberSaveable(original?.id) {mutableStateOf((original ?: JournalEntry(section="Inspiration",title="")).toJson().toString())}
    val card=JournalEntry.fromJson(JSONObject(json))
    val initial=rememberSaveable(original?.id) {json}
    var busy by remember {mutableStateOf(false)}
    var message by remember {mutableStateOf("")}
    var discard by remember {mutableStateOf(false)}
    var deleting by remember {mutableStateOf(false)}
    fun change(value:JournalEntry){json=value.toJson().toString()}
    fun cancel(){files.removeIfUnused(card.imageUri,store);onClose()}
    val close={if(!busy){if(json!=initial)discard=true else cancel()}}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {uri ->if(uri!=null)scope.launch {
        busy=true;message=""
        try {val image=withContext(Dispatchers.IO){files.importImage(uri)};files.removeIfUnused(card.imageUri,store);change(card.copy(imageUri=image))}
        catch(e:CancellationException){throw e}
        catch(e:Exception){message=e.message ?: "This image could not be imported."}
        finally {busy=false}
    }}
    val editorSheet=rememberModalBottomSheetState(skipPartiallyExpanded=true,confirmValueChange={next ->
        if(next!=SheetValue.Hidden) true
        else if(busy) false
        else if(json!=initial){discard=true;false}
        else true
    })
    ModalBottomSheet(onDismissRequest=close,sheetState=editorSheet) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp).testTag("inspiration-editor"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("A spark to keep",style=MaterialTheme.typography.headlineMedium)
            if(card.imageUri.isNotBlank()) AsyncImage(card.imageUri,"Inspiration preview",Modifier.fillMaxWidth().height(180.dp),contentScale=ContentScale.Fit)
            Button(onClick={picker.launch(arrayOf("image/*"))},enabled=!busy){Text(if(card.imageUri.isBlank()) "Choose image" else "Replace image")}
            if(busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Use an image you own or have permission to keep. Imported images stay on this device; source links are not fetched automatically.",style=MaterialTheme.typography.bodySmall)
            OutlinedTextField(card.title,{change(card.copy(title=it))},label={Text("Inspiration title")},enabled=!busy,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(card.body,{change(card.copy(body=it))},label={Text("What drew you to it?")},enabled=!busy,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(card.sourceUrl,{change(card.copy(sourceUrl=it))},label={Text("Source link (HTTPS, optional)")},enabled=!busy,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(card.sourceCredit,{change(card.copy(sourceCredit=it))},label={Text("Creator or source credit")},enabled=!busy,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(card.notebook,{change(card.copy(notebook=it))},label={Text("Collection")},enabled=!busy,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(card.tags,{change(card.copy(tags=it))},label={Text("Tags")},enabled=!busy,modifier=Modifier.fillMaxWidth())
            FilterChip(card.favorite,{change(card.copy(favorite=!card.favorite))},enabled=!busy,label={Text("Favorite inspiration")})
            inspirationUrl(card.sourceUrl)?.let {url->TextButton(onClick={try{context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(e:Exception){message="No app can open this source."}},enabled=!busy){Text("Open source")}}
            if(message.isNotBlank()) Text(message,color=MaterialTheme.colorScheme.error)
            Button(enabled=!busy,onClick={
                val source=if(card.sourceUrl.isBlank()) "" else inspirationUrl(card.sourceUrl)
                when {
                    source==null -> message="Use a full HTTPS source link without a username or password."
                    card.imageUri.isBlank() && source.isBlank() && card.body.isBlank() -> message="Add an image, source link or a thought to keep."
                    else -> {store.upsertJournal(card.copy(title=card.title.trim().ifBlank {"A little inspiration"},sourceUrl=source,notebook=card.notebook.trim()));original?.let {files.removeIfUnused(it.imageUri,store)};onClose()}
                }
            },modifier=Modifier.fillMaxWidth()){Text("Keep inspiration")}
            TextButton(onClick=close,enabled=!busy){Text("Close")}
            if(original!=null) TextButton(onClick={deleting=true},enabled=!busy){Text("Remove inspiration")}
        }
    }
    if(discard) AlertDialog(onDismissRequest={discard=false},title={Text("Discard unsaved inspiration?")},confirmButton={TextButton(onClick={cancel()}){Text("Discard")}},dismissButton={TextButton(onClick={discard=false}){Text("Keep editing")}})
    if(deleting) AlertDialog(onDismissRequest={deleting=false},title={Text("Remove this inspiration card?")},text={Text("Copies already saved on scrapbook pages will remain.")},confirmButton={TextButton(onClick={store.deleteJournal(card.id);files.removeIfUnused(card.imageUri,store);original?.let {files.removeIfUnused(it.imageUri,store)};onClose()}){Text("Remove card")}},dismissButton={TextButton(onClick={deleting=false}){Text("Keep it")}})
}
