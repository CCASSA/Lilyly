package com.lilyly.app

import android.app.DatePickerDialog
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
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(store:AppStore,onBack:()->Unit,onPage:(JournalEntry)->Unit,onSpace:(String,String)->Unit) {
    var dateText by rememberSaveable {mutableStateOf(LocalDate.now().toString())}
    var choosing by rememberSaveable {mutableStateOf(false)}
    val date=LocalDate.parse(dateText)
    val context=LocalContext.current
    val threads=dayThreads(store.cycleLogs,store.mentalCheckIns,store.sleepRecords,store.medications,store.medicationLogs,store.journalEntries,store.therapyNotes,store.tarotReadings)
        .filter {it.category in store.daySections}
    val entries=threads.filter {it.date==dateText}
    val dates=threads.map {it.date}.distinct().sortedDescending()
    Scaffold(topBar={TopAppBar(title={Text("Threads of a day")},navigationIcon={IconButton(onClick=onBack) {Icon(Icons.Default.ArrowBack,"Back")}})}) {padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=20.dp).testTag("day-threads"),contentPadding=PaddingValues(bottom=28.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
            item {
                Text("☾  ·  ❧",color=MaterialTheme.colorScheme.primary,style=MaterialTheme.typography.headlineLarge)
                Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),style=MaterialTheme.typography.headlineMedium)
                Text("${moonPhaseName(date.atTime(12,0))} · approximate",color=MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick={dateText=date.minusDays(1).toString()}) {Text("Previous day")}
                    TextButton(onClick={DatePickerDialog(context,{_,y,m,d -> dateText=LocalDate.of(y,m+1,d).toString()},date.year,date.monthValue-1,date.dayOfMonth).show()}) {Text("Choose date")}
                    TextButton(onClick={dateText=date.plusDays(1).toString()}) {Text("Next day")}
                }
                Row {TextButton(onClick={dateText=LocalDate.now().toString()}) {Text("Today")};TextButton(onClick={choosing=true}) {Text("Choose threads")}}
                Text("Your recorded moments, together. Missing records stay missing; they do not mean nothing happened.",style=MaterialTheme.typography.bodySmall)
            }
            if(dates.isNotEmpty()) item {
                Text("Days with saved threads",style=MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {items(dates.take(60)) {day -> FilterChip(day==dateText,{dateText=day},label={Text(day)})}}
            }
            if(entries.isEmpty()) item {Text(if(store.daySections.isEmpty()) "All threads are hidden. Choose the ones you want to see." else "No saved threads for this day in your selected spaces.",style=MaterialTheme.typography.headlineSmall)}
            dayThreadCategories.forEach {category ->
                val group=entries.filter {it.category==category}
                if(group.isNotEmpty()) {
                    item {Text(category,style=MaterialTheme.typography.headlineSmall,color=MaterialTheme.colorScheme.primary)}
                    items(group,key={it.key}) {entry ->
                        Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            Text(entry.title,style=MaterialTheme.typography.titleLarge)
                            Text(entry.detail,style=MaterialTheme.typography.bodyMedium)
                            val page=store.journalEntries.firstOrNull {it.id==entry.pageId}
                            if(page!=null) TextButton(onClick={onPage(page)}) {Text("Open page · ${page.title.ifBlank {"Untitled"}}")}
                            if(category!="Pages") TextButton(onClick={onSpace(category,dateText)}) {Text("Visit $category")}
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
    if(choosing) AlertDialog(onDismissRequest={choosing=false},title={Text("The threads you want to see")},text={Column {
        Text("This only changes this view. Your saved records stay in their own spaces.",style=MaterialTheme.typography.bodySmall)
        dayThreadCategories.forEach {category -> Row {
            Checkbox(category in store.daySections,{enabled ->store.updateDaySections(if(enabled)store.daySections+category else store.daySections-category)})
            Text(category,Modifier.padding(top=12.dp))
        }}
    }},confirmButton={TextButton(onClick={choosing=false}) {Text("Done")}})
}
