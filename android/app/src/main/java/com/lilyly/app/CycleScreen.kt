package com.lilyly.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.*

internal val Rose = Color(0xFFD58C9B)
internal val Sage = Color(0xFF9FB7A0)
internal val Gold = Color(0xFFCDB681)
internal val Lavender = Color(0xFFB6A1CD)

private fun phaseColor(state: CycleDayState): Color = when {
    state.predictedPeriod -> Rose
    state.ovulation -> Gold
    state.fertile -> Sage
    state.phase == "Menstrual phase" -> Rose
    state.phase == "Follicular phase" -> Sage
    else -> Lavender
}

@Composable
fun CycleScreen(store: AppStore, initialDate:String?=null) {
    val today = LocalDate.now()
    val openingDate=initialDate?.let {runCatching {LocalDate.parse(it)}.getOrNull()} ?: today
    var monthText by rememberSaveable(initialDate) { mutableStateOf(YearMonth.from(openingDate).toString()) }
    var selectedText by rememberSaveable(initialDate) { mutableStateOf(openingDate.toString()) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var settings by rememberSaveable { mutableStateOf(false) }
    val month = YearMonth.parse(monthText)
    val selected = LocalDate.parse(selectedText)
    val prefs = store.cyclePreferences
    val pattern = cyclePattern(store.cycleLogs.filter { it.period }.mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }, prefs)
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("THE CYCLE GARDEN", style = MaterialTheme.typography.labelSmall, letterSpacing = 3.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Your own rhythm", style = MaterialTheme.typography.headlineMedium)
                }
                IconButton(onClick = { settings = true }) { Icon(Icons.Default.Settings, "Cycle settings") }
            }
            CycleWheel(today, pattern, prefs) { selectedText = today.toString(); editing = true }
            Text(when {
                prefs.mode == "Pregnancy" -> "Cycle predictions are paused. Your daily body journal stays open."
                pattern.starts.isEmpty() -> "Begin with your last period. A little history helps this garden grow."
                pattern.irregular -> "Your recorded cycles vary. Timing is less predictable; fertility markers are hidden."
                prefs.contraception == "Hormonal" -> "Hormonal contraception can change bleeding patterns. Fertility markers are hidden."
                else -> "Timing is estimated${if (pattern.samples == 0 || !prefs.useHistory) " from your settings" else " from ${pattern.samples} recorded intervals"}. It cannot confirm ovulation or be used as contraception."
            }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            BotanicalDivider()
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = { monthText = month.minusMonths(1).toString() }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month") }
                Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = { monthText = month.plusMonths(1).toString() }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month") }
            }
            TextButton(onClick = { monthText = YearMonth.from(today).toString(); selectedText = today.toString() }) { Text("Return to today") }
            Row(Modifier.fillMaxWidth()) {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall) }
            }
            val leading = month.atDay(1).dayOfWeek.value - 1
            val cells = (0 until ((leading + month.lengthOfMonth() + 6) / 7) * 7).toList()
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { index ->
                        val number = index - leading + 1
                        if (number !in 1..month.lengthOfMonth()) Spacer(Modifier.weight(1f).height(66.dp)) else {
                            val date = month.atDay(number)
                            val log = store.cycleLogs.firstOrNull { it.date == date.toString() }
                            val state = cycleDayState(date, pattern, prefs)
                            val prediction = state.predictedPeriod && !date.isBefore(today) && log?.period != true
                            val color = if (log?.period == true || log?.spotting == true) Rose else phaseColor(state)
                            val description = "$date${if (date == today) ", today" else ""}${if (log?.period == true) ", recorded period" else if (prediction) ", estimated period" else ""}${if (state.fertile) ", estimated fertile window" else ""}${if (state.ovulation) ", estimated ovulation" else ""}${if (log != null) ", has a log" else ""}. Open daily log"
                            Column(
                                Modifier.weight(1f).height(66.dp).padding(2.dp)
                                    .border(if (date == selected || date == today) 1.dp else 0.dp, if (date == selected) Gold else if (date == today) Lavender else Color.Transparent, RoundedCornerShape(22.dp))
                                    .background(if (date == selected) MaterialTheme.colorScheme.primary.copy(alpha = .10f) else Color.Transparent, RoundedCornerShape(22.dp))
                                    .clickable { selectedText = date.toString(); editing = true }
                                    .semantics(mergeDescendants = true) { contentDescription = description },
                                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                            ) {
                                Canvas(Modifier.size(17.dp)) {
                                    when {
                                        log?.period == true -> drawCircle(Rose)
                                        log?.spotting == true -> drawCircle(Rose, radius = size.minDimension * .25f)
                                        prediction -> drawCircle(Rose, style = Stroke(1.6.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
                                        state.ovulation -> { drawCircle(Gold, style = Stroke(1.5.dp.toPx())); drawCircle(Gold, radius = 2.dp.toPx()) }
                                        state.fertile -> drawOval(Sage, topLeft = Offset(size.width * .25f, 0f), size = Size(size.width * .5f, size.height))
                                        else -> drawCircle(color.copy(alpha = .3f), radius = 2.dp.toPx())
                                    }
                                }
                                Text(number.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal)
                                Text(if (log != null) "·" else " ", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, lineHeight = 10.sp)
                            }
                        }
                    }
                }
            }
            Text("● Recorded bleeding    ◌ Estimated period\nLeaf · estimated fertile window    ⊙ Estimated ovulation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
        }
        item { DailyConnections(store, selected, pattern) }
        item { CycleTrends(store, pattern) }
        item { Spacer(Modifier.height(20.dp)) }
    }
    if (editing) DailyLogSheet(store, selected, onClose = { editing = false })
    if (settings) CycleSettingsSheet(store, onClose = { settings = false })
}

