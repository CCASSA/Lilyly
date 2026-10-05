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

data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    var section: String = "Journal",
    var title: String = "Untitled",
    var body: String = "",
    var tags: String = "",
    var imageUri: String = "",
    var inkJson: String = "[]",
    var createdAt: String = LocalDateTime.now().toString(),
    var updatedAt: String = LocalDateTime.now().toString(),
    val canvasJson: String = "[]",
    val paper: String = "Parchment",
    val favorite: Boolean = false,
    val notebook: String = ""
) {
    fun toJson() = JSONObject()
        .put("id", id).put("section", section).put("title", title).put("body", body)
        .put("tags", tags).put("imageUri", imageUri).put("inkJson", inkJson)
        .put("createdAt", createdAt).put("updatedAt", updatedAt)
        .put("canvasJson", canvasJson).put("paper", paper).put("favorite", favorite).put("notebook", notebook)

    companion object {
        fun fromJson(o: JSONObject) = JournalEntry(
            id = o.optString("id", UUID.randomUUID().toString()),
            section = o.optString("section", "Journal"),
            title = o.optString("title", "Untitled"),
            body = o.optString("body"),
            tags = o.optString("tags"),
            imageUri = o.optString("imageUri"),
            inkJson = o.optString("inkJson", "[]"),
            createdAt = o.optString("createdAt", LocalDateTime.now().toString()),
            updatedAt = o.optString("updatedAt", LocalDateTime.now().toString()),
            canvasJson = o.optString("canvasJson", "[]"), paper = o.optString("paper", "Parchment"),
            favorite = o.optBoolean("favorite", false), notebook = o.optString("notebook")
        )
    }
}

data class CycleLog(
    val id: String = UUID.randomUUID().toString(),
    var date: String = LocalDate.now().toString(),
    var period: Boolean = false,
    var spotting: Boolean = false,
    var flow: String = "None",
    var mood: Int = 5,
    var pain: Int = 0,
    var energy: Int = 5,
    var sleepHours: Double = 0.0,
    var cravings: String = "",
    var discharge: String = "",
    var libido: Int = 5,
    var symptoms: String = "",
    var sexualActivity: String = "",
    var testNotes: String = "",
    var medicationNotes: String = "",
    var notes: String = "",
    val selections: Set<String> = emptySet(),
    val ratingsRecorded: Boolean = false
) {
    fun toJson() = JSONObject().put("id", id).put("date", date).put("period", period).put("spotting", spotting).put("flow", flow)
        .put("mood", mood).put("pain", pain).put("energy", energy).put("sleepHours", sleepHours)
        .put("cravings", cravings).put("discharge", discharge).put("libido", libido).put("symptoms", symptoms)
        .put("sexualActivity", sexualActivity).put("testNotes", testNotes).put("medicationNotes", medicationNotes).put("notes", notes)
        .put("selections", JSONArray(selections.toList())).put("ratingsRecorded", ratingsRecorded)

    companion object {
        fun fromJson(o: JSONObject) = CycleLog(
            id = o.optString("id", UUID.randomUUID().toString()), date = o.optString("date", LocalDate.now().toString()),
            period = o.optBoolean("period"), spotting = o.optBoolean("spotting"), flow = o.optString("flow", "None"), mood = o.optInt("mood", 5),
            pain = o.optInt("pain"), energy = o.optInt("energy", 5), sleepHours = o.optDouble("sleepHours", 0.0),
            cravings = o.optString("cravings"), discharge = o.optString("discharge"), libido = o.optInt("libido", 5),
            symptoms = o.optString("symptoms"), sexualActivity = o.optString("sexualActivity"), testNotes = o.optString("testNotes"),
            medicationNotes = o.optString("medicationNotes"), notes = o.optString("notes"),
            selections = o.optJSONArray("selections")?.let { a -> (0 until a.length()).map { a.getString(it) }.toSet() } ?: emptySet(),
            ratingsRecorded = o.optBoolean("ratingsRecorded", false)
        )
    }
}

