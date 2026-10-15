package com.lilyly.app

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.hypot

/** Coordinates use a saved view box, so ink scales without changing its proportions. */
data class InkPoint(val x:Float,val y:Float,val pressure:Float=1f)
data class InkStroke(val points:List<InkPoint>,val color:Long=0xFF3A2422,val width:Float=5f,val highlighter:Boolean=false)
data class InkDocument(val width:Float=0f,val height:Float=0f,val strokes:List<InkStroke> = emptyList())
fun inkJson(doc:InkDocument):String = JSONObject().put("version",2).put("width",doc.width).put("height",doc.height).put("strokes",JSONArray().apply {
    doc.strokes.forEach {s-> put(JSONObject().put("color",s.color).put("width",s.width).put("highlighter",s.highlighter).put("points",JSONArray().apply {
        s.points.forEach {p->put(JSONObject().put("x",p.x).put("y",p.y).put("pressure",p.pressure))}
    }))}
}).toString()
fun readInk(raw:String):InkDocument = runCatching {
    fun points(a:JSONArray):List<InkPoint> = (0 until a.length()).mapNotNull {i->
        val p=a.optJSONObject(i) ?: return@mapNotNull null
        val x=p.optDouble("x",Double.NaN).toFloat();val y=p.optDouble("y",Double.NaN).toFloat()
        if(!x.isFinite() || !y.isFinite()) null else InkPoint(x,y,p.optDouble("pressure",1.0).toFloat().let {if(it.isFinite())it.coerceIn(.25f,2f) else 1f})
    }
    if(raw.trimStart().startsWith("[")) {
        val a=JSONArray(raw)
        InkDocument(strokes=(0 until a.length()).mapNotNull {i->a.optJSONArray(i)?.let {InkStroke(points(it))}})
    } else {
        val o=JSONObject(raw);require(o.optInt("version")==2)
        val a=o.optJSONArray("strokes") ?: JSONArray()
        InkDocument(o.optDouble("width",0.0).toFloat().let {if(it.isFinite() && it>0)it else 0f},o.optDouble("height",0.0).toFloat().let {if(it.isFinite() && it>0)it else 0f},(0 until a.length()).mapNotNull {i->
            val s=a.optJSONObject(i) ?: return@mapNotNull null
            InkStroke(points(s.optJSONArray("points") ?: JSONArray()),s.optLong("color",0xFF3A2422),s.optDouble("width",5.0).toFloat().let {if(it.isFinite())it.coerceIn(.1f,150f) else 5f},s.optBoolean("highlighter"))
        })
    }
}.getOrDefault(InkDocument())
fun clearInk():String="[]"
fun inkHits(stroke:InkStroke,p:InkPoint,radius:Float):Boolean {
    fun distance(a:InkPoint,b:InkPoint):Float {
        val dx=b.x-a.x;val dy=b.y-a.y;val length=dx*dx+dy*dy
        val t=if(length==0f)0f else (((p.x-a.x)*dx+(p.y-a.y)*dy)/length).coerceIn(0f,1f)
        return hypot(p.x-a.x-t*dx,p.y-a.y-t*dy)
    }
    val reach=radius+stroke.width
    return stroke.points.any {distance(it,it)<=reach} || stroke.points.zipWithNext().any {(a,b)->distance(a,b)<=reach}
}
