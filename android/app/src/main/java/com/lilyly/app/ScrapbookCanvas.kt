package com.lilyly.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.roundToInt

/** Normalized positions keep a page portable between different phone widths. */
data class PagePiece(val id: String = UUID.randomUUID().toString(), val kind: String = "text", val content: String = "", val x: Float = .12f, val y: Float = .25f, val width: Float = .55f, val rotation: Float = 0f) {
    fun json() = JSONObject().put("id", id).put("kind", kind).put("content", content).put("x", x).put("y", y).put("width", width).put("rotation", rotation)
}
fun pagePieces(json: String): List<PagePiece> = runCatching {
    val array = JSONArray(json)
    (0 until array.length()).map { i -> array.getJSONObject(i).let { o ->
        PagePiece(o.getString("id"), o.optString("kind", "text"), o.optString("content"), o.optDouble("x", .12).toFloat().coerceIn(0f, .9f), o.optDouble("y", .25).toFloat().coerceIn(0f, .9f), o.optDouble("width", .55).toFloat().coerceIn(.15f, .95f), o.optDouble("rotation", 0.0).toFloat().coerceIn(-45f, 45f))
    } }
}.getOrElse { emptyList() }
fun piecesJson(pieces: List<PagePiece>) = JSONArray().apply { pieces.forEach { put(it.json()) } }.toString()

