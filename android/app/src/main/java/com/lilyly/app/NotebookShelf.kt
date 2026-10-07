package com.lilyly.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotebookShelf(store:AppStore,section:String,selected:String,onSelect:(String)->Unit) {
    var dialog by rememberSaveable {mutableStateOf(false)}
    var existing by rememberSaveable {mutableStateOf(false)}
    var name by rememberSaveable {mutableStateOf("")}
    var cover by rememberSaveable {mutableStateOf("Botanical")}
    Column {
        Text("Your bound pages",style=MaterialTheme.typography.headlineMedium)
        LazyRow(horizontalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)) {
            items(store.notebookNames(section)) {book ->
                val style=store.notebookCovers[notebookKey(section,book)] ?: "Botanical"
                val color=when(style) {"Celestial"->Color(0xFF242E51);"Wine"->Color(0xFF522C40);"Parchment"->Color(0xFF514436);else->Color(0xFF263F38)}
                Box(Modifier.width(142.dp).height(192.dp).clip(RoundedCornerShape(topEnd=22.dp,bottomEnd=22.dp)).background(color).border(if(selected==book) 2.dp else 1.dp,Color(0xFFCDB681).copy(alpha=if(selected==book)1f else .4f),RoundedCornerShape(topEnd=22.dp,bottomEnd=22.dp)).clickable {onSelect(book)}) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawLine(Color(0xFFCDB681).copy(alpha=.3f),Offset(12.dp.toPx(),0f),Offset(12.dp.toPx(),size.height),2.dp.toPx())
                        drawCircle(Color(0xFFCDB681).copy(alpha=.3f),30.dp.toPx(),Offset(size.width*.55f,size.height*.3f),style=Stroke(1.dp.toPx()))
                    }
                    Column(Modifier.fillMaxSize().padding(22.dp),verticalArrangement=Arrangement.SpaceBetween,horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(if(style=="Celestial") "☾" else if(style=="Wine") "✦" else "❦",style=MaterialTheme.typography.headlineLarge,color=Color(0xFFEBDAB6))
                        Text(book,style=MaterialTheme.typography.titleMedium,color=Color(0xFFF4E7D5),maxLines=3)
                        Text("${store.journalEntries.count {it.section==section && it.notebook==book}} pages",style=MaterialTheme.typography.labelSmall,color=Color(0xFFEBDAB6))
                    }
                }
            }
        }
        Row {
            TextButton(onClick={existing=false;name="";cover="Botanical";dialog=true}) {Text("Bind a notebook")}
            if(selected.isNotBlank())TextButton(onClick={existing=true;name=selected;cover=store.notebookCovers[notebookKey(section,selected)] ?: "Botanical";dialog=true}) {Text("Change cover")}
        }
    }
    if(dialog)AlertDialog(onDismissRequest={dialog=false},title={Text(if(existing) "Dress your notebook" else "A book of your own")},text={Column {
        OutlinedTextField(name,{name=it},label={Text("Notebook name")},enabled=!existing,singleLine=true)
        FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)) {notebookCoverNames.forEach {label ->FilterChip(cover==label,{cover=label},label={Text(label)})}}
    }},confirmButton={TextButton(enabled=name.isNotBlank(),onClick={store.saveNotebook(section,name,cover);onSelect(name.trim());dialog=false}) {Text("Keep notebook")}},dismissButton={TextButton(onClick={dialog=false}) {Text("Cancel")}})
}

@Composable
fun PageTemplatePicker(store:AppStore,onDismiss:()->Unit,onUse:(JournalEntry)->Unit,onEdit:(String)->Unit) {
    AlertDialog(onDismissRequest=onDismiss,title={Text("Begin with a little structure")},text={
        LazyColumn(Modifier.heightIn(max=430.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item {Text("Creates a new page. Your template remains unchanged.",style=MaterialTheme.typography.bodySmall)}
            items(builtInPages()+store.journalEntries.filter {it.section=="Templates"},key={it.id}) {template ->
                Column {
                    Text(template.title.ifBlank {"Personal template"},style=MaterialTheme.typography.titleLarge)
                    Text(template.paper,style=MaterialTheme.typography.labelMedium)
                    Row {TextButton(onClick={onUse(template)}) {Text("Use ${template.title.ifBlank {"template"}}")};if(template.section=="Templates")TextButton(onClick={onEdit(template.id)}) {Text("Edit")}}
                    HorizontalDivider()
                }
            }
        }
    },confirmButton={TextButton(onClick=onDismiss) {Text("Close")}})
}
