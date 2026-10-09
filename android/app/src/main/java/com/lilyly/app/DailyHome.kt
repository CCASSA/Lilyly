package com.lilyly.app

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

val homeSectionNames = listOf("Rhythm", "Sanctuary", "Night", "Pages", "Reading", "Medication", "Season")

@Composable
fun LibraryHomeScreen(
    store: AppStore, onOpenSection: (String)->Unit, onOpenCycle: ()->Unit,
    onOpenSanctuary: ()->Unit, onOpenTarot: ()->Unit, onOpenBookshelf: ()->Unit,
    onOpenCalendar: ()->Unit, onReadBook: (String)->Unit = {onOpenBookshelf()},
    onNewPage: ()->Unit = {onOpenSection("Journal")}, onMedication: ()->Unit = onOpenSanctuary,
    onOpenDay: ()->Unit = {},
    onOpenEntry: (JournalEntry)->Unit = {onOpenSection(it.section)}
) {
    val now by produceState(LocalDateTime.now()) { while(true) {value=LocalDateTime.now();delay(60000)} }
    val today=now.toLocalDate()
    var personalize by rememberSaveable {mutableStateOf(false)}
    val greeting=when(now.hour) {in 5..11 -> "Good morning";in 12..17 -> "A quiet afternoon";else -> "Welcome to the evening"}
    val prompt=listOf("What would you like to make room for?", "Where did you find a little wonder?", "What feels unfinished, and what can rest?", "What do you want to remember about this season?", "What helped you feel more like yourself?", "What small kindness could you offer yourself?", "What are you ready to put into words?")[today.dayOfWeek.value-1]
    LazyColumn(Modifier.fillMaxSize().testTag("daily-home"), contentPadding=PaddingValues(bottom=28.dp), verticalArrangement=Arrangement.spacedBy(18.dp)) {
        item {
            HomeCanopy()
            Column(Modifier.padding(horizontal=24.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
                Text(today.format(DateTimeFormatter.ofPattern("EEEE · d MMMM")),style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
                Text(greeting + if(store.greetingName.isNotBlank()) ", ${store.greetingName}" else "", style=MaterialTheme.typography.headlineLarge)
                Text("Your pages are here. Take the time you need.",color=MaterialTheme.colorScheme.onSurface.copy(alpha=.72f))
                TextButton(onClick=onOpenDay) {Text("Gather the threads of a day")}
                TextButton(onClick=onOpenCalendar,contentPadding=PaddingValues(0.dp)) {Text("☾  ${moonPhaseName(now)} · approximate")}
            }
        }
        if("Rhythm" in store.homeSections) item {
            val pattern=cyclePattern(store.cycleLogs.filter {it.period}.mapNotNull {runCatching {LocalDate.parse(it.date)}.getOrNull()},store.cyclePreferences)
            val state=cycleDayState(today,pattern,store.cyclePreferences)
            HomeThread("❧","Your rhythm",state.day?.let {"Day $it · ${state.phase}"} ?: state.phase,"Open your cycle garden",onOpenCycle)
        }
        if("Sanctuary" in store.homeSections) item {
            val check=store.mentalCheckIns.filter {it.dateTime.take(10)==today.toString()}.maxByOrNull {it.dateTime}
            val feelings=check?.feelings?.joinToString(" · ").orEmpty()
            HomeThread("✧","How are you arriving today?",if(check==null) "There is room for whatever you are feeling." else feelings.ifBlank {"You've made space for a check-in today."},if(check==null) "Check in with yourself" else "Visit your Sanctuary",onOpenSanctuary)
        }
        if("Night" in store.homeSections) item {
            val night=store.sleepRecords.filter {it.date<=today.toString()}.maxByOrNull {it.wakeTime}
            HomeThread("☾","From the night garden",night?.let {"${if(it.date==today.toString()) "This morning" else it.date} · ${sleepDurationLabel(it.durationMinutes)} recorded rest${if(it.dream.isNotBlank()) " · a dream kept" else ""}"} ?: "A place for rest, dreams and the things you wake with.","Remember a night",{onOpenSection("Dreams")})
        }
        if("Pages" in store.homeSections) item {
            Column(Modifier.fillMaxWidth().padding(horizontal=24.dp).clip(RoundedCornerShape(topStart=36.dp,bottomEnd=36.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha=.09f)).padding(22.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text("A thought for your page",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
                Text(prompt,style=MaterialTheme.typography.headlineSmall)
                TextButton(onClick=onNewPage) {Text("Open a fresh page")}
                store.journalEntries.filter {it.section!="Templates" && "sample" !in it.tags.split(',').map(String::trim)}.maxByOrNull {it.updatedAt}?.let {entry ->
                    HorizontalDivider(color=MaterialTheme.colorScheme.primary.copy(alpha=.25f))
                    TextButton(onClick={onOpenEntry(entry)}) {Text("Return to ${entry.title.ifBlank {"your last page"}}",maxLines=2)}
                }
            }
        }
        if("Reading" in store.homeSections) item {
            val book=store.books.filter {!it.completed && it.lastReadAt.isNotBlank()}.maxByOrNull {it.lastReadAt}
                ?: store.books.filter {!it.completed}.maxByOrNull {it.addedAt}
            HomeThread("❦","One more chapter",book?.let {"${it.title}\n${it.format} · place ${it.position+1} of ${it.units}"} ?: "Bring a book into your own little library.",if(book==null) "Visit the reading nook" else "Continue reading",{if(book==null)onOpenBookshelf() else onReadBook(book.id)})
        }
        if("Medication" in store.homeSections && store.medications.any {it.active}) item {
            val meds=store.medications.filter {it.active}
            val logged=store.medicationLogs.filter {it.dateTime.take(10)==today.toString()}
            HomeThread("✺","Your medication cabinet",meds.joinToString("\n") {med ->
                val last=logged.filter {it.medicationId==med.id}.maxByOrNull {it.dateTime}
                "${med.name} · ${last?.status ?: "No entry today"}"
            },"Review your medication log",onMedication)
        }
        if("Season" in store.homeSections) item {
            val season=nextSabbat(store.hemisphere,today)
            HomeThread("✻","A turn of the season","${season.first} · ${season.second}\n${store.hemisphere} hemisphere · traditional calendar date","Explore the calendar",onOpenCalendar)
        }
        item {
            Column(Modifier.padding(horizontal=24.dp)) {
                BotanicalDivider()
                Text("Wander through your world",style=MaterialTheme.typography.headlineSmall,modifier=Modifier.padding(vertical=12.dp))
            }
            LazyRow(contentPadding=PaddingValues(horizontal=24.dp),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                items(listOf("Journal","Grimoire","Poems","Spells","Dreams","Ideas")) {section ->
                    val tone=MaterialTheme.colorScheme.tertiary
                    Column(Modifier.width(132.dp).height(176.dp).clip(RoundedCornerShape(topEnd=20.dp,bottomEnd=20.dp)).background(tone.copy(alpha=.14f)).clickable {onOpenSection(section)}.padding(16.dp),verticalArrangement=Arrangement.SpaceBetween) {
                        Text("❧",color=tone,style=MaterialTheme.typography.headlineMedium)
                        Text(if(section=="Dreams") "Dream book" else section,fontFamily=FontFamily.Serif,style=MaterialTheme.typography.titleLarge)
                        Text("Open →",style=MaterialTheme.typography.labelMedium,color=tone)
                    }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal=24.dp)) {
                TextButton(onClick=onOpenTarot) {Text("✦  Enter the tarot room")}
                TextButton(onClick=onOpenBookshelf) {Text("❦  Browse the reading nook")}
                TextButton(onClick={personalize=true}) {Text("Make Home your own")}
            }
        }
    }
    if(personalize) AlertDialog(onDismissRequest={personalize=false},title={Text("Make yourself at home")},text={
        Column(Modifier.verticalScroll(rememberScrollState())) {
            OutlinedTextField(store.greetingName,{store.personalizeHome(it,store.homeSections)},label={Text("Name for your greeting")},singleLine=true)
            Text("Choose the threads you want to see. Everything remains available in its own room.",modifier=Modifier.padding(vertical=12.dp),style=MaterialTheme.typography.bodySmall)
            homeSectionNames.forEach {name ->
                Row(Modifier.fillMaxWidth().clickable {store.personalizeHome(store.greetingName,if(name in store.homeSections) store.homeSections-name else store.homeSections+name)},verticalAlignment=Alignment.CenterVertically) {
                    Checkbox(name in store.homeSections,{selected ->store.personalizeHome(store.greetingName,if(selected)store.homeSections+name else store.homeSections-name)})
                    Text(name)
                }
            }
        }
    },confirmButton={TextButton(onClick={personalize=false}) {Text("Done")}})
}

@Composable
private fun HomeThread(symbol:String,title:String,detail:String,action:String,onClick:()->Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal=24.dp),horizontalArrangement=Arrangement.spacedBy(16.dp)) {
        Text(symbol,style=MaterialTheme.typography.headlineMedium,color=MaterialTheme.colorScheme.primary,modifier=Modifier.padding(top=4.dp))
        Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Text(title,style=MaterialTheme.typography.titleLarge)
            Text(detail,color=MaterialTheme.colorScheme.onSurface.copy(alpha=.75f))
            TextButton(onClick=onClick,contentPadding=PaddingValues(0.dp)) {Text(action)}
        }
    }
}

@Composable
private fun HomeCanopy() {
    val gold=MaterialTheme.colorScheme.primary
    val leaf=MaterialTheme.colorScheme.tertiary
    val background=MaterialTheme.colorScheme.background
    Canvas(Modifier.fillMaxWidth().height(160.dp)) {
        val w=size.width;val h=size.height
        drawRect(Brush.radialGradient(listOf(gold.copy(alpha=.13f),background),Offset(w*.5f,h*.4f),w*.65f))
        val radius=h*.28f;val center=Offset(w*.5f,h*.46f)
        drawCircle(gold.copy(alpha=.8f),radius,center)
        drawCircle(background,radius*.87f,center+Offset(radius*.45f,-radius*.15f))
        drawArc(gold.copy(alpha=.35f),180f,180f,false,Offset(w*.23f,h*.08f),Size(w*.54f,h*1.6f),style=Stroke(1.dp.toPx()))
        for(side in listOf(-1,1)) {
            val root=Offset(w*.5f+side*w*.37f,h*.95f)
            val stem=Path().apply {moveTo(root.x,root.y);cubicTo(root.x-side*25.dp.toPx(),h*.6f,root.x+side*8.dp.toPx(),h*.4f,root.x-side*34.dp.toPx(),h*.08f)}
            drawPath(stem,leaf.copy(alpha=.55f),style=Stroke(1.dp.toPx()))
            repeat(7) {i -> val y=h*(.18f+i*.105f);val x=root.x-side*(26-i*3).dp.toPx()
                drawOval(leaf.copy(alpha=.25f),Offset(x,y),Size(20.dp.toPx(),7.dp.toPx()))
            }
        }
        repeat(19) {i -> val a=i*2.39996;val r=h*(.45f+(i%4)*.09f);val p=Offset(center.x+cos(a).toFloat()*r*1.6f,center.y+sin(a).toFloat()*r)
            if(p.y>0 && p.y<h)drawCircle(gold.copy(alpha=.35f),if(i%4==0)1.5.dp.toPx() else .8.dp.toPx(),p)
        }
    }
}
