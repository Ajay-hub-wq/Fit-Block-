package com.fitblock.game.core

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

enum class GameState {
    Boot, MainMenu, Intro, Playing, Dragging, Placing, Clearing, Paused, GameOver, Result
}

data class CellOffset(val x: Int, val y: Int)

data class PieceDefinition(
    val id: Int,
    val cells: List<CellOffset>,
    val width: Int,
    val height: Int,
    val colorIndex: Int = 0,
    val weight: Float = 1f
) {
    val cellCount: Int get() = cells.size
}

data class BoardCell(
    val occupied: Boolean = false,
    val blockId: Int = -1,
    val color: Color = Color.Transparent
)

data class ClearResult(
    val rows: List<Int> = emptyList(),
    val cols: List<Int> = emptyList(),
    val cellsToClear: Set<Pair<Int,Int>> = emptySet()
) {
    val totalLines: Int get() = rows.size + cols.size
    val hasClear: Boolean get() = totalLines > 0
}

object GameBalance {
    const val scorePerCell = 10
    const val score1Line = 100
    const val score2Lines = 250
    const val score3Lines = 450
    const val score4Lines = 700
    const val coinsPerLine = 2
    const val coinsPerCombo = 5

    fun getComboMultiplier(combo: Int): Float = when(combo) {
        0,1 -> 1.0f
        2 -> 1.2f
        3 -> 1.5f
        4 -> 2.0f
        else -> 2.5f
    }
    fun getScoreForLines(lines: Int): Int = when(lines) {
        1 -> score1Line
        2 -> score2Lines
        3 -> score3Lines
        4 -> score4Lines
        else -> if (lines>4) score4Lines + (lines-4)*300 else 0
    }

    const val minGamesBetweenInterstitial = 3
    const val minSecondsBetweenInterstitial = 90f
    const val sessionInterstitialCap = 5
}

data class SaveData(
    val version: Int = 4,
    val bestScore: Int = 0,
    val coins: Int = 0,
    val gems: Int = 0,
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val musicVolume: Float = 0.25f,
    val sfxVolume: Float = 0.7f,
    val dailyStreak: Int = 0,
    val lastRewardDate: String = "",
    val tutorialCompleted: Boolean = false,
    val totalGamesPlayed: Int = 0,
    val totalLinesCleared: Int = 0
)

// FIXED: Ye object missing tha aur Random.Default ka error de raha tha
object PieceProvider {
    private val pieces = listOf(
        PieceDefinition(1, listOf(CellOffset(0,0)), 1, 1, 0),
        PieceDefinition(2, listOf(CellOffset(0,0), CellOffset(1,0)), 2, 1, 1),
        PieceDefinition(3, listOf(CellOffset(0,0), CellOffset(0,1), CellOffset(1,0), CellOffset(1,1)), 2, 2, 2),
        PieceDefinition(4, listOf(CellOffset(0,0), CellOffset(1,0), CellOffset(2,0)), 3, 1, 3),
        PieceDefinition(5, listOf(CellOffset(0,0), CellOffset(0,1), CellOffset(0,2)), 1, 3, 4),
        PieceDefinition(6, listOf(CellOffset(0,0), CellOffset(1,0), CellOffset(2,0), CellOffset(2,1)), 3, 2, 5),
        PieceDefinition(7, listOf(CellOffset(0,1), CellOffset(1,1), CellOffset(2,1), CellOffset(2,0)), 3, 2, 6)
    )

    fun getRandomPiece(random: Random = Random.Default): PieceDefinition {
        return pieces.random(random)
    }

    fun getThreePieces(random: Random = Random.Default): List<PieceDefinition> {
        return List(3) { getRandomPiece(random) }
    }
}
