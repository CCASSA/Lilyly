package com.lilyly.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.min

private fun DrawScope.paintInk(doc:InkDocument) {
    val scale=if(doc.width>0 && doc.height>0)min(size.width/doc.width,size.height/doc.height) else 1f
    doc.strokes.forEach {s->
        if(s.points.isEmpty())return@forEach
        val color=Color(s.color).copy(alpha=if(s.highlighter).28f else 1f)
        if(s.highlighter) {
            // One path avoids dark seams at each overlapping highlighter segment.
            val path=Path().apply {moveTo(s.points.first().x*scale,s.points.first().y*scale);s.points.drop(1).forEach {lineTo(it.x*scale,it.y*scale)}}
            if(s.points.size==1)drawCircle(color,s.width*scale/2,Offset(s.points[0].x*scale,s.points[0].y*scale))
            else drawPath(path,color,style=Stroke(s.width*scale,cap=StrokeCap.Round,join=StrokeJoin.Round))
        } else {
            s.points.forEach {p->drawCircle(color,s.width*p.pressure*scale/2,Offset(p.x*scale,p.y*scale))}
            s.points.zipWithNext().forEach {(a,b)->drawLine(color,Offset(a.x*scale,a.y*scale),Offset(b.x*scale,b.y*scale),s.width*(a.pressure+b.pressure)/2*scale,StrokeCap.Round)}
        }
    }
}

@Composable
fun InkArtwork(raw:String,modifier:Modifier=Modifier) {
    val doc=remember(raw){readInk(raw)}
    Canvas(modifier.semantics {contentDescription="Saved handwriting"}) {paintInk(doc)}
}

@Composable
fun InkStudio(raw:String,onChange:(String)->Unit,onPlace:(String)->Unit,modifier:Modifier=Modifier) {
    var tool by rememberSaveable {mutableStateOf("Pen")}
    var color by rememberSaveable {mutableStateOf(0xFF3A2422L)}
    var weight by rememberSaveable {mutableStateOf("Fine")}
    var stylusOnly by rememberSaveable {mutableStateOf(false)}
    var undo by remember {mutableStateOf<List<String>>(emptyList())}
    var redo by remember {mutableStateOf<List<String>>(emptyList())}
    var confirmClear by remember {mutableStateOf(false)}
    var surfaceSize by remember {mutableStateOf(IntSize.Zero)}
    val latest by rememberUpdatedState(raw)
    fun commit(next:String) {if(next!=latest){undo=(undo+latest).takeLast(40);redo=emptyList();onChange(next)}}
    Column(modifier,verticalArrangement=Arrangement.spacedBy(8.dp)) {
        Text("The ink studio",style=MaterialTheme.typography.headlineSmall)
        Text("Write a thought, sketch a sigil, keep a little of your own hand.",style=MaterialTheme.typography.bodySmall)
        LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            items(listOf("Pen","Highlighter","Erase stroke")){label->FilterChip(tool==label,{tool=label},label={Text(label)})}
        }
        LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            items(listOf("Ink" to 0xFF3A2422L,"Wine" to 0xFF842E4DL,"Moss" to 0xFF41644AL,"Gold" to 0xFFAD7C26L,"Ivory" to 0xFFFFF3D8L)){(name,value)->
                FilterChip(color==value,{color=value},label={Text(name)},leadingIcon={Box(Modifier.size(14.dp).background(Color(value)))})
            }
        }
        LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            items(listOf("Fine","Medium","Broad")){label->FilterChip(weight==label,{weight=label},label={Text(label)})}
            item {FilterChip(stylusOnly,{stylusOnly=!stylusOnly},label={Text("Stylus only")})}
        }
        Text(if(stylusOnly) "Only a stylus draws; fingers can scroll. Pen pressure is used when supplied by your device." else "Draw with a finger or stylus. Use Stylus only to keep finger touches off your ink.",style=MaterialTheme.typography.bodySmall)
        LazyRow {
            item {TextButton(enabled=undo.isNotEmpty(),onClick={redo=redo+raw;onChange(undo.last());undo=undo.dropLast(1)}){Text("Undo ink")}}
            item {TextButton(enabled=redo.isNotEmpty(),onClick={undo=(undo+raw).takeLast(40);onChange(redo.last());redo=redo.dropLast(1)}){Text("Redo ink")}}
            item {TextButton(enabled=readInk(raw).strokes.isNotEmpty(),onClick={confirmClear=true}){Text("Clear ink")}}
        }
        InkCanvas(raw,color,tool,weight,stylusOnly,::commit,Modifier.fillMaxWidth().height(430.dp).testTag("ink-surface").onSizeChanged {surfaceSize=it})
        Button(onClick={val d=readInk(raw);onPlace(inkJson(if(d.width>0 && d.height>0)d else d.copy(width=surfaceSize.width.toFloat(),height=surfaceSize.height.toFloat())))},enabled=readInk(raw).strokes.isNotEmpty() && surfaceSize.width>0){Text("Place ink on scrapbook")}
        Text("Placement creates an independent ink element. Save your page to keep it.",style=MaterialTheme.typography.bodySmall)
    }
    if(confirmClear)AlertDialog(onDismissRequest={confirmClear=false},title={Text("Clear this handwriting layer?")},text={Text("Ink already placed on your scrapbook stays there. You can undo this while the studio remains open.")},confirmButton={TextButton(onClick={commit(clearInk());confirmClear=false}){Text("Clear layer")}},dismissButton={TextButton(onClick={confirmClear=false}){Text("Keep ink")}})
}

