package com.lilyly.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun MoreHubScreen(
    store: AppStore,
    onOpenSection: (String) -> Unit,
    onOpenTarot: () -> Unit,
    onOpenBookshelf: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val rooms = listOf(
        Triple("🔮", "Tarot", onOpenTarot),
        Triple("📚", "Reading nook", onOpenBookshelf),
        Triple("🌙", "Magical calendar", onOpenCalendar),
        Triple("⚙", "Settings & privacy", onOpenSettings)
    )
    LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Books & rooms", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold) }
        items(listOf("Grimoire", "Poems", "Spells", "Dreams", "Ideas")) { section ->
            Card(Modifier.fillMaxWidth().clickable { onOpenSection(section) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(if(section == "Dreams") "Night garden · sleep & dreams" else section, fontWeight = FontWeight.Bold)
                    Text("Open your $section book", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        items(rooms) { room ->
            Card(Modifier.fillMaxWidth().clickable { room.third() }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) {
                Row(Modifier.padding(16.dp)) {
                    Text(room.first, style = MaterialTheme.typography.titleLarge)
                    Text(room.second, modifier = Modifier.padding(start = 14.dp, top = 4.dp), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item { Spacer(Modifier.height(25.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagicalCalendarScreen(store: AppStore, onBack: () -> Unit) {
    val sabbat = nextSabbat(store.hemisphere, LocalDate.now())
    Scaffold(topBar = { TopAppBar(title = { Text("Magical calendar") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .13f))) {
                    Column(Modifier.padding(18.dp)) {
                        Text("☾ ${moonPhaseName(java.time.LocalDateTime.now())}", style = MaterialTheme.typography.headlineSmall)
                        Text("Approximate current lunar phase")
                    }
                }
            }
            item {
                Text("Wheel of the Year • ${store.hemisphere}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Next: ${sabbat.first} — ${sabbat.second}")
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Planet cycles", fontWeight = FontWeight.SemiBold)
                        Text("Accurate ephemeris data, retrogrades, eclipses and location-aware moon timing are planned for the astronomy integration.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(store: AppStore, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Settings & privacy") }, navigationIcon = { IconButton(onClick = onBack, enabled = !store.privacyBusy) { Icon(Icons.Default.ArrowBack, "Back") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ThemePicker(store)
            }
            item {
                Text("Hemisphere", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Southern", "Northern").forEach { h ->
                        FilterChip(selected = store.hemisphere == h, onClick = { store.updateHemisphere(h) }, label = { Text(h) })
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Privacy foundation", fontWeight = FontWeight.Bold)
                        Text("Current journal, cycle and mental-health records are encrypted locally using an Android Keystore-backed AES key.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item { PrivacyPanel(store) }
        }
    }
}

@Composable
private fun SettingToggle(title: String, subtitle: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked, onChanged)
    }
}

@Composable
private fun DisabledSetting(title: String, subtitle: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(14.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(store: AppStore, onBack: () -> Unit, onOpenJournal: (JournalEntry) -> Unit) {
    var query by remember { mutableStateOf("") }
    val q = query.trim()
    val journalResults = if (q.isBlank()) emptyList() else store.journalEntries.filter {
        it.title.contains(q, true) || it.body.contains(q, true) || it.tags.contains(q, true) || it.section.contains(q, true)
    }
    val therapyResults = if (q.isBlank()) emptyList() else store.therapyNotes.filter { it.title.contains(q, true) || it.before.contains(q, true) || it.after.contains(q, true) || it.homework.contains(q, true) }
    val tarotResults = if (q.isBlank()) emptyList() else store.tarotReadings.filter { it.title.contains(q, true) || it.cards.contains(q, true) || it.notes.contains(q, true) }

    Scaffold(topBar = { TopAppBar(title = { Text("Search Lilyly") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { OutlinedTextField(query, { query = it }, label = { Text("Search your archive") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
            if (q.isBlank()) {
                item { Text("Search journal pages, poems, grimoire notes, therapy notes and tarot readings.", style = MaterialTheme.typography.bodySmall) }
            }
            items(journalResults) { entry ->
                Card(Modifier.fillMaxWidth().clickable { onOpenJournal(entry) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("${entry.section} • ${entry.title}", fontWeight = FontWeight.SemiBold)
                        Text(entry.body.take(120), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            items(therapyResults) { note ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp)) { Text("Therapy • ${note.title}", fontWeight = FontWeight.SemiBold); Text((note.after.ifBlank { note.before }).take(120), style = MaterialTheme.typography.bodySmall) }
                }
            }
            items(tarotResults) { reading ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp)) { Text("Tarot • ${reading.title}", fontWeight = FontWeight.SemiBold); Text("${reading.cards} ${reading.notes}".take(120), style = MaterialTheme.typography.bodySmall) }
                }
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}
