package com.lilyly.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(store:AppStore,onBack:()->Unit,onDay:(String)->Unit) {
    androidx.activity.compose.BackHandler(onBack=onBack)
    var choosing by rememberSaveable {mutableStateOf(false)}
    var expanded by rememberSaveable {mutableStateOf("")}
    val report=personalInsights(store.sleepRecords,store.mentalCheckIns,store.cycleLogs,store.insightData,store.insightDays,LocalDate.now())
    val phases=phaseInsights(store.sleepRecords,store.mentalCheckIns,store.cycleLogs,store.insightData,store.insightDays,LocalDate.now(),store.cyclePreferences)
    Scaffold(topBar={TopAppBar(title={Text("Threads over time")},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back")}})}) {padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal=22.dp).testTag("insights"),verticalArrangement=Arrangement.spacedBy(20.dp),contentPadding=PaddingValues(bottom=32.dp)) {
            item {
                Text("☾  ·  ❧",style=MaterialTheme.typography.displaySmall,color=MaterialTheme.colorScheme.primary)
                Text("Get to know your own rhythms",style=MaterialTheme.typography.headlineLarge)
                Text("Small observations, held gently. Choose which parts of your world can meet here.")
                TextButton(onClick={choosing=true}) {Text("Choose insight data")}
                LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {items(listOf(30,90,365)) {days -> FilterChip(store.insightDays==days,{store.updateInsightPreferences(days=days)},label={Text("$days days")})}}
                Text("${report.dates} recorded dates in this window",style=MaterialTheme.typography.labelLarge)
            }
            if(store.insightData.isEmpty()) item {Text("Your choice comes first",style=MaterialTheme.typography.titleLarge);Text("No data is included yet. Choose insight data to begin. Your selections stay on this device and are included in your Lilyly backup.")}
            else {
                item {
                    Text("Included · ${insightSources.filter {it in store.insightData}.joinToString(" · ")}",style=MaterialTheme.typography.labelLarge)
                    Text("Comparisons use recorded time in bed and ratings on the waking date. Multiple ratings become one daily average. Unrecorded ratings are never guessed.",style=MaterialTheme.typography.bodySmall)
                }
                if(report.comparisons.isEmpty()) item {
                    Text("Let the threads gather",style=MaterialTheme.typography.headlineSmall)
                    Text("Rest comparisons need Sleep plus Sanctuary or Cycle, with at least 5 matched dates under 6h and 5 dates at 7h or more. Dates from 6h to under 7h are outside these two groups. You do not need to change your sleep to make a comparison appear.")
                }
                items(report.comparisons,key={it.title}) {comparison ->
                    Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        BotanicalDivider()
                        Text(comparison.title,style=MaterialTheme.typography.headlineSmall)
                        comparison.groups.forEach {group ->
                            Text("${group.label} · ${group.points.size} dates",style=MaterialTheme.typography.titleSmall)
                            Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.weight(1f).padding(top=8.dp).height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                    Box(Modifier.fillMaxWidth((group.mean/10).toFloat().coerceIn(0f,1f)).fillMaxHeight().background(MaterialTheme.colorScheme.primary))
                                }
                                Text("${insightNumber(group.mean)}/10",style=MaterialTheme.typography.titleMedium)
                            }
                        }
                        Text("Average recorded ${comparison.metric.lowercase()}. These groups can differ for many reasons; this does not show that sleep caused a change.",style=MaterialTheme.typography.bodySmall)
                        TextButton(onClick={expanded=if(expanded==comparison.title) "" else comparison.title}) {Text(if(expanded==comparison.title) "Hide recorded days" else "View recorded days")}
                        if(expanded==comparison.title) comparison.groups.forEach {group ->
                            Text(group.label,style=MaterialTheme.typography.labelLarge)
                            group.points.forEach {point -> TextButton(onClick={onDay(point.date)}) {Text("${point.date} · ${insightNumber(point.value)}/10")}}
                        }
                    }
                }
                item {
                    BotanicalDivider()
                    Text("Your cycle rhythms",style=MaterialTheme.typography.headlineSmall)
                    Text(phases.explanation)
                    Text("These are calendar estimates, not confirmed ovulation or hormone measurements. Current period and luteal settings also apply to past dates; historical contraception changes are not recorded here.",style=MaterialTheme.typography.bodySmall)
                }
                phases.comparisons.forEach {comparison ->
                    item(key="phase-${comparison.metric}") {
                        Text("${comparison.metric} across phases",style=MaterialTheme.typography.titleLarge)
                        comparison.groups.forEach {group ->
                            Text("${group.label} · ${insightNumber(group.mean)}${comparison.unit}",style=MaterialTheme.typography.titleSmall)
                            Text("${group.points.size} dates · ${group.cycles} cycles",style=MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick={expanded=if(expanded=="phase-${comparison.metric}") "" else "phase-${comparison.metric}"}) {
                            Text(if(expanded=="phase-${comparison.metric}") "Hide phase dates" else "View ${comparison.metric.lowercase()} phase dates")
                        }
                    }
                    if(expanded=="phase-${comparison.metric}") comparison.groups.forEach {group ->
                        item {Text(group.label,style=MaterialTheme.typography.labelLarge)}
                        items(group.points,key={"${comparison.metric}-${it.date}"}) {point ->
                            TextButton(onClick={onDay(point.date)}) {Text("${point.date} · ${insightNumber(point.value)}${comparison.unit}")}
                        }
                    }
                }
                if("Dreams" in store.insightData) item {
                    BotanicalDivider();Text("Symbols that return",style=MaterialTheme.typography.headlineSmall)
                    if(report.symbols.isEmpty()) Text("Symbols tagged in at least two dream records will gather here.")
                    report.symbols.forEach {(symbol,count)->Text("$symbol · $count records")}
                    Text("Your own tags, counted once per record. Lilyly does not assign meanings or predictions to them.",style=MaterialTheme.typography.bodySmall)
                }
                item {Text("An exploratory view of what you chose to record, not a diagnosis or a complete picture of your life. Missing days and other influences can change these summaries.",style=MaterialTheme.typography.bodySmall)}
            }
        }
    }
    if(choosing) AlertDialog(onDismissRequest={choosing=false},title={Text("Your data, your choice")},text={Column {
        Text("Choose the sources used only in this insights space. Turning one off does not delete its records.")
        insightSources.forEach {source -> Row {Checkbox(source in store.insightData,{selected->store.updateInsightPreferences(sources=if(selected)store.insightData+source else store.insightData-source)});Text(source,Modifier.padding(top=12.dp))}}
    }},confirmButton={TextButton(onClick={choosing=false}){Text("Done")}})
}
