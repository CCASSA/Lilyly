package com.lilyly.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/** Search across saved writing and its source metadata without changing the original records. */
fun readingPassages(notes:List<BookNote>,books:List<LibraryBook>,query:String,kind:String):List<BookNote> {
    val sources=books.associateBy {it.id}
    val needle=query.trim()
    return notes.filter {note ->
        val book=sources[note.bookId]
        (kind!="Passages" || note.quote.isNotBlank()) &&
        (kind!="Notes" || note.note.isNotBlank()) &&
        listOf(note.quote,note.note,book?.title.orEmpty(),book?.author.orEmpty(),book?.collection.orEmpty()).any {it.contains(needle,true)}
    }.sortedWith(compareByDescending<BookNote> {it.date}.thenBy {it.id})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingPassagesScreen(store:AppStore,onBack:()->Unit,onBook:(BookNote)->Unit,onJournal:(JournalEntry)->Unit) {
    BackHandler(onBack=onBack)
    var query by rememberSaveable {mutableStateOf("")}
    var kind by rememberSaveable {mutableStateOf("Everything")}
    var editing by rememberSaveable {mutableStateOf<String?>(null)}
    var deleting by rememberSaveable {mutableStateOf<String?>(null)}
    val rows=readingPassages(store.bookNotes,store.books,query,kind)
    Scaffold(topBar={TopAppBar(title={Text("Words to return to")},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back to shelves")}})}) {padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("reading-passages"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
            item {
                Text("A little library of remembered words",style=MaterialTheme.typography.headlineMedium)
                Text("Passages and thoughts gathered from your books. Follow a thread back to its page, or keep it in your journal.")
                OutlinedTextField(query,{query=it},label={Text("Search words, books or shelves")},modifier=Modifier.fillMaxWidth(),singleLine=true)
                LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {items(listOf("Everything","Passages","Notes")) {value ->FilterChip(kind==value,{kind=value},label={Text(value)})}}
                Text("${rows.size} saved pieces",style=MaterialTheme.typography.labelMedium)
            }
            if(rows.isEmpty()) item {Text(if(store.bookNotes.isEmpty()) "Your remembered words will gather here. Open a book and keep a passage or write a reading note to begin." else "No saved pieces match. Try another word or choose Everything.")}
            items(rows,key={it.id}) {note ->
                val book=store.books.firstOrNull {it.id==note.bookId}
                Column(Modifier.testTag("passage-${note.id}"),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    BotanicalDivider()
                    Text(book?.title ?: "Source book unavailable",style=MaterialTheme.typography.titleLarge)
                    Text("${book?.author?.takeIf {it.isNotBlank()}?.plus(" · ") ?: ""}${if(book?.format=="PDF") "Page" else "Section"} ${note.position+1} · ${note.date.take(10)}",style=MaterialTheme.typography.labelSmall)
                    if(note.quote.isNotBlank()) Text("“${note.quote}”",fontFamily=FontFamily.Serif,style=MaterialTheme.typography.bodyLarge)
                    if(note.note.isNotBlank()) Text(note.note)
                    Row {
                        TextButton(onClick={onBook(note)},enabled=book!=null) {Text("Return to book")}
                        TextButton(onClick={onJournal(store.journalBookNote(note))},enabled=book!=null || store.journalEntries.any {it.id==note.journalId}) {Text(if(store.journalEntries.any {it.id==note.journalId}) "Open journal" else "Send to journal")}
                    }
                    Row {
                        TextButton(onClick={editing=note.id}) {Text("Edit saved words")}
                        TextButton(onClick={deleting=note.id}) {Text("Remove",color=MaterialTheme.colorScheme.error)}
                    }
                }
            }
        }
    }
    store.bookNotes.firstOrNull {it.id==editing}?.let {note ->
        key(note.id) { PassageEditor(note,onDismiss={editing=null}) {quote,body ->
            store.bookNotes.firstOrNull {it.id==note.id}?.let {store.saveBookNote(it.copy(quote=quote,note=body))}
            editing=null
        } }
    }
    deleting?.let {id ->AlertDialog(onDismissRequest={deleting=null},title={Text("Remove these saved words?")},text={Text("This removes this passage and its reading note from Lilyly. The book, bookmarks and any journal copy remain.")},confirmButton={TextButton(onClick={store.removeBookNote(id);deleting=null}) {Text("Remove saved words")}},dismissButton={TextButton(onClick={deleting=null}) {Text("Keep them")}})}
}

@Composable
private fun PassageEditor(note:BookNote,onDismiss:()->Unit,onSave:(String,String)->Unit) {
    var quote by rememberSaveable {mutableStateOf(note.quote)}
    var body by rememberSaveable {mutableStateOf(note.note)}
    var discard by rememberSaveable {mutableStateOf(false)}
    val close={if(quote!=note.quote || body!=note.note) discard=true else onDismiss()}
    AlertDialog(onDismissRequest=close,title={Text("Keep the words that matter")},text={Column(Modifier.heightIn(max=400.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(quote,{quote=it},label={Text("Saved passage")},modifier=Modifier.fillMaxWidth(),maxLines=6)
        OutlinedTextField(body,{body=it},label={Text("Reading thoughts")},modifier=Modifier.fillMaxWidth(),maxLines=6)
        Text("Your original book stays as it is. Any existing journal copy keeps its own wording.",style=MaterialTheme.typography.bodySmall)
    }},confirmButton={TextButton(onClick={onSave(quote,body)},enabled=quote.isNotBlank() || body.isNotBlank()) {Text("Keep changes")}},dismissButton={TextButton(onClick=close) {Text("Cancel")}})
    if(discard) AlertDialog(onDismissRequest={discard=false},title={Text("Discard these edits?")},confirmButton={TextButton(onClick=onDismiss) {Text("Discard edits")}},dismissButton={TextButton(onClick={discard=false}) {Text("Keep writing")}})
}
