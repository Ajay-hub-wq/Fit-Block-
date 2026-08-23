
package com.fitblock.game.board

import androidx.compose.ui.graphics.Color

data class BoardCell(val occupied: Boolean = false, val pieceId: Int = -1, val color: Color = Color.Transparent)

class BoardData {
    companion object { const val SIZE = 10 }
    private val cells = Array(SIZE) { Array(SIZE) { BoardCell() } }
    fun get(x: Int, y: Int): BoardCell = cells[y][x]
    fun set(x: Int, y: Int, cell: BoardCell) { cells[y][x] = cell }
    fun clone(): BoardData {
        val b = BoardData()
        for(y in 0 until SIZE) for(x in 0 until SIZE) b.set(x,y,get(x,y))
        return b
    }
}
