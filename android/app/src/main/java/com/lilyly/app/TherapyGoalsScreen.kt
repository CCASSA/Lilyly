package com.lilyly.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.json.JSONObject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TherapyGoalsScreen(store:AppStore,onBack:()->Unit,onSession:(TherapyNote)->Unit,onJournal:(JournalEntry)->Unit) {
    var selectedId by rememberSaveable {mutableStateOf<String?>(null)}
    var draft by rememberSaveable {mutableStateOf<String?>(null)}
    var reviewOpen by rememberSaveable {mutableStateOf(false)}
    var removeGoal by rememberSaveable {mutableStateOf(false)}
    var removeReflection by rememberSaveable {mutableStateOf<String?>(null)}
    var filter by rememberSaveable {mutableStateOf("All")}
    val goal=store.therapyGoals.firstOrNull {it.id==selectedId}
    val back={if(selectedId!=null)selectedId=null else onBack()}
    BackHandler(onBack=back)
    LazyColumn(Modifier.fillMaxSize().testTag("therapy-goals"),contentPadding=PaddingValues(bottom=28.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
        item {TextButton(onClick=back){Text(if(goal==null) "Back to sessions" else "Back to my goals")}}
        if(goal==null) {
            item {
                Text("My growing things",style=MaterialTheme.typography.headlineMedium)
                Text("Things you want to nurture, at your own pace. Resting is allowed; progress doesn't need a score.")
                Button(onClick={draft=TherapyGoal().toJson().toString()}) {Text("Plant a goal")}
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {(listOf("All")+therapyGoalStates).forEach {state ->FilterChip(filter==state,{filter=state},label={Text(state)})}}
            }
            val rows=store.therapyGoals.filter {filter=="All" || it.state==filter}
            if(rows.isEmpty())item {BotanicalDivider();Text("Room for what matters to you",style=MaterialTheme.typography.titleLarge);Text(if(store.therapyGoals.isEmpty()) "Start with one small intention. You can connect it to your sessions and revisit it whenever you choose." else "No goals in this view. Try another state.")}
            items(rows,key={it.id}) {row ->Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                BotanicalDivider();Text(row.title,style=MaterialTheme.typography.titleLarge)
                Text(row.state,color=MaterialTheme.colorScheme.primary)
                if(row.intention.isNotBlank())Text(row.intention,maxLines=3)
                Text("${row.reflections.size} reflections · ${store.therapyNotes.count {row.id in it.goalIds}} linked sessions",style=MaterialTheme.typography.bodySmall)
                TextButton(onClick={selectedId=row.id}) {Text("Open goal")}
            }}
        } else {
            item {
                Text(goal.title,style=MaterialTheme.typography.headlineMedium)
                Text(goal.state,color=MaterialTheme.colorScheme.primary)
                if(goal.intention.isNotBlank())Text(goal.intention)
                FlowRow {Button(onClick={reviewOpen=true}) {Text("Pause & reflect")};TextButton(onClick={draft=goal.toJson().toString()}) {Text("Edit intention")}}
                TextButton(onClick={onJournal(store.journalTherapyGoal(goal))}) {Text(if(store.journalEntries.any {it.id==goal.journalId}) "Open goal journal page" else "Keep a goal journal page")}
                Text("Journal pages are independent copies. Later reflections won't overwrite your page.",style=MaterialTheme.typography.bodySmall)
            }
            item {BotanicalDivider();Text("Along the way",style=MaterialTheme.typography.titleLarge)}
            if(goal.reflections.isEmpty())item {Text("Your reflections will gather here. Record what changed, what helped, or what needs time.")}
            items(goal.reflections.reversed(),key={it.id}) {row ->Column(verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Text("${row.recordedAt.take(10)} · ${row.state}",style=MaterialTheme.typography.labelLarge)
                if(row.body.isNotBlank())Text(row.body)
                TextButton(onClick={removeReflection=row.id}) {Text("Remove reflection")}
            }}
            item {BotanicalDivider();Text("Connected sessions",style=MaterialTheme.typography.titleLarge)}
            val sessions=store.therapyNotes.filter {goal.id in it.goalIds}.sortedByDescending {it.date}
            if(sessions.isEmpty())item {Text("In a therapy session, choose Connect goals to link this intention.")}
            items(sessions,key={it.id}) {note ->TextButton(onClick={onSession(note)}) {Text("${note.date} · ${note.title}")}}
            item {TextButton(onClick={removeGoal=true}) {Text("Remove goal")}}
        }
    }
    draft?.let {raw ->
        val o=JSONObject(raw)
        TherapyGoalEditor(TherapyGoal(id=o.getString("id"),title=o.optString("title"),intention=o.optString("intention")),onCancel={draft=null},onSave={value ->
            val current=store.therapyGoals.firstOrNull {it.id==value.id} ?: value
            store.saveTherapyGoal(current.copy(title=value.title,intention=value.intention))
            selectedId=value.id;draft=null
        })
    }
    if(reviewOpen && goal!=null) GoalReflectionEditor(goal,onCancel={reviewOpen=false},onSave={row ->
        store.therapyGoals.firstOrNull {it.id==goal.id}?.let {store.saveTherapyGoal(keepGoalReflection(it,row))}
        reviewOpen=false
    })
    if(removeGoal && goal!=null)AlertDialog(onDismissRequest={removeGoal=false},title={Text("Remove this goal?")},text={Text("This removes the goal, its reflections and its session links. Your session notes and journal pages stay. This cannot be undone.")},confirmButton={TextButton(onClick={store.deleteTherapyGoal(goal.id);selectedId=null;removeGoal=false}){Text("Remove goal and reflections")}},dismissButton={TextButton(onClick={removeGoal=false}){Text("Keep goal")}})
    removeReflection?.let {id ->if(goal!=null)AlertDialog(onDismissRequest={removeReflection=null},title={Text("Remove this reflection?")},text={Text("The goal will return to the state of its latest remaining reflection, or Growing if none remain. Your journal copies stay.")},confirmButton={TextButton(onClick={store.saveTherapyGoal(removeGoalReflection(goal,id));removeReflection=null}){Text("Remove recorded reflection")}},dismissButton={TextButton(onClick={removeReflection=null}){Text("Keep reflection")}})}
}

