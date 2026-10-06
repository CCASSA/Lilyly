package com.lilyly.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.math.floor

fun moonPhaseName(now: LocalDateTime): String {
    val epoch = LocalDateTime.of(2000, 1, 6, 18, 14)
    val days = ChronoUnit.SECONDS.between(epoch, now).toDouble() / 86400.0
    val cycle = ((days / 29.53058867) % 1.0 + 1.0) % 1.0
    return when {
        cycle < 0.03 || cycle > 0.97 -> "New moon"
        cycle < 0.22 -> "Waxing crescent"
        cycle < 0.28 -> "First quarter"
        cycle < 0.47 -> "Waxing gibbous"
        cycle < 0.53 -> "Full moon"
        cycle < 0.72 -> "Waning gibbous"
        cycle < 0.78 -> "Last quarter"
        else -> "Waning crescent"
    }
}

fun nextSabbat(hemisphere: String, today: LocalDate): Pair<String, String> {
    val south = listOf(
        Triple(2, 1, "Lughnasadh"), Triple(3, 21, "Mabon"), Triple(5, 1, "Samhain"), Triple(6, 21, "Yule"),
        Triple(8, 1, "Imbolc"), Triple(9, 21, "Ostara"), Triple(11, 1, "Beltane"), Triple(12, 21, "Litha")
    )
    val north = listOf(
        Triple(2, 1, "Imbolc"), Triple(3, 21, "Ostara"), Triple(5, 1, "Beltane"), Triple(6, 21, "Litha"),
        Triple(8, 1, "Lughnasadh"), Triple(9, 21, "Mabon"), Triple(11, 1, "Samhain"), Triple(12, 21, "Yule")
    )
    val list = if (hemisphere == "Southern") south else north
    val candidates = list.map { (m, d, name) ->
        var date = LocalDate.of(today.year, m, d)
        if (date.isBefore(today)) date = date.plusYears(1)
        Triple(date, name, "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }}")
    }
    val next = candidates.minBy { it.first }
    return next.second to next.third
}
