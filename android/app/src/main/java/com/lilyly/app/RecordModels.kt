package com.lilyly.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

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
    var active: Boolean = true,
    var asNeeded: Boolean = false,
    var reminders: Boolean = false,
    var remaining: Int = -1,
    var refillAt: Int = 5
) {
    fun toJson() = JSONObject().put("id", id).put("name", name).put("dose", dose).put("time", time).put("reason", reason).put("active", active).put("asNeeded",asNeeded).put("reminders",reminders).put("remaining",remaining).put("refillAt",refillAt)
    companion object {
        fun fromJson(o: JSONObject) = Medication(o.optString("id", UUID.randomUUID().toString()), o.optString("name"), o.optString("dose"), o.optString("time", "08:00"), o.optString("reason"), o.optBoolean("active", true),o.optBoolean("asNeeded"),o.optBoolean("reminders"),o.optInt("remaining",-1),o.optInt("refillAt",5))
    }
}

data class MedicationLog(
    val id: String = UUID.randomUUID().toString(),
    var medicationId: String = "",
    var dateTime: String = LocalDateTime.now().toString(),
    var status: String = "Taken",
    var scheduledFor: String = "",
    var notes: String = ""
) {
    fun toJson() = JSONObject().put("id", id).put("medicationId", medicationId).put("dateTime", dateTime).put("status", status).put("scheduledFor",scheduledFor).put("notes",notes)
    companion object {
        fun fromJson(o: JSONObject) = MedicationLog(o.optString("id", UUID.randomUUID().toString()), o.optString("medicationId"), o.optString("dateTime", LocalDateTime.now().toString()), o.optString("status", "Taken"),o.optString("scheduledFor"),o.optString("notes"))
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

