package com.lilyly.app

import java.time.LocalDate

val dayThreadCategories=listOf("Cycle","Sanctuary","Sleep","Medication","Pages","Therapy","Tarot","Rituals")
data class DayThread(val key:String,val date:String,val category:String,val title:String,val detail:String,val pageId:String="")
/** Dates reflect recorded local dates; no timezone or missing-observation inference. */
fun dayThreads(cycles:List<CycleLog>,mind:List<MentalCheckIn>,sleeps:List<SleepRecord>,medications:List<Medication>,doses:List<MedicationLog>,pages:List<JournalEntry>,therapy:List<TherapyNote>,tarot:List<TarotReading>):List<DayThread> = buildList {
    cycles.forEach {r -> add(DayThread("cycle:${r.id}",r.date,"Cycle","Your cycle log",buildList {
        add("Flow · ${r.flow}")
        if(r.spotting)add("Spotting")
        if(r.selections.isNotEmpty())add(r.selections.sorted().joinToString(" · "))
        if(r.symptoms.isNotBlank())add(r.symptoms)
        if(r.ratingsRecorded)add("Mood ${r.mood}/10 · energy ${r.energy}/10 · pain ${r.pain}/10")
        if(r.notes.isNotBlank())add(r.notes)
    }.joinToString("\n"))) }
    mind.forEach {r -> add(DayThread("mind:${r.id}",r.dateTime.take(10),"Sanctuary","Check-in · ${r.dateTime.drop(11).take(5)}",buildList {
        if(r.feelings.isNotEmpty())add(r.feelings.sorted().joinToString(" · "))
        if(r.detailedRatings)add("Mood ${r.mood}/10 · anxiety ${r.anxiety}/10 · energy ${r.energy}/10")
        if(r.notes.isNotBlank())add(r.notes)
    }.joinToString("\n").ifBlank {"A check-in kept."})) }
    sleeps.forEach {r -> add(DayThread("sleep:${r.id}",r.date,"Sleep","${sleepDurationLabel(r.durationMinutes)} of recorded rest",buildList {
        add("Wake date · ${r.wakeTime.take(10)}")
        if(r.quality.isNotBlank())add(r.quality)
        if(r.dream.isNotBlank())add("Dream · ${r.dream}")
        if(r.wakingEnergy.isNotBlank())add("Waking energy · ${r.wakingEnergy}")
    }.joinToString("\n"),r.journalId)) }
    doses.forEach {r -> add(DayThread("dose:${r.id}",r.dateTime.take(10),"Medication",medications.firstOrNull {it.id==r.medicationId}?.name ?: "Medication record","${r.status} · ${r.dateTime.drop(11).take(5)}${if(r.scheduledFor.isNotBlank()) "\nScheduled for ${r.scheduledFor}" else ""}${if(r.notes.isNotBlank()) "\n${r.notes}" else ""}")) }
    pages.filter {it.section!="Templates"}.forEach {p ->
        add(DayThread("page:${p.id}",p.createdAt.take(10),"Pages",p.title.ifBlank {"Untitled page"},"${p.section} · created this day${if(p.notebook.isNotBlank()) " · ${p.notebook}" else ""}",p.id))
        if(p.ritualJson.isNotBlank()) {
            val r=RitualDetails.fromJson(p.ritualJson)
            if(r.plannedDate.isNotBlank())add(DayThread("plan:${p.id}",r.plannedDate,"Rituals","Planned · ${p.title}",r.intention,p.id))
            if(r.completedDate.isNotBlank())add(DayThread("ritual:${p.id}",r.completedDate,"Rituals","Practised · ${p.title}",r.reflection.ifBlank {r.intention},p.id))
        }
    }
    therapy.forEach {r -> add(DayThread("therapy:${r.id}",r.date,"Therapy",r.title,listOf(r.before,r.after).filter {it.isNotBlank()}.joinToString("\n").ifBlank {"A session note kept."},r.journalId)) }
    tarot.forEach {r -> add(DayThread("tarot:${r.id}",r.date,"Tarot",r.title,"${r.spread}\n${r.cards}",r.journalId)) }
}.filter {runCatching {LocalDate.parse(it.date)}.isSuccess}
