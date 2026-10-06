package com.lilyly.app

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.view.WindowManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class PrivacyJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    private class IsolatedContext(base:Context,val root:File,val prefix:String):ContextWrapper(base) {
        override fun getApplicationContext():Context=this
        override fun getFilesDir():File=File(root,"files").apply {mkdirs()}
        override fun getCacheDir():File=File(root,"cache").apply {mkdirs()}
        override fun getSharedPreferences(name:String,mode:Int):SharedPreferences=super.getSharedPreferences(prefix+name,mode)
    }
    @Test fun authenticatedBackupRestoresPhotosAndKeepsCurrentRecords() {
        val id=UUID.randomUUID().toString()
        val root=File(rule.activity.cacheDir,"privacy-fixture-$id").apply {mkdirs()}
        val source=IsolatedContext(rule.activity,File(root,"source"),"source-$id-")
        val target=IsolatedContext(rule.activity,File(root,"target"),"target-$id-")
        try {
            val store=AppStore(source)
            val photo=File(source.filesDir,"fixture.png")
            Bitmap.createBitmap(20,20,Bitmap.Config.ARGB_8888).let {bitmap ->photo.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()}
            store.upsertJournal(JournalEntry(id="photo-fixture",title="Photo page",imageUri=Uri.fromFile(photo).toString()))
            store.upsertJournal(JournalEntry(id="collision-fixture",body="Older backup text"))
            store.saveSleep(SleepRecord(bedtime="2026-10-01T23:00",wakeTime="2026-10-02T07:00",dream="Synthetic garden",symbols=setOf("garden")))
            val pdf=File(root,"book.pdf")
            val document=android.graphics.pdf.PdfDocument()
            try {
                val page=document.startPage(android.graphics.pdf.PdfDocument.PageInfo.Builder(200,300,1).create())
                page.canvas.drawColor(android.graphics.Color.WHITE);document.finishPage(page)
                pdf.outputStream().use {document.writeTo(it)}
            } finally {document.close()}
            val imported=BookFiles(source).importBook(Uri.fromFile(pdf))
            store.saveBook(imported.copy(bookmarks=setOf(0)))
            store.updatePrivacy(lock=true)
            val backup=File(root,"fixture.lilyly")
            BackupFiles(source).export(store.backupSnapshot(),Uri.fromFile(backup),"A private fixture garden".toCharArray())
            val targetStore=AppStore(target)
            targetStore.upsertJournal(JournalEntry(id="collision-fixture",body="Current local text"))
            assertTrue(runCatching {BackupFiles(target).prepare(Uri.fromFile(backup),"Not the correct password".toCharArray())}.isFailure)
            val files=BackupFiles(target)
            val prepared=files.prepare(Uri.fromFile(backup),"A private fixture garden".toCharArray())
            try {
                files.installAssets(prepared)
                runBlocking {withContext(Dispatchers.Main) {targetStore.restoreSnapshot(prepared.records)}}
            } finally {prepared.discard()}
            photo.delete()
            val restored=AppStore(target)
            assertEquals("Current local text",restored.journalEntries.first {it.id=="collision-fixture"}.body)
            val restoredPhoto=Uri.parse(restored.journalEntries.first {it.id=="photo-fixture"}.imageUri)
            assertTrue(target.contentResolver.openInputStream(restoredPhoto)!!.use {it.readBounded(8)}.contentEquals(byteArrayOf(-119,80,78,71,13,10,26,10)))
            assertTrue(restored.sleepRecords.any {it.dream=="Synthetic garden"})
            assertTrue(restored.books.any {it.id==imported.id && 0 in it.bookmarks})
            BookFiles(target).pdf(imported.id,0,250).let {assertEquals(250,it.width);it.recycle()}
            assertFalse("Restoring must not enable a different device's app lock",restored.appLockEnabled)
        } finally {root.deleteRecursively()}
    }
    @Test fun protectedColdStartHidesPrivateScreensAndBlocksScreenshots() {
        try {
            rule.runOnUiThread {AppStore(rule.activity).updatePrivacy(lock=true)}
            rule.activityRule.scenario.recreate()
            rule.onNodeWithText("Open Lilyly").assertIsDisplayed()
            rule.onNodeWithText("Journal",useUnmergedTree=true).assertDoesNotExist()
            rule.runOnUiThread {assertTrue(rule.activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0)}
        } finally {
            rule.runOnUiThread {AppStore(rule.activity).updatePrivacy(lock=false,screenshots=false)}
            rule.activityRule.scenario.recreate()
        }
    }
}
