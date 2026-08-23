package com.fitblock.game.board

import com.fitblock.game.core.BoardCell
import com.fitblock.game.core.PieceDefinition

class BoardData {
    companion object {
        const val SIZE = 8
    }
    private val cells = Array(SIZE) { Array(SIZE) { BoardCell() } }

    fun get(x: Int, y: Int): BoardCell {
        if (x!in 0 until SIZE || y!in 0 until SIZE) return BoardCell(occupied = true)
        return cells[y][x]
    }

    fun set(x: Int, y: Int, cell: BoardCell) {
        if (x in 0 until SIZE && y in 0 until SIZE) {
            cells[y][x] = cell
        }
    }

    fun clone(): BoardData {
        val newBoard = BoardData()
        for (y in 0 until SIZE) {
            for (x in 0 until SIZE) {
                newBoard.set(x, y, this.get(x, y))
            }
        }
        return newBoard
    }
}

object PlacementValidator {
    fun canPlace(board: BoardData, piece: PieceDefinition, startX: Int, startY: Int): Boolean {
        for (c in piece.cells) {
            val x = startX + c.x
            val y = startY + c.y
            if (x!in 0 until BoardData.SIZE || y!in 0 until BoardData.SIZE) return false
            if (board.get(x, y).occupied) return false
        }
        return true
    }
}