data class MentalCheckIn(
    val id: String = UUID.randomUUID().toString(),
    var dateTime: String = LocalDateTime.now().toString(),
    var mood: Int = 5,
    var anxiety: Int = 5,
    var energy: Int = 5,
    var irritability: Int = 0,
    var emptiness: Int = 0,
    var dissociation: Int = 0,
    var intrusiveThoughts: Int = 0,
    var selfHarmUrge: Int = 0,
    var suicidalThoughts: Int = 0,
    var sleepHours: Double = 0.0,
    var appetite: Int = 5,
    var connection: Int = 5,
    var sensoryOverload: Int = 0,
    var notes: String = "",
    val feelings: Set<String> = emptySet(),
    val detailedRatings: Boolean = true
) {
    fun toJson() = JSONObject().put("id", id).put("dateTime", dateTime).put("mood", mood)
        .put("anxiety", anxiety).put("energy", energy).put("irritability", irritability).put("emptiness", emptiness)
        .put("dissociation", dissociation).put("intrusiveThoughts", intrusiveThoughts).put("selfHarmUrge", selfHarmUrge)
        .put("suicidalThoughts", suicidalThoughts).put("sleepHours", sleepHours).put("appetite", appetite)
        .put("connection", connection).put("sensoryOverload", sensoryOverload).put("notes", notes)
        .put("feelings", JSONArray(feelings.toList())).put("detailedRatings", detailedRatings)

    companion object {
        fun fromJson(o: JSONObject) = MentalCheckIn(
            id = o.optString("id", UUID.randomUUID().toString()),
            dateTime = o.optString("dateTime", LocalDateTime.now().toString()),
            mood = o.optInt("mood", 5), anxiety = o.optInt("anxiety", 5), energy = o.optInt("energy", 5),
            irritability = o.optInt("irritability"), emptiness = o.optInt("emptiness"), dissociation = o.optInt("dissociation"),
            intrusiveThoughts = o.optInt("intrusiveThoughts"), selfHarmUrge = o.optInt("selfHarmUrge"),
            suicidalThoughts = o.optInt("suicidalThoughts"), sleepHours = o.optDouble("sleepHours", 0.0),
            appetite = o.optInt("appetite", 5), connection = o.optInt("connection", 5),
            sensoryOverload = o.optInt("sensoryOverload"), notes = o.optString("notes"),
            feelings = o.optJSONArray("feelings")?.let { a -> (0 until a.length()).map { a.getString(it) }.toSet() } ?: emptySet(),
            detailedRatings = o.optBoolean("detailedRatings", true)
        )
    }
}

data class Medication(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var dose: String = "",
    var time: String = "08:00",
    var reason: String = "",
    var active: Boolean = true
) {
    fun toJson() = JSONObject().put("id", id).put("name", name).put("dose", dose).put("time", time).put("reason", reason).put("active", active)
    companion object {
        fun fromJson(o: JSONObject) = Medication(o.optString("id", UUID.randomUUID().toString()), o.optString("name"), o.optString("dose"), o.optString("time", "08:00"), o.optString("reason"), o.optBoolean("active", true))
    }
}

data class MedicationLog(
    val id: String = UUID.randomUUID().toString(),
    var medicationId: String = "",
    var dateTime: String = LocalDateTime.now().toString(),
    var status: String = "Taken"
) {
    fun toJson() = JSONObject().put("id", id).put("medicationId", medicationId).put("dateTime", dateTime).put("status", status)
    companion object {
        fun fromJson(o: JSONObject) = MedicationLog(o.optString("id", UUID.randomUUID().toString()), o.optString("medicationId"), o.optString("dateTime", LocalDateTime.now().toString()), o.optString("status", "Taken"))
    }
}

data class TherapyNote(
    val id: String = UUID.randomUUID().toString(),
    var date: String = LocalDate.now().toString(),
    var title: String = "Therapy session",
    var before: String = "",
    var after: String = "",
    var homework: String = "",
    var nextAppointment: String = ""
) {
    fun toJson() = JSONObject().put("id", id).put("date", date).put("title", title).put("before", before).put("after", after).put("homework", homework).put("nextAppointment", nextAppointment)
    companion object {
        fun fromJson(o: JSONObject) = TherapyNote(o.optString("id", UUID.randomUUID().toString()), o.optString("date", LocalDate.now().toString()), o.optString("title", "Therapy session"), o.optString("before"), o.optString("after"), o.optString("homework"), o.optString("nextAppointment"))
    }
}