@Composable
internal fun BotanicalDivider() {
    val color = MaterialTheme.colorScheme.primary.copy(alpha = .55f)
    Canvas(Modifier.fillMaxWidth().height(24.dp)) {
        val y = size.height / 2
        drawLine(color, Offset(0f, y), Offset(size.width * .40f, y), 1f)
        drawLine(color, Offset(size.width * .60f, y), Offset(size.width, y), 1f)
        val x = size.width / 2
        drawLine(color, Offset(x - 20, y + 4), Offset(x + 20, y - 4), 1.5f)
        for (i in -1..1) {
            drawOval(color, Offset(x + i * 12 - 4, y - 9), Size(8f, 10f), style = Stroke(1f))
        }
    }
}

@Composable
private fun CycleWheel(today: LocalDate, pattern: CyclePattern, prefs: CyclePreferences, onLog: () -> Unit) {
    val state = cycleDayState(today, pattern, prefs)
    val last = pattern.starts.lastOrNull { !it.isAfter(today) }
    val until = last?.let { ChronoUnit.DAYS.between(today, it.plusDays(pattern.length.toLong())).toInt() }
    val title = when {
        prefs.mode == "Pregnancy" -> "A new chapter"
        until == null -> "Find your rhythm"
        state.day!! <= prefs.periodLength -> "Day ${state.day}"
        until > 0 -> "$until ${if (until == 1) "day" else "days"}"
        until == 0 -> "Around today"
        else -> "${-until} days beyond"
    }
    val subtitle = when {
        prefs.mode == "Pregnancy" -> "BODY • MIND • REST"
        until == null -> "YOUR BODY, YOUR STORY"
        state.day!! <= prefs.periodLength -> "OF YOUR CYCLE"
        until >= 0 -> "UNTIL ESTIMATED PERIOD"
        else -> "YOUR ESTIMATED DATE"
    }
    val ink = MaterialTheme.colorScheme.primary
    Box(Modifier.fillMaxWidth().aspectRatio(1f).semantics { contentDescription = "$title. $subtitle. ${state.phase}" }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val c = center
            val r = size.minDimension * .44f
            drawCircle(Brush.radialGradient(listOf(Lavender.copy(alpha = .12f), Color.Transparent)), r, c)
            drawCircle(ink.copy(alpha = .18f), r * .85f, c, style = Stroke(1.dp.toPx()))
            drawCircle(ink.copy(alpha = .2f), r * 1.12f, c, style = Stroke(.5.dp.toPx()))
            val count = if (prefs.mode == "Pregnancy") 28 else pattern.length
            for (i in 0 until count) {
                val angle = (i * 2.0 * PI / count - PI / 2)
                val p = Offset(c.x + cos(angle).toFloat() * r, c.y + sin(angle).toFloat() * r)
                val day = i + 1
                val ov = pattern.length - prefs.lutealLength
                val color = when {
                    last == null || prefs.mode == "Pregnancy" -> ink.copy(alpha = .45f)
                    day <= prefs.periodLength -> Rose
                    day < ov - 1 -> Sage
                    day <= ov + 1 -> Gold
                    else -> Lavender
                }
                val leaf = Path().apply {
                    moveTo(p.x, p.y - 6.dp.toPx())
                    quadraticTo(p.x + 8.dp.toPx(), p.y, p.x, p.y + 6.dp.toPx())
                    quadraticTo(p.x - 8.dp.toPx(), p.y, p.x, p.y - 6.dp.toPx())
                }
                drawPath(leaf, color, style = if (last == null) Stroke(1.dp.toPx()) else androidx.compose.ui.graphics.drawscope.Fill)
                if (state.day == day) drawCircle(ink, 10.dp.toPx(), p, style = Stroke(1.5.dp.toPx()))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(.64f)) {
            Text("✧", color = ink, fontSize = 24.sp)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp, textAlign = TextAlign.Center)
            Text(title, fontFamily = FontFamily.Serif, fontSize = 30.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 6.dp))
            Text(if (state.day != null) "Day ${state.day} · ${state.phase.removeSuffix(" phase")}" else "A space to listen", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
            FilledTonalButton(onClick = onLog) { Text("Log today") }
            if (until != null && until in 1..prefs.pmsDays && prefs.mode != "Pregnancy") Text("Possible premenstrual days", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
    if (prefs.mode != "Pregnancy") Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        listOf("Menstrual" to Rose, "Follicular" to Sage, "Ovulatory" to Gold, "Luteal" to Lavender).forEach { (label, color) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) { Box(Modifier.size(5.dp).background(color, CircleShape)); Text(label, style = MaterialTheme.typography.labelSmall) }
        }
    }
}

