
package com.fitblock.game.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7C5CFF),
    background = Color(0xFF0F1220),
    surface = Color(0xFF242A4A)
)

@Composable
fun FitBlockTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, content = content)
}
