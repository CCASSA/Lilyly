package com.lilyly.app

import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

data class TarotCardInfo(val name: String, val upright: String, val reversed: String, val suit: String = "Major", val number: Int = 0)
private val majorArcana = listOf(
    TarotCardInfo("The Fool", "beginnings, freedom, leap of faith", "recklessness, hesitation, poor judgement"),
    TarotCardInfo("The Magician", "will, skill, manifestation, agency", "manipulation, scattered power, blocked action"),
    TarotCardInfo("The High Priestess", "intuition, mystery, inner knowing", "disconnection from intuition, secrets, avoidance"),
    TarotCardInfo("The Empress", "nurture, creativity, abundance", "creative block, overgiving, neglect of self"),
    TarotCardInfo("The Emperor", "structure, authority, boundaries", "rigidity, control, domination"),
    TarotCardInfo("The Hierophant", "tradition, teaching, shared systems", "rebellion, personal belief, challenging convention"),
    TarotCardInfo("The Lovers", "choice, intimacy, alignment", "disharmony, difficult choice, misaligned values"),
    TarotCardInfo("The Chariot", "direction, determination, movement", "loss of direction, force without control"),
    TarotCardInfo("Strength", "courage, patience, gentle power", "self-doubt, depletion, reactivity"),
    TarotCardInfo("The Hermit", "solitude, reflection, guidance within", "isolation, withdrawal, avoidance"),
    TarotCardInfo("Wheel of Fortune", "cycles, change, turning point", "resistance to change, repeating pattern"),
    TarotCardInfo("Justice", "truth, accountability, balance", "unfairness, denial, consequences avoided"),
    TarotCardInfo("The Hanged Man", "pause, surrender, new perspective", "stagnation, needless sacrifice, refusal to release"),
    TarotCardInfo("Death", "ending, transformation, transition", "clinging, stalled transition, fear of ending"),
    TarotCardInfo("Temperance", "integration, patience, moderation", "imbalance, excess, fragmentation"),
    TarotCardInfo("The Devil", "attachment, compulsion, shadow, bondage", "release, recognition, reclaiming agency"),
    TarotCardInfo("The Tower", "rupture, truth revealed, collapse", "avoided change, internal upheaval, delayed reckoning"),
    TarotCardInfo("The Star", "hope, renewal, openness", "discouragement, disconnection, loss of faith"),
    TarotCardInfo("The Moon", "uncertainty, dream, intuition, illusion", "clarity emerging, fear exposed, confusion lifting"),
    TarotCardInfo("The Sun", "clarity, vitality, joy", "temporary cloud, forced optimism, delayed warmth"),
    TarotCardInfo("Judgement", "reckoning, awakening, calling", "self-judgement, avoidance, refusal to answer"),
    TarotCardInfo("The World", "completion, integration, arrival", "unfinished business, delay, lack of closure")
)

private val rankThemes = listOf(
    "Ace" to ("an opening; notice a small beginning" to "an intention that needs room or care"),
    "Two" to ("partnership and a choice between possibilities" to "imbalance or a choice being postponed"),
    "Three" to ("growth through expression and collaboration" to "friction, exclusion or scattered effort"),
    "Four" to ("a foundation; pause to appreciate what supports you" to "stagnation or a foundation asking for attention"),
    "Five" to ("disruption; name the tension and what is still available" to "recovery or a conflict that remains unresolved"),
    "Six" to ("exchange, recovery and the support of others" to "unequal exchange or difficulty accepting support"),
    "Seven" to ("discernment; consider your options with care" to "doubt, avoidance or a need to simplify"),
    "Eight" to ("movement and sustained practice" to "overextension or movement without direction"),
    "Nine" to ("experience; recognise both progress and your limits" to "depletion or a result that feels incomplete"),
    "Ten" to ("a cycle reaching fullness; consider what follows" to "excess, burden or difficulty ending a cycle"),
    "Page" to ("curiosity, learning and a beginner's perspective" to "inexperience or curiosity without follow-through"),
    "Knight" to ("a quest; put intention into motion" to "impatience, extremes or inconsistent effort"),
    "Queen" to ("cultivation, receptivity and inward confidence" to "overgiving or disconnection from your own needs"),
    "King" to ("stewardship, responsibility and outward confidence" to "rigidity or power that needs accountability")
)
val tarotDeck: List<TarotCardInfo> = majorArcana.mapIndexed { index, c -> c.copy(number = index) } +
    listOf("Wands", "Cups", "Swords", "Pentacles").flatMap { suit ->
        val domain = when(suit) { "Wands" -> "creativity and will"; "Cups" -> "feeling and connection"; "Swords" -> "thought and communication"; else -> "resources and everyday care" }
        rankThemes.mapIndexed { index, (rank, themes) -> TarotCardInfo("$rank of $suit", "In $domain: ${themes.first}.", "In $domain: ${themes.second}.", suit, index + 1) }
    }
fun TarotCardInfo.symbol() = when(suit) { "Wands" -> "❦"; "Cups" -> "∪"; "Swords" -> "✧"; "Pentacles" -> "✦"; else -> listOf("✧", "∞", "☾", "❦", "♜", "✥", "♡", "✶", "∞", "✴", "☸", "⚖", "☥", "❧", "∪", "♑", "ϟ", "✦", "☾", "☀", "✥", "◎")[number] }
fun TarotCardInfo.correspondence() = when(suit) { "Wands" -> "Fire · creativity · action"; "Cups" -> "Water · emotion · relationship"; "Swords" -> "Air · thought · communication"; "Pentacles" -> "Earth · resources · care"; else -> "Major Arcana · a wider life theme" }
fun TarotCardInfo.prompt() = "Where does ${upright.substringBefore(';').lowercase()} meet your life today? What would a kind, practical next step look like?"

data class DrawnCard(val name: String, val reversed: Boolean = false, val position: String = "Reflection") {
    fun toJson() = JSONObject().put("name", name).put("reversed", reversed).put("position", position)
}
fun drawnCards(json: String): List<DrawnCard> = runCatching {
    val a = JSONArray(json); (0 until a.length()).map { a.getJSONObject(it).let { o -> DrawnCard(o.getString("name"), o.optBoolean("reversed"), o.optString("position", "Reflection")) } }
}.getOrDefault(emptyList())
fun drawTarot(positions: List<String>, reversals: Boolean, random: Random = Random.Default): List<DrawnCard> {
    require(positions.size in 1..tarotDeck.size)
    return tarotDeck.shuffled(random).take(positions.size).mapIndexed { i, card -> DrawnCard(card.name, reversals && random.nextBoolean(), positions[i]) }
}
fun List<DrawnCard>.json() = JSONArray(map { it.toJson() }).toString()
