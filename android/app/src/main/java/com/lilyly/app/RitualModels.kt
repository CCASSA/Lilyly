package com.lilyly.app

import org.json.JSONObject
import java.time.LocalDate

data class RitualDetails(
    val intention:String="", val materials:String="", val steps:String="",
    val plannedDate:String="", val completedDate:String="", val moon:String="", val reflection:String=""
) {
    fun toJson()=JSONObject().put("intention",intention).put("materials",materials).put("steps",steps)
        .put("plannedDate",plannedDate).put("completedDate",completedDate).put("moon",moon).put("reflection",reflection).toString()
    companion object {
        fun fromJson(raw:String):RitualDetails {
            val o=runCatching {JSONObject(raw)}.getOrDefault(JSONObject())
            return RitualDetails(o.optString("intention"),o.optString("materials"),o.optString("steps"),o.optString("plannedDate"),o.optString("completedDate"),o.optString("moon"),o.optString("reflection"))
        }
    }
}
fun ritualSummary(r:RitualDetails)=buildString {
    append("Intention\n${r.intention}\n\n")
    if(r.plannedDate.isNotBlank()) append("Planned · ${r.plannedDate}\n")
    if(r.completedDate.isNotBlank()) append("Practised · ${r.completedDate}\n${r.moon}\n")
    if(r.materials.isNotBlank()) append("\nWhat I gathered\n${r.materials}\n")
    if(r.steps.isNotBlank()) append("\nMy practice\n${r.steps}\n")
    if(r.reflection.isNotBlank()) append("\nWhat I noticed\n${r.reflection}")
}
fun ritualStatus(r:RitualDetails,today:LocalDate=LocalDate.now()):String = when {
    r.completedDate.isNotBlank() -> "Practised · ${r.completedDate}"
    r.plannedDate==today.toString() -> "For today"
    r.plannedDate.isNotBlank() -> "Planned · ${r.plannedDate}"
    else -> "An intention taking shape"
}
fun repeatRitual(page:JournalEntry):JournalEntry {
    val r=RitualDetails.fromJson(page.ritualJson)
    return freshPage(page,"Spells",page.notebook).copy(title=page.title,body="",imageUri="",inkJson="[]",canvasJson="[]",
        ritualJson=r.copy(plannedDate="",completedDate="",moon="",reflection="").toJson())
}
val ritualStarters=listOf(
    "A quiet beginning" to RitualDetails(intention="Make space for a new beginning.",materials="A page and something to write with",steps="Settle somewhere comfortable.\nName what you would like to welcome.\nWrite one small action you can take.\nClose the page when you feel ready."),
    "A threshold of rest" to RitualDetails(intention="Mark the end of the day.",materials="A familiar object and a quiet corner",steps="Place the object near you.\nNotice what you are carrying from today.\nWrite what can wait until tomorrow.\nChoose one gentle way to settle."),
    "A seasonal page" to RitualDetails(intention="Notice the season where I live.",materials="A photograph, sketch or fallen leaf",steps="Observe one change in your surroundings.\nKeep an image or a few words.\nWrite what this season means to you.")
)
