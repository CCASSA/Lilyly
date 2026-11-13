package com.lilyly.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.floor

internal fun validWakingTemperature(text:String):Boolean {
    val value=text.trim().replace(',','.')
    return Regex("[0-9]{2}([.][0-9]{1,2})?").matches(value) && value.toDouble() in 25.0..50.0
}
@Composable
internal fun TemperatureChart(logs:List<CycleLog>,today:LocalDate) {
    val first=today.minusDays(29)
    val rows=logs.mapNotNull { row ->
        val date=runCatching { LocalDate.parse(row.date) }.getOrNull()
        if(date==null || date<first || date>today || !validWakingTemperature(row.wakingTemperature)) null else date to row.wakingTemperature.replace(',','.')
    }.sortedBy {it.first}
    val ink=MaterialTheme.colorScheme.primary
    Column {
        BotanicalDivider()
        Text("Waking temperatures",style=MaterialTheme.typography.titleLarge)
        Text("Recorded readings · last 30 days. These readings do not confirm ovulation.",style=MaterialTheme.typography.bodySmall)
        if(rows.isEmpty()) Text("Add a waking temperature to a daily cycle log to see it here.")
        else {
            val low=floor(rows.minOf {it.second.toDouble()}*2)/2
            val high=maxOf(low+0.5,ceil(rows.maxOf {it.second.toDouble()}*2)/2)
            Text("$high °C",style=MaterialTheme.typography.labelSmall)
            Canvas(Modifier.fillMaxWidth().height(92.dp)) {
                val margin=5.dp.toPx()
                var last:Pair<LocalDate,Offset>?=null
                rows.forEach {(date,raw) ->
                    val p=Offset(((ChronoUnit.DAYS.between(first,date)+0.5)/30*size.width).toFloat(),margin+(1-(raw.toDouble()-low)/(high-low)).toFloat()*(size.height-2*margin))
                    last?.let {(prev,q)->if(prev.plusDays(1)==date) drawLine(ink,q,p,2.dp.toPx())}
                    drawCircle(ink,3.dp.toPx(),p)
                    last=date to p
                }
            }
            Text("$low °C",style=MaterialTheme.typography.labelSmall)
            var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            TextButton(onClick={expanded=!expanded}) {Text(if(expanded)"Hide readings" else "Read all measurements ("+rows.size+")")}
            if(expanded) rows.asReversed().forEach {(date,value)->Text("$date · $value °C")}
        }
    }
}
