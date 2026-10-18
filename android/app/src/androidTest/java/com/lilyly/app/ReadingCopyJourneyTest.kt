package com.lilyly.app

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ReadingCopyJourneyTest {
    @get:Rule val rule=createAndroidComposeRule<MainActivity>()
    @Test fun readingCopyPaginatesTextAndIncludesInkPhotoAndMissingMediaNotice() {
        val context=rule.activity
        val photo=File(context.cacheDir,"export-fixture.png")
        val bitmap=Bitmap.createBitmap(160,120,Bitmap.Config.ARGB_8888).apply {eraseColor(Color.rgb(65,100,74))}
        photo.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)};bitmap.recycle()
        val ink=inkJson(InkDocument(300f,150f,listOf(InkStroke(listOf(InkPoint(20f,30f),InkPoint(250f,100f)),0xFF842E4D,8f))))
        val a=JournalEntry(title="A garden to keep",body=(1..95).joinToString("\n"){"Line $it: a thought kept in the garden, with room to breathe."}+"\nFINAL-PARAGRAPH-SURVIVES",inkJson=ink,imageUri=Uri.fromFile(photo).toString(),canvasJson=piecesJson(listOf(PagePiece(content="A movable thought",sourceCredit="Garden artist",sourceUrl="https://example.org/art"),PagePiece(kind="ink",content=ink),PagePiece(kind="photo",content="file:///missing-photo.png"))))
        val b=JournalEntry(title="The second chapter",body="SECOND-ENTRY-SURVIVES",musicJson=musicJson(listOf(MusicAttachment("https://open.spotify.com/track/1234567890123456789012","Reading soundtrack"))))
        val file=File(context.filesDir,"reading-copy.pdf")
        val result=ReadingCopyPdf(context).write(listOf(a,b),"Moon keepsakes",file)
        assertTrue(result.pages>=5);assertEquals(1,result.missingImages)
        PdfRenderer(ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY)).use {pdf->
            assertEquals(result.pages,pdf.pageCount)
            for(i in 0 until pdf.pageCount)pdf.openPage(i).use {page->
                assertEquals(595,page.width);assertEquals(842,page.height)
                val rendered=Bitmap.createBitmap(page.width,page.height,Bitmap.Config.ARGB_8888)
                page.render(rendered,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                var nonWhite=0
                for(y in 40 until 780 step 4)for(x in 40 until 550 step 4)if(rendered.getPixel(x,y)!=Color.WHITE)nonWhite++
                assertTrue("Rendered PDF sheet $i must contain visible content",nonWhite>5);rendered.recycle()
            }
        }
        photo.delete()
    }
    @Test fun exportConfirmationNamesSelectionAndExplainsPrivacy() {
        rule.runOnUiThread {rule.activity.setContent {LilylyTheme(true) {ReadingCopyButton(listOf(JournalEntry(title="A private page")),"Moon notebook")}}}
        rule.onNodeWithText("Export reading copy").performClick()
        rule.onNodeWithText("Book title").assertExists()
        rule.onNodeWithText("The PDF is not encrypted",substring=true).assertExists()
        rule.onNodeWithText("Export 1 entry",substring=true).assertExists()
        rule.onNodeWithText("Keep private").performClick()
        rule.onNodeWithText("Choose PDF destination").assertDoesNotExist()
    }
}