@Composable
private fun DailyConnections(store: AppStore, date: LocalDate, pattern: CyclePattern) {
    val key = date.toString()
    val log = store.cycleLogs.firstOrNull { it.date == key }
    val mind = store.mentalCheckIns.filter { it.dateTime.startsWith(key) }
    val journals = store.journalEntries.count { it.section!="Templates" && it.createdAt.startsWith(key) && "sample" !in it.tags }
    val meds = store.medicationLogs.filter { it.dateTime.startsWith(key) }
    val sleep = store.sleepRecords.filter { it.date == key }
    Column {
        BotanicalDivider()
        Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), style = MaterialTheme.typography.titleLarge)
        val state = cycleDayState(date, pattern, store.cyclePreferences)
        Text("${state.day?.let { "Cycle day $it · " } ?: ""}${state.phase}", color = MaterialTheme.colorScheme.primary)
        Text("☾ ${moonPhaseName(date.atTime(12, 0))} · approximate", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        if (log == null && mind.isEmpty() && journals == 0 && meds.isEmpty() && sleep.isEmpty()) Text("An unwritten day. Tap its date to leave a little trace.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        log?.let {
            if (it.period || it.spotting) Text(if (it.spotting) "Body · spotting" else "Body · ${it.flow.lowercase()} bleeding")
            if (it.selections.isNotEmpty()) Text(it.selections.joinToString(" · "), style = MaterialTheme.typography.bodyMedium)
            if (it.symptoms.isNotBlank()) Text(it.symptoms)
            if (it.sleepHours > 0 && it.sleepHours.isFinite()) Text("Sleep · ${it.sleepHours} hours")
            if (it.notes.isNotBlank()) Text(it.notes, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
        }
        if (sleep.isNotEmpty()) Text("Rest · ${sleepDurationLabel(sleep.sumOf { it.durationMinutes })} · ${sleep.count { it.dream.isNotBlank() }} dreams")
        if (mind.isNotEmpty()) {
            val latest = mind.maxBy { it.dateTime }
            Text("Sanctuary · ${mind.size} check-in${if (mind.size == 1) "" else "s"}")
            if (latest.feelings.isNotEmpty()) Text(latest.feelings.joinToString(" · "))
            if (latest.detailedRatings) Text("Latest mood ${latest.mood}/10", style = MaterialTheme.typography.bodySmall)
        }
        if (journals > 0) Text("Journal · $journals ${if (journals == 1) "page" else "pages"}")
        if (meds.isNotEmpty()) Text("Medication · " + meds.groupingBy { it.status }.eachCount().entries.joinToString(" · ") { "${it.value} ${it.key.lowercase()}" })
    }
}

