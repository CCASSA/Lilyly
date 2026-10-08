package com.lilyly.app

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

data class LilylyPalette(val name: String, val description: String, val dark: Boolean, val background: Long, val surface: Long, val ink: Long, val accent: Long, val rose: Long, val leaf: Long)
val lilylyPalettes = listOf(
    LilylyPalette("Lilyly", "Ink, antique gold and quiet botanicals", true, 0xFF100F16, 0xFF211E29, 0xFFF2E6D4, 0xFFCDB681, 0xFFD4A6B1, 0xFFADBE9B),
    LilylyPalette("Celestial", "Midnight blue, silver and starlight", true, 0xFF0C1325, 0xFF1B2941, 0xFFEBEDF9, 0xFFB9C8FF, 0xFFCFB6E9, 0xFF9BCACD),
    LilylyPalette("Gothic", "Black plum, wine and tarnished gold", true, 0xFF170F17, 0xFF30212F, 0xFFF5E3EA, 0xFFE8A5B8, 0xFFCAB0DC, 0xFFD2BE93),
    LilylyPalette("Cottage Witch", "Warm linen, rosewood and garden sage", false, 0xFFF5EBDD, 0xFFFFF9F0, 0xFF2C221D, 0xFF76572F, 0xFF89515E, 0xFF536742),
    LilylyPalette("Antique Grimoire", "Aged parchment and dark sepia ink", false, 0xFFEDE0C7, 0xFFF8EDD7, 0xFF302719, 0xFF6A482A, 0xFF804B46, 0xFF53623D),
    LilylyPalette("Botanical", "Deep fern, soft ivory and moss", true, 0xFF0E1916, 0xFF20332B, 0xFFE8EEDF, 0xFFB2CE99, 0xFFD8B4AD, 0xFFA0CDCF)
)
val lilylyTypeStyles = listOf("Storybook", "Clear", "Letters")
fun lilylyPalette(name: String, dark: Boolean = true) = lilylyPalettes.firstOrNull {it.name == name} ?: lilylyPalettes.first {it.name == if(dark) "Lilyly" else "Cottage Witch"}
fun paletteScheme(p: LilylyPalette): ColorScheme {
    val ink=Color(p.ink); val surface=Color(p.surface); val accent=Color(p.accent)
    val base=if(p.dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary=accent, onPrimary=Color(p.background), primaryContainer=surface, onPrimaryContainer=ink,
        secondary=Color(p.rose), onSecondary=Color(p.background), secondaryContainer=surface, onSecondaryContainer=ink,
        tertiary=Color(p.leaf), onTertiary=Color(p.background), tertiaryContainer=surface, onTertiaryContainer=ink,
        background=Color(p.background), onBackground=ink, surface=surface, onSurface=ink,
        surfaceVariant=surface, onSurfaceVariant=ink.copy(alpha=.82f), outline=ink.copy(alpha=.55f), outlineVariant=ink.copy(alpha=.28f),
        surfaceTint=accent, inverseSurface=ink, inverseOnSurface=Color(p.background), inversePrimary=Color(p.background),
        surfaceBright=surface, surfaceDim=Color(p.background), surfaceContainer=surface,
        surfaceContainerHigh=surface, surfaceContainerHighest=surface, surfaceContainerLow=Color(p.background), surfaceContainerLowest=Color(p.background)
    )
}
@Composable
fun LilylyTheme(dark: Boolean, themeName: String = "", typeStyle: String = "Storybook", content: @Composable () -> Unit) {
    val base=Typography()
    val heading=if(typeStyle == "Clear") FontFamily.SansSerif else FontFamily.Serif
    val body=if(typeStyle == "Letters") FontFamily.Serif else FontFamily.SansSerif
    MaterialTheme(colorScheme=paletteScheme(lilylyPalette(themeName,dark)), typography=base.copy(
        headlineLarge=base.headlineLarge.copy(fontFamily=heading), headlineMedium=base.headlineMedium.copy(fontFamily=heading),
        headlineSmall=base.headlineSmall.copy(fontFamily=heading), titleLarge=base.titleLarge.copy(fontFamily=heading),
        bodyLarge=base.bodyLarge.copy(fontFamily=body), bodyMedium=base.bodyMedium.copy(fontFamily=body), bodySmall=base.bodySmall.copy(fontFamily=body)
    ), content=content)
}
