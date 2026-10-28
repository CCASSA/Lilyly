package com.lilyly.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingHistoryScreen(store:AppStore,onBack:()->Unit,onBook:(String,Int)->Unit) {
    BackHandler(onBack=onBack)
    var removing by remember {mutableStateOf<Pair<String,String>?>(null)}
    val rows=store.books.flatMap {book ->book.sessions.map {book to it}}.sortedByDescending {OffsetDateTime.parse(it.second.startedAt).toInstant()}
    val earlier=store.books.sumOf {(it.readingSeconds-it.sessions.sumOf {row->row.seconds}).coerceAtLeast(0)}
    Scaffold(topBar={TopAppBar(title={Text("Hours among the pages")},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back to shelves")}})}) {padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("reading-history"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
            item {
                Text(readingDuration(store.books.sumOf {it.readingSeconds}),style=MaterialTheme.typography.displaySmall)
                Text("Time among your saved books",style=MaterialTheme.typography.titleMedium)
                Text("Sessions record time with the reader open in the foreground, including its tools. They pause when you leave or put Lilyly in the background; they do not measure attention or reading speed.",style=MaterialTheme.typography.bodySmall)
                if(earlier>0) Text("${readingDuration(earlier)} from earlier versions has no dated session history.",style=MaterialTheme.typography.bodySmall)
            }
            if(rows.isEmpty()) item {Text("Your next chapter starts here",style=MaterialTheme.typography.headlineSmall);Text("Open a book and spend a little time with it. Your session will appear after you leave the reader.")}
            items(rows,key={"${it.first.id}-${it.second.id}"}) {(book,session) ->
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    BotanicalDivider()
                    Text(book.title,style=MaterialTheme.typography.headlineSmall)
                    val started=OffsetDateTime.parse(session.startedAt)
                    Text(started.format(DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm XXX")),style=MaterialTheme.typography.labelLarge)
                    Text(readingDuration(session.seconds),style=MaterialTheme.typography.titleLarge)
                    Text("${if(book.format=="PDF") "Pages" else "Sections"} ${session.firstPosition+1} → ${session.lastPosition+1}")
                    TextButton(onClick={onBook(book.id,session.lastPosition)}) {Text("Return to this place")}
                    TextButton(onClick={removing=book.id to session.id}) {Text("Remove session") }
                }
            }
        }
    }
    removing?.let {target ->
        AlertDialog(onDismissRequest={removing=null},title={Text("Remove this session?")},
            text={Text("This removes the session and its time from your reading total. Your book, saved place, bookmarks, notes and journal pages stay. This cannot be undone.")},
            confirmButton={TextButton(onClick={
                store.books.firstOrNull {it.id==target.first}?.let {store.saveBook(removeReadingSession(it,target.second))}
                removing=null
            }) {Text("Remove recorded time")}},
            dismissButton={TextButton(onClick={removing=null}) {Text("Keep session")}})
    }
}
