package com.lilyly.app

import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Versioned, authenticated backup envelope. Plaintext is never accepted without a valid GCM tag. */
object BackupCipher {
    private val magic="LILYLY01".toByteArray(Charsets.US_ASCII)
    private const val iterations=600000
    private fun cipher(mode:Int,password:CharArray,salt:ByteArray,nonce:ByteArray):Cipher {
        val spec=PBEKeySpec(password,salt,iterations,256)
        val key=try {SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded} finally {spec.clearPassword()}
        return try {Cipher.getInstance("AES/GCM/NoPadding").apply {init(mode,SecretKeySpec(key,"AES"),GCMParameterSpec(128,nonce));updateAAD(magic+salt+nonce)}} finally {key.fill(0)}
    }
    fun encrypt(input:InputStream,output:OutputStream,password:CharArray) {
        require(password.size>=12) {"Choose a backup password of at least 12 characters"}
        val random=SecureRandom();val salt=ByteArray(16).also(random::nextBytes);val nonce=ByteArray(12).also(random::nextBytes)
        val cipher=cipher(Cipher.ENCRYPT_MODE,password,salt,nonce)
        output.write(magic+salt+nonce)
        transfer(input,output,cipher)
    }
    fun decrypt(input:InputStream,output:OutputStream,password:CharArray) {
        val header=input.readBounded(36)
        require(header.size==36 && header.copyOfRange(0,8).contentEquals(magic)) {"This is not a supported Lilyly backup"}
        transfer(input,output,cipher(Cipher.DECRYPT_MODE,password,header.copyOfRange(8,24),header.copyOfRange(24,36)))
    }
    private fun transfer(input:InputStream,output:OutputStream,cipher:Cipher) {
        val buffer=ByteArray(65536);var total=0L
        while(true) {val count=input.read(buffer);if(count<0)break;total+=count;require(total<=1024L*1024*1024) {"Backup exceeds the 1 GB safety limit"};cipher.update(buffer,0,count)?.let(output::write)}
        output.write(cipher.doFinal());output.flush()
    }
}

val recordArrayKeys=setOf("journal","cycle","mental","medications","medicationLogs","therapy","incidents","tarot","sleep","books","bookNotes")
fun validateSnapshot(records: Map<String,String>) {
    require(records.keys.all {it in recordArrayKeys || it in setOf("settings","tarotCards","nookTheme","sleepMindPatterns","privacy")}) {"This backup contains a newer unsupported data type"}
    records.forEach { (key,value) ->
        if(key in recordArrayKeys) {
            val array=JSONArray(value);require(array.length()<=100000) {"Too many records in backup"}
            repeat(array.length()) { i -> val o=array.getJSONObject(i);require(o.getString("id").isNotBlank())
                when(key) {
                    "journal" -> {JournalEntry.fromJson(o);JSONArray(o.optString("canvasJson","[]"))}
                    "cycle" -> {CycleLog.fromJson(o);java.time.LocalDate.parse(o.getString("date"))}
                    "mental" -> MentalCheckIn.fromJson(o)
                    "medications" -> Medication.fromJson(o)
                    "medicationLogs" -> MedicationLog.fromJson(o)
                    "therapy" -> TherapyNote.fromJson(o)
                    "incidents" -> IncidentLog.fromJson(o)
                    "tarot" -> TarotReading.fromJson(o)
                    "sleep" -> {val r=SleepRecord.fromJson(o);require(sleepMinutes(r.bedtime,r.wakeTime)!=null)}
                    "books" -> LibraryBook.fromJson(o)
                    "bookNotes" -> BookNote.fromJson(o)
                }
            }
        } else if(key in setOf("settings","tarotCards","privacy")) JSONObject(value)
    }
}
/** Current records win on ID/date collisions. Restore never deletes a current record. */
fun mergeSnapshots(current:Map<String,String>,incoming:Map<String,String>):Map<String,String> {
    validateSnapshot(incoming)
    val result=current.toMutableMap()
    incoming.forEach { (key,value) ->
        if(key=="privacy") return@forEach // Never import another device's lock configuration.
        if(key in recordArrayKeys) {
            val old=JSONArray(current[key] ?: "[]");val added=JSONArray(value);val seen=mutableSetOf<String>();val output=JSONArray()
            listOf(old,added).forEach { a -> repeat(a.length()) { i -> val o=a.getJSONObject(i);val identity=o.getString(if(key=="cycle") "date" else "id");if(seen.add(identity))output.put(o) } }
            result[key]=output.toString()
        } else if(key in setOf("settings","tarotCards")) {
            fun merge(old:JSONObject,new:JSONObject):JSONObject {
                val out=JSONObject(new.toString())
                old.keys().forEach { field -> val v=old.get(field);val n=out.opt(field)
                    when {
                        v is JSONObject && n is JSONObject -> out.put(field,merge(v,n))
                        key=="tarotCards" && v is JSONArray && n is JSONArray -> {val unique=linkedSetOf<String>();listOf(v,n).forEach { a -> repeat(a.length()) {unique.add(a.getString(it))} };out.put(field,JSONArray(unique.toList()))}
                        v is String && v.isBlank() && n is String -> Unit
                        else -> out.put(field,v)
                    }
                };return out
            }
            result[key]=merge(JSONObject(current[key] ?: "{}"),JSONObject(value)).toString()
        } else if(key !in current) result[key]=value
    }
    return result
}
