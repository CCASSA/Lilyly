package com.lilyly.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun InkCanvas(
    initialInkJson: String,
    inkColor: Color,
    paperColor: Color,
    onInkChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var strokes by remember(initialInkJson) { mutableStateOf(decodeInk(initialInkJson)) }
    var current by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Canvas(
        modifier
            .background(paperColor)
            .pointerInput(strokes) {
                detectDragGestures(
                    onDragStart = { pos -> current = listOf(pos) },
                    onDrag = { change, _ ->
                        change.consume()
                        current = current + change.position
                    },
                    onDragEnd = {
                        if (current.size > 1) {
                            strokes = strokes + listOf(current)
                            onInkChanged(encodeInk(strokes))
                        }
                        current = emptyList()
                    },
                    onDragCancel = { current = emptyList() }
                )
            }
    ) {
        fun drawStroke(points: List<Offset>) {
            if (points.size < 2) return
            points.zipWithNext().forEach { (a, b) ->
                drawLine(inkColor, a, b, strokeWidth = 5f, cap = StrokeCap.Round)
            }
        }
        strokes.forEach(::drawStroke)
        drawStroke(current)
    }
}

fun clearInk(): String = "[]"

private fun encodeInk(strokes: List<List<Offset>>): String {
    val arr = JSONArray()
    strokes.forEach { stroke ->
        val s = JSONArray()
        stroke.forEach { p -> s.put(JSONObject().put("x", p.x).put("y", p.y)) }
        arr.put(s)
    }
    return arr.toString()
}

private fun decodeInk(raw: String): List<List<Offset>> {
    return runCatching {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val s = arr.getJSONArray(i)
            (0 until s.length()).map { j ->
                val p = s.getJSONObject(j)
                Offset(p.optDouble("x").toFloat(), p.optDouble("y").toFloat())
            }
        }
    }.getOrElse { emptyList() }
}
