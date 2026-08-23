
package com.fitblock.game.core
import com.fitblock.game.board.BoardData
object PlacementValidator {
    fun canPlace(board: BoardData, piece: PieceDefinition, bx: Int, by: Int): Boolean {
        for(c in piece.cells) {
            val x = bx + c.x
            val y = by + c.y
            if(x !in 0 until BoardData.SIZE || y !in 0 until BoardData.SIZE) return false
            if(board.get(x,y).occupied) return false
        }
        return true
    }
}
