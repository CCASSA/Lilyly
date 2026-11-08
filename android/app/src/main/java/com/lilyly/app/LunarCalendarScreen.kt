package com.lilyly.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.*

@Composable
private fun MoonDisc(fraction: Double, southern: Boolean, modifier: Modifier) {
    val light = MaterialTheme.colorScheme.primary
    val shadow = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier) {
        val r = size.minDimension * .45f
        drawCircle(shadow,r)
        val side = (if (fraction < .5) 1 else -1) * (if (southern) -1 else 1)
        val terminator = cos(fraction * 2 * PI).toFloat()
        val path = Path()
        fun vertex(i: Int, scale: Float, first: Boolean = false) {
            val angle = PI * i / 48
            val x = center.x + side * r * sin(angle).toFloat() * scale
            val y = center.y - r * cos(angle).toFloat()
            if (first) path.moveTo(x,y) else path.lineTo(x,y)
        }
        for (i in 0..48) vertex(i,1f,i==0)
        for (i in 48 downTo 0) vertex(i,terminator)
        path.close()
        drawPath(path,light)
        drawCircle(light.copy(alpha=.6f),r,center,style=Stroke(1.dp.toPx()))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagicalCalendarScreen(store: AppStore, initialDate:String?, onBack:()->Unit, onDay:(String)->Unit, onJournal:(String)->Unit) {
    BackHandler(onBack=onBack)
    val zone=ZoneId.systemDefault()
    val today=LocalDate.now(zone)
    var dateText by rememberSaveable { mutableStateOf(initialDate ?: today.toString()) }
    val date=LocalDate.parse(dateText)
    val month=YearMonth.from(date)
    val locale=Locale.getDefault()
    val firstDay=WeekFields.of(locale).firstDayOfWeek
    val start=month.atDay(1)
    val blanks=Math.floorMod(start.dayOfWeek.value-firstDay.value,7)
    val turns=remember(month,zone) { lunarTurns(start,month.plusMonths(1).atDay(1),zone).associateBy { it.date } }
    val upcoming=remember(date,zone) { lunarTurns(date.plusDays(1),date.plusDays(34),zone).take(4) }
    val sabbat=nextSabbat(store.hemisphere,date)
    val label=moonPhaseName(date.atTime(12,0),zone)
    Scaffold(topBar={TopAppBar(title={Text("Magical calendar")},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back")}})}) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("moon-almanac"),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item { Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)) {
                MoonDisc(lunarFraction(date.atTime(12,0).atZone(zone).toInstant()),store.hemisphere=="Southern",Modifier.size(80.dp))
                Column { Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM",locale))); Text(label,style=MaterialTheme.typography.headlineSmall) }
            } }
            item { Row(verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick={dateText=date.minusMonths(1).toString()}) {Icon(Icons.Default.ChevronLeft,"Previous month")}
                Text(date.format(DateTimeFormatter.ofPattern("MMMM yyyy",locale)),Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
                IconButton(onClick={dateText=date.plusMonths(1).toString()}) {Icon(Icons.Default.ChevronRight,"Next month")}
            }
            Row { repeat(7) { i -> Box(Modifier.weight(1f),contentAlignment=Alignment.Center) {Text(firstDay.plus(i.toLong()).getDisplayName(TextStyle.SHORT,locale),style=MaterialTheme.typography.labelSmall)} } }
            for (week in 0 until (blanks+month.lengthOfMonth()+6)/7) Row {
                repeat(7) { col ->
                    val number=week*7+col-blanks+1
                    if(number !in 1..month.lengthOfMonth()) Spacer(Modifier.weight(1f))
                    else {
                        val day=month.atDay(number)
                        val turn=turns[day]?.phase
                        val selectedDay=day==date
                        val bg=if(selectedDay) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent
                        Column(Modifier.weight(1f).heightIn(min=48.dp).background(bg,CircleShape).clickable {dateText=day.toString()}.semantics {
                            selected=selectedDay;contentDescription=listOfNotNull(day.toString(),if(day==today) "Today" else null,turn).joinToString(", ")
                        },horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                            Text(number.toString(),color=if(selectedDay) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                            if(turn!=null) Box(Modifier.size(5.dp).background(MaterialTheme.colorScheme.primary,CircleShape))
                        }
                    }
                }
            } }
            item { Text("Dots mark estimated phase changes. Dates use "+zone.id+".",style=MaterialTheme.typography.bodySmall)
                Row {TextButton(onClick={dateText=today.toString()}) {Text("Today")}; TextButton(onClick={onDay(dateText)}) {Text("Open this day’s threads")} }
            }
            item { FilledTonalButton(onClick={onJournal(dateText)}) { Text("Begin a moon page") } }
            item { Text("Next moons",style=MaterialTheme.typography.headlineSmall)
                upcoming.forEach { event -> TextButton(onClick={dateText=event.date.toString()}) {Text(event.date.format(DateTimeFormatter.ofPattern("d MMM",locale))+"  ·  "+event.phase)} }
            }
            item { HorizontalDivider();Spacer(Modifier.height(12.dp));Text("Wheel of the Year · "+store.hemisphere,style=MaterialTheme.typography.titleMedium)
                Text("Next: "+sabbat.first+" · "+sabbat.second)
                Text("Moon dates follow an approximate repeating cycle. The illustration uses your hemisphere setting; the angle in your sky can differ.",style=MaterialTheme.typography.bodySmall)
            }
        }
    }
}
