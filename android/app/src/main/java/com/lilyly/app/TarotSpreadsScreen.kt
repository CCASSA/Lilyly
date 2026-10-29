package com.lilyly.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarotSpreadsScreen(store:AppStore,onBack:()->Unit,onUse:(String)->Unit) {
    var draft by rememberSaveable {mutableStateOf<String?>(null)}
    var removing by rememberSaveable {mutableStateOf<String?>(null)}
    BackHandler(onBack=onBack)
    Scaffold(topBar={TopAppBar(title={Text("Spreads of your own")},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Back to tarot")}})}) {padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("personal-spreads"),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)) {
            item {
                Text("Let your questions shape the cards",style=MaterialTheme.typography.headlineMedium)
                Text("Create one to six positions, each with a question or intention. The cards offer a reflective perspective.")
                Button(onClick={draft=TarotSpread().toJson().toString()}) {Text("Create a spread")}
            }
            if(store.tarotSpreads.isEmpty())item {BotanicalDivider();Text("A spread shaped by you",style=MaterialTheme.typography.titleLarge);Text("Try a question for where you are, what needs care, and what you want to carry forward.")}
            items(store.tarotSpreads.sortedBy {it.name.lowercase()},key={it.id}) {spread ->
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    BotanicalDivider()
                    Text(spread.name,style=MaterialTheme.typography.headlineSmall)
                    spread.positions.forEachIndexed {i,label ->Text("${i+1} · $label")}
                    Button(onClick={onUse(spread.id)}) {Text("Use ${spread.name}")}
                    Row {TextButton(onClick={draft=spread.toJson().toString()}) {Text("Edit spread")};TextButton(onClick={removing=spread.id}) {Text("Remove spread")}}
                }
            }
        }
    }
    draft?.let {raw ->
        val o=JSONObject(raw);val a=o.getJSONArray("positions")
        val source=TarotSpread(o.getString("id"),o.getString("name"),(0 until a.length()).map {a.getString(it)})
        TarotSpreadEditor(source,onSave={store.saveTarotSpread(it);draft=null},onCancel={draft=null})
    }
    removing?.let {id ->
        AlertDialog(onDismissRequest={removing=null},title={Text("Remove this spread?")},text={Text("This removes the reusable template. Past readings and their grimoire pages keep their original cards and position labels.")},
            confirmButton={TextButton(onClick={store.deleteTarotSpread(id);removing=null}) {Text("Remove template")}},
            dismissButton={TextButton(onClick={removing=null}) {Text("Keep spread")}})
    }
}

@Composable
private fun TarotSpreadEditor(source:TarotSpread,onSave:(TarotSpread)->Unit,onCancel:()->Unit) {
    var name by rememberSaveable(source.id) {mutableStateOf(source.name)}
    var positions by rememberSaveable(source.id) {mutableStateOf(source.positions.joinToString("\n"))}
    var discard by rememberSaveable(source.id) {mutableStateOf(false)}
    val changed=name!=source.name || positions!=source.positions.joinToString("\n")
    val close={if(changed)discard=true else onCancel()}
    val value=source.copy(name=name.trim(),positions=positions.lines().map {it.trim()})
    val error=tarotSpreadError(value)
    val keyboard=LocalSoftwareKeyboardController.current
    val focus=LocalFocusManager.current
    AlertDialog(onDismissRequest=close,title={Text("Shape your spread")},text={
        Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(name,{name=it},label={Text("Spread name")},singleLine=true,modifier=Modifier.fillMaxWidth())
            Text("One question or label per line, in card order. Use up to six lines. Move a line to change the layout order.")
            OutlinedTextField(positions,{positions=it},label={Text("Card positions")},minLines=3,maxLines=6,modifier=Modifier.fillMaxWidth(),keyboardOptions=KeyboardOptions(imeAction=ImeAction.Done),keyboardActions=KeyboardActions(onDone={keyboard?.hide();focus.clearFocus()}))
            if(error!=null)Text(error,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall)
            Text("Changes apply to future draws. Saved readings keep their original layout and wording.",style=MaterialTheme.typography.bodySmall)
        }
    },confirmButton={TextButton(enabled=error==null,onClick={keyboard?.hide();focus.clearFocus();onSave(value)}) {Text("Keep spread")}},dismissButton={TextButton(onClick=close) {Text("Cancel")}})
    if(discard)AlertDialog(onDismissRequest={discard=false},title={Text("Leave these changes?")},text={Text("Your unsaved spread changes will be discarded.")},confirmButton={TextButton(onClick=onCancel){Text("Discard changes")}},dismissButton={TextButton(onClick={discard=false}){Text("Keep editing")}})
}
