package com.lilyly.app

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.*

/** Retains the in-flight transaction through rotation; never holds the Activity. */
class SharedInspirationViewModel(application:Application):AndroidViewModel(application) {
    var busy by mutableStateOf(false);private set
    var kept by mutableStateOf(false);private set
    var message by mutableStateOf("");private set
    private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main.immediate)
    fun keep(store:AppStore,id:String,title:String,body:String,source:String,credit:String,collection:String,images:List<String>) {
        if(busy || kept)return
        busy=true;message=""
        scope.launch(NonCancellable) {
            val files=InspirationFiles(getApplication())
            val imported=mutableListOf<String>()
            try {
                withContext(Dispatchers.IO) {images.forEach {imported+=files.importImage(Uri.parse(it))}}
                store.keepSharedInspiration(sharedInspirationEntries(id,title,body,source,credit,collection,imported))
                kept=true
            } catch(e:Exception) {
                imported.forEach {files.removeIfUnused(it,store)}
                message="Could not keep this share. No cards were added. Try sharing again or choose an image inside Lilyly."
            } finally {busy=false}
        }
    }
    override fun onCleared() {scope.cancel()}
}