@Composable
private fun CycleTrends(store: AppStore, pattern: CyclePattern) {
    var metric by rememberSaveable { mutableStateOf("Mood") }
    val today = LocalDate.now()
    val first = today.minusDays(29)
    val entries = store.cycleLogs.filter { it.ratingsRecorded }.mapNotNull { log ->
        runCatching { LocalDate.parse(log.date) to log }.getOrNull()
    }.filter { !it.first.isBefore(first) && !it.first.isAfter(today) }.sortedBy { it.first }
    val color = MaterialTheme.colorScheme.primary
    Column {
        BotanicalDivider()
        Text("Threads of your month", style = MaterialTheme.typography.titleLarge)
        Text("Your recorded ratings · last 30 days · 0–10", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Mood", "Energy", "Pain").forEach { name -> FilterChip(selected = metric == name, onClick = { metric = name }, label = { Text(name) }) }
        }
        if (entries.isEmpty()) {
            Text("Your graph grows from your own observations. Add optional ratings in a daily log to begin.", modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Canvas(Modifier.fillMaxWidth().height(150.dp).semantics { contentDescription = "$metric graph. ${entries.size} recorded days. Text values follow." }) {
                val bottom = size.height - 8.dp.toPx()
                val top = 8.dp.toPx()
                val dx = size.width / 30f
                for (i in 0..29) {
                    val state = cycleDayState(first.plusDays(i.toLong()), pattern, store.cyclePreferences)
                    if (state.day != null) drawRect(phaseColor(state).copy(alpha = .10f), Offset(i * dx, top), Size(dx, bottom - top))
                }
                listOf(0, 5, 10).forEach { value ->
                    val y = bottom - value / 10f * (bottom - top)
                    drawLine(color.copy(alpha = .15f), Offset(0f, y), Offset(size.width, y), 1f)
                }
                var previous: Pair<LocalDate, Offset>? = null
                entries.forEach { (date, log) ->
                    val value = when (metric) { "Energy" -> log.energy; "Pain" -> log.pain; else -> log.mood }.coerceIn(0, 10)
                    val p = Offset((ChronoUnit.DAYS.between(first, date) + .5f) * dx, bottom - value / 10f * (bottom - top))
                    previous?.let { (d, point) -> if (d.plusDays(1) == date) drawLine(color, point, p, 2.dp.toPx(), StrokeCap.Round) }
                    drawCircle(color, 3.dp.toPx(), p)
                    previous = date to p
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(first.toString(), style = MaterialTheme.typography.labelSmall); Text("Today", style = MaterialTheme.typography.labelSmall) }
            Text("Tint shows estimated cycle phase. Gaps mean no rating, not zero.", style = MaterialTheme.typography.bodySmall)
            entries.takeLast(5).forEach { (date, log) -> Text("${date.format(DateTimeFormatter.ofPattern("d MMM"))} · $metric ${when(metric) { "Pain" -> log.pain; "Energy" -> log.energy; else -> log.mood }}/10", style = MaterialTheme.typography.labelSmall) }
        }
    }
}
