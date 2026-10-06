package com.lilyly.app

import android.app.KeyguardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun PrivacyPanel(store:AppStore) {
    val context=LocalContext.current
    val files=remember {BackupFiles(context.applicationContext)}
    val fallbackScope=rememberCoroutineScope()
    val scope=(context as? MainActivity)?.lifecycleScope ?: fallbackScope
    var active by remember {mutableStateOf(true)}
    var exportUri by remember {mutableStateOf<Uri?>(null)}
    var mode by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var repeated by remember {mutableStateOf("")}
    var importUri by remember {mutableStateOf<Uri?>(null)}
    var prepared by remember {mutableStateOf<PreparedRestore?>(null)}
    BackHandler(enabled=store.privacyBusy) { }
    DisposableEffect(Unit) {onDispose {active=false;if(!store.privacyBusy)prepared?.discard()}}
    val export=rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if(uri!=null) {exportUri=uri;mode="Export";password="";repeated=""}
    }
    val importer=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {uri ->if(uri!=null) {importUri=uri;mode="Restore";password="";repeated=""}}
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("The key to your world",style=MaterialTheme.typography.titleLarge)
        Row {
            Column(Modifier.weight(1f)) {Text("App lock");Text("Android biometrics or screen lock. Locks when Lilyly leaves the foreground and hides screenshots/recent-app previews.",style=MaterialTheme.typography.bodySmall)}
            Switch(store.appLockEnabled,onCheckedChange={value ->
                val secure=(context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceSecure
                if(!secure) store.privacyMessage="Set a PIN, pattern or password in Android Settings first."
                else (context as? MainActivity)?.authenticate {success ->if(success)store.updatePrivacy(lock=value)}
            },enabled=!store.privacyBusy)
        }
        Text("Save before leaving a form. App lock closes open dialogs; older unsaved forms may reset.",style=MaterialTheme.typography.bodySmall)
        Row {Column(Modifier.weight(1f)) {Text("Hide screenshots");Text("Also hides Lilyly in Android's recent-app preview.",style=MaterialTheme.typography.bodySmall)};Switch(store.hideScreenshots || store.appLockEnabled,{store.updatePrivacy(screenshots=it)},enabled=!store.privacyBusy && !store.appLockEnabled)}
        HorizontalDivider()
        Text("Carry your world safely",style=MaterialTheme.typography.titleLarge)
        Text("A password-encrypted backup includes records, journal photos and imported books. You choose where the file is saved. Your password is never stored.")
        Button(onClick={export.launch("Lilyly-${LocalDate.now()}.lilyly")},enabled=!store.privacyBusy,modifier=Modifier.fillMaxWidth()) {Text("Create encrypted backup")}
        OutlinedButton(onClick={importer.launch(arrayOf("*/*"))},enabled=!store.privacyBusy,modifier=Modifier.fillMaxWidth()) {Text("Restore from backup")}
        Text("Restore merges records and keeps your current version when an ID or cycle date already exists. It does not import another device's app-lock setting. Original document-picker files are never deleted.",style=MaterialTheme.typography.bodySmall)
        if(store.privacyBusy) {LinearProgressIndicator(Modifier.fillMaxWidth());Text("Keeping your pages safe…")}
        if(store.privacyMessage.isNotBlank()) Text(store.privacyMessage)
    }
    if(mode.isNotEmpty()) AlertDialog(onDismissRequest={mode="";password="";repeated=""},title={Text(if(mode=="Export") "Protect this backup" else "Open your backup")},text={Column {
        Text(if(mode=="Export") "Use at least 12 characters. You'll need this password to restore your records." else "Enter the password used to create this backup.")
        OutlinedTextField(password,{password=it},label={Text("Backup password")},visualTransformation=PasswordVisualTransformation(),singleLine=true)
        if(mode=="Export") OutlinedTextField(repeated,{repeated=it},label={Text("Repeat password")},visualTransformation=PasswordVisualTransformation(),singleLine=true)
    }},confirmButton={TextButton(enabled=password.length>=12 && (mode!="Export" || password==repeated),onClick={
        if(mode=="Export") {
            mode="";store.privacyBusy=true;store.privacyMessage=""
            val secret=password.toCharArray();password="";repeated=""
            scope.launch {
                try {val snapshot=store.backupSnapshot();withContext(Dispatchers.IO) {files.export(snapshot,checkNotNull(exportUri),secret)};store.privacyMessage="Encrypted backup saved. Keep its password somewhere safe; Lilyly cannot recover it."}
                catch(e:Exception) {store.privacyMessage="Backup was not completed. ${e.message.orEmpty()}"}
                finally {secret.fill('\u0000');store.privacyBusy=false}
            }
        }
        else {
            mode="";store.privacyBusy=true;val secret=password.toCharArray();password=""
            scope.launch {
                try {val ready=withContext(Dispatchers.IO) {files.prepare(checkNotNull(importUri),secret)};if(active) {prepared=ready;store.privacyMessage=""} else {ready.discard();store.privacyMessage="Verification closed while Lilyly was locked. Open the backup again to review it."}}
                catch(e:Exception) {store.privacyMessage="Backup could not be verified. Check the password and file; no records were changed."}
                finally {secret.fill('\u0000');store.privacyBusy=false}
            }
        }
    }) {Text(if(mode=="Export") "Encrypt & save" else "Verify backup")}},dismissButton={TextButton(onClick={mode="";password="";repeated=""}) {Text("Cancel")}})
    prepared?.let { backup -> AlertDialog(onDismissRequest={if(!store.privacyBusy) {backup.discard();prepared=null}},title={Text("Restore ${backup.count()} records?")},text={Text("Your current records will remain. New records and attachments from this verified backup will be added. Existing IDs keep their current version.")},confirmButton={TextButton(enabled=!store.privacyBusy,onClick={
        store.privacyBusy=true
        scope.launch {
            try {withContext(Dispatchers.IO) {files.installAssets(backup)};store.restoreSnapshot(backup.records);store.privacyMessage="Your backup has been merged. Existing records were kept."}
            catch(e:Exception) {store.privacyMessage="Restore could not finish. Your existing records were not deleted. ${e.message.orEmpty()}"}
            finally {backup.discard();prepared=null;store.privacyBusy=false}
        }
    }) {Text("Merge backup")}},dismissButton={TextButton(enabled=!store.privacyBusy,onClick={backup.discard();prepared=null}) {Text("Cancel")}}) }
}
