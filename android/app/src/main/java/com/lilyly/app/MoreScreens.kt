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
                    Text(section, fontWeight = FontWeight.Bold)
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
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f))) {
                Column(Modifier.padding(16.dp)) {
                    Text("Archive", fontWeight = FontWeight.Bold)
                    Text("Global reusable objects — songs, quotes, dreams, poems, images and readings — are planned as the next data layer.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(25.dp))
        }
    }
}

private data class TarotCardInfo(val name: String, val upright: String, val reversed: String)
private val majorArcana = listOf(
    TarotCardInfo("The Fool", "beginnings, freedom, leap of faith", "recklessness, hesitation, poor judgement"),
    TarotCardInfo("The Magician", "will, skill, manifestation, agency", "manipulation, scattered power, blocked action"),
    TarotCardInfo("The High Priestess", "intuition, mystery, inner knowing", "disconnection from intuition, secrets, avoidance"),
    TarotCardInfo("The Empress", "nurture, creativity, abundance", "creative block, overgiving, neglect of self"),
    TarotCardInfo("The Emperor", "structure, authority, boundaries", "rigidity, control, domination"),
    TarotCardInfo("The Hierophant", "tradition, teaching, shared systems", "rebellion, personal belief, challenging convention"),
    TarotCardInfo("The Lovers", "choice, intimacy, alignment", "disharmony, difficult choice, misaligned values"),
    TarotCardInfo("The Chariot", "direction, determination, movement", "loss of direction, force without control"),
    TarotCardInfo("Strength", "courage, patience, gentle power", "self-doubt, depletion, reactivity"),
    TarotCardInfo("The Hermit", "solitude, reflection, guidance within", "isolation, withdrawal, avoidance"),
    TarotCardInfo("Wheel of Fortune", "cycles, change, turning point", "resistance to change, repeating pattern"),
    TarotCardInfo("Justice", "truth, accountability, balance", "unfairness, denial, consequences avoided"),
    TarotCardInfo("The Hanged Man", "pause, surrender, new perspective", "stagnation, needless sacrifice, refusal to release"),
    TarotCardInfo("Death", "ending, transformation, transition", "clinging, stalled transition, fear of ending"),
    TarotCardInfo("Temperance", "integration, patience, moderation", "imbalance, excess, fragmentation"),
    TarotCardInfo("The Devil", "attachment, compulsion, shadow, bondage", "release, recognition, reclaiming agency"),
    TarotCardInfo("The Tower", "rupture, truth revealed, collapse", "avoided change, internal upheaval, delayed reckoning"),
    TarotCardInfo("The Star", "hope, renewal, openness", "discouragement, disconnection, loss of faith"),
    TarotCardInfo("The Moon", "uncertainty, dream, intuition, illusion", "clarity emerging, fear exposed, confusion lifting"),
    TarotCardInfo("The Sun", "clarity, vitality, joy", "temporary cloud, forced optimism, delayed warmth"),
    TarotCardInfo("Judgement", "reckoning, awakening, calling", "self-judgement, avoidance, refusal to answer"),
    TarotCardInfo("The World", "completion, integration, arrival", "unfinished business, delay, lack of closure")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotScreen(store: AppStore, onBack: () -> Unit) {
    var tab by remember { mutableStateOf("Cards") }
    var query by remember { mutableStateOf("") }
    var reading by remember { mutableStateOf(TarotReading()) }
    Scaffold(topBar = { TopAppBar(title = { Text("Tarot") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }) }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = tab == "Cards", onClick = { tab = "Cards" }, label = { Text("Card library") })
                FilterChip(selected = tab == "Readings", onClick = { tab = "Readings" }, label = { Text("Reading log") })
            }
            Spacer(Modifier.height(8.dp))
            if (tab == "Cards") {
                OutlinedTextField(query, { query = it }, label = { Text("Find a card") }, modifier = Modifier.fillMaxWidth())
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(majorArcana.filter { it.name.contains(query, true) || it.upright.contains(query, true) }) { card ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(14.dp)) {
                                Text(card.name, fontWeight = FontWeight.Bold)
                                Text("Upright — ${card.upright}", style = MaterialTheme.typography.bodySmall)
                                Text("Reversed — ${card.reversed}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .7f))
                            }
                        }
                    }
                    item { Spacer(Modifier.height(20.dp)) }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        OutlinedTextField(reading.title, { reading = reading.copy(title = it) }, label = { Text("Reading title / question") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(reading.cards, { reading = reading.copy(cards = it) }, label = { Text("Cards drawn") }, placeholder = { Text("The Moon — upright, Two of Cups — reversed…") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(reading.notes, { reading = reading.copy(notes = it) }, label = { Text("Interpretation / what it felt like") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { store.addTarotReading(reading.copy(date = LocalDate.now().toString())); reading = TarotReading() }, modifier = Modifier.fillMaxWidth()) { Text("Save reading") }
                        Text("Photo attachment and visual spread placement are next-build features.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    items(store.tarotReadings) { item ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(14.dp)) {
                                Text("${item.date} • ${item.title}", fontWeight = FontWeight.Bold)
                                Text(item.cards, style = MaterialTheme.typography.bodySmall)
                                if (item.notes.isNotBlank()) Text(item.notes.take(180), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    item { Spacer(Modifier.height(25.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfScreen(onBack: () -> Unit) {
    var style by remember { mutableStateOf("Cottage") }
    val samples = listOf("The Herbal Shelf", "Poetry by Candlelight", "Imported books will live here")
    Scaffold(topBar = { TopAppBar(title = { Text("Reading nook") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("Room mood", fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Cottage", "Gothic", "Celestial", "Botanical")) { mood ->
                        FilterChip(selected = style == mood, onClick = { style = mood }, label = { Text(mood) })
                    }
                }
            }
            items(samples) { title ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(18.dp)) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("📖  $title", fontWeight = FontWeight.SemiBold)
                        Text("$style shelf", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            item {
                Button(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) { Text("Import owned PDF / DRM-free EPUB — next build") }
                Text("Highlights, bookmarks and ‘Add to Commonplace Book’ will be part of the reader layer.", style = MaterialTheme.typography.bodySmall)
            }
        }
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
    Scaffold(topBar = { TopAppBar(title = { Text("Settings & privacy") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }) }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                SettingToggle("Nightshade theme", "Dark Victorian / candlelit palette", store.darkTheme) { store.setTheme(it) }
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
            item {
                Text("Device protection", fontWeight = FontWeight.SemiBold)
                Text("Lilyly does not yet require a separate app lock. Use your phone's screen lock. Keep this installation: export and restore are not available in this build.", style = MaterialTheme.typography.bodySmall)
            }
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
