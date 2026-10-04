package com.lilyly.app

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private data class SymptomGroup(val name: String, val icon: ImageVector, val choices: List<String>)
private val groups = listOf(
    SymptomGroup("Feelings", Icons.Default.FavoriteBorder, listOf("Happy", "Calm", "Confident", "Affectionate", "Anxious", "Sad", "Irritable", "Angry", "Sensitive", "Overwhelmed", "Numb", "Low mood")),
    SymptomGroup("Body", Icons.Default.Spa, listOf("Cramps", "Pelvic pain", "Headache", "Migraine", "Breast tenderness", "Bloating", "Acne", "Nausea", "Fatigue", "Body aches", "Back pain")),
    SymptomGroup("Energy & focus", Icons.Default.WbSunny, listOf("Energetic", "Usual energy", "Tired", "Exhausted", "Focused", "Distracted", "Brain fog")),
    SymptomGroup("Cravings & appetite", Icons.Default.Restaurant, listOf("Sweet", "Salty", "Carbs", "Increased appetite", "Decreased appetite")),
    SymptomGroup("Cervical mucus", Icons.Default.WaterDrop, listOf("Dry", "Sticky", "Creamy", "Watery", "Egg-white mucus", "Unusual discharge")),
    SymptomGroup("Gut", Icons.Default.LocalFlorist, listOf("Constipation", "Diarrhea", "Bloating", "Nausea", "Gas", "Digestive discomfort")),
    SymptomGroup("Sex & desire", Icons.Default.FavoriteBorder, listOf("Sex", "Protected sex", "Unprotected sex", "Solo", "Low libido", "High libido", "Pain during sex")),
    SymptomGroup("Movement & rest", Icons.Default.DirectionsWalk, listOf("Exercise", "Walking", "Running", "Rest", "Strenuous activity", "Meditation")),
    SymptomGroup("Changes in routine", Icons.Default.AirplanemodeActive, listOf("Illness", "Stress", "Travel", "Sleep disruption", "Major routine change")),
    SymptomGroup("Contraception", Icons.Default.Shield, listOf("Pill taken", "Missed pill", "Patch changed", "Ring changed", "Contraceptive injection", "Emergency contraception")),
    SymptomGroup("Tests", Icons.Default.Science, listOf("Pregnancy test positive", "Pregnancy test negative", "Ovulation test positive", "Ovulation test negative")),
    SymptomGroup("Supplements", Icons.Default.Eco, listOf("Iron", "Magnesium", "Folic acid", "Vitamin D", "Calcium")),
    SymptomGroup("Endometriosis", Icons.Default.LocalFlorist, listOf("Pelvic flare", "Sciatic pain", "Joint pain", "Bowel pain", "Bladder pain", "Sharp pelvic pain"))
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun DailyLogSheet(store: AppStore, date: LocalDate, onClose: () -> Unit) {
    val existing = store.cycleLogs.firstOrNull { it.date == date.toString() }
    // JSON-backed saveable draft survives Activity recreation without writing until Save.
    var draft by rememberSaveable(date.toString()) { mutableStateOf((existing?.copy() ?: CycleLog(date = date.toString())).toJson().toString()) }
    val log = remember(draft) { CycleLog.fromJson(JSONObject(draft)) }
    fun update(value: CycleLog) { draft = value.toJson().toString() }
    var category by rememberSaveable { mutableStateOf("Feelings") }
    var details by rememberSaveable { mutableStateOf(false) }
    var custom by rememberSaveable { mutableStateOf("") }
    var confirmClose by remember { mutableStateOf(false) }
    val initial = remember(date) { (existing?.copy() ?: log).toJson().toString() }
    fun close() { if (draft != initial) confirmClose = true else onClose() }
    val future = date.isAfter(LocalDate.now())
    ModalBottomSheet(onDismissRequest = { close() }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(.94f).imePadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("A moment with yourself", style = MaterialTheme.typography.headlineSmall)
                    Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")), color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { close() }) { Icon(Icons.Default.Close, "Close daily log") }
            }
            LazyColumn(Modifier.weight(1f).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (future) item { Text("This day is still ahead. Come back to record what actually happened.") }
                else {
                    item {
                        Text("Bleeding", style = MaterialTheme.typography.titleLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("None", "Spotting", "Light", "Medium", "Heavy", "Very heavy").forEach { value ->
                                val selected = if (value == "Spotting") log.spotting else !log.spotting && log.flow == value
                                FilterChip(selected, { update(log.copy(flow = if (value == "Spotting") "None" else value, spotting = value == "Spotting", period = value !in listOf("None", "Spotting"))) }, label = { Text(value) }, leadingIcon = { Icon(Icons.Default.WaterDrop, null, Modifier.size(16.dp)) })
                            }
                        }
                    }
                    item {
                        Text("Choose whatever fits", style = MaterialTheme.typography.titleLarge)
                        Text("Tap to select; tap again to remove.", style = MaterialTheme.typography.bodySmall)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(groups) { group -> FilterChip(category == group.name, { category = group.name }, label = { Text(group.name) }) }
                        }
                        val group = groups.first { it.name == category }
                        group.choices.chunked(3).forEach { row ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { value ->
                                    val selected = value in log.selections
                                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                        FilledIconToggleButton(checked = selected, onCheckedChange = {
                                            update(log.copy(selections = if (selected) log.selections - value else log.selections + value))
                                        }, modifier = Modifier.size(56.dp).border(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, CircleShape)) {
                                            Icon(if (selected) Icons.Default.Check else group.icon, contentDescription = value)
                                        }
                                        Text(value, style = MaterialTheme.typography.labelMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                        if (category == "Cervical mucus") Text("Mucus observations alone do not confirm ovulation.", style = MaterialTheme.typography.bodySmall)
                    }
                    if (log.selections.isNotEmpty()) item {
                        Text("Your day, gathered", style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            log.selections.forEach { value -> InputChip(selected = true, onClick = { update(log.copy(selections = log.selections - value)) }, label = { Text(value) }, trailingIcon = { Icon(Icons.Default.Close, "Remove $value", Modifier.size(14.dp)) }) }
                        }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(custom, { custom = it.take(60) }, label = { Text("Your own tag") }, modifier = Modifier.weight(1f), singleLine = true)
                            TextButton(onClick = { val tag = custom.trim(); if (tag.isNotEmpty()) { update(log.copy(selections = log.selections + tag)); custom = "" } }, enabled = custom.isNotBlank()) { Text("Add") }
                        }
                    }
                    item { OutlinedTextField(log.notes, { update(log.copy(notes = it)) }, label = { Text("Anything you want to remember") }, minLines = 2, modifier = Modifier.fillMaxWidth()) }
                    item {
                        TextButton(onClick = { details = !details }) { Text(if (details) "Close detailed observations" else "More · ratings, sleep & existing notes") }
                    }
                    if (details) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(log.ratingsRecorded, { update(log.copy(ratingsRecorded = it)) })
                                Text("Include my ratings in trends")
                            }
                            Text("Enable only when these ratings reflect your day.", style = MaterialTheme.typography.bodySmall)
                            listOf("Mood" to log.mood, "Pain" to log.pain, "Energy" to log.energy, "Libido" to log.libido).forEach { (label, value) ->
                                Text("$label · $value / 10")
                                Slider(value.toFloat(), { v -> update(when (label) {
                                    "Mood" -> log.copy(mood = v.roundToInt(), ratingsRecorded = true)
                                    "Pain" -> log.copy(pain = v.roundToInt(), ratingsRecorded = true)
                                    "Energy" -> log.copy(energy = v.roundToInt(), ratingsRecorded = true)
                                    else -> log.copy(libido = v.roundToInt())
                                }) }, valueRange = 0f..10f, steps = 9)
                            }
                        }
                        item {
                            var sleep by rememberSaveable(date.toString()) { mutableStateOf(log.sleepHours.takeIf { it > 0 && it.isFinite() }?.toString() ?: "") }
                            val valid = sleep.isBlank() || sleep.toDoubleOrNull()?.let { it.isFinite() && it in 0.0..24.0 } == true
                            OutlinedTextField(sleep, { v -> sleep = v; v.toDoubleOrNull()?.takeIf { it.isFinite() && it in 0.0..24.0 }?.let { update(log.copy(sleepHours = it)) }; if (v.isBlank()) update(log.copy(sleepHours = 0.0)) }, label = { Text("Sleep hours · 0–24") }, isError = !valid, supportingText = { if (!valid) Text("Use a number from 0 to 24. This invalid value will not be saved.") }, modifier = Modifier.fillMaxWidth())
                        }
                        item {
                            listOf("Symptoms" to log.symptoms, "Cravings" to log.cravings, "Discharge" to log.discharge, "Sex & contraception" to log.sexualActivity, "Tests" to log.testNotes, "Medication notes" to log.medicationNotes).forEach { (label, value) ->
                                OutlinedTextField(value, { v -> update(when(label) {
                                    "Symptoms" -> log.copy(symptoms = v); "Cravings" -> log.copy(cravings = v)
                                    "Discharge" -> log.copy(discharge = v); "Sex & contraception" -> log.copy(sexualActivity = v)
                                    "Tests" -> log.copy(testNotes = v); else -> log.copy(medicationNotes = v)
                                }) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp))
                            }
                            Text("Medication taken/skipped records live in Sanctuary and appear in your daily summary here.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
            if (!future) Button(onClick = { store.upsertCycle(log); onClose() }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) { Text("Save this day") }
        }
    }
    if (confirmClose) AlertDialog(onDismissRequest = { confirmClose = false }, title = { Text("Keep this moment?") }, text = { Text("Your changes have not been saved yet.") }, confirmButton = { TextButton(onClick = { store.upsertCycle(log); onClose() }) { Text("Save & close") } }, dismissButton = { TextButton(onClick = onClose) { Text("Discard changes") } })
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CycleSettingsSheet(store: AppStore, onClose: () -> Unit) {
    var length by rememberSaveable { mutableStateOf(store.cyclePreferences.length) }
    var period by rememberSaveable { mutableStateOf(store.cyclePreferences.periodLength) }
    var luteal by rememberSaveable { mutableStateOf(store.cyclePreferences.lutealLength) }
    var pms by rememberSaveable { mutableStateOf(store.cyclePreferences.pmsDays) }
    var mode by rememberSaveable { mutableStateOf(store.cyclePreferences.mode) }
    var contraception by rememberSaveable { mutableStateOf(store.cyclePreferences.contraception) }
    var history by rememberSaveable { mutableStateOf(store.cyclePreferences.useHistory) }
    ModalBottomSheet(onDismissRequest = onClose, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LazyColumn(Modifier.padding(horizontal = 20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("Your rhythm, your settings", style = MaterialTheme.typography.headlineSmall) }
            item {
                Text("Mode")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Cycle", "Trying to conceive", "Pregnancy").forEach { FilterChip(mode == it, { mode = it }, label = { Text(it) }) } }
                Text("Pregnancy pauses predictions. Trying to conceive uses the same uncertain fertility estimates; it does not confirm ovulation.", style = MaterialTheme.typography.bodySmall)
            }
            item {
                Text("Contraception context")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("None", "Barrier", "Hormonal", "Copper IUD", "Other").forEach { FilterChip(contraception == it, { contraception = it }, label = { Text(it) }) } }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) { Switch(history, { history = it }); Text("Learn length from recorded periods", modifier = Modifier.padding(start = 8.dp)) }
                Text("Missing bleeding days within 10 days are grouped into one episode. Review sparse logs before relying on an estimate.", style = MaterialTheme.typography.bodySmall)
            }
            item { DayStepper("Usual cycle length", length, 15..90) { length = it } }
            item { DayStepper("Usual period length", period, 1..10) { period = it } }
            item { DayStepper("Assumed luteal length", luteal, 10..18) { luteal = it } }
            item { DayStepper("Premenstrual reminder window", pms, 1..10) { pms = it } }
            item {
                Text("These assumptions guide estimates, not medical conclusions. No health data leaves your device for these calculations.", style = MaterialTheme.typography.bodySmall)
                Button(onClick = { store.updateCyclePreferences(CyclePreferences(length, period, luteal, pms, mode, contraception, history)); onClose() }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) { Text("Save settings") }
            }
        }
    }
}

@Composable
private fun DayStepper(label: String, value: Int, range: IntRange, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(label); Text("$value days", color = MaterialTheme.colorScheme.primary) }
        IconButton(onClick = { onChange(value - 1) }, enabled = value > range.first) { Icon(Icons.Default.Remove, "Decrease $label") }
        IconButton(onClick = { onChange(value + 1) }, enabled = value < range.last) { Icon(Icons.Default.Add, "Increase $label") }
    }
}
