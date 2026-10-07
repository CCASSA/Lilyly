package com.lilyly.app

import android.app.DatePickerDialog
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
import org.json.JSONObject
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TherapyTab(store:AppStore,onJournal:(JournalEntry)->Unit) {
    val context=LocalContext.current
    var draft by rememberSaveable {mutableStateOf<String?>(null)}
    var section by rememberSaveable {mutableStateOf("Before")}
    var filter by rememberSaveable {mutableStateOf("All sessions")}
    var query by rememberSaveable {mutableStateOf("")}
    val sessions=store.therapyNotes.filter {
        (filter!="Practice to revisit" || (it.homework.isNotBlank() && !it.homeworkDone)) &&
        (query.isBlank() || listOf(it.title,it.date,it.before,it.after,it.homework,it.goals,it.questions).any {text -> text.contains(query,true)})
    }.sortedByDescending {it.date}
    LazyColumn(Modifier.testTag("therapy-room"),verticalArrangement=Arrangement.spacedBy(16.dp),contentPadding=PaddingValues(bottom=28.dp)) {
        item {
            Text("Room to untangle",style=MaterialTheme.typography.headlineMedium)
            Text("Bring the things you don't want to forget. Keep the words that helped.")
            TextButton(onClick={draft=TherapyNote().toJson().toString();section="Before"}) {Text("Prepare a session")}
            BotanicalDivider()
        }
        item {
            OutlinedTextField(query,{query=it},label={Text("Find a thought or session")},modifier=Modifier.fillMaxWidth())
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {listOf("All sessions","Practice to revisit").forEach {label -> FilterChip(filter==label,{filter=label},label={Text(label)})}}
        }
        if(sessions.isEmpty()) item {Text(if(query.isNotBlank()) "No sessions match that thought." else if(filter=="Practice to revisit") "Nothing waiting here. You can add a practice to any session." else "Your sessions will gather here, at your own pace.")}
        items(sessions,key={it.id}) {note ->
            Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(note.date,style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
                Text(note.title,style=MaterialTheme.typography.titleLarge)
                Text(note.after.ifBlank {note.before}.ifBlank {"A place kept for this session."},maxLines=4)
                if(note.goals.isNotBlank())Text("Working toward · ${note.goals}",maxLines=3)
                if(note.homework.isNotBlank()) {
                    Row {
                        Checkbox(note.homeworkDone,{store.addTherapyNote(note.copy(homeworkDone=it))})
                        Column(Modifier.weight(1f)) {Text(if(note.homeworkDone) "Practice explored" else "Practice to revisit",style=MaterialTheme.typography.labelLarge);Text(note.homework,maxLines=4)}
                    }
                }
                if(note.nextAppointment.isNotBlank())Text("Next appointment · ${note.nextAppointment}",style=MaterialTheme.typography.bodySmall)
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick={draft=note.toJson().toString();section="After"}) {Text("Open session")}
                    TextButton(onClick={onJournal(store.journalTherapy(note))}) {Text(if(store.journalEntries.any {it.id==note.journalId}) "Open linked page" else "Make a journal page")}
                }
                HorizontalDivider()
            }
        }
    }
    draft?.let {raw ->
        val note=TherapyNote.fromJson(JSONObject(raw))
        fun change(updated:TherapyNote) {draft=updated.toJson().toString()}
        AlertDialog(onDismissRequest={/* Keep deliberate drafts until Save or Cancel. */},title={Text("A little space for you")},text={
            Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(note.title,{change(note.copy(title=it))},label={Text("Session title")})
                TextButton(onClick={val d=runCatching {LocalDate.parse(note.date)}.getOrDefault(LocalDate.now());DatePickerDialog(context,{_,y,m,day ->change(note.copy(date=LocalDate.of(y,m+1,day).toString()))},d.year,d.monthValue-1,d.dayOfMonth).show()}) {Text("Session date · ${note.date}")}
                FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {listOf("Before","After","Practice").forEach {label ->FilterChip(section==label,{section=label},label={Text(label)})}}
                when(section) {
                    "Before" -> {
                        OutlinedTextField(note.before,{change(note.copy(before=it))},label={Text("What I want to bring")},minLines=3)
                        OutlinedTextField(note.questions,{change(note.copy(questions=it))},label={Text("Questions for my therapist")},minLines=2)
                    }
                    "After" -> {
                        OutlinedTextField(note.after,{change(note.copy(after=it))},label={Text("What mattered / what I learned")},minLines=4)
                        OutlinedTextField(note.goals,{change(note.copy(goals=it))},label={Text("What I'm working toward")},minLines=2)
                    }
                    else -> {
                        OutlinedTextField(note.homework,{change(note.copy(homework=it))},label={Text("Homework or something to try")},minLines=3)
                        Row {Checkbox(note.homeworkDone,{change(note.copy(homeworkDone=it))});Text("I've explored this practice")}
                        TextButton(onClick={val d=runCatching {LocalDate.parse(note.nextAppointment)}.getOrDefault(LocalDate.now());DatePickerDialog(context,{_,y,m,day ->change(note.copy(nextAppointment=LocalDate.of(y,m+1,day).toString()))},d.year,d.monthValue-1,d.dayOfMonth).show()}) {Text(if(note.nextAppointment.isBlank()) "Choose next appointment date" else "Next · ${note.nextAppointment}")}
                        if(note.nextAppointment.isNotBlank())TextButton(onClick={change(note.copy(nextAppointment=""))}) {Text("Clear appointment date")}
                        Text("Appointment dates are notes here, not calendar reminders.",style=MaterialTheme.typography.bodySmall)
                    }
                }
                if(note.journalId.isNotBlank())Text("Your linked journal page is a separate copy. Editing this session won't overwrite personal changes on that page.",style=MaterialTheme.typography.bodySmall)
            }
        },confirmButton={TextButton(enabled=note.title.isNotBlank(),onClick={store.addTherapyNote(note);draft=null}) {Text("Keep session")}},dismissButton={TextButton(onClick={draft=null}) {Text("Cancel")}})
    }
}
