package com.lilyly.app
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
/** Synthetic image fixtures in the test APK only. No production provider is added. */
class ShareFixtureProvider:ContentProvider() {
    override fun onCreate()=true
    override fun getType(uri:Uri)="image/png"
    override fun openFile(uri:Uri,mode:String):ParcelFileDescriptor {
        require(mode=="r" && uri.lastPathSegment in listOf("one.png","two.png","broken.png"))
        return ParcelFileDescriptor.open(File(requireNotNull(context).cacheDir,uri.lastPathSegment!!),ParcelFileDescriptor.MODE_READ_ONLY)
    }
    override fun query(uri:Uri,projection:Array<out String>?,selection:String?,selectionArgs:Array<out String>?,sortOrder:String?):Cursor?=null
    override fun insert(uri:Uri,values:ContentValues?):Uri?=null
    override fun delete(uri:Uri,selection:String?,selectionArgs:Array<out String>?)=0
    override fun update(uri:Uri,values:ContentValues?,selection:String?,selectionArgs:Array<out String>?)=0
}
