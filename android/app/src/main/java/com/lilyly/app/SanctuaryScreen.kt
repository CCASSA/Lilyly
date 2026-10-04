package com.lilyly.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Composable
fun SanctuaryScreen(store: AppStore) {
    var tab by remember { mutableStateOf("Check-in") }
    Column(Modifier.padding(horizontal = 16.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Check-in", "Profile", "Medication", "Therapy", "Safety")) { item ->
                FilterChip(selected = tab == item, onClick = { tab = item }, label = { Text(item) })
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.weight(1f)) {
            when (tab) {
                "Check-in" -> MentalCheckInTab(store)
                "Profile" -> MentalProfileTab(store)
                "Medication" -> MedicationTab(store)
                "Therapy" -> TherapyTab(store)
                "Safety" -> SafetyTab(store)
            }
        }
    }
}

@Composable
private fun MentalCheckInTab(store: AppStore) {
    var check by remember { mutableStateOf(MentalCheckIn()) }
    var saved by remember { mutableStateOf(false) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f)), shape = RoundedCornerShape(20.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("How are you, really?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("A quick record for you — not a diagnosis, score or judgement.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { MentalSlider("Mood", check.mood) { check = check.copy(mood = it) } }
        item { MentalSlider("Anxiety", check.anxiety) { check = check.copy(anxiety = it) } }
        item { MentalSlider("Energy", check.energy) { check = check.copy(energy = it) } }
        item { MentalSlider("Irritability", check.irritability) { check = check.copy(irritability = it) } }
        item { MentalSlider("Emptiness", check.emptiness) { check = check.copy(emptiness = it) } }
        item { MentalSlider("Dissociation", check.dissociation) { check = check.copy(dissociation = it) } }
        item { MentalSlider("Intrusive thoughts", check.intrusiveThoughts) { check = check.copy(intrusiveThoughts = it) } }
        item { MentalSlider("Self-harm urge", check.selfHarmUrge) { check = check.copy(selfHarmUrge = it) } }
        item { MentalSlider("Suicidal thoughts", check.suicidalThoughts) { check = check.copy(suicidalThoughts = it) } }
        item { MentalSlider("Appetite", check.appetite) { check = check.copy(appetite = it) } }
        item { MentalSlider("Social connection", check.connection) { check = check.copy(connection = it) } }
        item { MentalSlider("Sensory overload", check.sensoryOverload) { check = check.copy(sensoryOverload = it) } }
        item {
            OutlinedTextField(
                value = check.sleepHours.takeIf { it > 0 }?.toString() ?: "",
                onValueChange = { check = check.copy(sleepHours = it.toDoubleOrNull() ?: 0.0) },
                label = { Text("Sleep hours") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(check.notes, { check = check.copy(notes = it) }, label = { Text("What happened / what do you need to remember?") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        }
        if (check.selfHarmUrge >= 6 || check.suicidalThoughts >= 6) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Your log says things are intense right now.", fontWeight = FontWeight.Bold)
                        Text("Use your safety plan, contact someone you trust or your clinician. If you may act on these thoughts now, contact local emergency services or go somewhere you can be with another person.")
                    }
                }
            }
        }
        item {
            Button(
                onClick = {
                    store.addMentalCheckIn(check.copy(dateTime = LocalDateTime.now().toString()))
                    saved = true
                    check = MentalCheckIn()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (saved) "Saved ✓" else "Save check-in") }
        }
        item {
            Text("Recent check-ins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        items(store.mentalCheckIns.take(12)) { item ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(15.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(item.dateTime.take(16).replace("T", "  "), fontWeight = FontWeight.SemiBold)
                    Text("Mood ${item.mood}/10 • Anxiety ${item.anxiety}/10 • Energy ${item.energy}/10", style = MaterialTheme.typography.bodySmall)
                    if (item.selfHarmUrge > 0 || item.suicidalThoughts > 0) Text("Urges ${item.selfHarmUrge}/10 • Suicidal thoughts ${item.suicidalThoughts}/10", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun MentalProfileTab(store: AppStore) {
    var profile by remember(store.mentalProfile) { mutableStateOf(store.mentalProfile) }
    val conditionIdeas = listOf("ADHD", "Autism spectrum", "Anxiety disorders", "Depressive disorders", "OCD", "PTSD / CPTSD", "Borderline personality disorder", "Bipolar disorders", "Eating disorders", "PMDD", "Other")
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("My mental-health profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Keep professionally diagnosed conditions separate from things you are exploring. Lilyly will organize information, not diagnose you.", style = MaterialTheme.typography.bodySmall)
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Common categories you may want to record", fontWeight = FontWeight.SemiBold)
                    Text(conditionIdeas.joinToString(" • "), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            OutlinedTextField(
                profile,
                { profile = it },
                label = { Text("Diagnosed / being assessed / suspected / past") },
                placeholder = { Text("Example:\nProfessionally diagnosed — …\nExploring — …\nPast/remission — …") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 8
            )
        }
        item {
            Button(onClick = { store.updateMentalProfile(profile) }, modifier = Modifier.fillMaxWidth()) { Text("Save profile") }
        }
        item {
            Text("Evidence-based condition explainers and a selectable diagnosis library are planned for the next build.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .65f))
        }
    }
}

@Composable
private fun MedicationTab(store: AppStore) {
    var name by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("08:00") }
    var reason by remember { mutableStateOf("") }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Medication cabinet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Record the schedule you were prescribed. Lilyly does not change doses or tell you how much to take.", style = MaterialTheme.typography.bodySmall)
        }
        item {
            OutlinedTextField(name, { name = it }, label = { Text("Medication") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(dose, { dose = it }, label = { Text("Prescribed dose") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(time, { time = it }, label = { Text("Reminder time") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(reason, { reason = it }, label = { Text("Reason / notes") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        store.addMedication(Medication(name = name.trim(), dose = dose.trim(), time = time.trim(), reason = reason.trim()))
                        name = ""; dose = ""; reason = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add medication") }
        }
        item {
            Text("Device notification alarms are the next step; the schedule and medication log already live here.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        items(store.medications) { med ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("${med.name}  ${med.dose}", fontWeight = FontWeight.Bold)
                    Text("${med.time}${if (med.reason.isNotBlank()) " • ${med.reason}" else ""}", style = MaterialTheme.typography.bodySmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("Taken", "Late", "Skipped", "PRN")) { status ->
                            TextButton(onClick = { store.addMedicationLog(MedicationLog(medicationId = med.id, status = status)) }) { Text(status) }
                        }
                    }
                }
            }
        }
        item {
            if (store.medicationLogs.isNotEmpty()) {
                Text("Recent medication log", fontWeight = FontWeight.SemiBold)
                store.medicationLogs.take(10).forEach { log ->
                    val med = store.medications.firstOrNull { it.id == log.medicationId }
                    Text("${log.dateTime.take(16).replace("T", " ")} • ${med?.name ?: "Medication"} • ${log.status}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(35.dp))
        }
    }
}

@Composable
private fun TherapyTab(store: AppStore) {
    var note by remember { mutableStateOf(TherapyNote()) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Therapy journal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(note.title, { note = note.copy(title = it) }, label = { Text("Session title") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(note.before, { note = note.copy(before = it) }, label = { Text("Before session — what do I need to bring up?") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(note.after, { note = note.copy(after = it) }, label = { Text("After session — what mattered?") }, modifier = Modifier.fillMaxWidth(), minLines = 4)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(note.homework, { note = note.copy(homework = it) }, label = { Text("Homework / skill to try") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(note.nextAppointment, { note = note.copy(nextAppointment = it) }, label = { Text("Next appointment") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = { store.addTherapyNote(note.copy(date = LocalDate.now().toString())); note = TherapyNote() }, modifier = Modifier.fillMaxWidth()) { Text("Save therapy note") }
        }
        items(store.therapyNotes.take(15)) { item ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("${item.date} • ${item.title}", fontWeight = FontWeight.SemiBold)
                    if (item.after.isNotBlank()) Text(item.after.take(180), style = MaterialTheme.typography.bodySmall)
                    if (item.homework.isNotBlank()) Text("Homework: ${item.homework}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item { Spacer(Modifier.height(30.dp)) }
    }
}

@Composable
private fun SafetyTab(store: AppStore) {
    var plan by remember(store.safetyPlan) { mutableStateOf(store.safetyPlan) }
    var incident by remember { mutableStateOf(IncidentLog()) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = .12f)), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("My safety plan", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text("Write this while things are relatively calm: warning signs, grounding steps, safe places, people to contact, clinician details, reasons to get through tonight, and what to put distance between you and when you are unsafe.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            OutlinedTextField(plan, { plan = it }, label = { Text("Safety plan") }, modifier = Modifier.fillMaxWidth(), minLines = 9)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { store.updateSafetyPlan(plan) }, modifier = Modifier.fillMaxWidth()) { Text("Save safety plan") }
        }
        item {
            Text("Incident record", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("For pattern recognition and care — never streaks, badges or injury rankings.", style = MaterialTheme.typography.bodySmall)
        }
        item {
            OutlinedTextField(incident.trigger, { incident = incident.copy(trigger = it) }, label = { Text("What happened before?") }, modifier = Modifier.fillMaxWidth())
        }
        item { MentalSlider("Urge before", incident.urgeBefore) { incident = incident.copy(urgeBefore = it) } }
        item {
            OutlinedTextField(incident.emotions, { incident = incident.copy(emotions = it) }, label = { Text("Emotions / state") }, modifier = Modifier.fillMaxWidth())
            CheckRowMental("An injury occurred", incident.injuryOccurred) { incident = incident.copy(injuryOccurred = it) }
            if (incident.injuryOccurred) {
                OutlinedTextField(incident.bodyArea, { incident = incident.copy(bodyArea = it) }, label = { Text("Body area") }, modifier = Modifier.fillMaxWidth())
                CheckRowMental("Medical attention may be needed", incident.medicalAttention) { incident = incident.copy(medicalAttention = it) }
                CheckRowMental("Medical care received", incident.medicalCareReceived) { incident = incident.copy(medicalCareReceived = it) }
            }
        }
        item {
            OutlinedTextField(incident.whatHelped, { incident = incident.copy(whatHelped = it) }, label = { Text("What helped afterward?") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(incident.contacted, { incident = incident.copy(contacted = it) }, label = { Text("Who did I contact?") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(incident.nextTime, { incident = incident.copy(nextTime = it) }, label = { Text("What might help next time?") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Button(onClick = { store.addIncident(incident.copy(dateTime = LocalDateTime.now().toString())); incident = IncidentLog() }, modifier = Modifier.fillMaxWidth()) { Text("Save incident record") }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Clinical wound photo attachments", fontWeight = FontWeight.SemiBold)
                    Text("Planned as hidden/private attachments for medical documentation only — never a visible injury gallery or automatic memory resurfacing.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        items(store.incidents.take(8)) { item ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(15.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(item.dateTime.take(16).replace("T", "  "), fontWeight = FontWeight.SemiBold)
                    Text("Urge ${item.urgeBefore}/10${if (item.injuryOccurred) " • injury recorded" else ""}${if (item.medicalAttention) " • medical attention flagged" else ""}", style = MaterialTheme.typography.bodySmall)
                    if (item.trigger.isNotBlank()) Text("Before: ${item.trigger}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { Spacer(Modifier.height(35.dp)) }
    }
}

@Composable
private fun MentalSlider(label: String, value: Int, onChange: (Int) -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.SemiBold)
            Text("$value / 10")
        }
        Slider(value = value.toFloat(), onValueChange = { onChange(it.roundToInt()) }, valueRange = 0f..10f, steps = 9)
    }
}

@Composable
private fun CheckRowMental(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.padding(top = 12.dp))
        Checkbox(checked, onChange)
    }
}
