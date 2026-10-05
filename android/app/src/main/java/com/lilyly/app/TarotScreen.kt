package com.lilyly.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.sin

private val spreads = linkedMapOf(
    "One quiet question" to listOf("A reflection"),
    "Three lanterns" to listOf("What is here", "What needs care", "A next step"),
    "Root & bloom" to listOf("The root", "What is growing", "What to release")
)

@Composable
fun TarotFace(card: TarotCardInfo?, modifier: Modifier = Modifier, reversed: Boolean = false, onClick: (() -> Unit)? = null) {
    val gold = Color(0xFFC8AC75)
    val shade = when(card?.suit) { "Cups" -> Color(0xFF223A47); "Wands" -> Color(0xFF3C2839); "Swords" -> Color(0xFF303247); "Pentacles" -> Color(0xFF263C33); else -> Color(0xFF282235) }
    val shape = RoundedCornerShape(13.dp)
    Column(modifier.aspectRatio(.61f).clip(shape).background(Brush.verticalGradient(listOf(shade, Color(0xFF121019))))
        .border(1.dp, gold.copy(alpha = .7f), shape)
        .then(if(onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
        .semantics { contentDescription = if(card == null) "Face-down card. Tap to reveal" else "${card.name}, ${if(reversed) "reversed" else "upright"}" }
        .padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(if(card == null) "L I L Y L Y" else if(card.suit == "Major") "${card.number} · ARCANA" else card.suit.uppercase(), color = gold, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        Box(Modifier.weight(1f).fillMaxWidth().graphicsLayer { rotationZ = if(reversed) 180f else 0f }, contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val c = center; val r = size.minDimension * .36f
                drawCircle(gold.copy(alpha = .55f), r, c, style = Stroke(1.dp.toPx()))
                drawCircle(gold.copy(alpha = .2f), r * .83f, c, style = Stroke(.5.dp.toPx()))
                val points = (card?.number ?: 7) % 8 + 5
                repeat(points) { i ->
                    val a = i * Math.PI * 2 / points
                    val p = Offset(c.x + cos(a).toFloat()*r*1.14f, c.y + sin(a).toFloat()*r*1.14f)
                    drawCircle(gold, 1.6.dp.toPx(), p)
                }
                // Original botanical sprigs, generated from geometry, with no third-party assets.
                listOf(-1,1).forEach { side ->
                    val x = c.x + side*r*.9f
                    drawLine(gold.copy(alpha=.5f), Offset(x,size.height*.8f), Offset(x-side*r*.35f,size.height*.25f), 1.dp.toPx())
                    repeat(4) { j ->
                        val y = size.height*(.7f-j*.1f)
                        drawOval(gold.copy(alpha=.3f), Offset(x-side*r*.08f*j-r*.1f,y), androidx.compose.ui.geometry.Size(r*.22f,r*.1f))
                    }
                }
            }
            Text(card?.symbol() ?: "☾", color = gold, style = MaterialTheme.typography.displayMedium)
        }
        Text(card?.name ?: "A little mystery", color = Color(0xFFF3EAD8), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center, minLines = 2)
        if(reversed && card != null) Text("REVERSED", style = MaterialTheme.typography.labelSmall, color = gold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotScreen(store: AppStore, onBack: () -> Unit, onJournal: (JournalEntry) -> Unit) {
    var tab by rememberSaveable { mutableStateOf("Draw") }
    var query by rememberSaveable { mutableStateOf("") }
    var suit by rememberSaveable { mutableStateOf("All") }
    var favorites by rememberSaveable { mutableStateOf(false) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }
    var readingId by rememberSaveable { mutableStateOf<String?>(null) }
    var spread by rememberSaveable { mutableStateOf(spreads.keys.first()) }
    var reversals by rememberSaveable { mutableStateOf(true) }
    var draft by rememberSaveable { mutableStateOf("[]") }
    var revealed by rememberSaveable { mutableIntStateOf(0) }
    var question by rememberSaveable { mutableStateOf("") }
    var reflection by rememberSaveable { mutableStateOf("") }
    var discard by remember { mutableStateOf(false) }
    val drawn = remember(draft) { drawnCards(draft) }
    val leave = { if(drawn.isNotEmpty() || question.isNotBlank() || reflection.isNotBlank()) discard = true else onBack() }
    BackHandler(onBack = leave)
    Scaffold(topBar = { TopAppBar(title = { Text("The tarot room") }, navigationIcon = { IconButton(onClick = leave) { Icon(Icons.Default.ArrowBack, "Back") } }) }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 18.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Draw", "Deck", "History").forEach { FilterChip(tab == it, { tab = it }, label = { Text(it) }) }
            }
            LazyColumn(Modifier.weight(1f).testTag("tarot-screen"), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 28.dp)) {
                if(tab == "Draw") {
                    item {
                        Text("A moment between you & the cards", style = MaterialTheme.typography.headlineMedium)
                        Text("Reflect, imagine, listen inward. A reading offers a perspective, never a guaranteed prediction.", style = MaterialTheme.typography.bodySmall)
                    }
                    item {
                        if(drawn.isEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(spreads.keys.toList()) { name -> FilterChip(spread == name, { spread = name }, label = { Text(name) }) } }
                            Row(verticalAlignment = Alignment.CenterVertically) { Switch(reversals, { reversals = it }); Text("Include reversed cards") }
                        }
                        OutlinedTextField(question, { question = it }, label = { Text("What are you bringing to this reading?") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                    }
                    if(drawn.isEmpty()) {
                        item {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { TarotFace(null, Modifier.width(150.dp)) }
                            Button(onClick = { draft = drawTarot(spreads.getValue(spread), reversals).json(); revealed = 0 }, modifier = Modifier.fillMaxWidth().padding(top = 14.dp)) { Text("Shuffle & lay the cards") }
                        }
                    } else {
                        item {
                            Text(spread, style = MaterialTheme.typography.titleLarge)
                            Row(Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).widthIn(max = if(drawn.size == 1) 190.dp else 600.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                drawn.forEachIndexed { index, draw ->
                                    val visible = revealed and (1 shl index) != 0
                                    val alpha by animateFloatAsState(if(visible) 1f else .7f, label = "reveal")
                                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(draw.position, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.heightIn(min = 36.dp))
                                        TarotFace(if(visible) tarotDeck.first { it.name == draw.name } else null, Modifier.fillMaxWidth().graphicsLayer { this.alpha = alpha }, draw.reversed) {
                                            if(visible) detail = draw.name else revealed = revealed or (1 shl index)
                                        }
                                    }
                                }
                            }
                            Text("Tap each card to turn it over. Tap it again to explore.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                        }
                        items(drawn.filterIndexed { index, _ -> revealed and (1 shl index) != 0 }) { draw ->
                            val info = tarotDeck.first { it.name == draw.name }
                            Column { Text("${draw.position} · ${draw.name}", style = MaterialTheme.typography.titleMedium); Text(if(draw.reversed) info.reversed else info.upright) }
                        }
                        item {
                            OutlinedTextField(reflection, { reflection = it }, label = { Text("What do you notice?") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                            Button(onClick = {
                                val reading = TarotReading(title = question.ifBlank { spread }, cards = drawn.joinToString("\n") { "${it.position}: ${it.name} · ${if(it.reversed) "reversed" else "upright"}" }, notes = reflection,
                                    drawsJson = draft, spread = spread, context = "${moonPhaseName(LocalDateTime.now())} (approximate)")
                                store.saveTarotReading(reading); readingId = reading.id
                                draft = "[]"; reflection = ""; question = ""; revealed = 0; tab = "History"
                            }, enabled = revealed == (1 shl drawn.size)-1, modifier = Modifier.fillMaxWidth()) { Text("Keep this reading") }
                        }
                    }
                }
                if(tab == "Deck") {
                    item {
                        Text("78 small mirrors", style = MaterialTheme.typography.headlineMedium)
                        OutlinedTextField(query, { query = it }, label = { Text("Search cards & meanings") }, modifier = Modifier.fillMaxWidth())
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { items(listOf("All", "Major", "Wands", "Cups", "Swords", "Pentacles")) { name -> FilterChip(suit == name, { suit = name }, label = { Text(name) }) } }
                        FilterChip(favorites, { favorites = !favorites }, label = { Text("My favorites") })
                    }
                    val cards = tarotDeck.filter { (suit == "All" || it.suit == suit) && (!favorites || it.name in store.tarotFavorites) && (it.name+it.upright+it.reversed).contains(query,true) }
                    if(cards.isEmpty()) item { Text("No cards match. Try another word or suit.") }
                    items(cards.chunked(3)) { row -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { card -> TarotFace(card, Modifier.weight(1f), onClick = { detail = card.name }) }
                        repeat(3-row.size) { Spacer(Modifier.weight(1f)) }
                    } }
                }
                if(tab == "History") {
                    item { Text("Your trail through the cards", style = MaterialTheme.typography.headlineMedium) }
                    if(store.tarotReadings.isEmpty()) item { Text("Your kept readings will gather here, with their reflections and the cards that return.") }
                    items(store.tarotReadings, key = { it.id }) { reading ->
                        Column(Modifier.fillMaxWidth().clickable { readingId = reading.id }.padding(vertical = 10.dp)) {
                            Text(reading.date, style = MaterialTheme.typography.labelMedium)
                            Text(reading.title, style = MaterialTheme.typography.titleLarge)
                            Text(reading.cards, style = MaterialTheme.typography.bodySmall)
                            BotanicalDivider()
                        }
                    }
                }
            }
        }
    }
    if(discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Leave this reading?") }, text = { Text("Your unsaved cards and reflection will be discarded.") }, confirmButton = { TextButton(onClick = onBack) { Text("Discard & leave") } }, dismissButton = { TextButton(onClick = { discard = false }) { Text("Keep reflecting") } })
    detail?.let { name -> TarotCardSheet(store, tarotDeck.first { it.name == name }, onClose = { detail = null }) }
    readingId?.let { id -> store.tarotReadings.firstOrNull { it.id == id }?.let { reading ->
        ModalBottomSheet(onDismissRequest = { readingId = null }) {
            LazyColumn(Modifier.fillMaxWidth().padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                item { Text(reading.title, style = MaterialTheme.typography.headlineMedium); Text("${reading.date} · ${reading.spread}"); Text(reading.context, style = MaterialTheme.typography.bodySmall) }
                val cards = drawnCards(reading.drawsJson)
                if(cards.isNotEmpty()) item { LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) { items(cards) { d -> tarotDeck.firstOrNull { it.name == d.name }?.let { c -> TarotFace(c, Modifier.width(120.dp), d.reversed, onClick = { detail = d.name; readingId = null }) } } } }
                item { Text(reading.cards); if(reading.notes.isNotBlank()) Text(reading.notes) }
                item { Button(onClick = { onJournal(store.journalReading(reading)); readingId = null }, modifier = Modifier.fillMaxWidth()) { Text(if(store.journalEntries.any { it.id == reading.journalId }) "Open grimoire page" else "Make a grimoire page") } }
            }
        }
    } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TarotCardSheet(store: AppStore, card: TarotCardInfo, onClose: () -> Unit) {
    var notes by rememberSaveable(card.name) { mutableStateOf(store.tarotNotes[card.name] ?: "") }
    var favorite by rememberSaveable(card.name) { mutableStateOf(card.name in store.tarotFavorites) }
    var closing by remember { mutableStateOf(false) }
    val dirty = notes != (store.tarotNotes[card.name] ?: "") || favorite != (card.name in store.tarotFavorites)
    ModalBottomSheet(onDismissRequest = { if(dirty) closing = true else onClose() }) {
        LazyColumn(Modifier.padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { TarotFace(card, Modifier.width(145.dp)) } }
            item { Text(card.name, style = MaterialTheme.typography.headlineMedium); Text(card.correspondence(), color = MaterialTheme.colorScheme.primary) }
            item { Text("Upright", style = MaterialTheme.typography.titleMedium); Text(card.upright); Text("Reversed", style = MaterialTheme.typography.titleMedium); Text(card.reversed) }
            item { Text("A question for your page", style = MaterialTheme.typography.titleMedium); Text(card.prompt()); Text("Lilyly's original reflective interpretations. Suit correspondences are traditional associations, not scientific claims.", style = MaterialTheme.typography.bodySmall) }
            item {
                val history = store.tarotReadings.filter { r -> drawnCards(r.drawsJson).any { it.name == card.name } }
                Text("In your readings · ${history.size}", style = MaterialTheme.typography.titleMedium)
                history.take(5).forEach { Text("${it.date} · ${it.title}", style = MaterialTheme.typography.bodySmall) }
                FilterChip(favorite, { favorite = !favorite }, label = { Text(if(favorite) "♥ Favorite" else "♡ Keep as a favorite") })
                OutlinedTextField(notes, { notes = it }, label = { Text("My associations & notes") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Button(onClick = { store.updateTarotCard(card.name, notes, favorite); onClose() }, modifier = Modifier.fillMaxWidth()) { Text("Save my card notes") }
            }
        }
    }
    if(closing) AlertDialog(onDismissRequest = { closing = false }, title = { Text("Keep your card notes?") }, confirmButton = { TextButton(onClick = { store.updateTarotCard(card.name, notes, favorite); onClose() }) { Text("Save & close") } }, dismissButton = { TextButton(onClick = onClose) { Text("Discard changes") } })
}