@Composable
internal fun ScrapbookCanvas(entry: JournalEntry, onChange: (JournalEntry) -> Unit, onPhoto: () -> Unit, contextStamp: String) {
    val pieces = remember(entry.canvasJson) { pagePieces(entry.canvasJson) }
    var selectedId by rememberSaveable(entry.id) { mutableStateOf<String?>(null) }
    val selected = pieces.firstOrNull { it.id == selectedId }
    val latestEntry by rememberUpdatedState(entry)
    fun update(piece: PagePiece) { onChange(latestEntry.copy(canvasJson = piecesJson(pagePieces(latestEntry.canvasJson).map { if (it.id == piece.id) piece else it }))) }
    fun add(kind: String, content: String) {
        val piece = PagePiece(kind = kind, content = content, y = (.22f + pieces.size * .06f).coerceAtMost(.7f))
        onChange(entry.copy(canvasJson = piecesJson(pieces + piece))); selectedId = piece.id
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Parchment", "Lined", "Midnight", "Botanical")) { paper -> FilterChip(entry.paper == paper, { onChange(entry.copy(paper = paper)) }, label = { Text(paper) }) }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            item { FilledTonalButton(onClick = { add("text", "A thought to keep…") }) { Text("+ Words") } }
            item { FilledTonalButton(onClick = onPhoto) { Text("+ Photo") } }
            item { FilledTonalButton(onClick = { add("sticker", "❦") }) { Text("+ Botanical") } }
            item { FilledTonalButton(onClick = { add("sticker", "☾ ✧") }) { Text("+ Celestial") } }
            item { FilledTonalButton(onClick = { add("text", contextStamp) }) { Text("+ Today's context") } }
        }
        Text("Drag an element to move it. Select it to edit, resize or turn it.", style = MaterialTheme.typography.bodySmall)
        val night = entry.paper == "Midnight"
        val paperColor = if (night) Color(0xFF222032) else if (entry.paper == "Botanical") Color(0xFFE3E5D5) else Color(0xFFF0E5D1)
        val ink = if (night) Color(0xFFE8DABF) else Color(0xFF3C3035)
        BoxWithConstraints(Modifier.fillMaxWidth().height(500.dp).clipToBounds().background(paperColor, RoundedCornerShape(6.dp)).border(1.dp, Gold.copy(alpha = .6f), RoundedCornerShape(6.dp))) {
            val density = LocalDensity.current
            val widthPx = with(density) { maxWidth.toPx() }
            val heightPx = with(density) { maxHeight.toPx() }
            val pageWidth = maxWidth
            Canvas(Modifier.fillMaxSize()) {
                // Restrained paper flecks and ruled/ornamental margins, all original vector geometry.
                for (i in 0..180) {
                    val x = ((i * 73) % 997) / 997f * size.width
                    val y = ((i * 139) % 991) / 991f * size.height
                    drawCircle(ink.copy(alpha = .055f), 1f, Offset(x, y))
                }
                drawLine(ink.copy(alpha = .18f), Offset(18.dp.toPx(), 20.dp.toPx()), Offset(18.dp.toPx(), size.height - 20.dp.toPx()), 1f)
                if (entry.paper == "Lined") for (i in 2..17) drawLine(ink.copy(alpha = .13f), Offset(25.dp.toPx(), i * 27.dp.toPx()), Offset(size.width - 15.dp.toPx(), i * 27.dp.toPx()), 1f)
            }
            Column(Modifier.padding(30.dp)) {
                Text("❦", color = ink.copy(alpha = .65f), fontSize = 26.sp)
                Text(entry.title.ifBlank { "An unwritten page" }, color = ink, fontFamily = FontFamily.Serif, fontSize = 24.sp)
                if (entry.body.isNotBlank()) Text(entry.body, color = ink, fontFamily = FontFamily.Serif, fontSize = 16.sp, maxLines = 10, modifier = Modifier.padding(top = 12.dp))
                if (entry.imageUri.isNotBlank()) AsyncImage(entry.imageUri, "Existing journal photograph", Modifier.fillMaxWidth().height(160.dp).padding(top = 12.dp), contentScale = ContentScale.Fit)
            }
            pieces.forEach { piece -> key(piece.id) {
                val current by rememberUpdatedState(piece)
                Box(Modifier.offset { IntOffset((piece.x * widthPx).roundToInt(), (piece.y * heightPx).roundToInt()) }
                    .width(pageWidth * piece.width)
                    .graphicsLayer { rotationZ = piece.rotation }
                    .border(if (selectedId == piece.id) 1.dp else 0.dp, if (selectedId == piece.id) Color(0xFF846047) else Color.Transparent, RoundedCornerShape(4.dp))
                    .pointerInput(piece.id, widthPx, heightPx) {
                        detectDragGestures(onDragStart = { selectedId = piece.id }, onDrag = { change, drag ->
                            change.consume()
                            update(current.copy(x = (current.x + drag.x / widthPx).coerceIn(0f, (1f - current.width).coerceAtLeast(0f)), y = (current.y + drag.y / heightPx).coerceIn(0f, .85f)))
                        })
                    }) {
                    androidx.compose.foundation.text.selection.DisableSelection {
                        TextButton(onClick = { selectedId = piece.id }, contentPadding = PaddingValues(6.dp)) {
                            when (piece.kind) {
                                "photo" -> AsyncImage(piece.content, "Scrapbook photograph", Modifier.fillMaxWidth().height(150.dp).background(Color(0xFFF8F0E3)).padding(7.dp), contentScale = ContentScale.Fit)
                                "sticker" -> Text(piece.content, color = ink, fontFamily = FontFamily.Serif, fontSize = (piece.width * 85).sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                                else -> Text(piece.content, color = ink, fontFamily = FontFamily.Serif, fontSize = 17.sp, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            } }
        }
        selected?.let { piece ->
            if (piece.kind != "photo") OutlinedTextField(piece.content, { update(piece.copy(content = it)) }, label = { Text("Selected ${piece.kind}") }, modifier = Modifier.fillMaxWidth())
            Text("Size")
            Slider(piece.width, { update(piece.copy(width = it)) }, valueRange = .15f.. .95f)
            Text("Rotation · ${piece.rotation.roundToInt()}°")
            Slider(piece.rotation, { update(piece.copy(rotation = it)) }, valueRange = -45f..45f)
            TextButton(onClick = { onChange(entry.copy(canvasJson = piecesJson(pieces.filterNot { it.id == piece.id }))); selectedId = null }) { Text("Remove selected element") }
        }
    }
}
