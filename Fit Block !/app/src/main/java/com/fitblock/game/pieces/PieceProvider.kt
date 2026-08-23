package com.fitblock.game.pieces

import com.fitblock.game.core.CellOffset
import com.fitblock.game.core.PieceDefinition
import kotlin.random.Random

object PieceProvider {
    private val pieces = listOf(
        PieceDefinition(1, listOf(CellOffset(0,0)), 1, 1, 0),
        PieceDefinition(2, listOf(CellOffset(0,0), CellOffset(1,0)), 2, 1, 1),
        PieceDefinition(3, listOf(CellOffset(0,0), CellOffset(0,1), CellOffset(1,0), CellOffset(1,1)), 2, 2, 2),
        PieceDefinition(4, listOf(CellOffset(0,0), CellOffset(1,0), CellOffset(2,0)), 3, 1, 3),
        PieceDefinition(5, listOf(CellOffset(0,0), CellOffset(0,1), CellOffset(0,2)), 1, 3, 4),
        PieceDefinition(6, listOf(CellOffset(0,0), CellOffset(1,0), CellOffset(2,0), CellOffset(2,1)), 3, 2, 5)
    )

    fun getRandomPiece(random: Random = Random.Default): PieceDefinition {
        return pieces.random(random)
    }

    fun getThreePieces(random: Random = Random.Default): List<PieceDefinition> {
        return List(3) { getRandomPiece(random) }
    }

    // FIXED: occupiedCount ki jagah cellCount use kiya
    fun getHeaviestPiece(): PieceDefinition {
        return pieces.maxByOrNull { it.cellCount }?: pieces[0]
    }
}
