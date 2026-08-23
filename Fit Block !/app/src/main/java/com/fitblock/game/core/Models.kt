package com.fitblock.game.core

import androidx.compose.ui.graphics.Color
import com.fitblock.game.ui.theme.FitBlockColors
import kotlin.random.Random

data class BoardCell(val x: Int, val y: Int)

data class PieceDefinition(val cells: List<BoardCell>) {
    val width: Int get() = (cells.maxOfOrNull { it.x } ?: 0) + 1
    val height: Int get() = (cells.maxOfOrNull { it.y } ?: 0) + 1
}

data class ClearResult(val cellsToClear: List<Pair<Int, Int>>, val linesCleared: Int = 0)

enum class GameState {
    MainMenu, Playing, Dragging, Clearing, Paused, GameOver, Result
}

data class BoardCellState(
    val occupied: Boolean = false,
    val color: Color = Color.Transparent
)

// Ye Random wala error isi object me tha - ab fixed hai
object PieceProvider {
    private val allPieces = listOf(
        PieceDefinition(listOf(BoardCell(0,0))),
        PieceDefinition(listOf(BoardCell(0,0), BoardCell(1,0))),
        PieceDefinition(listOf(BoardCell(0,0), BoardCell(0,1))),
        PieceDefinition(listOf(BoardCell(0,0), BoardCell(0,1), BoardCell(1,0), BoardCell(1,1))),
        PieceDefinition(listOf(BoardCell(0,0), BoardCell(1,0), BoardCell(2,0))),
        PieceDefinition(listOf(BoardCell(0,0), BoardCell(0,1), BoardCell(0,2))),
        PieceDefinition(listOf(BoardCell(0,0), BoardCell(1,0), BoardCell(2,0), BoardCell(2,1))),
        PieceDefinition(listOf(BoardCell(0,0), BoardCell(0,1), BoardCell(1,1), BoardCell(2,1))),
        PieceDefinition(listOf(BoardCell(1,0), BoardCell(0,1), BoardCell(1,1), BoardCell(2,1))),
        PieceDefinition(listOf(BoardCell(0,0), BoardCell(0,1), BoardCell(0,2), BoardCell(0,2)))
    )

    fun getRandomPiece(random: Random = Random.Default): PieceDefinition {
        // FIXED: Random.Default sahi se use kiya
        return allPieces.random(random)
    }

    fun getThreePieces(random: Random = Random.Default): List<PieceDefinition> {
        return List(3) { getRandomPiece(random) }
    }
    
    fun getColorForPiece(piece: PieceDefinition): Color {
        val index = allPieces.indexOf(piece).coerceAtLeast(0)
        return FitBlockColors.PieceColors[index % FitBlockColors.PieceColors.size]
    }
}
