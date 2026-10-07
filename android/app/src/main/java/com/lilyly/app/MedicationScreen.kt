package com.lilyly.app

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicationTab(store:AppStore) {
    val context=LocalContext.current
    var editing by rememberSaveable {mutableStateOf<String?>(null)}
    var entry by rememberSaveable {mutableStateOf<String?>(null)}
    var day by rememberSaveable {mutableStateOf(LocalDate.now().toString())}
    var history by rememberSaveable {mutableStateOf(false)}
    var archived by rememberSaveable {mutableStateOf(false)}
    var message by remember {mutableStateOf("")}
    var delete by remember {mutableStateOf<MedicationLog?>(null)}
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {granted ->
        message=if(granted) "Notifications allowed. Turn on reminders for each daily medication you want to hear about." else "Notifications are off. You can allow them in Android notification settings."
        MedicationReminders.schedule(context,store.medications,store.medicationLogs)
    }
    LazyColumn(Modifier.testTag("medication-cabinet"),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(bottom=30.dp)) {
        item {
            Text("Your apothecary",style=MaterialTheme.typography.headlineMedium)
            Text("A quiet place to keep the schedule you were prescribed. Lilyly never changes your dose.",style=MaterialTheme.typography.bodyMedium)
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                FilterChip(!history,{history=false},label={Text("Daily care")})
                FilterChip(history,{history=true},label={Text("History")})
                TextButton(onClick={editing=Medication().toJson().toString()}) {Text("Add medication")}
            }
        }
        if(!history) {
            item {
                TextButton(onClick={val d=LocalDate.parse(day);DatePickerDialog(context,{_,y,m,n -> day=LocalDate.of(y,m+1,n).toString()},d.year,d.monthValue-1,d.dayOfMonth).apply {datePicker.maxDate=System.currentTimeMillis()}.show()}) {Text("Care for $day")}
                Text("Tap a status to record it, or reopen it to correct a log. Blank means unrecorded.",style=MaterialTheme.typography.bodySmall)
            }
            items(store.medications.filter {it.active || archived},key={it.id}) {med ->
                Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {Text(med.name,style=MaterialTheme.typography.titleLarge);Text(med.dose,style=MaterialTheme.typography.bodyMedium)}
                        TextButton(onClick={editing=med.toJson().toString()}) {Text("Edit")}
                    }
                    if(!med.active) Text("Archived · history kept")
                    else if(med.asNeeded) TextButton(onClick={entry=MedicationLog(medicationId=med.id,dateTime=LocalDate.parse(day).atTime(java.time.LocalTime.now()).toString(),status="PRN").toJson().toString()}) {Text("Record as needed")}
                    else medicationTimes(med.time).forEach {time ->
                        val at=LocalDate.parse(day).atTime(time).toString()
                        val log=store.medicationLogs.firstOrNull {it.medicationId==med.id && it.scheduledFor==at}
                        TextButton(onClick={entry=(log ?: MedicationLog(medicationId=med.id,scheduledFor=at,dateTime=LocalDate.parse(day).atTime(java.time.LocalTime.now()).toString())).toJson().toString()}) {Text("$time · ${log?.status ?: "Not recorded"}")}
                    }
                    if(med.active && !med.asNeeded && medicationTimes(med.time).isEmpty()) Text("Edit to add a valid daily time.")
                    if(med.remaining>=0) Text("${med.remaining} doses left · manually counted${if(med.remaining<=med.refillAt) " · refill check" else ""}",color=MaterialTheme.colorScheme.primary)
                    if(med.reason.isNotBlank()) Text(med.reason,style=MaterialTheme.typography.bodySmall)
                    if(med.reminders && med.active && !med.asNeeded) Text("Approximate daily reminders requested",style=MaterialTheme.typography.bodySmall)
                    HorizontalDivider()
                }
            }
            item {
                if(store.medications.isEmpty()) Text("Your cabinet is waiting. Add a medication to keep its schedule, notes and history together.")
                TextButton(onClick={archived=!archived}) {Text(if(archived) "Hide archived medications" else "Show archived medications")}
                Text("Reminders",style=MaterialTheme.typography.titleLarge)
                Text("Android may delay notifications, especially with battery restrictions. They are a prompt to review your schedule, not a precise alarm. Names and doses stay out of the notification.",style=MaterialTheme.typography.bodySmall)
                TextButton(onClick={if(Build.VERSION.SDK_INT>=33)permission.launch(Manifest.permission.POST_NOTIFICATIONS) else message=if(MedicationReminders.enabled(context)) "Notifications are allowed." else "Enable Lilyly in Android notification settings."}) {Text("Allow notifications")}
                TextButton(onClick={context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,context.packageName))}) {Text("Android notification settings")}
                TextButton(onClick={if(MedicationReminders.enabled(context)) {MedicationReminders.notify(context);message="Test notification requested; check your notification shade."} else message="Notifications are disabled in Android settings."}) {Text("Send a test reminder")}
                if(message.isNotBlank())Text(message)
            }
        } else {
            if(store.medicationLogs.isEmpty()) item {Text("Your care history will grow here as you record it.")}
            items(store.medicationLogs.sortedByDescending {it.dateTime},key={it.id}) {log ->
                Column {
                    Text(store.medications.firstOrNull {it.id==log.medicationId}?.name ?: "Medication",style=MaterialTheme.typography.titleMedium)
                    Text("${log.dateTime.take(16).replace('T',' ')} · ${log.status}")
                    if(log.scheduledFor.isNotBlank())Text("Scheduled ${log.scheduledFor.replace('T',' ')}",style=MaterialTheme.typography.bodySmall)
                    if(log.notes.isNotBlank())Text(log.notes)
                    Row {TextButton(onClick={entry=log.toJson().toString()}) {Text("Correct entry")};TextButton(onClick={delete=log}) {Text("Remove entry")}}
                    HorizontalDivider()
                }
            }
        }
    }
    editing?.let {raw ->
        val med=Medication.fromJson(org.json.JSONObject(raw))
        fun change(value:Medication) {editing=value.toJson().toString()}
        var count by rememberSaveable(med.id) {mutableStateOf(if(med.remaining<0) "" else med.remaining.toString())}
        var threshold by rememberSaveable(med.id) {mutableStateOf(med.refillAt.toString())}
        AlertDialog(onDismissRequest={editing=null},title={Text("Your medication")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(med.name,{change(med.copy(name=it))},label={Text("Medication name")})
            OutlinedTextField(med.dose,{change(med.copy(dose=it))},label={Text("Prescribed dose · your words")})
            Row {Checkbox(med.asNeeded,{change(med.copy(asNeeded=it))});Text("As needed (PRN)")}
            if(!med.asNeeded) {
                OutlinedTextField(med.time,{change(med.copy(time=it))},label={Text("Daily times · 08:00, 20:00")},isError=!validMedicationTimes(med.time))
                Row {Checkbox(med.reminders,{change(med.copy(reminders=it))});Text("Request daily reminders")}
            }
            OutlinedTextField(count,{count=it},label={Text("Doses left · optional manual count")})
            OutlinedTextField(threshold,{threshold=it},label={Text("Refill check at this count")})
            Text("Update the count after taking or refilling. Lilyly does not infer quantities from your prescribed dose.",style=MaterialTheme.typography.bodySmall)
            OutlinedTextField(med.reason,{change(med.copy(reason=it))},label={Text("Notes")})
            Row {Checkbox(med.active,{change(med.copy(active=it))});Text("Active · uncheck to archive")}
        }},confirmButton={TextButton(enabled=med.name.isNotBlank() && (med.asNeeded || validMedicationTimes(med.time)) && (count.isBlank() || (count.toIntOrNull() ?: -1) in 0..100000) && (threshold.toIntOrNull() ?: -1) in 0..100000,onClick={store.addMedication(med.copy(name=med.name.trim(),remaining=count.toIntOrNull() ?: -1,refillAt=threshold.toInt()));editing=null}) {Text("Save medication")}},dismissButton={TextButton(onClick={editing=null}) {Text("Cancel")}})
    }
    entry?.let {raw ->
        val log=MedicationLog.fromJson(org.json.JSONObject(raw))
        AlertDialog(onDismissRequest={entry=null},title={Text("Record your care")},text={Column(Modifier.verticalScroll(rememberScrollState())) {
            Text(store.medications.firstOrNull {it.id==log.medicationId}?.name ?: "Medication")
            FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {listOf("Taken","Late","Skipped","PRN").forEach {status -> FilterChip(log.status==status,{entry=log.copy(status=status).toJson().toString()},label={Text(status)})}}
            OutlinedTextField(log.dateTime.take(16),{entry=log.copy(dateTime=it).toJson().toString()},label={Text("Recorded at · YYYY-MM-DDTHH:MM")})
            OutlinedTextField(log.notes,{entry=log.copy(notes=it).toJson().toString()},label={Text("Optional note")})
        }},confirmButton={TextButton(enabled=runCatching {LocalDateTime.parse(log.dateTime)<=LocalDateTime.now()}.getOrDefault(false),onClick={store.addMedicationLog(log);entry=null}) {Text("Save entry")}},dismissButton={TextButton(onClick={entry=null}) {Text("Cancel")}})
    }
    delete?.let {log ->AlertDialog(onDismissRequest={delete=null},title={Text("Remove this entry?")},text={Text("Only this log entry will be removed. Your medication and other history remain.")},confirmButton={TextButton(onClick={store.removeMedicationLog(log.id);delete=null}) {Text("Remove")}},dismissButton={TextButton(onClick={delete=null}) {Text("Cancel")}})}
}
