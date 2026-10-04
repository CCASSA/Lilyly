package com.lilyly.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.floor

private data class ShelfBook(val title: String, val subtitle: String, val symbol: String, val route: String)

@Composable
fun LibraryHomeScreen(
    store: AppStore,
    onOpenSection: (String) -> Unit,
    onOpenCycle: () -> Unit,
    onOpenSanctuary: () -> Unit,
    onOpenTarot: () -> Unit,
    onOpenBookshelf: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    val books = listOf(
        ShelfBook("Journal", "daily pages & memory keeping", "✒", "Journal"),
        ShelfBook("Grimoire", "rituals, correspondences & craft", "☾", "Grimoire"),
        ShelfBook("Poems", "the things that need another language", "❦", "Poems"),
        ShelfBook("Spells", "working book & results", "✦", "Spells"),
        ShelfBook("Dream Book", "nightmares, symbols & strange places", "☁", "Dreams"),
        ShelfBook("Ideas", "fragments worth keeping", "✧", "Ideas")
    )

    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(6.dp))
            Text("A little world of your own", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Come in. Leave a thought, follow a rhythm, make a little magic.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .72f))
            Spacer(Modifier.height(12.dp))
            TodayCard(store, onOpenCalendar)
            Spacer(Modifier.height(12.dp))
            val pattern = cyclePattern(store.cycleLogs.filter { it.period }.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }, store.cyclePreferences)
            val state = cycleDayState(LocalDate.now(), pattern, store.cyclePreferences)
            TextButton(onClick = onOpenCycle) { Text("❧  ${state.day?.let { "Cycle day $it · " } ?: ""}${state.phase}  →") }
            BotanicalDivider()
        }
        items(books.chunked(2)) { rowBooks ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowBooks.forEach { book ->
                    Card(
                        modifier = Modifier.weight(1f).height(145.dp).clickable { onOpenSection(book.route) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                            Text(book.symbol, style = MaterialTheme.typography.headlineMedium)
                            Column {
                                Text(book.title, fontWeight = FontWeight.Bold)
                                Text(book.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .68f))
                            }
                        }
                    }
                }
                if (rowBooks.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item {
            Text("Other rooms", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
            QuickRoom("🩸", "Cycle & body", "Track your cycle, symptoms and body patterns", onOpenCycle)
            QuickRoom("🕯", "Mental Health Sanctuary", "Check-ins, medication, therapy and safety plan", onOpenSanctuary)
            QuickRoom("🔮", "Tarot", "Meanings, readings and your personal patterns", onOpenTarot)
            QuickRoom("📚", "Reading nook", "Your imported books and commonplace notes", onOpenBookshelf)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TodayCard(store: AppStore, onCalendar: () -> Unit) {
    val moon = moonPhaseName(LocalDateTime.now())
    val nextSabbat = nextSabbat(store.hemisphere, LocalDate.now())
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onCalendar() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .14f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text("Today in your sky", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("☾  $moon", style = MaterialTheme.typography.titleMedium)
                    Text("Moon phase • approximate", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(nextSabbat.first, style = MaterialTheme.typography.titleMedium)
                    Text("${nextSabbat.second} • ${store.hemisphere}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun QuickRoom(symbol: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)).padding(12.dp),
                contentAlignment = Alignment.Center
            ) { Text(symbol, style = MaterialTheme.typography.titleLarge) }
            Column(Modifier.padding(start = 14.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
            }
        }
    }
}

fun moonPhaseName(now: LocalDateTime): String {
    val epoch = LocalDateTime.of(2000, 1, 6, 18, 14)
    val days = ChronoUnit.SECONDS.between(epoch, now).toDouble() / 86400.0
    val cycle = ((days / 29.53058867) % 1.0 + 1.0) % 1.0
    return when {
        cycle < 0.03 || cycle > 0.97 -> "New moon"
        cycle < 0.22 -> "Waxing crescent"
        cycle < 0.28 -> "First quarter"
        cycle < 0.47 -> "Waxing gibbous"
        cycle < 0.53 -> "Full moon"
        cycle < 0.72 -> "Waning gibbous"
        cycle < 0.78 -> "Last quarter"
        else -> "Waning crescent"
    }
}

fun nextSabbat(hemisphere: String, today: LocalDate): Pair<String, String> {
    val south = listOf(
        Triple(2, 1, "Lughnasadh"), Triple(3, 21, "Mabon"), Triple(5, 1, "Samhain"), Triple(6, 21, "Yule"),
        Triple(8, 1, "Imbolc"), Triple(9, 21, "Ostara"), Triple(11, 1, "Beltane"), Triple(12, 21, "Litha")
    )
    val north = listOf(
        Triple(2, 1, "Imbolc"), Triple(3, 21, "Ostara"), Triple(5, 1, "Beltane"), Triple(6, 21, "Litha"),
        Triple(8, 1, "Lughnasadh"), Triple(9, 21, "Mabon"), Triple(11, 1, "Samhain"), Triple(12, 21, "Yule")
    )
    val list = if (hemisphere == "Southern") south else north
    val candidates = list.map { (m, d, name) ->
        var date = LocalDate.of(today.year, m, d)
        if (date.isBefore(today)) date = date.plusYears(1)
        Triple(date, name, "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }}")
    }
    val next = candidates.minBy { it.first }
    return next.second to next.third
}
