package com.lilyly.app

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RitualScreen(store:AppStore,onBack:()->Unit,onPages:()->Unit,onJournal:(JournalEntry)->Unit) {
    var editing by rememberSaveable {mutableStateOf<String?>(null)}
    var search by rememberSaveable {mutableStateOf("")}
    var filter by rememberSaveable {mutableStateOf("All")}
    var discard by remember {mutableStateOf(false)}
    val context=LocalContext.current
    fun begin(title:String="",details:RitualDetails=RitualDetails()) {editing=JournalEntry(section="Spells",title=title,paper="Botanical",ritualJson=details.toJson()).toJson().toString()}
    val records=store.journalEntries.filter {it.section!="Templates" && it.ritualJson.isNotBlank()}.filter {page ->
        val r=RitualDetails.fromJson(page.ritualJson)
        (filter=="All" || (filter=="Planned" && r.completedDate.isBlank()) || (filter=="Practised" && r.completedDate.isNotBlank())) &&
        (search.isBlank() || "${page.title} ${page.tags} ${ritualSummary(r)}".contains(search,true))
    }.sortedByDescending {it.updatedAt}
    BackHandler {if(editing!=null)discard=true else onBack()}
    Scaffold(topBar={TopAppBar(title={Text("The ritual room")},navigationIcon={IconButton(onClick={if(editing!=null)discard=true else onBack()}) {Icon(Icons.Default.ArrowBack,"Back")}})}) {padding ->
        if(editing==null) LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=20.dp),contentPadding=PaddingValues(bottom=32.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
            item {
                Text("☾  ❧  ✧",style=MaterialTheme.typography.headlineLarge,color=MaterialTheme.colorScheme.primary)
                Text("Small acts, held with intention",style=MaterialTheme.typography.headlineLarge)
                Text("Your own practices, plans and reflections. Let them mean what they mean to you.")
                Button(onClick={begin()}) {Text("Create a ritual")}
                TextButton(onClick=onPages) {Text("Open all spell pages")}
            }
            item {
                Text("A place to begin",style=MaterialTheme.typography.titleLarge)
                Text("Original reflective prompts by Lilyly; no promised outcomes or historical claims.",style=MaterialTheme.typography.bodySmall)
                ritualStarters.forEach {(title,details) -> TextButton(onClick={begin(title,details)}) {Text(title)}}
            }
            item {
                OutlinedTextField(search,{search=it},label={Text("Search rituals")},modifier=Modifier.fillMaxWidth())
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {listOf("All","Planned","Practised").forEach {label -> FilterChip(filter==label,{filter=label},label={Text(label)})}}
                if(records.isEmpty()) Text("Your next intention has room here. Create a ritual or choose a starting prompt.")
            }
            items(records,key={it.id}) {page ->
                val r=RitualDetails.fromJson(page.ritualJson)
                Column(Modifier.fillMaxWidth().clickable {editing=page.toJson().toString()}.padding(vertical=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text(ritualStatus(r),color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.labelLarge)
                    Text(page.title,style=MaterialTheme.typography.headlineSmall)
                    Text(r.intention,maxLines=3)
                    Row {TextButton(onClick={editing=page.toJson().toString()}) {Text("Tend this ritual")};TextButton(onClick={onJournal(page)}) {Text("Open grimoire page")}}
                    HorizontalDivider()
                }
            }
        } else {
            val page=JournalEntry.fromJson(org.json.JSONObject(editing!!))
            val details=RitualDetails.fromJson(page.ritualJson)
            fun change(value:RitualDetails) {editing=page.copy(ritualJson=value.toJson()).toJson().toString()}
            var tab by rememberSaveable(page.id) {mutableStateOf("Intention")}
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=20.dp).testTag("ritual-editor"),contentPadding=PaddingValues(bottom=32.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
                item {
                    OutlinedTextField(page.title,{editing=page.copy(title=it).toJson().toString()},label={Text("Ritual title")},modifier=Modifier.fillMaxWidth())
                    LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {items(listOf("Intention","Gather & practise","Reflection")) {label -> FilterChip(tab==label,{tab=label},label={Text(label)})}}
                }
                item {
                    when(tab) {
                        "Intention" -> Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(details.intention,{change(details.copy(intention=it))},label={Text("What am I making space for?")},modifier=Modifier.fillMaxWidth(),minLines=3)
                            Text(ritualStatus(details))
                            TextButton(onClick={val d=runCatching {LocalDate.parse(details.plannedDate)}.getOrDefault(LocalDate.now());DatePickerDialog(context,{_,y,m,day ->change(details.copy(plannedDate=LocalDate.of(y,m+1,day).toString()))},d.year,d.monthValue-1,d.dayOfMonth).show()}) {Text("Choose planned date")}
                            if(details.plannedDate.isNotBlank()) TextButton(onClick={change(details.copy(plannedDate=""))}) {Text("Clear planned date")}
                            Text("A date in your private record, without a reminder alarm.",style=MaterialTheme.typography.bodySmall)
                        }
                        "Gather & practise" -> Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(details.materials,{change(details.copy(materials=it))},label={Text("What I will gather")},modifier=Modifier.fillMaxWidth(),minLines=2)
                            OutlinedTextField(details.steps,{change(details.copy(steps=it))},label={Text("My practice · one step per line")},modifier=Modifier.fillMaxWidth(),minLines=5)
                        }
                        else -> Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            Text(if(details.completedDate.isBlank()) "Not yet marked as practised" else "Practised ${details.completedDate} · ${details.moon}")
                            TextButton(onClick={val d=runCatching {LocalDate.parse(details.completedDate)}.getOrDefault(LocalDate.now());DatePickerDialog(context,{_,y,m,day ->val date=LocalDate.of(y,m+1,day);change(details.copy(completedDate=date.toString(),moon=moonPhaseName(date.atTime(12,0))+" · approximate"))},d.year,d.monthValue-1,d.dayOfMonth).show()}) {Text("Record practice date")}
                            if(details.completedDate.isNotBlank()) TextButton(onClick={change(details.copy(completedDate="",moon=""))}) {Text("Clear practice date")}
                            OutlinedTextField(details.reflection,{change(details.copy(reflection=it))},label={Text("What I noticed afterward")},modifier=Modifier.fillMaxWidth(),minLines=4)
                        }
                    }
                }
                item {
                    Button(enabled=page.title.isNotBlank(),onClick={store.upsertJournal(page);editing=null}) {Text("Keep ritual")}
                    if(store.journalEntries.any {it.id==page.id}) TextButton(onClick={editing=repeatRitual(page).toJson().toString()}) {Text("Begin a fresh practice from this plan")}
                    TextButton(onClick={discard=true}) {Text("Close without saving")}
                    Text("Saved rituals open as grimoire pages for photographs, handwriting and personal notes. Ritual details stay attached to that same page.",style=MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    if(discard) AlertDialog(onDismissRequest={discard=false},title={Text("Leave this draft?")},text={Text("Unsaved changes will be discarded. Your saved ritual stays as it was.")},confirmButton={TextButton(onClick={editing=null;discard=false}) {Text("Discard draft")}},dismissButton={TextButton(onClick={discard=false}) {Text("Keep editing")}})
}
