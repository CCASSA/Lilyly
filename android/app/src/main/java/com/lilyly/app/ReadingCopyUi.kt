package com.lilyly.app

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ReadingCopyButton(entries:List<JournalEntry>,defaultTitle:String) {
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    var show by remember {mutableStateOf(false)}
    var title by rememberSaveable {mutableStateOf("")}
    var snapshotPath by rememberSaveable {mutableStateOf<String?>(null)}
    var busy by remember {mutableStateOf(false)}
    var message by remember {mutableStateOf("")}
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")){uri->
        val snapshot=snapshotPath?.let(::File);snapshotPath=null
        if(uri==null){snapshot?.delete();busy=false}
        else scope.launch {
            busy=true
            try {
                val result=withContext(Dispatchers.IO) {
                    require(snapshot?.exists()==true){"The export selection expired. Please choose Export reading copy again."}
                    val data=org.json.JSONObject(snapshot!!.readText())
                    val temp=File.createTempFile("reading-copy-",".pdf",context.cacheDir)
                    try {
                        val result=ReadingCopyPdf(context).write(readingCopyEntries(data.getString("entries")),data.getString("title"),temp)
                        context.contentResolver.openOutputStream(uri,"wt")?.use {out->temp.inputStream().use {it.copyTo(out)}} ?: error("The selected destination could not be opened.")
                        result
                    }finally {temp.delete()}
                }
                message="Reading copy saved · ${result.pages} PDF sheets."+(if(result.missingImages>0)" ${result.missingImages} unavailable images are marked in the PDF." else "")
            }catch(e:Exception){message="Could not save the reading copy. ${e.message ?: "Please try again."}"}
            finally {snapshot?.delete();busy=false}
        }
    }
    Column {
        TextButton(enabled=entries.isNotEmpty() && !busy && snapshotPath==null,onClick={title=defaultTitle;show=true}) {Text("Export reading copy")}
        if(busy)Text("Preparing your reading copy…",style=MaterialTheme.typography.bodySmall)
        if(message.isNotBlank())Text(message,style=MaterialTheme.typography.bodySmall)
    }
    if(show)AlertDialog(onDismissRequest={show=false},title={Text("Keep a printable copy")},text={Column {
        Text("Export ${entries.size} ${if(entries.size==1)"entry" else "entries"} from the current selection, in the order shown. ${entries.take(3).joinToString(" · "){it.title.ifBlank {"Untitled"}}}${if(entries.size>3)" …" else ""}")
        OutlinedTextField(title,{title=it},label={Text("Book title")},modifier=Modifier.fillMaxWidth())
        Text("Includes text, images, handwriting, credits and music references. This is a reading layout; scrapbook positions and rotations are not reproduced.",style=MaterialTheme.typography.bodySmall)
        Text("The PDF is not encrypted or protected by Lilyly’s app lock. Choose a private destination. Only these entries are exported; source links are not fetched.",style=MaterialTheme.typography.bodySmall)
        if(entries.size>200)Text("Choose a notebook or narrow your search to 200 entries or fewer.")
    }},confirmButton={TextButton(enabled=entries.size<=200,onClick={
        val frozen=readingCopySnapshot(entries);val chosenTitle=title.ifBlank {defaultTitle};show=false;busy=true
        scope.launch {
            try {
                val path=withContext(Dispatchers.IO){
                    context.cacheDir.listFiles()?.filter {it.name.startsWith("reading-selection-") && System.currentTimeMillis()-it.lastModified()>86400000L}?.forEach {it.delete()}
                    File.createTempFile("reading-selection-",".json",context.cacheDir).apply {writeText(org.json.JSONObject().put("title",chosenTitle).put("entries",frozen).toString())}.absolutePath
                }
                snapshotPath=path;busy=false;picker.launch(readingCopyFilename(chosenTitle))
            }catch(e:Exception){snapshotPath?.let {File(it).delete()};snapshotPath=null;busy=false;message="Could not prepare the export. Please try again."}
        }
    }){Text("Choose PDF destination")}},dismissButton={TextButton(onClick={show=false}){Text("Keep private")}})
}
