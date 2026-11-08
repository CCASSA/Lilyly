package com.lilyly.app

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class SharedInspirationJourneyTest {
    @get:Rule val rule=createEmptyComposeRule()
    private lateinit var activity:MainActivity
    private lateinit var scenario:androidx.test.core.app.ActivityScenario<MainActivity>
    @org.junit.Before fun launchActivity() {
        scenario=androidx.test.core.app.ActivityScenario.launch(Intent(InstrumentationRegistry.getInstrumentation().targetContext,MainActivity::class.java))
        scenario.onActivity {activity=it}
    }
    @org.junit.After fun closeActivity() {scenario.close()}
    private fun openShare(intent:Intent) {
        scenario.close()
        scenario=androidx.test.core.app.ActivityScenario.launch(intent)
        scenario.onActivity {activity=it}
    }
    private fun uri(name:String)="content://com.lilyly.app.test.sharefixtures/$name"
    private fun fixture(name:String,valid:Boolean=true):Uri {
        val resolver=activity.contentResolver
        val values=android.content.ContentValues().apply {
            put(android.provider.MediaStore.Images.Media.DISPLAY_NAME,name)
            put(android.provider.MediaStore.Images.Media.MIME_TYPE,"image/png")
        }
        val value=requireNotNull(resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values))
        requireNotNull(resolver.openOutputStream(value)).use { out ->
            if(valid) {
                val bitmap=Bitmap.createBitmap(24,24,Bitmap.Config.ARGB_8888)
                try { bitmap.eraseColor(android.graphics.Color.GREEN); check(bitmap.compress(Bitmap.CompressFormat.PNG,100,out)) }
                finally { bitmap.recycle() }
            } else out.write("not an image".toByteArray())
        }
        return value
    }
    @Test fun realShareReviewsRotatesAndKeepsAttribution() {
        val intent=Intent(activity,MainActivity::class.java).setAction(Intent.ACTION_SEND).setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT,"A shared fern").putExtra(Intent.EXTRA_TEXT,"A study https://example.org/fern")
        openShare(intent)
        rule.onNodeWithText("A spark from elsewhere").assertIsDisplayed()
        rule.onNodeWithTag("shared-inspiration").performScrollToNode(hasText("Creator or source credit"))
        rule.onNodeWithText("Creator or source credit").performTextInput("Fern artist")
        rule.onNodeWithTag("shared-inspiration").performScrollToNode(hasText("Collection"))
        rule.onNodeWithText("Collection").performTextInput("Shared wonders")
        rule.onNodeWithText("Collection").performImeAction()
        scenario.recreate()
        scenario.onActivity {activity=it}
        rule.onNodeWithTag("shared-inspiration").performScrollToNode(hasText("Collection"))
        rule.onNodeWithText("Collection").assertTextContains("Shared wonders")
        rule.waitForIdle()
        val bitmap=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(activity.filesDir,"shared-inspiration.png").outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        rule.onNodeWithText("Keep inspiration").performClick()
        rule.waitUntil(10000) {rule.onAllNodesWithText("Gathered inspiration").fetchSemanticsNodes().isNotEmpty()}
        rule.runOnUiThread {
            val store=AppStore(activity)
            val card=store.journalEntries.single {it.title=="A shared fern"}
            assertEquals("Fern artist",card.sourceCredit);assertEquals("Shared wonders",card.notebook)
            assertEquals("https://example.org/fern",card.sourceUrl)
            assertEquals("Fern artist",inspirationPiece(card).sourceCredit)
            store.deleteJournal(card.id)
        }
    }
    @Test fun batchCopiesImagesAndKeepsOtherStoresNewerPages() {
        val one=fixture("one.png");val two=fixture("two.png")
        lateinit var store:AppStore;lateinit var job:SharedInspirationViewModel
        rule.runOnUiThread {
            store=AppStore(activity)
            AppStore(activity).upsertJournal(JournalEntry(id="newer-share-fixture",title="A newer unrelated page"))
            job=ViewModelProvider(activity)[SharedInspirationViewModel::class.java]
            job.keep(store,"shared-batch","Shared leaves","","https://example.org/leaves","Artist","Garden",listOf(one.toString(),two.toString()))
        }
        rule.waitUntil(10000){!job.busy}
        assertTrue(job.message,job.kept);activity.contentResolver.delete(one,null,null);activity.contentResolver.delete(two,null,null)
        rule.runOnUiThread {
            val restored=AppStore(activity)
            assertTrue(restored.journalEntries.any {it.id=="newer-share-fixture"})
            val rows=restored.journalEntries.filter {it.id.startsWith("shared-batch-")}
            assertEquals(2,rows.size)
            rows.forEach {card ->assertTrue(File(Uri.parse(card.imageUri).path!!).exists());restored.deleteJournal(card.id);InspirationFiles(activity).removeIfUnused(card.imageUri,restored)}
            restored.deleteJournal("newer-share-fixture")
        }
    }
    @Test fun failedBatchLeavesNoCardsOrCopiedFiles() {
        val one=fixture("one.png");val broken=fixture("broken.png",false)
        val dir=File(activity.filesDir,"inspiration-media")
        val before=dir.list()?.toSet().orEmpty()
        lateinit var job:SharedInspirationViewModel
        rule.runOnUiThread {
            job=ViewModelProvider(activity)[SharedInspirationViewModel::class.java]
            job.keep(AppStore(activity),"failed-share","Bad batch","","","","",listOf(one.toString(),broken.toString()))
        }
        rule.waitUntil(10000){!job.busy}
        assertFalse(job.kept);assertTrue(job.message.isNotBlank())
        rule.runOnUiThread {assertFalse(AppStore(activity).journalEntries.any {it.id.startsWith("failed-share-")})}
        assertEquals(before,dir.list()?.toSet().orEmpty());activity.contentResolver.delete(one,null,null);activity.contentResolver.delete(broken,null,null)
    }
    @Test fun incomingShareCannotBypassAppLock() {
        val secure=SecurePreferences(activity)
        val old=secure.get("privacy","{}")
        rule.runOnUiThread {AppStore(activity).updatePrivacy(lock=true)}
        try {
            val intent=Intent(activity,MainActivity::class.java).setAction(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,"A private shared thought")
            openShare(intent)
            rule.onNodeWithText("Open Lilyly").assertIsDisplayed()
            rule.onNodeWithText("A spark from elsewhere").assertDoesNotExist()
            rule.onNodeWithText("A private shared thought").assertDoesNotExist()
        } finally {rule.runOnUiThread {secure.put("privacy",old)}}
    }
    @Test fun incomingIntentRejectsFilesAndUnsupportedTypes() {
        val base=Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM,Uri.parse("file:///private/image.png"))
        assertTrue(runCatching {readSharedInspiration(base)}.isFailure)
        assertTrue(runCatching {readSharedInspiration(Intent(Intent.ACTION_SEND).setType("application/pdf"))}.isFailure)
        val valid=Intent(Intent.ACTION_SEND_MULTIPLE).setType("image/png").putParcelableArrayListExtra(Intent.EXTRA_STREAM,arrayListOf(Uri.parse(uri("one.png")),Uri.parse(uri("two.png"))))
        assertEquals(2,readSharedInspiration(valid).images.size)
    }
}