data class IncidentLog(
    val id: String = UUID.randomUUID().toString(),
    var dateTime: String = LocalDateTime.now().toString(),
    var trigger: String = "",
    var urgeBefore: Int = 0,
    var emotions: String = "",
    var injuryOccurred: Boolean = false,
    var bodyArea: String = "",
    var medicalAttention: Boolean = false,
    var medicalCareReceived: Boolean = false,
    var whatHelped: String = "",
    var contacted: String = "",
    var nextTime: String = ""
) {
    fun toJson() = JSONObject().put("id", id).put("dateTime", dateTime).put("trigger", trigger).put("urgeBefore", urgeBefore)
        .put("emotions", emotions).put("injuryOccurred", injuryOccurred).put("bodyArea", bodyArea)
        .put("medicalAttention", medicalAttention).put("medicalCareReceived", medicalCareReceived)
        .put("whatHelped", whatHelped).put("contacted", contacted).put("nextTime", nextTime)
    companion object {
        fun fromJson(o: JSONObject) = IncidentLog(
            o.optString("id", UUID.randomUUID().toString()), o.optString("dateTime", LocalDateTime.now().toString()),
            o.optString("trigger"), o.optInt("urgeBefore"), o.optString("emotions"), o.optBoolean("injuryOccurred"),
            o.optString("bodyArea"), o.optBoolean("medicalAttention"), o.optBoolean("medicalCareReceived"),
            o.optString("whatHelped"), o.optString("contacted"), o.optString("nextTime")
        )
    }
}

data class TarotReading(
    val id: String = UUID.randomUUID().toString(),
    var date: String = LocalDate.now().toString(),
    var title: String = "Tarot reading",
    var cards: String = "",
    var notes: String = "",
    var imageUri: String = "",
    val drawsJson: String = "[]",
    val spread: String = "Free reading",
    val context: String = "",
    val journalId: String = ""
) {
    fun toJson() = JSONObject().put("id", id).put("date", date).put("title", title).put("cards", cards).put("notes", notes).put("imageUri", imageUri)
        .put("drawsJson", drawsJson).put("spread", spread).put("context", context).put("journalId", journalId)
    companion object {
        fun fromJson(o: JSONObject) = TarotReading(o.optString("id", UUID.randomUUID().toString()), o.optString("date", LocalDate.now().toString()), o.optString("title", "Tarot reading"), o.optString("cards"), o.optString("notes"), o.optString("imageUri"), o.optString("drawsJson", "[]"), o.optString("spread", "Free reading"), o.optString("context"), o.optString("journalId"))
    }
}

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

    fun put(name: String, plain: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val payload = Base64.encodeToString(iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP)
        prefs.edit().putString(name, payload).apply()
    }

    fun get(name: String, fallback: String = ""): String {
        val payload = prefs.getString(name, null) ?: return fallback
        return runCatching {
            val parts = payload.split(":", limit = 2)
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val encrypted = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        }.getOrElse { fallback }
    }
}

class AppStore(context: Context) {
    private val secure = SecurePreferences(context.applicationContext)

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
    fun addMedication(item: Medication) { medications.add(item); saveMedications() }
    fun addMedicationLog(item: MedicationLog) { medicationLogs.add(0, item); saveMedications() }
    fun addTherapyNote(item: TherapyNote) { therapyNotes.add(0, item); saveTherapy() }
    fun addIncident(item: IncidentLog) { incidents.add(0, item); saveIncidents() }
    fun addTarotReading(item: TarotReading) { tarotReadings.add(0, item); saveTarot() }

    private fun loadAll() {
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

    private fun saveSettings() = secure.put("settings", JSONObject().put("darkTheme", darkTheme).put("hemisphere", hemisphere).put("safetyPlan", safetyPlan).put("mentalProfile", mentalProfile)
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
