package com.lilyly.app

import java.time.LocalDate

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
