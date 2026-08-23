
package com.fitblock.game.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object FitBlockColors {
    val BackgroundTop = Color(0xFF1A1F3C)
    val BackgroundBottom = Color(0xFF0F1220)
    val BackgroundGradient = Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom))
    val BoardSurface = Color(0xFF242A4A)
    val BoardCellEmpty = Color(0xFF2E3560)
    val BoardCellHighlightValid = Color(0xFF66BB6A).copy(alpha=0.5f)
    val BoardCellHighlightInvalid = Color(0xFFEF5350).copy(alpha=0.5f)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF8A90B5)
    val Coin = Color(0xFFFFD700)
    val Gem = Color(0xFF00E5FF)
    val PieceColors = listOf(
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
        Color(0xFFFFA600), Color(0xFF96CEB4), Color(0xFFDDA0DD),
        Color(0xFFFFE66D), Color(0xFF6C5CE7)
    )
    val ComboColors = listOf(Color(0xFFFFEB3B), Color(0xFFFF9800), Color(0xFFF44336))
}
