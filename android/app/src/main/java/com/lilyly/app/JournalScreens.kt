package com.lilyly.app

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.lazy.LazyRow
import org.json.JSONObject
import java.time.LocalDate
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalListScreen(
    store: AppStore,
    section: String,
    onBack: (() -> Unit)? = null,
    onEdit: (String) -> Unit,
    onNew: () -> Unit
) {
    var search by rememberSaveable(section) { mutableStateOf("") }
    var favoritesOnly by rememberSaveable(section) { mutableStateOf(false) }
    var notebook by rememberSaveable(section) { mutableStateOf("") }
    val notebooks = store.notebookNames(section)
    var chooseTemplate by rememberSaveable { mutableStateOf(false) }
    val entries = store.journalEntries.filter { it.section == section && (!favoritesOnly || it.favorite) && (notebook.isBlank() || it.notebook == notebook) && (search.isBlank() || "${it.title} ${it.body} ${it.tags} ${it.canvasJson}".contains(search, true)) }.sortedByDescending { it.updatedAt }
    Scaffold(
        topBar = {
            if (onBack != null) {
                TopAppBar(
                    title = { Text(section) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { if(notebook.isBlank()) onNew() else { val page=JournalEntry(section=section,title="",notebook=notebook);store.upsertJournal(page);onEdit(page.id) } }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("New page") })
        }
    ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    NotebookShelf(store,section,notebook,{notebook=it})
                    TextButton(onClick={chooseTemplate=true}) { Text("Create from a template") }
                    Text("Pages to return to", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(search, { search = it }, label = { Text("Search pages & tags") }, modifier = Modifier.fillMaxWidth())
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(favoritesOnly, { favoritesOnly = !favoritesOnly }, label = { Text("Favorites") }) }
                        item { FilterChip(notebook.isBlank(), { notebook = "" }, label = { Text("All notebooks") }) }
                        items(notebooks) { name -> FilterChip(notebook == name, { notebook = name }, label = { Text(name) }) }
                    }
                    if (entries.isEmpty()) Text("No matching pages. Open a fresh page and make this book yours.")
                    BotanicalDivider()
                }
                items(entries, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onEdit(entry.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${if (entry.favorite) "✦ " else "❦ "}${entry.title.ifBlank { "Untitled" }}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            if (entry.notebook.isNotBlank()) Text(entry.notebook, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            if (entry.canvasJson != "[]") Text("Scrapbook · ${pagePieces(entry.canvasJson).size} elements", style = MaterialTheme.typography.labelSmall)
                            if (entry.body.isNotBlank()) {
                                Text(entry.body.replace("\n", " ").take(130), maxLines = 3, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f))
                            }
                            if (entry.tags.isNotBlank()) Text(entry.tags, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                item { Spacer(Modifier.height(90.dp)) }
            }
    }
    if(chooseTemplate) PageTemplatePicker(store,onDismiss={chooseTemplate=false},onUse={template ->
        val page=freshPage(template,section,notebook);store.upsertJournal(page);chooseTemplate=false;onEdit(page.id)
    },onEdit={id ->chooseTemplate=false;onEdit(id)})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalEditorScreen(
    store: AppStore,
    editingId: String?,
    section: String,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val original = remember(editingId) { store.journalEntries.firstOrNull { it.id == editingId } }
    var draft by rememberSaveable(editingId, section) { mutableStateOf((original?.copy() ?: JournalEntry(section = section, title = "")).toJson().toString()) }
    var entry by remember(editingId, section) { mutableStateOf(JournalEntry.fromJson(JSONObject(draft))) }
    LaunchedEffect(entry) { draft = entry.toJson().toString() }
    val initial = remember(editingId, section) { entry.toJson().toString() }
    var confirmLeave by remember { mutableStateOf(false) }
    fun leave() { if (entry.toJson().toString() != initial) confirmLeave = true else onBack() }
    BackHandler { leave() }
    var mode by rememberSaveable { mutableStateOf("Canvas") }
    var confirmDelete by remember { mutableStateOf(false) }
    var templateSaved by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            entry = if (mode == "Canvas") entry.copy(canvasJson = piecesJson(pagePieces(entry.canvasJson) + PagePiece(kind = "photo", content = uri.toString()))) else entry.copy(imageUri = uri.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry.title.ifBlank { "New ${entry.section} page" }) },
                navigationIcon = { IconButton(onClick = { leave() }) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    if (editingId != null) {
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Default.Delete, "Delete") }
                    }
                    IconButton(onClick = { store.upsertJournal(entry); onDone() }) { Icon(Icons.Default.Save, "Save") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).clipToBounds().testTag("journal-editor"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = mode == "Canvas", onClick = { mode = "Canvas" }, label = { Text("Scrapbook") })
                    FilterChip(selected = mode == "Text", onClick = { mode = "Text" }, label = { Text("Type") })
                    FilterChip(selected = mode == "Ink", onClick = { mode = "Ink" }, label = { Text("Write / draw") })
                }
            }
            item {
                OutlinedTextField(
                    value = entry.title,
                    onValueChange = { entry = entry.copy(title = it) },
                    label = { Text("Page title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            if (mode == "Canvas") {
                item {
                    val date = LocalDate.now()
                    val pattern = cyclePattern(store.cycleLogs.filter { it.period }.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }, store.cyclePreferences)
                    val state = cycleDayState(date, pattern, store.cyclePreferences)
                    val mind = store.mentalCheckIns.filter { it.dateTime.startsWith(date.toString()) }.maxByOrNull { it.dateTime }
                    val stamp = "$date\n${moonPhaseName(date.atTime(12, 0))}\n${state.day?.let { "Cycle day $it · " } ?: ""}${state.phase}" + (mind?.feelings?.takeIf { it.isNotEmpty() }?.joinToString(" · ")?.let { "\n$it" } ?: "")
                    ScrapbookCanvas(entry, { entry = it }, { imageLauncher.launch(arrayOf("image/*")) }, stamp)
                }
            } else if (mode == "Text") {
                item {
                    OutlinedTextField(
                        value = entry.body,
                        onValueChange = { entry = entry.copy(body = it) },
                        label = { Text("Write, paste, remember…") },
                        modifier = Modifier.fillMaxWidth().height(360.dp),
                        minLines = 12
                    )
                }
            } else {
                item {
                    Text("Stylus / handwriting layer", fontWeight = FontWeight.SemiBold)
                    Text("Let your handwriting be part of the page. Your strokes are saved locally.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f))
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(430.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (store.darkTheme) Color(0xFFEFE1CB) else Color(0xFFFFFBF2))
                    ) {
                        InkCanvas(
                            initialInkJson = entry.inkJson,
                            inkColor = Color(0xFF3A2422),
                            paperColor = if (store.darkTheme) Color(0xFFEFE1CB) else Color(0xFFFFFBF2),
                            onInkChanged = { entry = entry.copy(inkJson = it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    TextButton(onClick = { entry = entry.copy(inkJson = clearInk()) }) { Text("Clear handwriting") }
                }
            }
            if (mode != "Canvas") item {
                Button(onClick = { imageLauncher.launch(arrayOf("image/*")) }) {
                    Icon(Icons.Default.Image, null)
                    Text(if (entry.imageUri.isBlank()) "  Add image" else "  Change image")
                }
            }
            if (entry.imageUri.isNotBlank() && mode != "Canvas") {
                item {
                    AsyncImage(
                        model = entry.imageUri,
                        contentDescription = "Journal image",
                        modifier = Modifier.fillMaxWidth().height(240.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            item {
                TextButton(onClick={store.keepPageTemplate(entry);templateSaved=true},enabled=!templateSaved) { Text(if(templateSaved) "Template kept" else "Save as reusable template") }
                FilterChip(entry.favorite, { entry = entry.copy(favorite = !entry.favorite) }, label = { Text("Favorite page") })
                OutlinedTextField(entry.notebook, { entry = entry.copy(notebook = it) }, label = { Text("Notebook or collection") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = entry.tags,
                    onValueChange = { entry = entry.copy(tags = it) },
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (confirmDelete) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Delete this page?", fontWeight = FontWeight.Bold)
                            Row {
                                TextButton(onClick = { confirmDelete = false }) { Text("Keep") }
                                TextButton(onClick = { store.deleteJournal(entry.id); onDone() }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
    if (confirmLeave) AlertDialog(onDismissRequest = { confirmLeave = false }, title = { Text("Keep your page?") }, text = { Text("Save your changes before leaving?") }, confirmButton = { TextButton(onClick = { store.upsertJournal(entry); onDone() }) { Text("Save & close") } }, dismissButton = { TextButton(onClick = onBack) { Text("Discard changes") } })
}
