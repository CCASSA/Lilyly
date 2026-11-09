package com.lilyly.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CycleHistorySheet(records:List<CycleLog>,today:LocalDate,onEdit:(LocalDate)->Unit,onClose:()->Unit) {
    val chapters=remember(records,today){cycleChapters(records,today)}
    var expanded by rememberSaveable {mutableStateOf<String?>(null)}
    val fmt=DateTimeFormatter.ofPattern("d MMM yyyy")
    ModalBottomSheet(onDismissRequest=onClose,sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) {
        LazyColumn(contentPadding=PaddingValues(start=22.dp,end=22.dp,bottom=32.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            item {
                Text("Your cycle history",style=MaterialTheme.typography.headlineMedium)
                Text("From the first logged period day to the day before the next recorded start. Unlogged days stay unknown.",style=MaterialTheme.typography.bodyMedium)
            }
            if(chapters.isEmpty()) item {
                Text("Your first chapter begins with a period log.",style=MaterialTheme.typography.titleMedium)
                Text("Use the calendar to record bleeding on the date it happened. Earlier notes stay in the calendar.")
            }
            chapters.forEach {chapter ->
                val id=chapter.start.toString()
                item(key=id) {
                    Column(Modifier.fillMaxWidth().clickable {expanded=if(expanded==id)null else id}.semantics {stateDescription=if(expanded==id)"Expanded" else "Collapsed"}.padding(vertical=8.dp)) {
                        Text(if(chapter.ongoing) "Latest recorded cycle" else "Between recorded starts",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
                        Text(chapter.start.format(fmt)+" – "+(if(chapter.ongoing)"today" else chapter.through.format(fmt)),style=MaterialTheme.typography.titleMedium)
                        Text(if(chapter.ongoing) "Day "+chapter.days+" since the recorded start" else "${chapter.days} days between starts")
                        Text("${chapter.logs.size} logged · ${chapter.bleedingDays} marked as period",style=MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider()
                }
                if(expanded==id) items(chapter.logs.asReversed(),key={"day-"+it.first}) {(date,log) ->
                    TextButton(onClick={onEdit(date)}) {
                        Column(Modifier.fillMaxWidth()) {
                            Text("Edit "+date.format(fmt))
                            val marks=listOfNotNull(if(log.period)"Period" else if(log.spotting)"Spotting" else null)+log.selections.sorted()
                            if(marks.isNotEmpty()) Text(marks.joinToString(" · "),style=MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            item { TextButton(onClick=onClose){Text("Close history")} }
        }
    }
}
