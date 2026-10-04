package com.lilyly.app

import android.content.Intent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    val entries = store.journalEntries.filter { it.section == section }.sortedByDescending { it.updatedAt }
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
            ExtendedFloatingActionButton(onClick = onNew, icon = { Icon(Icons.Default.Add, null) }, text = { Text("New page") })
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("No pages yet. Open a fresh page and make this book yours.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(entries, key = { it.id }) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onEdit(entry.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(entry.title.ifBlank { "Untitled" }, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
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
    }
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
    var entry by remember(editingId, section) { mutableStateOf(original?.copy() ?: JournalEntry(section = section, title = "")) }
    var mode by remember { mutableStateOf("Text") }
    var confirmDelete by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            entry = entry.copy(imageUri = uri.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry.title.ifBlank { "New ${entry.section} page" }) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
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
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            if (mode == "Text") {
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
                    Text("Your ink is saved with the page. Handwriting-to-neat-font conversion is one of the next integrations.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .66f))
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
            item {
                Button(onClick = { imageLauncher.launch(arrayOf("image/*")) }) {
                    Icon(Icons.Default.Image, null)
                    Text(if (entry.imageUri.isBlank()) "  Add image" else "  Change image")
                }
            }
            if (entry.imageUri.isNotBlank()) {
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
                OutlinedTextField(
                    value = entry.tags,
                    onValueChange = { entry = entry.copy(tags = it) },
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Spotify soundtrack", style = MaterialTheme.typography.bodyMedium)
                    Text("Coming next", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pinterest scrapbook drawer", style = MaterialTheme.typography.bodyMedium)
                    Text("Coming next", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Print-perfect A5 export", style = MaterialTheme.typography.bodyMedium)
                    Text("Coming next", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                }
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
}
