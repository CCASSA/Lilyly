package com.lilyly.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(store: AppStore, onBack: () -> Unit, onJournal: (JournalEntry) -> Unit, onDreamPages: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf("Nights") }
    var query by rememberSaveable { mutableStateOf("") }
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    BackHandler(onBack=onBack)
    Scaffold(topBar={TopAppBar(title={Text("Night garden")},navigationIcon={IconButton(onClick=onBack) {Icon(Icons.Default.ArrowBack,"Back")}})},bottomBar={Surface {Button(onClick={creating=true;editing=null},modifier=Modifier.fillMaxWidth().padding(16.dp)) {Text("Remember a night")}}}) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal=20.dp).testTag("night-garden"),verticalArrangement=Arrangement.spacedBy(16.dp),contentPadding=PaddingValues(bottom=28.dp)) {
            item {
                Text("What did the night leave with you?",style=MaterialTheme.typography.headlineMedium)
                Text("Rest, fragments of dreams, a feeling on waking. Keep as much or as little as you wish.")
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {listOf("Nights","Dreams","Patterns").forEach { name -> FilterChip(tab==name,{tab=name},label={Text(name)})}}
            }
            if(tab=="Patterns") {
                item {
                    Text("Threads of rest",style=MaterialTheme.typography.titleLarge)
                    Text("These summaries use your saved sleep records. Turn on Sanctuary comparisons only if you want those ratings included.",style=MaterialTheme.typography.bodySmall)
                    Row { Switch(store.includeMindInSleepPatterns,{store.setSleepMindPatterns(it)});Text("Include Sanctuary anxiety ratings",modifier=Modifier.padding(12.dp)) }
                    val recent=store.sleepRecords.sortedBy { it.wakeTime }.takeLast(14)
                    if(recent.isNotEmpty()) {
                        val color=MaterialTheme.colorScheme.primary
                        Canvas(Modifier.fillMaxWidth().height(100.dp)) {
                            val max=recent.maxOf { it.durationMinutes }.toFloat().coerceAtLeast(480f)
                            val dx=size.width/recent.size
                            recent.forEachIndexed { i,r -> val x=(i+.5f)*dx;drawLine(color,Offset(x,size.height),Offset(x,size.height-size.height*r.durationMinutes/max),strokeWidth=dx*.4f,cap=StrokeCap.Round) }
                        }
                        Text("Last ${recent.size} records · time in bed",style=MaterialTheme.typography.labelSmall)
                        recent.forEach { Text("${it.date} · ${sleepDurationLabel(it.durationMinutes)}",style=MaterialTheme.typography.bodySmall) }
                    }
                }
                val patterns=sleepPatterns(store.sleepRecords,store.mentalCheckIns,store.includeMindInSleepPatterns)
                if(patterns.isEmpty()) item {Text("Patterns need repeated observations. Rest averages appear after 7 recorded dates. Anxiety comparisons require at least 5 matched dates in each duration group; unrecorded ratings are never guessed.")}
                items(patterns) { p -> Column { Text(p.title,style=MaterialTheme.typography.titleLarge);Text(p.detail) } }
                item { Text("Symbols that return",style=MaterialTheme.typography.titleLarge)
                    val symbols=recurringDreamSymbols(store.sleepRecords)
                    if(symbols.isEmpty()) Text("Tag a symbol when you remember it. Repeated tags will gather here; Lilyly does not assign a hidden meaning to them.")
                    symbols.forEach { (symbol,count) -> Text("$symbol · $count dreams") }
                    Text("Associations describe your recorded experience. They do not establish causes or diagnose a condition.",style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=14.dp))
                }
            } else {
                item {OutlinedTextField(query,{query=it},label={Text("Find a dream, symbol, person or place")},modifier=Modifier.fillMaxWidth());if(tab=="Dreams") TextButton(onClick=onDreamPages) {Text("Open dream journal pages")}}
                val records=store.sleepRecords.filter { (tab!="Dreams" || it.dream.isNotBlank()) && (it.dream+it.symbols.joinToString()+it.people+it.places+it.notes+it.date).contains(query,true) }.sortedByDescending { it.wakeTime }
                if(records.isEmpty()) item {Text("An unwritten night. Your remembered sleep and dreams will live here.")}
                items(records,key={it.id}) { r ->
                    Column(Modifier.fillMaxWidth().clickable {editing=r.id;creating=false}.padding(vertical=8.dp)) {
                        Text(r.date,style=MaterialTheme.typography.labelLarge)
                        Text(sleepDurationLabel(r.durationMinutes),style=MaterialTheme.typography.headlineMedium)
                        Text(listOf(r.quality,r.wakingEnergy).filter(String::isNotBlank).joinToString(" · "))
                        if(r.dream.isNotBlank()) Text(r.dream.take(220),style=MaterialTheme.typography.bodyMedium)
                        if(r.symbols.isNotEmpty()) Text(r.symbols.joinToString(" · "),color=MaterialTheme.colorScheme.primary)
                        val feelings=store.mentalCheckIns.filter { it.dateTime.startsWith(r.date) }.flatMap { it.feelings }.distinct()
                        if(feelings.isNotEmpty()) Text("Sanctuary · ${feelings.joinToString(" · ")}",style=MaterialTheme.typography.bodySmall)
                        val pattern=cyclePattern(store.cycleLogs.filter {it.period}.mapNotNull {runCatching {LocalDate.parse(it.date)}.getOrNull()},store.cyclePreferences)
                        val day=cycleDayState(LocalDate.parse(r.date),pattern,store.cyclePreferences)
                        if(day.day!=null) Text("Cycle day ${day.day} · ${day.phase}",style=MaterialTheme.typography.bodySmall)
                        if(r.dream.isNotBlank()) TextButton(onClick={onJournal(store.journalDream(r))}) {Text(if(store.journalEntries.any {it.id==r.journalId}) "Open dream page" else "Make a dream page")}
                        BotanicalDivider()
                    }
                }
            }
        }
    }
    if(creating || editing!=null) {
        val original=store.sleepRecords.firstOrNull {it.id==editing}
        SleepSheet(store,original,onClose={creating=false;editing=null})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepSheet(store: AppStore, original: SleepRecord?, onClose: () -> Unit) {
    val default=remember(original?.id) { val wake=LocalDateTime.now().withSecond(0).withNano(0);original ?: SleepRecord(bedtime=wake.minusHours(8).toString(),wakeTime=wake.toString()) }
    var json by rememberSaveable(default.id) {mutableStateOf(default.toJson().toString())}
    val draft=SleepRecord.fromJson(JSONObject(json))
    fun change(r: SleepRecord) {json=r.toJson().toString()}
    var details by rememberSaveable {mutableStateOf(false)}
    var discard by remember {mutableStateOf(false)}
    var deletion by remember {mutableStateOf(false)}
    var error by remember {mutableStateOf("")}
    var symbols by rememberSaveable(default.id) {mutableStateOf(default.symbols.joinToString(", "))}
    val dirty=json!=default.toJson().toString() || symbols!=default.symbols.joinToString(", ")
    val close={if(dirty) discard=true else onClose()}
    ModalBottomSheet(onDismissRequest=close) {
        Column(Modifier.fillMaxWidth().padding(horizontal=20.dp)) {
            Text("A night to remember",style=MaterialTheme.typography.headlineMedium)
            LazyColumn(Modifier.weight(1f,false).testTag("sleep-editor"),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                item {DateTimeChoice("Settled into bed",draft.bedtime) {change(draft.copy(bedtime=it))};DateTimeChoice("Got up",draft.wakeTime) {change(draft.copy(wakeTime=it))}
                    Text(sleepMinutes(draft.bedtime,draft.wakeTime)?.let {"${sleepDurationLabel(it)} in bed"} ?: "Wake time must follow bedtime, within 24 hours.",color=MaterialTheme.colorScheme.primary)
                    Text("Manual local times; adjust your record if travel or a clock change affected the night.",style=MaterialTheme.typography.bodySmall)
                }
                item {Text("How did your rest feel?");LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {items(listOf("Restful","Light","Broken","Difficult")) {q -> FilterChip(draft.quality==q,{change(draft.copy(quality=if(draft.quality==q) "" else q))},label={Text(q)})}}}
                item {Text("On waking");LazyRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {items(listOf("Refreshed","Steady","Tired","Exhausted")) {e -> FilterChip(draft.wakingEnergy==e,{change(draft.copy(wakingEnergy=if(draft.wakingEnergy==e) "" else e))},label={Text(e)})}}}
                item {OutlinedTextField(draft.dream,{change(draft.copy(dream=it))},label={Text("A dream, a fragment, a feeling…")},minLines=3,modifier=Modifier.fillMaxWidth())
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {FilterChip(draft.nightmare,{change(draft.copy(nightmare=!draft.nightmare))},label={Text("Nightmare")});FilterChip(draft.lucid,{change(draft.copy(lucid=!draft.lucid))},label={Text("Lucid")})}
                    OutlinedTextField(symbols,{symbols=it},label={Text("Symbols & themes, separated by commas")},modifier=Modifier.fillMaxWidth())
                    TextButton(onClick={details=!details}) {Text(if(details) "Less detail" else "More about this night")}
                }
                if(details) item {
                    Row { Text("Interruptions · ${draft.interruptions}",modifier=Modifier.weight(1f));TextButton(onClick={change(draft.copy(interruptions=(draft.interruptions-1).coerceAtLeast(0)))}) {Text("−")};TextButton(onClick={change(draft.copy(interruptions=(draft.interruptions+1).coerceAtMost(99)))}) {Text("+")} }
                    OutlinedTextField(draft.dreamMood,{change(draft.copy(dreamMood=it))},label={Text("Dream mood")},modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(draft.people,{change(draft.copy(people=it))},label={Text("People")},modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(draft.places,{change(draft.copy(places=it))},label={Text("Places")},modifier=Modifier.fillMaxWidth())
                    OutlinedTextField(draft.notes,{change(draft.copy(notes=it))},label={Text("Anything else about your sleep")},modifier=Modifier.fillMaxWidth())
                }
                if(original!=null) item {TextButton(onClick={deletion=true}) {Text("Delete this night",color=MaterialTheme.colorScheme.error)}}
            }
            if(error.isNotBlank()) Text(error,color=MaterialTheme.colorScheme.error)
            Button(onClick={
                val wake=runCatching {LocalDateTime.parse(draft.wakeTime)}.getOrNull()
                val start=runCatching {LocalDateTime.parse(draft.bedtime)}.getOrNull()
                if(sleepMinutes(draft.bedtime,draft.wakeTime)==null) error="Choose a wake time after bedtime, within 24 hours."
                else if(wake!!.isAfter(LocalDateTime.now())) error="Sleep can only be saved after waking."
                else if(store.sleepRecords.any {r -> r.id!=draft.id && start!!<LocalDateTime.parse(r.wakeTime) && wake>LocalDateTime.parse(r.bedtime)}) error="This overlaps a saved sleep record. Edit that night instead."
                else {store.saveSleep(draft.copy(symbols=symbols.split(',').map(String::trim).filter(String::isNotEmpty).toSet()));onClose()}
            },modifier=Modifier.fillMaxWidth().padding(vertical=12.dp)) {Text("Keep this night")}
        }
    }
    if(discard) AlertDialog(onDismissRequest={discard=false},title={Text("Discard this unsaved night?")},confirmButton={TextButton(onClick=onClose) {Text("Discard")}},dismissButton={TextButton(onClick={discard=false}) {Text("Keep writing")}})
    if(deletion) AlertDialog(onDismissRequest={deletion=false},title={Text("Delete this sleep and dream record?")},text={Text("A linked journal page will remain.")},confirmButton={TextButton(onClick={store.deleteSleep(draft.id);onClose()}) {Text("Delete night")}},dismissButton={TextButton(onClick={deletion=false}) {Text("Keep it")}})
}

@Composable
private fun DateTimeChoice(label: String, value: String, onChange: (String)->Unit) {
    val context=LocalContext.current
    val date=LocalDateTime.parse(value)
    Text(label,style=MaterialTheme.typography.labelLarge)
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick={DatePickerDialog(context,{_,y,m,d -> onChange(date.withYear(y).withMonth(1).withDayOfMonth(1).withMonth(m+1).withDayOfMonth(d).toString())},date.year,date.monthValue-1,date.dayOfMonth).show()}) {Text(date.format(DateTimeFormatter.ofPattern("d MMM yyyy")))}
        OutlinedButton(onClick={TimePickerDialog(context,{_,h,m -> onChange(date.withHour(h).withMinute(m).toString())},date.hour,date.minute,true).show()}) {Text(date.format(DateTimeFormatter.ofPattern("HH:mm")))}
    }
}
