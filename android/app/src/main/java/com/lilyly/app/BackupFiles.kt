package com.lilyly.app

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

data class PreparedRestore(val directory: File, val records: Map<String,String>) {
    fun discard() {directory.deleteRecursively()}
    fun count() = records.filterKeys {it in recordArrayKeys}.values.sumOf {JSONArray(it).length()}
}
class BackupFiles(private val context:Context) {
    private fun attachments(records:Map<String,String>,transform:(String)->String):Map<String,String> {
        val out=records.toMutableMap()
        listOf("journal","tarot").forEach { key -> records[key]?.let { raw ->
            val a=JSONArray(raw)
            repeat(a.length()) {i -> val o=a.getJSONObject(i)
                val image=o.optString("imageUri");if(image.isNotBlank())o.put("imageUri",transform(image))
                if(key=="journal") {
                    val pieces=JSONArray(o.optString("canvasJson","[]"))
                    repeat(pieces.length()) { j -> val p=pieces.getJSONObject(j);if(p.optString("kind")=="photo" && p.optString("content").isNotBlank())p.put("content",transform(p.getString("content"))) }
                    o.put("canvasJson",pieces.toString())
                }
            };out[key]=a.toString()
        } }
        return out
    }
    fun export(records:Map<String,String>,destination:Uri,password:CharArray) {
        validateSnapshot(records)
        val stage=File(context.cacheDir,"backup-${UUID.randomUUID()}").apply {mkdirs()}
        try {
            val photos=linkedMapOf<String,String>()
            val rewritten=attachments(records) { uri -> "lilyly-backup://media/"+photos.getOrPut(uri) {UUID.randomUUID().toString()} }
            val zip=File(stage,"archive.zip")
            ZipOutputStream(zip.outputStream()).use { out ->
                var total=0L
                fun add(name:String,input:java.io.InputStream) {
                    out.putNextEntry(ZipEntry(name));input.use { stream ->val buffer=ByteArray(65536);while(true) {val n=stream.read(buffer);if(n<0)break;total+=n;require(total<=750L*1024*1024) {"Your backup is larger than the supported 750 MB of content"};out.write(buffer,0,n)}};out.closeEntry()
                }
                val header=JSONObject().put("format","Lilyly").put("version",1).put("createdAt",java.time.LocalDateTime.now().toString()).put("records",JSONObject(rewritten))
                val encodedRecords=header.toString().toByteArray(Charsets.UTF_8)
                require(encodedRecords.size<=16*1024*1024) {"Records exceed the supported 16 MB backup index"}
                require(photos.size + JSONArray(records["books"] ?: "[]").length()*3 + 1<=10000) {"Too many attachments for one backup"}
                add("records.json",encodedRecords.inputStream())
                photos.forEach { (uri,name) ->
                    val parsed=Uri.parse(uri);require(parsed.scheme in listOf("file","content")) {"A photo is not stored locally. Save a local copy before making a full backup."}
                    add("media/$name",context.contentResolver.openInputStream(parsed) ?: error("A journal photo is no longer accessible. Reattach it before making a full backup."))
                }
                val books=JSONArray(records["books"] ?: "[]")
                repeat(books.length()) { i -> val id=books.getJSONObject(i).getString("id");val dir=BookFiles(context).directory(id)
                    require(File(dir,"source").isFile) {"A saved book is missing. Reimport or remove it before making a full backup."}
                    listOf("source","text.json","cover.png").forEach {name ->val f=File(dir,name);if(f.isFile)add("books/$id/$name",f.inputStream())}
                }
            }
            context.contentResolver.openOutputStream(destination,"wt")?.use {out ->zip.inputStream().use {BackupCipher.encrypt(it,out,password)}} ?: error("The backup destination could not be opened")
        } finally {password.fill('\u0000');stage.deleteRecursively()}
    }
    fun prepare(source:Uri,password:CharArray):PreparedRestore {
        val stage=File(context.cacheDir,"restore-${UUID.randomUUID()}").apply {mkdirs()}
        try {
            val zip=File(stage,"authenticated.zip")
            context.contentResolver.openInputStream(source)?.use {input ->zip.outputStream().use {out ->BackupCipher.decrypt(input,out,password)}} ?: error("The selected backup could not be opened")
            // Only extract after decrypt consumed the full input and verified the authentication tag.
            val content=File(stage,"content").apply {mkdirs()}
            ZipFile(zip).use {archive ->
                require(archive.size() in 1..10000) {"Unsupported backup size"}
                val names=mutableSetOf<String>();var total=0L
                archive.entries().asSequence().forEach { entry ->
                    val name=entry.name
                    require(names.add(name) && (name=="records.json" || Regex("media/[a-zA-Z0-9-]+").matches(name) || Regex("books/[a-zA-Z0-9-]+/(source|text\\.json|cover\\.png)").matches(name))) {"Unexpected or unsafe backup file"}
                    val target=File(content,name);target.parentFile!!.mkdirs()
                    archive.getInputStream(entry).use {input ->target.outputStream().use {out ->val b=ByteArray(65536);var size=0L;while(true) {val n=input.read(b);if(n<0)break;size+=n;total+=n;require(total<=750L*1024*1024 && (name!="records.json" || size<=16L*1024*1024)) {"Backup expands beyond the supported size"};out.write(b,0,n)}}}
                }
            }
            val header=JSONObject(File(content,"records.json").readText())
            require(header.getString("format")=="Lilyly" && header.getInt("version")==1) {"Unsupported backup version"}
            val objectRecords=header.getJSONObject("records")
            val records=objectRecords.keys().asSequence().associateWith {objectRecords.getString(it)}
            validateSnapshot(records)
            val rewritten=attachments(records) { uri ->
                require(uri.startsWith("lilyly-backup://media/")) {"Unsupported photo reference in backup"}
                val name=uri.substringAfterLast('/');require(Regex("[a-zA-Z0-9-]+").matches(name) && File(content,"media/$name").isFile) {"A photo is missing from this backup"}
                Uri.fromFile(File(context.filesDir,"restored-media/$name")).toString()
            }
            val books=JSONArray(rewritten["books"] ?: "[]")
            repeat(books.length()) { i ->val b=LibraryBook.fromJson(books.getJSONObject(i));require(Regex("[a-zA-Z0-9-]+").matches(b.id));require(File(content,"books/${b.id}/source").isFile);if(b.format=="EPUB")require(File(content,"books/${b.id}/text.json").isFile)}
            zip.delete()
            return PreparedRestore(stage,rewritten)
        } catch(e:Exception) {stage.deleteRecursively();throw e} finally {password.fill('\u0000')}
    }
    fun installAssets(prepared:PreparedRestore) {
        val content=File(prepared.directory,"content")
        listOf("books" to "books","media" to "restored-media").forEach { (source,target) ->
            File(content,source).walkTopDown().filter {it.isFile}.forEach {f ->
                val destination=File(context.filesDir,"$target/${f.relativeTo(File(content,source)).path}")
                if(!destination.exists()) {destination.parentFile!!.mkdirs();check(f.renameTo(destination)) {"Not enough space to restore attachments"}}
            }
        }
    }
}
