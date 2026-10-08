package com.lilyly.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun ThemePicker(store: AppStore) {
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
        Text("Make yourself at home", style=MaterialTheme.typography.headlineMedium)
        Text("Choose the atmosphere for your Lilyly. Your page paper and Reading Nook room keep their own styles.", style=MaterialTheme.typography.bodyMedium)
        LazyRow(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            items(lilylyPalettes) { palette ->
                val chosen=palette.name==store.themeName
                Column(Modifier.width(224.dp).clip(RoundedCornerShape(topStart=32.dp,bottomEnd=32.dp))
                    .background(Color(palette.background))
                    .border(if(chosen) 2.dp else 1.dp,Color(palette.accent).copy(alpha=if(chosen) 1f else .45f),RoundedCornerShape(topStart=32.dp,bottomEnd=32.dp))
                    .semantics {selected=chosen}
                    .clickable(role=Role.RadioButton) {store.personalizeTheme(palette.name)}.padding(20.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
                    Text(if(palette.name=="Celestial") "☾  ·  ✧" else "❧  ·  ☾", color=Color(palette.accent),style=MaterialTheme.typography.headlineLarge)
                    Text(palette.name, color=Color(palette.ink),style=MaterialTheme.typography.titleLarge)
                    Text(palette.description, color=Color(palette.ink),style=MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        listOf(palette.accent,palette.rose,palette.leaf).forEach {color -> Box(Modifier.size(22.dp).background(Color(color),CircleShape))}
                    }
                    Text(if(chosen) "Selected" else "Choose ${palette.name}",color=Color(palette.accent),style=MaterialTheme.typography.labelLarge)
                }
            }
        }
        Text("The voice of your pages",style=MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            lilylyTypeStyles.forEach {style -> FilterChip(selected=store.typeStyle==style,onClick={store.personalizeTheme(store.themeName,style)},label={Text(style)})}
        }
        Text(when(store.typeStyle) {"Clear" -> "Simple sans-serif headings and body text."; "Letters" -> "Serif headings and body text, like a personal letter."; else -> "Serif headings with clear sans-serif body text."},style=MaterialTheme.typography.bodySmall)
        Text("A little wonder, kept just for you.",style=MaterialTheme.typography.headlineSmall)
        HorizontalDivider()
    }
}