@Composable
private fun TherapyGoalEditor(source:TherapyGoal,onCancel:()->Unit,onSave:(TherapyGoal)->Unit) {
    var title by rememberSaveable(source.id) {mutableStateOf(source.title)}
    var intention by rememberSaveable(source.id) {mutableStateOf(source.intention)}
    var discard by rememberSaveable {mutableStateOf(false)}
    val dirty=title!=source.title || intention!=source.intention
    val close={if(dirty)discard=true else onCancel()}
    val keyboard=LocalSoftwareKeyboardController.current;val focus=LocalFocusManager.current
    val valid=title.trim().isNotBlank() && title.trim().length<=160 && intention.length<=4000
    AlertDialog(onDismissRequest=close,title={Text("Something to nurture")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(title,{title=it},label={Text("Goal title")},singleLine=true)
        OutlinedTextField(intention,{intention=it},label={Text("Why this matters to me")},minLines=3,keyboardOptions=KeyboardOptions(imeAction=ImeAction.Done),keyboardActions=KeyboardActions(onDone={keyboard?.hide();focus.clearFocus()}))
        Text("A title can hold up to 160 characters; your intention up to 4,000.",style=MaterialTheme.typography.bodySmall)
    }},confirmButton={TextButton(enabled=valid,onClick={keyboard?.hide();focus.clearFocus();onSave(source.copy(title=title.trim(),intention=intention))}){Text("Keep intention")}},dismissButton={TextButton(onClick=close){Text("Cancel")}})
    if(discard)DiscardGoalChanges(onCancel={discard=false},onDiscard=onCancel)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalReflectionEditor(goal:TherapyGoal,onCancel:()->Unit,onSave:(GoalReflection)->Unit) {
    var body by rememberSaveable(goal.id) {mutableStateOf("")}
    var state by rememberSaveable(goal.id) {mutableStateOf(goal.state)}
    var discard by rememberSaveable {mutableStateOf(false)}
    val dirty=body.isNotBlank() || state!=goal.state
    val close={if(dirty)discard=true else onCancel()}
    val keyboard=LocalSoftwareKeyboardController.current;val focus=LocalFocusManager.current
    AlertDialog(onDismissRequest=close,title={Text("Where am I with this?")},text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {therapyGoalStates.forEach {value ->FilterChip(state==value,{state=value},label={Text(value)})}}
        OutlinedTextField(body,{body=it},label={Text("What I want to remember")},minLines=3,keyboardOptions=KeyboardOptions(imeAction=ImeAction.Done),keyboardActions=KeyboardActions(onDone={keyboard?.hide();focus.clearFocus()}))
        Text("Your state is your own description, not a clinical assessment. Add a reflection or choose a different state to save today's entry.",style=MaterialTheme.typography.bodySmall)
        if(body.length>8000)Text("Please keep this reflection within 8,000 characters.",color=MaterialTheme.colorScheme.error)
    }},confirmButton={TextButton(enabled=dirty && body.length<=8000,onClick={keyboard?.hide();focus.clearFocus();onSave(GoalReflection(state=state,body=body))}){Text("Keep reflection")}},dismissButton={TextButton(onClick=close){Text("Cancel")}})
    if(discard)DiscardGoalChanges(onCancel={discard=false},onDiscard=onCancel)
}
@Composable
private fun DiscardGoalChanges(onCancel:()->Unit,onDiscard:()->Unit) {
    AlertDialog(onDismissRequest=onCancel,title={Text("Leave these changes?")},text={Text("Your unsaved words will be discarded.")},confirmButton={TextButton(onClick=onDiscard){Text("Discard changes")}},dismissButton={TextButton(onClick=onCancel){Text("Keep writing")}})
}
