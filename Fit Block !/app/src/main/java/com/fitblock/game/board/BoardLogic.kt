
package com.fitblock.game.board

import com.fitblock.game.core.BoardCell
import com.fitblock.game.core.CellOffset
import com.fitblock.game.core.ClearResult
import com.fitblock.game.core.PieceDefinition

class BoardData {
    companion object { const val SIZE = 8; const val TOTAL = 64 }
    private val cells = Array(SIZE) { Array(SIZE) { BoardCell() } }

    fun clearAll() { for (y in 0 until SIZE) for (x in 0 until SIZE) cells[y][x] = BoardCell() }
    fun get(x: Int, y: Int): BoardCell = if (inBounds(x,y)) cells[y][x] else BoardCell()
    fun set(x: Int, y: Int, cell: BoardCell) { if (inBounds(x,y)) cells[y][x] = cell }
    fun inBounds(x: Int, y: Int) = x in 0 until SIZE && y in 0 until SIZE
    fun isOccupied(x: Int, y: Int) = inBounds(x,y) && cells[y][x].occupied
    fun occupiedCount(): Int { var c=0; for (y in 0 until SIZE) for (x in 0 until SIZE) if (cells[y][x].occupied) c++; return c }
    fun clone(): Array<Array<BoardCell>> = Array(SIZE) { y -> Array(SIZE) { x -> cells[y][x] } }
}

object PlacementValidator {
    fun canPlace(board: BoardData, def: PieceDefinition, originX: Int, originY: Int): Boolean {
        for (off in def.cells) {
            val x = originX + off.x
            val y = originY + off.y
            if (!board.inBounds(x,y)) return false
            if (board.isOccupied(x,y)) return false
        }
        return true
    }
    fun hasAnyValidPlacement(board: BoardData, def: PieceDefinition): Boolean {
        for (y in 0 until BoardData.SIZE) for (x in 0 until BoardData.SIZE) if (canPlace(board, def, x, y)) return true
        return false
    }
    fun isGameOver(board: BoardData, pieces: List<PieceDefinition?>): Boolean {
        if (pieces.isEmpty()) return true
        for (p in pieces) { if (p!=null && hasAnyValidPlacement(board, p)) return false }
        return true
    }
    fun getAllValidOrigins(board: BoardData, def: PieceDefinition): List<Pair<Int,Int>> {
        val list = mutableListOf<Pair<Int,Int>>()
        for (y in 0 until BoardData.SIZE) for (x in 0 until BoardData.SIZE) if (canPlace(board, def, x, y)) list.add(x to y)
        return list
    }
}

object LineDetector {
    fun detect(board: BoardData): ClearResult {
        val rows = mutableListOf<Int>()
        val cols = mutableListOf<Int>()
        val toClear = mutableSetOf<Pair<Int,Int>>()
        for (y in 0 until BoardData.SIZE) {
            var full = true
            for (x in 0 until BoardData.SIZE) if (!board.isOccupied(x,y)) { full=false; break }
            if (full) { rows.add(y); for (x in 0 until BoardData.SIZE) toClear.add(x to y) }
        }
        for (x in 0 until BoardData.SIZE) {
            var full = true
            for (y in 0 until BoardData.SIZE) if (!board.isOccupied(x,y)) { full=false; break }
            if (full) { cols.add(x); for (y in 0 until BoardData.SIZE) toClear.add(x to y) }
        }
        return ClearResult(rows, cols, toClear)
    }
}
