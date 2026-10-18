package com.lilyly.app

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File
import kotlin.math.min

/** Reflowed reading layout, deliberately distinct from the editable scrapbook canvas. */
class ReadingCopyPdf(private val context:Context) {
    data class Result(val pages:Int,val missingImages:Int)
    fun write(entries:List<JournalEntry>,title:String,target:File):Result {
        require(entries.isNotEmpty() && entries.size<=200) {"Choose between 1 and 200 entries for a reading copy."}
        val pdf=PdfDocument()
        var page:PdfDocument.Page?=null
        var count=0;var y=0f;var missing=0
        val left=46f;val contentWidth=503
        val textPaint=TextPaint(Paint.ANTI_ALIAS_FLAG).apply {color=Color.rgb(49,38,47);typeface=Typeface.create("serif",Typeface.NORMAL);textSize=12f}
        fun finish() {page?.let {p->p.canvas.drawText("Lilyly  ·  $count",left,808f,TextPaint(textPaint).apply {textSize=9f});pdf.finishPage(p)};page=null}
        fun newPage() {
            finish();require(count<500){"This copy exceeds 500 sheets. Export a smaller selection."};count++
            page=pdf.startPage(PdfDocument.PageInfo.Builder(595,842,count).create());y=67f
            page!!.canvas.drawColor(Color.WHITE)
            page!!.canvas.drawLine(left,43f,549f,43f,Paint().apply {color=Color.rgb(170,139,77);strokeWidth=1f})
        }
        fun space(height:Float){if(page==null || y+height>777f)newPage()}
        fun paragraph(raw:String,size:Float=12f) {
            if(raw.isBlank())return
            val paint=TextPaint(textPaint).apply {textSize=size}
            val layout=StaticLayout.Builder.obtain(raw,0,raw.length,paint,contentWidth).setAlignment(Layout.Alignment.ALIGN_NORMAL).setIncludePad(false).setLineSpacing(3f,1f).build()
            var first=0
            while(first<layout.lineCount) {
                space(25f);val top=layout.getLineTop(first);var end=first
                while(end<layout.lineCount && layout.getLineBottom(end)-top<=777f-y)end++
                if(end==first){newPage();continue}
                val height=layout.getLineBottom(end-1)-top
                val canvas=page!!.canvas;canvas.save();canvas.clipRect(left,y,left+contentWidth,y+height);canvas.translate(left,y-top);layout.draw(canvas);canvas.restore()
                y+=height+9;first=end
                if(first<layout.lineCount)newPage()
            }
        }
        fun image(uri:String) {
            if(uri.isBlank())return
            val bitmap=runCatching {localBitmap(uri)}.getOrNull()
            if(bitmap==null){missing++;paragraph("[Image unavailable — the original file could not be read.]",10f);return}
            try {val scale=min(contentWidth.toFloat()/bitmap.width,300f/bitmap.height);val w=bitmap.width*scale;val h=bitmap.height*scale;space(h+12);page!!.canvas.drawBitmap(bitmap,null,RectF(left,y,left+w,y+h),Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG));y+=h+12}finally {bitmap.recycle()}
        }
        fun ink(raw:String) {
            val d=readInk(raw);if(d.strokes.isEmpty())return
            val w=d.width.takeIf {it>0} ?: (d.strokes.flatMap {it.points}.maxOfOrNull {it.x} ?: 300f).coerceAtLeast(1f)
            val h=d.height.takeIf {it>0} ?: (d.strokes.flatMap {it.points}.maxOfOrNull {it.y} ?: 430f).coerceAtLeast(1f)
            val scale=min(contentWidth/w,420f/h);val height=h*scale
            space(height+14);val canvas=page!!.canvas;canvas.save();canvas.translate(left,y);canvas.clipRect(0f,0f,contentWidth.toFloat(),height)
            canvas.drawColor(Color.rgb(240,229,209));canvas.scale(scale,scale)
            d.strokes.forEach {s->
                val p=Paint(Paint.ANTI_ALIAS_FLAG).apply {color=s.color.toInt();alpha=if(s.highlighter)71 else 255;strokeWidth=s.width;strokeCap=Paint.Cap.ROUND;strokeJoin=Paint.Join.ROUND}
                if(s.highlighter && s.points.size>1){val path=Path();path.moveTo(s.points[0].x,s.points[0].y);s.points.drop(1).forEach {path.lineTo(it.x,it.y)};p.style=Paint.Style.STROKE;canvas.drawPath(path,p)}
                else {s.points.forEach {canvas.drawCircle(it.x,it.y,s.width*it.pressure/2,p)};s.points.zipWithNext().forEach {(a,b)->p.strokeWidth=s.width*(a.pressure+b.pressure)/2;canvas.drawLine(a.x,a.y,b.x,b.y,p)}}
            }
            canvas.restore();y+=height+14
        }
        try {
            newPage();y=220f;paragraph(title.ifBlank {"Pages from Lilyly"},30f);paragraph("A reading copy",16f);paragraph("${entries.size} saved ${if(entries.size==1)"entry" else "entries"}")
            paragraph("Text, images and handwriting gathered into a printable layout. Scrapbook elements follow their layer order; their on-screen positions and rotations are not reproduced.",11f)
            entries.forEach {entry->
                newPage();paragraph(entry.title.ifBlank {"Untitled page"},23f)
                paragraph(listOf(entry.section,entry.notebook,entry.createdAt.take(10)).filter {it.isNotBlank()}.joinToString(" · "),10f)
                paragraph(entry.body);image(entry.imageUri)
                if(entry.ritualJson.isNotBlank())paragraph(ritualSummary(RitualDetails.fromJson(entry.ritualJson)))
                if(readInk(entry.inkJson).strokes.isNotEmpty()){paragraph("Handwriting",16f);ink(entry.inkJson)}
                val pieces=pagePieces(entry.canvasJson)
                if(pieces.isNotEmpty())paragraph("From the scrapbook",16f)
                pieces.forEach {p->
                    when(p.kind){"photo"->image(p.content);"ink"->ink(p.content);else->paragraph(readingCopyElementText(p),if(p.kind=="sticker")22f else 12f)}
                    if(p.sourceCredit.isNotBlank())paragraph("Credit: ${p.sourceCredit}",10f)
                    if(p.sourceUrl.isNotBlank())paragraph("Source: ${p.sourceUrl}",10f)
                }
                if(entry.sourceCredit.isNotBlank())paragraph("Credit: ${entry.sourceCredit}",10f)
                if(entry.sourceUrl.isNotBlank())paragraph("Source: ${entry.sourceUrl}",10f)
                val music=musicAttachments(entry.musicJson)
                if(music.isNotEmpty())paragraph("Music to return to",16f)
                music.forEach {m->paragraph(listOf(m.title,m.creator,m.label,m.url).filter {it.isNotBlank()}.joinToString("\n"))}
                if(entry.tags.isNotBlank())paragraph("Tags: ${entry.tags}",10f)
            }
            finish();target.outputStream().use {pdf.writeTo(it)}
            return Result(count,missing)
        }catch(e:Exception){target.delete();throw e}finally {page?.let {pdf.finishPage(it)};pdf.close()}
    }
    private fun localBitmap(raw:String):Bitmap {
        val uri=Uri.parse(raw);require(uri.scheme in listOf("file","content"))
        val file=File.createTempFile("reading-image-",".tmp",context.cacheDir)
        try {
            context.contentResolver.openInputStream(uri)?.use {input->file.outputStream().use {out->val b=ByteArray(65536);var total=0L;while(true){val n=input.read(b);if(n<0)break;total+=n;require(total<=25L*1024*1024);out.write(b,0,n)}}} ?: error("Image unavailable")
            val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true};BitmapFactory.decodeFile(file.path,bounds)
            require(bounds.outWidth>0 && bounds.outHeight>0 && bounds.outWidth.toLong()*bounds.outHeight<=200_000_000L)
            var sample=1;while(maxOf(bounds.outWidth,bounds.outHeight)/sample>2048)sample*=2
            val bitmap=BitmapFactory.decodeFile(file.path,BitmapFactory.Options().apply {inSampleSize=sample}) ?: error("Image unavailable")
            val orientation=runCatching {ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION,1)}.getOrDefault(1)
            val matrix=Matrix().apply {when(orientation){2->setScale(-1f,1f);3->setRotate(180f);4->setScale(1f,-1f);5->{setRotate(90f);postScale(-1f,1f)};6->setRotate(90f);7->{setRotate(90f);postScale(1f,-1f)};8->setRotate(270f)}}
            if(matrix.isIdentity)return bitmap
            val rotated=Bitmap.createBitmap(bitmap,0,0,bitmap.width,bitmap.height,matrix,true);if(rotated!==bitmap)bitmap.recycle();return rotated
        }finally {file.delete()}
    }
}
