package com.lilyly.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.util.UUID

class InspirationFiles(private val context:Context) {
    /** Bounded local import, normalized pixels, no retained EXIF metadata or remote fetch. */
    fun importImage(uri:Uri):String {
        require(uri.scheme in listOf("content","file")) {"Choose an image from your device."}
        val stage=File.createTempFile("inspiration-",".image",context.cacheDir)
        var bitmap:Bitmap?=null
        var output:File?=null
        try {
            context.contentResolver.openInputStream(uri)?.use {input ->stage.outputStream().use {out ->
                val buf=ByteArray(65536);var total=0L
                while(true){val n=input.read(buf);if(n<0)break;total+=n;require(total<=25L*1024*1024){"Choose an image smaller than 25 MB."};out.write(buf,0,n)}
            }} ?: error("This image could not be opened.")
            val bounds=BitmapFactory.Options().apply {inJustDecodeBounds=true}
            BitmapFactory.decodeFile(stage.path,bounds)
            require(bounds.outWidth>0 && bounds.outHeight>0 && bounds.outWidth.toLong()*bounds.outHeight<=200_000_000L) {"This file is not a supported image, or is too large."}
            var sample=1
            while(maxOf(bounds.outWidth,bounds.outHeight)/sample>2048)sample*=2
            bitmap=BitmapFactory.decodeFile(stage.path,BitmapFactory.Options().apply {inSampleSize=sample}) ?: error("This image could not be decoded.")
            val orientation=runCatching {ExifInterface(stage.path).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)}.getOrDefault(1)
            val matrix=Matrix().apply {when(orientation){2->setScale(-1f,1f);3->setRotate(180f);4->setScale(1f,-1f);5->{setRotate(90f);postScale(-1f,1f)};6->setRotate(90f);7->{setRotate(90f);postScale(1f,-1f)};8->setRotate(270f)}}
            if(!matrix.isIdentity){val old=bitmap!!;bitmap=Bitmap.createBitmap(old,0,0,old.width,old.height,matrix,true);if(bitmap!==old)old.recycle()}
            val dir=File(context.filesDir,"inspiration-media").apply {mkdirs()}
            output=File(dir,"${UUID.randomUUID()}.png")
            output.outputStream().use {check(bitmap!!.compress(Bitmap.CompressFormat.PNG,100,it)) {"The image could not be saved."}}
            return Uri.fromFile(output).toString()
        } catch(e:Exception) {output?.delete();throw e}
        finally {bitmap?.recycle();stage.delete()}
    }
    fun removeIfUnused(uri:String,store:AppStore) {
        if(uri.isBlank())return
        val used=store.journalEntries.any {it.imageUri==uri || pagePieces(it.canvasJson).any {p->p.kind=="photo" && p.content==uri}} || store.tarotReadings.any {it.imageUri==uri}
        if(used)return
        val parsed=Uri.parse(uri);if(parsed.scheme!="file")return
        val file=File(parsed.path ?: return).canonicalFile
        val owned=File(context.filesDir,"inspiration-media").canonicalFile
        if(file.parentFile==owned)file.delete()
    }
}
