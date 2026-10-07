package com.lilyly.app

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private class SecurePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("lilyly_secure", Context.MODE_PRIVATE)
    private val keyAlias = "lilyly_local_data_key"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun key(): SecretKey {
        val existing = keyStore.getKey(keyAlias, null) as? SecretKey
        if (existing != null) return existing
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private fun encode(plain: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        return Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(cipher.doFinal(plain.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }
    private fun decode(payload: String): String {
        val parts = payload.split(":", limit = 2)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
        return String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8)
    }
    fun put(name: String, plain: String) { prefs.edit().putString(name, encode(plain)).apply() }
    fun get(name: String, fallback: String = ""): String {
        val payload = prefs.getString(name, null) ?: return fallback
        return runCatching { decode(payload) }.getOrElse { fallback }
    }
    fun snapshot(): Map<String,String> = prefs.all.mapValues { (_, value) -> decode(value as String) }
    fun writeBatch(values: Map<String,String>) {
        val encrypted = values.mapValues { encode(it.value) }
        val edit = prefs.edit()
        encrypted.forEach { (key,value) -> edit.putString(key,value) }
        check(edit.commit()) { "The restored records could not be written" }
    }

}

class AppStore(context: Context) {
    private val appContext = context.applicationContext
    private val secure = SecurePreferences(appContext)

    var privacyBusy by mutableStateOf(false)
    var privacyMessage by mutableStateOf("")
    var appLockEnabled by mutableStateOf(false)
        private set
    var hideScreenshots by mutableStateOf(false)
        private set
    fun updatePrivacy(lock: Boolean = appLockEnabled, screenshots: Boolean = hideScreenshots) {
        appLockEnabled = lock; hideScreenshots = screenshots
        secure.put("privacy", JSONObject().put("lock",lock).put("screenshots",screenshots).toString())
    }
    fun backupSnapshot(): Map<String,String> = secure.snapshot()
    suspend fun restoreSnapshot(incoming: Map<String,String>) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { secure.writeBatch(mergeSnapshots(secure.snapshot(),incoming)) }
        journalEntries.clear(); cycleLogs.clear(); mentalCheckIns.clear(); medications.clear(); medicationLogs.clear()
        therapyNotes.clear(); incidents.clear(); tarotReadings.clear(); tarotFavorites.clear(); tarotNotes=emptyMap()
        sleepRecords.clear(); books.clear(); bookNotes.clear()
        loadAll()
        MedicationReminders.schedule(appContext,medications,medicationLogs)
    }
    val journalEntries = mutableStateListOf<JournalEntry>()
    val cycleLogs = mutableStateListOf<CycleLog>()
    val mentalCheckIns = mutableStateListOf<MentalCheckIn>()
    val medications = mutableStateListOf<Medication>()
    val medicationLogs = mutableStateListOf<MedicationLog>()
    val therapyNotes = mutableStateListOf<TherapyNote>()
    val incidents = mutableStateListOf<IncidentLog>()
    val sleepRecords = mutableStateListOf<SleepRecord>()
    var includeMindInSleepPatterns by mutableStateOf(false)
        private set
    fun setSleepMindPatterns(value: Boolean) { includeMindInSleepPatterns=value;secure.put("sleepMindPatterns",value.toString()) }
    fun saveSleep(record: SleepRecord) {
        require(sleepMinutes(record.bedtime,record.wakeTime) != null)
        val i=sleepRecords.indexOfFirst { it.id==record.id }
        if(i<0) sleepRecords.add(0,record) else sleepRecords[i]=record
        saveArray("sleep",sleepRecords.map { it.toJson() })
    }
    fun deleteSleep(id: String) { sleepRecords.removeAll { it.id==id };saveArray("sleep",sleepRecords.map { it.toJson() }) }
    fun journalDream(record: SleepRecord): JournalEntry {
        journalEntries.firstOrNull { it.id==record.journalId }?.let { return it }
        val page=JournalEntry(section="Dreams",title="Dream · ${record.date}",body="${record.dream}\n\n${record.dreamMood} · ${record.symbols.joinToString(" · ")}\nPeople: ${record.people}\nPlaces: ${record.places}\n\n${sleepDurationLabel(record.durationMinutes)} rest · ${record.quality}\n${record.notes}",tags=record.symbols.joinToString(", "),paper="Midnight")
        upsertJournal(page);saveSleep(record.copy(journalId=page.id));return page
    }
    val books = mutableStateListOf<LibraryBook>()
    val bookNotes = mutableStateListOf<BookNote>()
    var nookTheme by mutableStateOf("Cottage")
        private set
    fun updateNookTheme(value: String) { nookTheme = value; secure.put("nookTheme", value) }
    fun saveBook(book: LibraryBook) {
        val i = books.indexOfFirst { it.id == book.id }
        if(i < 0) books.add(0,book) else books[i] = book
        saveArray("books", books.map { it.toJson() })
    }
    fun removeBook(id: String) {
        books.removeAll { it.id == id }; bookNotes.removeAll { it.bookId == id }
        saveArray("books", books.map { it.toJson() }); saveArray("bookNotes", bookNotes.map { it.toJson() })
    }
    fun saveBookNote(note: BookNote) {
        val i = bookNotes.indexOfFirst { it.id == note.id }
        if(i < 0) bookNotes.add(0,note) else bookNotes[i] = note
        saveArray("bookNotes",bookNotes.map { it.toJson() })
    }
    fun journalBookNote(note: BookNote): JournalEntry {
        journalEntries.firstOrNull { it.id == note.journalId }?.let { return it }
        val book = books.first { it.id == note.bookId }
        val page = JournalEntry(title = "From ${book.title}", body = "${note.quote}\n\n${note.note}\n\n— ${book.title}, ${book.author} · ${if(book.format == "PDF") "page" else "section"} ${note.position+1}", tags = "reading, quote", notebook = "Commonplace book", paper = "Parchment")
        upsertJournal(page); saveBookNote(note.copy(journalId = page.id)); return page
    }
    val tarotReadings = mutableStateListOf<TarotReading>()
    val tarotFavorites = mutableStateListOf<String>()
    var tarotNotes by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    fun updateTarotCard(name: String, note: String, favorite: Boolean) {
        tarotNotes = tarotNotes + (name to note)
        tarotFavorites.remove(name)
        if (favorite) tarotFavorites.add(name)
        secure.put("tarotCards", JSONObject().put("notes", JSONObject(tarotNotes)).put("favorites", JSONArray(tarotFavorites)).toString())
    }
    fun saveTarotReading(item: TarotReading) {
        val index = tarotReadings.indexOfFirst { it.id == item.id }
        if (index < 0) tarotReadings.add(0, item) else tarotReadings[index] = item
        saveTarot()
    }
    fun journalReading(reading: TarotReading): JournalEntry {
        val existing = journalEntries.firstOrNull { it.id == reading.journalId }
        if (existing != null) return existing
        val page = JournalEntry(section = "Grimoire", title = reading.title,
            body = "${reading.date} · ${reading.spread}\n${reading.context}\n\n${reading.cards}\n\n${reading.notes}", tags = "tarot", paper = "Midnight",
            canvasJson = JSONArray(drawnCards(reading.drawsJson).mapIndexed { i, card ->
                JSONObject().put("id", java.util.UUID.randomUUID().toString()).put("kind", "tarot")
                    .put("content", card.toJson().toString())
                    .put("x", .04 + i * .31).put("y", .30).put("width", .28).put("rotation", 0)
            }).toString())
        upsertJournal(page)
        saveTarotReading(reading.copy(journalId = page.id))
        return page
    }

    var homeSections by mutableStateOf(homeSectionNames.toSet())
        private set
    var greetingName by mutableStateOf("")
        private set
    fun personalizeHome(name: String, sections: Set<String>) { greetingName=name.take(40);homeSections=sections.intersect(homeSectionNames.toSet());saveSettings() }

    var darkTheme by mutableStateOf(true)
        private set
    var hemisphere by mutableStateOf("Southern")
        private set
    var safetyPlan by mutableStateOf("")
        private set
    var mentalProfile by mutableStateOf("")
        private set

    var cyclePreferences by mutableStateOf(CyclePreferences())
        private set

    fun updateCyclePreferences(value: CyclePreferences) { cyclePreferences = value; saveSettings() }

    init { loadAll() }

    fun setTheme(dark: Boolean) { darkTheme = dark; saveSettings() }
    fun updateHemisphere(value: String) { hemisphere = value; saveSettings() }
    fun updateSafetyPlan(value: String) { safetyPlan = value; saveSettings() }
    fun updateMentalProfile(value: String) { mentalProfile = value; saveSettings() }

    fun upsertJournal(entry: JournalEntry) {
        entry.updatedAt = LocalDateTime.now().toString()
        val index = journalEntries.indexOfFirst { it.id == entry.id }
        if (index >= 0) journalEntries[index] = entry else journalEntries.add(0, entry)
        saveJournal()
    }

    fun deleteJournal(id: String) { journalEntries.removeAll { it.id == id }; saveJournal() }

    fun upsertCycle(log: CycleLog) {
        val index = cycleLogs.indexOfFirst { it.date == log.date }
        if (index >= 0) cycleLogs[index] = log else cycleLogs.add(0, log)
        saveCycle()
    }

    fun addMentalCheckIn(item: MentalCheckIn) { mentalCheckIns.add(0, item); saveMental() }
    fun addMedication(item: Medication) {
        val i=medications.indexOfFirst {it.id==item.id}
        if(i<0) medications.add(item) else medications[i]=item
        saveMedications(); MedicationReminders.schedule(appContext,medications,medicationLogs)
    }
    fun addMedicationLog(item: MedicationLog) {
        val i=medicationLogs.indexOfFirst {it.id==item.id || (item.scheduledFor.isNotBlank() && it.medicationId==item.medicationId && it.scheduledFor==item.scheduledFor)}
        if(i<0) medicationLogs.add(0,item) else medicationLogs[i]=item.copy(id=medicationLogs[i].id)
        saveMedications(); MedicationReminders.schedule(appContext,medications,medicationLogs)
    }
    fun removeMedicationLog(id:String) {medicationLogs.removeAll {it.id==id};saveMedications();MedicationReminders.schedule(appContext,medications,medicationLogs)}
    fun addTherapyNote(item: TherapyNote) { therapyNotes.add(0, item); saveTherapy() }
    fun addIncident(item: IncidentLog) { incidents.add(0, item); saveIncidents() }
    fun addTarotReading(item: TarotReading) { tarotReadings.add(0, item); saveTarot() }

    private fun loadAll() {
        runCatching {
            val privacy = JSONObject(secure.get("privacy", "{}"))
            appLockEnabled=privacy.optBoolean("lock");hideScreenshots=privacy.optBoolean("screenshots")
        }
        runCatching {
            val settings = JSONObject(secure.get("settings", "{}"))
            val c = settings.optJSONObject("cyclePreferences") ?: JSONObject()
            cyclePreferences = CyclePreferences(
                length = c.optInt("length", 28).coerceIn(15, 90),
                periodLength = c.optInt("periodLength", 5).coerceIn(1, 10),
                lutealLength = c.optInt("lutealLength", 14).coerceIn(10, 18),
                pmsDays = c.optInt("pmsDays", 5).coerceIn(1, 10),
                mode = c.optString("mode", "Cycle"), contraception = c.optString("contraception", "None"),
                useHistory = c.optBoolean("useHistory", true)
            )
            greetingName = settings.optString("greetingName", "")
            homeSections = settings.optJSONArray("homeSections")?.let { a -> (0 until a.length()).map { a.getString(it) }.toSet().intersect(homeSectionNames.toSet()) } ?: homeSectionNames.toSet()
            darkTheme = settings.optBoolean("darkTheme", true)
            hemisphere = settings.optString("hemisphere", "Southern")
            safetyPlan = settings.optString("safetyPlan", "")
            mentalProfile = settings.optString("mentalProfile", "")
        }
        loadArray("journal") { journalEntries.add(JournalEntry.fromJson(it)) }
        loadArray("cycle") { cycleLogs.add(CycleLog.fromJson(it)) }
        loadArray("mental") { mentalCheckIns.add(MentalCheckIn.fromJson(it)) }
        loadArray("medications") { medications.add(Medication.fromJson(it)) }
        loadArray("medicationLogs") { medicationLogs.add(MedicationLog.fromJson(it)) }
        loadArray("therapy") { therapyNotes.add(TherapyNote.fromJson(it)) }
        loadArray("incidents") { incidents.add(IncidentLog.fromJson(it)) }
        loadArray("tarot") { tarotReadings.add(TarotReading.fromJson(it)) }
        runCatching {
            val cards = JSONObject(secure.get("tarotCards", "{}"))
            val notes = cards.optJSONObject("notes") ?: JSONObject()
            tarotNotes = notes.keys().asSequence().associateWith { notes.getString(it) }
            cards.optJSONArray("favorites")?.let { a -> (0 until a.length()).forEach { tarotFavorites.add(a.getString(it)) } }
        }
        loadArray("sleep") { sleepRecords.add(SleepRecord.fromJson(it)) }
        includeMindInSleepPatterns = secure.get("sleepMindPatterns", "false").toBoolean()
        loadArray("books") { books.add(LibraryBook.fromJson(it)) }
        loadArray("bookNotes") { bookNotes.add(BookNote.fromJson(it)) }
        nookTheme = secure.get("nookTheme", "Cottage")
        if (journalEntries.isEmpty()) seedSamples()
    }

    private fun seedSamples() {
        journalEntries.add(JournalEntry(section = "Journal", title = "Welcome to Lilyly", body = "This is a sample page. Lilyly is your private library for words, magic, body, mind, books, memories and everything in between. Delete this whenever you are ready to begin.", tags = "sample, welcome"))
        journalEntries.add(JournalEntry(section = "Grimoire", title = "A blank spell begins here", body = "Intention • ingredients • steps • moon phase • notes • what happened afterward", tags = "sample, grimoire"))
        saveJournal()
    }

    private fun saveSettings() = secure.put("settings", JSONObject().put("darkTheme", darkTheme).put("hemisphere", hemisphere).put("safetyPlan", safetyPlan).put("mentalProfile", mentalProfile).put("greetingName",greetingName).put("homeSections",JSONArray(homeSections.toList()))
        .put("cyclePreferences", JSONObject().put("length", cyclePreferences.length).put("periodLength", cyclePreferences.periodLength)
            .put("lutealLength", cyclePreferences.lutealLength).put("pmsDays", cyclePreferences.pmsDays)
            .put("mode", cyclePreferences.mode).put("contraception", cyclePreferences.contraception).put("useHistory", cyclePreferences.useHistory)).toString())
    private fun saveJournal() = saveArray("journal", journalEntries.map { it.toJson() })
    private fun saveCycle() = saveArray("cycle", cycleLogs.map { it.toJson() })
    private fun saveMental() = saveArray("mental", mentalCheckIns.map { it.toJson() })
    private fun saveMedications() {
        saveArray("medications", medications.map { it.toJson() })
        saveArray("medicationLogs", medicationLogs.map { it.toJson() })
    }
    private fun saveTherapy() = saveArray("therapy", therapyNotes.map { it.toJson() })
    private fun saveIncidents() = saveArray("incidents", incidents.map { it.toJson() })
    private fun saveTarot() = saveArray("tarot", tarotReadings.map { it.toJson() })

    private fun saveArray(key: String, objects: List<JSONObject>) {
        val arr = JSONArray(); objects.forEach { arr.put(it) }; secure.put(key, arr.toString())
    }

    private fun loadArray(key: String, consume: (JSONObject) -> Unit) {
        runCatching {
            val arr = JSONArray(secure.get(key, "[]"))
            for (i in 0 until arr.length()) consume(arr.getJSONObject(i))
        }
    }
}
