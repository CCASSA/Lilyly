package com.lilyly.app

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class TarotSpread(val id:String=UUID.randomUUID().toString(),val name:String="",val positions:List<String> = listOf("")) {
    fun toJson()=JSONObject().put("id",id).put("name",name).put("positions",JSONArray(positions))
}
val builtInTarotSpreads=listOf(
    TarotSpread("quiet","One quiet question",listOf("A reflection")),
    TarotSpread("lanterns","Three lanterns",listOf("What is here","What needs care","A next step")),
    TarotSpread("bloom","Root & bloom",listOf("The root","What is growing","What to release"))
)
fun tarotSpreadError(spread:TarotSpread):String? = when {
    spread.id.isBlank() || spread.id in builtInTarotSpreads.map {it.id} -> "Choose a personal spread to edit."
    spread.name.isBlank() || spread.name.length>80 -> "Give your spread a name of 1–80 characters."
    spread.positions.size !in 1..6 -> "Use one to six card positions."
    spread.positions.any {it.isBlank() || it.length>120} -> "Each position needs a question or label of 1–120 characters."
    else -> null
}
fun readTarotSpreads(settings:JSONObject):List<TarotSpread> {
    val saved=settings.optJSONObject("tarotSpreads") ?: return emptyList()
    return saved.keys().asSequence().mapNotNull {id ->runCatching {
        val o=saved.getJSONObject(id);val a=o.getJSONArray("positions")
        val spread=TarotSpread(id,o.getString("name"),(0 until a.length()).map {a.getString(it)})
        spread.takeIf {tarotSpreadError(it)==null}
    }.getOrNull()}.toList().sortedWith(compareBy<TarotSpread> {it.name.lowercase()}.thenBy {it.id})
}
fun tarotSpreadsJson(spreads:List<TarotSpread>)=JSONObject().apply {spreads.forEach {put(it.id,it.toJson())}}
