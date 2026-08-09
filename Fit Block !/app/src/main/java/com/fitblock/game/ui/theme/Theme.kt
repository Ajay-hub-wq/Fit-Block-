
package com.fitblock.game.ui.theme

import androidx.compose.ui.graphics.Color

object FitBlockColors {
    val Background = Color(0xFF0E1120)
    val BoardSurface = Color(0xFF1C2140)
    val BoardCellEmpty = Color(0xFF252B4D)
    val BoardCellHighlight = Color(0xFF2E365F)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFFA0A6C8)

    val PieceColors = listOf(
        Color(0xFF00D1FF), // cyan
        Color(0xFF7B5CFF), // purple
        Color(0xFFFF5C8A), // pink
        Color(0xFFFF8C2F), // orange
        Color(0xFFFFD93D), // yellow
        Color(0xFF2ECC71), // green
        Color(0xFF3A86FF), // blue
        Color(0xFFFF6B6B), // red accent
    )

    val ComboColors = listOf(
        Color(0xFFFFD93D),
        Color(0xFFFF8C2F),
        Color(0xFFFF5C8A),
        Color(0xFF7B5CFF),
        Color(0xFF00D1FF)
    )

    val Coin = Color(0xFFFFD93D)
    val Gem = Color(0xFF7B5CFF)
}
