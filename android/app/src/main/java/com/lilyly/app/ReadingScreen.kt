package com.lilyly.app

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

private data class NookPalette(val wall: Color, val wood: Color, val ink: Color, val accent: Color)
private fun nookPalette(theme: String) = when(theme) {
    "Gothic" -> NookPalette(Color(0xFF201C26),Color(0xFF3B2633),Color(0xFFF1E6DB),Color(0xFFBA8E99))
    "Celestial" -> NookPalette(Color(0xFF141D36),Color(0xFF29324D),Color(0xFFE7E9F4),Color(0xFFC6B781))
    "Botanical" -> NookPalette(Color(0xFF1D3029),Color(0xFF385047),Color(0xFFE7EBDC),Color(0xFFB1C394))
    else -> NookPalette(Color(0xFF342A24),Color(0xFF76543B),Color(0xFFF6E9D3),Color(0xFFD2B178))
}

@Composable
private fun NookAtmosphere(theme: String, modifier: Modifier = Modifier) {
    val p = nookPalette(theme)
    Canvas(modifier.fillMaxWidth().height(115.dp)) {
        drawRect(Brush.verticalGradient(listOf(p.wall,p.wood.copy(alpha=.4f))))
        val mid = size.width/2
        drawArc(p.accent.copy(alpha=.45f),180f,180f,false,Offset(mid-65.dp.toPx(),18.dp.toPx()),androidx.compose.ui.geometry.Size(130.dp.toPx(),150.dp.toPx()),style=Stroke(1.dp.toPx()))
        if(theme == "Celestial") {
            repeat(24) { i -> drawCircle(p.accent.copy(alpha=.4f+(i%3)*.2f), if(i%4==0) 2.dp.toPx() else 1.dp.toPx(),Offset(size.width*((i*37%97)/100f),size.height*((i*17%83)/100f))) }
            drawCircle(p.ink,18.dp.toPx(),Offset(mid,55.dp.toPx())); drawCircle(p.wall,16.dp.toPx(),Offset(mid+9.dp.toPx(),50.dp.toPx()))
        } else if(theme == "Botanical") {
            listOf(.12f,.85f).forEach { x ->
                drawLine(p.accent,Offset(size.width*x,0f),Offset(size.width*x-20.dp.toPx(),size.height),2.dp.toPx())
                repeat(6) { i -> drawOval(p.accent.copy(alpha=.6f),Offset(size.width*x-(i%2)*25.dp.toPx(),i*18.dp.toPx()),androidx.compose.ui.geometry.Size(26.dp.toPx(),10.dp.toPx())) }
            }
        } else {
            val x = if(theme == "Gothic") size.width*.2f else size.width*.8f
            drawRect(p.accent,Offset(x,55.dp.toPx()),androidx.compose.ui.geometry.Size(13.dp.toPx(),40.dp.toPx()))
            drawCircle(Brush.radialGradient(listOf(Color(0xFFFFDC8F).copy(alpha=.5f),Color.Transparent),Offset(x+6.dp.toPx(),45.dp.toPx()),35.dp.toPx()),35.dp.toPx(),Offset(x+6.dp.toPx(),45.dp.toPx()))
            drawOval(Color(0xFFFFDC8F),Offset(x+2.dp.toPx(),35.dp.toPx()),androidx.compose.ui.geometry.Size(8.dp.toPx(),18.dp.toPx()))
            if(theme=="Gothic") repeat(5) { i -> drawLine(p.accent.copy(alpha=.2f),Offset(size.width*i/4,0f),Offset(size.width*i/4,size.height),2.dp.toPx()) }
        }
        drawRect(p.wood,Offset(0f,size.height-8.dp.toPx()),androidx.compose.ui.geometry.Size(size.width,8.dp.toPx()))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(store: AppStore, onBack: () -> Unit, onJournal: (JournalEntry) -> Unit, initialBookId: String? = null) {
    val context = LocalContext.current
    val files = remember { BookFiles(context.applicationContext) }
    val scope = rememberCoroutineScope()
    var selected by rememberSaveable(initialBookId) { mutableStateOf(initialBookId) }
    var query by rememberSaveable { mutableStateOf("") }
    var onlyFavorites by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri != null) scope.launch {
            busy = true
            try { val book = withContext(Dispatchers.IO) { files.importBook(uri) }; store.saveBook(book) }
            catch(e: Exception) { error = e.message ?: "This book could not be imported" }
            finally { busy = false }
        }
    }
    val book = store.books.firstOrNull { it.id == selected }
    if(book != null) {
        BookReaderScreen(store,book,files,onBack={selected=null},onJournal=onJournal)
        return
    }
    BackHandler { if(!busy) onBack() }
    val palette = nookPalette(store.nookTheme)
    Scaffold(topBar = { TopAppBar(title = { Text("Reading nook") }, navigationIcon = { IconButton(onClick = onBack, enabled = !busy) { Icon(Icons.Default.ArrowBack,"Back") } }) }, bottomBar = {
        Surface(Modifier.navigationBarsPadding()) { Button(onClick = { importer.launch(arrayOf("application/pdf","application/epub+zip")) }, enabled = !busy, modifier = Modifier.fillMaxWidth().padding(14.dp)) { Text(if(busy) "Bringing your book inside…" else "Import EPUB or PDF") } }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).background(palette.wall).testTag("bookshelf"), contentPadding=PaddingValues(bottom=24.dp)) {
            item { NookAtmosphere(store.nookTheme); Text("Stay for one more chapter", style=MaterialTheme.typography.headlineMedium,color=palette.ink,modifier=Modifier.padding(18.dp)) }
            item {
                LazyRow(contentPadding=PaddingValues(horizontal=18.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) { items(listOf("Cottage","Gothic","Celestial","Botanical")) { theme -> FilterChip(store.nookTheme==theme,{store.updateNookTheme(theme)},label={Text(theme,color=palette.ink)}) } }
                OutlinedTextField(query,{query=it},label={Text("Find a book")},modifier=Modifier.fillMaxWidth().padding(horizontal=18.dp),colors=OutlinedTextFieldDefaults.colors(unfocusedTextColor=palette.ink,focusedTextColor=palette.ink,unfocusedLabelColor=palette.ink))
                FilterChip(onlyFavorites,{onlyFavorites=!onlyFavorites},label={Text("My favorites",color=palette.ink)},modifier=Modifier.padding(start=18.dp))
                if(busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            val visible = store.books.filter { (it.title+it.author).contains(query,true) && (!onlyFavorites || it.favorite) }.sortedByDescending { it.lastReadAt }
            if(visible.isEmpty()) item { Text(if(store.books.isEmpty()) "Your shelves are waiting for the stories you love. Import a book you own to begin." else "No books match this search.",color=palette.ink,modifier=Modifier.padding(24.dp)) }
            items(visible.chunked(3)) { row ->
                Column(Modifier.padding(horizontal=14.dp,vertical=10.dp)) {
                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.Bottom) {
                        row.forEach { b -> Column(Modifier.weight(1f).testTag("book-${b.id}").clickable { selected=b.id }) {
                            Box(Modifier.fillMaxWidth().aspectRatio(.65f).clip(RoundedCornerShape(topEnd=8.dp,bottomEnd=8.dp)).background(palette.wood).border(1.dp,palette.accent.copy(alpha=.5f)),contentAlignment=Alignment.Center) {
                                if(files.cover(b.id).exists()) AsyncImage(files.cover(b.id),b.title,modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                                else Column(Modifier.padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally) { Text("❦",color=palette.accent,style=MaterialTheme.typography.headlineLarge); Text(b.title,color=palette.ink,textAlign=TextAlign.Center,maxLines=4,fontFamily=FontFamily.Serif) }
                                Box(Modifier.align(Alignment.CenterStart).fillMaxHeight().width(6.dp).background(Color.Black.copy(alpha=.2f)))
                            }
                            Text(b.title,color=palette.ink,maxLines=2,style=MaterialTheme.typography.labelMedium,modifier=Modifier.padding(top=7.dp))
                            Text(if(b.completed) "Finished ♡" else if(b.lastReadAt.isBlank()) "${b.format} · Unopened" else "${b.position+1} / ${b.units}",color=palette.accent,style=MaterialTheme.typography.labelSmall)
                        } }
                        repeat(3-row.size) { Spacer(Modifier.weight(1f)) }
                    }
                    Box(Modifier.fillMaxWidth().padding(top=6.dp).height(10.dp).background(Brush.verticalGradient(listOf(palette.wood,palette.wood.copy(alpha=.4f)))))
                }
            }
            item { Text("Books stay on this device. EPUBs use a text reading view; PDF pages keep their original layout. No account or broad storage permission is needed.",color=palette.ink.copy(alpha=.7f),style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(18.dp)) }
        }
    }
    error?.let { message -> AlertDialog(onDismissRequest={error=null},title={Text("Couldn't open this book")},text={Text(message)},confirmButton={TextButton(onClick={error=null}) { Text("Close") }}) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookReaderScreen(store: AppStore, initial: LibraryBook, files: BookFiles, onBack: () -> Unit, onJournal: (JournalEntry) -> Unit) {
    val book = store.books.firstOrNull { it.id == initial.id } ?: return
    var position by rememberSaveable(book.id) { mutableIntStateOf(book.position) }
    var font by rememberSaveable { mutableIntStateOf(19) }
    var text by remember(book.id) { mutableStateOf<List<ReadingUnit>>(emptyList()) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var panel by rememberSaveable { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    var noteQuote by rememberSaveable { mutableStateOf("") }
    var noteBody by rememberSaveable { mutableStateOf("") }
    var noteOpen by rememberSaveable { mutableStateOf(false) }
    var discardNote by remember { mutableStateOf(false) }
    var remove by remember { mutableStateOf(false) }
    val list = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    val savePosition = { p: Int ->
        position=p.coerceIn(0,book.units-1)
        val current=store.books.first { it.id==book.id }
        store.saveBook(current.copy(position=position,lastReadAt=LocalDateTime.now().toString()))
    }
    DisposableEffect(book.id, owner) {
        var started = if(owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) System.nanoTime() else 0L
        fun pause() {
            if(started!=0L) {
                val seconds=(System.nanoTime()-started)/1_000_000_000
                store.books.firstOrNull { it.id==book.id }?.let { if(seconds>0) store.saveBook(it.copy(readingSeconds=it.readingSeconds+seconds)) }
                started=0
            }
        }
        val observer=LifecycleEventObserver { _,event -> if(event==Lifecycle.Event.ON_RESUME && started==0L) started=System.nanoTime() else if(event==Lifecycle.Event.ON_PAUSE) pause() }
        owner.lifecycle.addObserver(observer)
        onDispose { pause(); owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(book.id) {
        savePosition(position)
        if(book.format=="EPUB") try { text=withContext(Dispatchers.IO) { files.text(book.id) }; loading=false } catch(e: Exception) { error="The local book text could not be opened. Your saved notes remain available."; loading=false }
    }
    LaunchedEffect(book.id,position) {
        list.scrollToItem(0)
        if(book.format=="PDF") {
            loading=true; bitmap=null; error=null
            try { bitmap=withContext(Dispatchers.IO) { files.pdf(book.id,position) } } catch(e: Exception) { error="This PDF page could not be rendered." }
            loading=false
        }
    }
    BackHandler(onBack=onBack)
    Scaffold(topBar={ TopAppBar(title={Text(book.title,maxLines=1)},navigationIcon={IconButton(onClick=onBack) { Icon(Icons.Default.ArrowBack,"Back to shelves") }},actions={TextButton(onClick={panel="Tools"}) { Text("Tools") }}) },bottomBar={
        Surface(Modifier.navigationBarsPadding()) { Row(Modifier.fillMaxWidth().padding(8.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
            TextButton(onClick={savePosition(position-1)},enabled=position>0) { Text("Previous") }
            Text("${position+1} / ${book.units}",style=MaterialTheme.typography.labelLarge)
            TextButton(onClick={if(position<book.units-1) savePosition(position+1) else store.saveBook(book.copy(completed=true))},enabled=!book.completed || position<book.units-1) { Text(if(position==book.units-1) "Finish book" else "Next") }
        } }
    }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("book-reader"),state=list,contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
            if(loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            error?.let { item { Text(it) } }
            if(book.format=="PDF") {
                bitmap?.let { bmp -> item {
                    var scale by remember(position) { mutableFloatStateOf(1f) }
                    var offset by remember(position) { mutableStateOf(Offset.Zero) }
                    val transform = rememberTransformableState { zoom,pan,_ -> scale=(scale*zoom).coerceIn(1f,3f); offset=if(scale==1f) Offset.Zero else offset+pan }
                    Box(Modifier.fillMaxWidth().aspectRatio(bmp.width.toFloat()/bmp.height).clip(RoundedCornerShape(4.dp)).transformable(transform)) {
                        Image(bmp.asImageBitmap(),"Page ${position+1}",modifier=Modifier.fillMaxWidth().graphicsLayer { scaleX=scale;scaleY=scale;translationX=offset.x;translationY=offset.y })
                    }
                    Text("Pinch to zoom. Use Tools to bookmark or add a note.",style=MaterialTheme.typography.bodySmall)
                } }
            } else text.getOrNull(position)?.let { unit ->
                item { Text(unit.title,style=MaterialTheme.typography.headlineSmall) }
                items(unit.text.split(Regex("\\n\\s*\\n")).filter { it.isNotBlank() }) { paragraph ->
                    Column {
                        SelectionContainer { Text(paragraph,fontSize=font.sp,lineHeight=(font*1.55).sp,fontFamily=FontFamily.Serif) }
                        TextButton(onClick={noteQuote=paragraph;noteBody="";noteOpen=true},modifier=Modifier.semantics {contentDescription="Keep passage: ${paragraph.take(80)}"}) { Text("Keep this passage",style=MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
    }
    if(panel.isNotEmpty()) ModalBottomSheet(onDismissRequest={panel=""}) {
        LazyColumn(Modifier.padding(horizontal=20.dp),contentPadding=PaddingValues(bottom=28.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item {
                Text(book.title,style=MaterialTheme.typography.headlineSmall)
                Text("${book.author} · ${book.readingSeconds/60} minutes of reading",style=MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    FilterChip(book.favorite,{store.saveBook(book.copy(favorite=!book.favorite))},label={Text("Favorite")})
                    FilterChip(position in book.bookmarks,{store.saveBook(book.copy(bookmarks=if(position in book.bookmarks) book.bookmarks-position else book.bookmarks+position))},label={Text("Bookmark")})
                }
                if(book.format=="EPUB") Row(verticalAlignment=Alignment.CenterVertically) { Text("Text size"); TextButton(onClick={font=(font-2).coerceAtLeast(13)}) {Text("A−")}; TextButton(onClick={font=(font+2).coerceAtMost(31)}) {Text("A+")} }
                OutlinedTextField(query,{query=it},label={Text(if(book.format=="EPUB") "Search book text" else "Go to page number")},modifier=Modifier.fillMaxWidth())
                if(book.format=="PDF") TextButton(onClick={query.toIntOrNull()?.takeIf { it in 1..book.units }?.let { savePosition(it-1);panel="" }},enabled=query.toIntOrNull()?.let { it in 1..book.units }==true) {Text("Go to page")}
                Button(onClick={panel="";noteQuote="";noteBody="";noteOpen=true}) {Text("Write a reading note")}
            }
            if(book.format=="EPUB" && query.isNotBlank()) {
                val hits=text.mapIndexedNotNull { i,u -> if((u.title+u.text).contains(query,true)) i to u else null }.take(60)
                if(hits.isEmpty()) item {Text("No matching passages.")}
                items(hits) { (i,u) -> TextButton(onClick={savePosition(i);panel=""}) { Text("${i+1} · ${u.title}\n${u.text.substring((u.text.indexOf(query,ignoreCase=true)-25).coerceAtLeast(0)).take(130)}",maxLines=4) } }
            }
            item {Text("Bookmarks",style=MaterialTheme.typography.titleLarge); if(book.bookmarks.isEmpty()) Text("Keep a place using Bookmark above.",style=MaterialTheme.typography.bodySmall)}
            items(book.bookmarks.sorted()) { p -> TextButton(onClick={savePosition(p);panel=""}) {Text("${if(book.format=="PDF") "Page" else "Section"} ${p+1}")} }
            item {Text("My passages & notes",style=MaterialTheme.typography.titleLarge)}
            items(store.bookNotes.filter { it.bookId==book.id }) { note ->
                Column { if(note.quote.isNotBlank()) Text("“${note.quote.take(500)}”",fontFamily=FontFamily.Serif); Text(note.note)
                    Row { TextButton(onClick={savePosition(note.position);panel=""}) {Text("Return to passage")}; TextButton(onClick={onJournal(store.journalBookNote(note))}) {Text(if(store.journalEntries.any { it.id==note.journalId }) "Open journal" else "Send to journal")} }
                }
            }
            if(book.format=="EPUB" && query.isBlank()) {
                item { Text("Contents",style=MaterialTheme.typography.titleLarge) }
                items(text.mapIndexed { i,u -> i to u.title }) { (i,title) -> TextButton(onClick={savePosition(i);panel=""}) {Text("${i+1} · $title")} }
            }
            item { TextButton(onClick={remove=true}) {Text("Remove this book",color=MaterialTheme.colorScheme.error)} }
        }
    }
    if(noteOpen) AlertDialog(onDismissRequest={if(noteBody.isNotBlank() || noteQuote.isNotBlank()) discardNote=true else noteOpen=false},title={Text("A passage to keep")},text={
        Column(Modifier.heightIn(max=380.dp).verticalScroll(rememberScrollState())) {
            OutlinedTextField(noteQuote,{noteQuote=it},label={Text("Quote / highlighted passage")},modifier=Modifier.fillMaxWidth(),maxLines=5)
            OutlinedTextField(noteBody,{noteBody=it},label={Text("Your thoughts")},modifier=Modifier.fillMaxWidth(),maxLines=5)
        }
    },confirmButton={TextButton(onClick={store.saveBookNote(BookNote(bookId=book.id,position=position,quote=noteQuote,note=noteBody));noteOpen=false;noteBody="";noteQuote=""},enabled=noteQuote.isNotBlank() || noteBody.isNotBlank()) {Text("Keep passage")}},dismissButton={TextButton(onClick={if(noteBody.isNotBlank() || noteQuote.isNotBlank()) discardNote=true else noteOpen=false}) {Text("Cancel")}})
    if(discardNote) AlertDialog(onDismissRequest={discardNote=false},title={Text("Discard this unsaved note?")},confirmButton={TextButton(onClick={noteOpen=false;discardNote=false;noteQuote="";noteBody=""}) {Text("Discard")}},dismissButton={TextButton(onClick={discardNote=false}) {Text("Keep writing")}})
    if(remove) AlertDialog(onDismissRequest={remove=false},title={Text("Remove ${book.title}?")},text={Text("This removes Lilyly's copy, bookmarks and reading notes. Your original document and any journal pages you made will remain.")},confirmButton={TextButton(onClick={scope.launch { withContext(Dispatchers.IO) { files.directory(book.id).deleteRecursively() };store.removeBook(book.id);onBack() }}) {Text("Remove book")}},dismissButton={TextButton(onClick={remove=false}) {Text("Keep it")}})
}
