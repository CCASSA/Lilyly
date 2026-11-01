package com.lilyly.app

import org.json.JSONArray
import org.json.JSONObject
import java.time.OffsetDateTime
import java.util.UUID

val therapyGoalStates=listOf("Growing","Resting","Reached")
data class GoalReflection(val id:String=UUID.randomUUID().toString(),val recordedAt:String=OffsetDateTime.now().toString(),val state:String="Growing",val body:String="") {
    fun toJson()=JSONObject().put("id",id).put("recordedAt",recordedAt).put("state",state).put("body",body)
    companion object {
        fun fromJson(o:JSONObject):GoalReflection {
            val row=GoalReflection(o.getString("id"),o.getString("recordedAt"),o.getString("state"),o.optString("body"))
            require(row.id.isNotBlank() && row.state in therapyGoalStates && row.body.length<=8000)
            OffsetDateTime.parse(row.recordedAt)
            return row
        }
    }
}
data class TherapyGoal(val id:String=UUID.randomUUID().toString(),val title:String="",val intention:String="",val reflections:List<GoalReflection> = emptyList(),val journalId:String="") {
    val state:String get()=reflections.lastOrNull()?.state ?: "Growing"
    fun toJson()=JSONObject().put("id",id).put("title",title).put("intention",intention).put("reflections",JSONArray(reflections.map {it.toJson()})).put("journalId",journalId)
    companion object {
        fun fromJson(o:JSONObject):TherapyGoal {
            val rows=o.optJSONArray("reflections") ?: JSONArray()
            val result=TherapyGoal(o.getString("id"),o.getString("title"),o.optString("intention"),(0 until rows.length()).map {GoalReflection.fromJson(rows.getJSONObject(it))}.distinctBy {it.id},o.optString("journalId"))
            require(result.id.isNotBlank() && result.title.isNotBlank() && result.title.length<=160 && result.intention.length<=4000)
            return result
        }
    }
}
fun keepGoalReflection(goal:TherapyGoal,row:GoalReflection):TherapyGoal {
    GoalReflection.fromJson(row.toJson())
    return if(goal.reflections.any {it.id==row.id})goal else goal.copy(reflections=goal.reflections+row)
}
fun removeGoalReflection(goal:TherapyGoal,id:String)=goal.copy(reflections=goal.reflections.filterNot {it.id==id})
