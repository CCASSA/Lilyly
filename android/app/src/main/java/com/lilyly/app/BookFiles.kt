package com.lilyly.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import androidx.core.text.HtmlCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.zip.ZipFile

class BookFiles(private val context: Context) {
    fun directory(id: String): File {
        require(Regex("[a-zA-Z0-9-]+").matches(id))
        return File(context.filesDir,"books/$id")
    }
    fun cover(id: String) = File(directory(id),"cover.png")
    fun importBook(uri: Uri): LibraryBook {
        val resolver = context.contentResolver
        val filename = resolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use { c -> if(c.moveToFirst()) c.getString(0) else null } ?: "Untitled book"
        val id = UUID.randomUUID().toString()
        val dir = directory(id).apply { mkdirs() }
        try {
            val source = File(dir,"source")
            resolver.openInputStream(uri)?.use { input -> source.outputStream().use { out ->
                val buffer = ByteArray(65536); var total = 0L
                while(true) { val count = input.read(buffer); if(count < 0) break; total += count; require(total <= 100L*1024*1024) { "Choose a book smaller than 100 MB" }; out.write(buffer,0,count) }
            } } ?: error("This document could not be opened")
            val signature = source.inputStream().use { it.readBounded(5) }
            if(signature.toString(Charsets.US_ASCII).startsWith("%PDF-")) {
                val count = renderer(source).use { pdf -> require(pdf.pageCount > 0); pdf.pageCount }
                val bitmap = renderPdf(source,0,360)
                cover(id).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,90,it) }; bitmap.recycle()
                return LibraryBook(id,filename.substringBeforeLast('.'),format="PDF",units=count)
            }
            require(signature.size >= 2 && signature[0] == 80.toByte() && signature[1] == 75.toByte()) { "Choose a PDF or DRM-free EPUB" }
            val epub = ZipFile(source).use { EpubPackage(it).parse() }
            val units = mutableListOf<ReadingUnit>()
            epub.chapters.forEach { chapter ->
                val cleaned = chapter.html.replace(Regex("<(script|style)\\b[^>]*>.*?</\\1\\s*>", setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL)), "")
                val text = HtmlCompat.fromHtml(cleaned,HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
                if(text.isNotBlank()) text.chunked(6000).forEachIndexed { i, part -> units.add(ReadingUnit(if(i == 0) chapter.title else "${chapter.title} · continued",part)) }
            }
            require(units.isNotEmpty()) { "This EPUB has no readable text. Image-only or fixed-layout editions are not supported yet." }
            File(dir,"text.json").writeText(JSONArray(units.map { JSONObject().put("title",it.title).put("text",it.text) }).toString())
            epub.cover?.let { bytes ->
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds=true }; BitmapFactory.decodeByteArray(bytes,0,bytes.size,bounds)
                if(bounds.outWidth > 0 && bounds.outHeight > 0) {
                    val options = BitmapFactory.Options().apply { inSampleSize = (maxOf(bounds.outWidth,bounds.outHeight)/600).coerceAtLeast(1) }
                    BitmapFactory.decodeByteArray(bytes,0,bytes.size,options)?.let { bitmap -> cover(id).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,90,it) }; bitmap.recycle() }
                }
            }
            return LibraryBook(id,epub.title.ifBlank { filename.substringBeforeLast('.') },epub.author,"EPUB",units.size)
        } catch(e: Exception) { dir.deleteRecursively(); throw e }
    }
    fun text(id: String): List<ReadingUnit> {
        val a = JSONArray(File(directory(id),"text.json").readText())
        return (0 until a.length()).map { a.getJSONObject(it).let { o -> ReadingUnit(o.getString("title"),o.getString("text")) } }
    }
    fun pdf(id: String, page: Int, width: Int = 1200): Bitmap = renderPdf(File(directory(id),"source"),page,width)
    private fun renderer(file: File): PdfRenderer {
        val fd = ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY)
        return try { PdfRenderer(fd) } catch(e: Exception) { fd.close(); throw e }
    }
    private fun renderPdf(file: File, index: Int, width: Int): Bitmap = renderer(file).use { pdf ->
        pdf.openPage(index).use { page ->
            val w = width.coerceIn(240,1600)
            val h = (w.toLong()*page.height/page.width.coerceAtLeast(1)).coerceIn(1,3000).toInt()
            Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888).also { bitmap ->
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            }
        }
    }
}
