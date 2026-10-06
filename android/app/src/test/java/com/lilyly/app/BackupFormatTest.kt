package com.lilyly.app

import org.junit.Assert.*
import org.junit.Test
import org.json.JSONObject
import org.json.JSONArray
import java.io.ByteArrayOutputStream

class BackupFormatTest {
    private val password="A private garden 2026".toCharArray()
    private fun encrypted(text:String):ByteArray = ByteArrayOutputStream().also {BackupCipher.encrypt(text.byteInputStream(),it,password)}.toByteArray()
    @Test fun authenticatedBackupRoundTripHidesItsPlaintext() {
        val source="A private journal page with intimate notes"
        val encoded=encrypted(source)
        assertFalse(encoded.toString(Charsets.ISO_8859_1).contains("intimate"))
        val decoded=ByteArrayOutputStream();BackupCipher.decrypt(encoded.inputStream(),decoded,password)
        assertEquals(source,decoded.toString(Charsets.UTF_8.name()))
        assertFalse(encoded.contentEquals(encrypted(source)))
    }
    @Test fun wrongPasswordTruncationAndTamperingFailAuthentication() {
        val encoded=encrypted("Keep my notes private")
        assertTrue(runCatching {BackupCipher.decrypt(encoded.inputStream(),ByteArrayOutputStream(),"different password".toCharArray())}.isFailure)
        val changed=encoded.copyOf().apply {this[lastIndex]=(this[lastIndex].toInt() xor 1).toByte()}
        assertTrue(runCatching {BackupCipher.decrypt(changed.inputStream(),ByteArrayOutputStream(),password)}.isFailure)
        assertTrue(runCatching {BackupCipher.decrypt(encoded.dropLast(1).toByteArray().inputStream(),ByteArrayOutputStream(),password)}.isFailure)
    }
    @Test fun mergePreservesCurrentRecordsAndAddsNewOnesWithoutImportingLock() {
        val mine=JournalEntry(id="existing",body="newer local text")
        val old=mine.copy(body="older backup text")
        val added=JournalEntry(id="new",body="restore me")
        val result=mergeSnapshots(mapOf("journal" to JSONArray(listOf(mine.toJson())).toString(),"privacy" to "{\"lock\":false}"),mapOf("journal" to JSONArray(listOf(old.toJson(),added.toJson())).toString(),"privacy" to "{\"lock\":true}"))
        val a=JSONArray(result.getValue("journal"))
        assertEquals(2,a.length());assertEquals("newer local text",a.getJSONObject(0).getString("body"))
        assertFalse(JSONObject(result.getValue("privacy")).getBoolean("lock"))
    }
    @Test fun cycleDatesAreUniqueEvenWhenRecordIdsDiffer() {
        val old=CycleLog(id="a",date="2026-10-01",notes="current")
        val incoming=old.copy(id="b",notes="backup")
        val result=mergeSnapshots(mapOf("cycle" to JSONArray(listOf(old.toJson())).toString()),mapOf("cycle" to JSONArray(listOf(incoming.toJson())).toString()))
        assertEquals(1,JSONArray(result.getValue("cycle")).length())
    }
}