@Composable
private fun InkCanvas(raw:String,color:Long,tool:String,weight:String,stylusOnly:Boolean,onChange:(String)->Unit,modifier:Modifier) {
    val doc=remember(raw){readInk(raw)}
    val latestDoc by rememberUpdatedState(doc)
    val latestChange by rememberUpdatedState(onChange)
    var live by remember {mutableStateOf<InkDocument?>(null)}
    Canvas(modifier.background(Color(0xFFF0E5D1)).semantics {contentDescription="Handwriting surface"}.pointerInput(tool,color,weight,stylusOnly) {
        awaitEachGesture {
            val down=awaitFirstDown(requireUnconsumed=false)
            val stylus=down.type==PointerType.Stylus || down.type==PointerType.Eraser
            if(stylusOnly && !stylus)return@awaitEachGesture
            down.consume()
            val base=latestDoc.let {if(it.width>0 && it.height>0)it else it.copy(width=size.width.toFloat(),height=size.height.toFloat())}
            val scale=min(size.width/base.width,size.height/base.height)
            fun point(c:PointerInputChange)=InkPoint((c.position.x/scale).coerceIn(0f,base.width),(c.position.y/scale).coerceIn(0f,base.height),if(stylus && c.pressure.isFinite() && c.pressure>0)c.pressure.coerceIn(.25f,2f) else 1f)
            val erasing=tool=="Erase stroke" || down.type==PointerType.Eraser
            val factor=when(weight){"Broad"->.014f;"Medium"->.009f;else->.0045f}
            val stroke=InkStroke(listOf(point(down)),color,base.width*factor*(if(tool=="Highlighter")4 else 1),tool=="Highlighter")
            var working=if(erasing)base.copy(strokes=base.strokes.filterNot {inkHits(it,point(down),12.dp.toPx()/scale)}) else base.copy(strokes=base.strokes+stroke)
            live=working
            try {
                do {
                    val event=awaitPointerEvent()
                    val change=event.changes.firstOrNull {it.id==down.id} ?: break
                    change.consume()
                    val p=point(change)
                    working=if(erasing)working.copy(strokes=working.strokes.filterNot {inkHits(it,p,12.dp.toPx()/scale)}) else {
                        val last=working.strokes.last()
                        if(last.points.size<12000)working.copy(strokes=working.strokes.dropLast(1)+last.copy(points=last.points+p)) else working
                    }
                    live=working
                }while(change.pressed)
                if(working.strokes!=base.strokes)latestChange(inkJson(working))
            }finally {live=null}
        }
    }) {paintInk(live ?: doc)}
}
