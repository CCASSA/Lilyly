package com.lilyly.app

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NightScheme = darkColorScheme(
    primary = Color(0xFFCDB681),
    onPrimary = Color(0xFF281E12),
    secondary = Color(0xFFB08B8F),
    tertiary = Color(0xFF9FAE8A),
    background = Color(0xFF100F16),
    surface = Color(0xFF1B1923),
    surfaceVariant = Color(0xFF2B2535),
    onBackground = Color(0xFFF2E6D4),
    onSurface = Color(0xFFF2E6D4)
)

private val HearthScheme = lightColorScheme(
    primary = Color(0xFF76572F),
    onPrimary = Color.White,
    secondary = Color(0xFF895E65),
    tertiary = Color(0xFF667454),
    background = Color(0xFFF5EBDD),
    surface = Color(0xFFFFF9F0),
    surfaceVariant = Color(0xFFEADCC9),
    onBackground = Color(0xFF2C221D),
    onSurface = Color(0xFF2C221D)
)

@Composable
fun LilylyTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (dark) NightScheme else HearthScheme,
        typography = Typography().let { base -> base.copy(
            headlineLarge = base.headlineLarge.copy(fontFamily = FontFamily.Serif),
            headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif),
            headlineSmall = base.headlineSmall.copy(fontFamily = FontFamily.Serif),
            titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif)
        ) },
        content = content
    )
}
