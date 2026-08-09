
package com.fitblock.game.ui

import androidx.compose.runtime.*
import com.fitblock.game.board.BoardData

fun GameViewModel.onDragMove(x: Int, y: Int) {
    this.dragPreviewPos = x to y
    val idx = this.draggingIndex ?: return
    val piece = this.tray.getOrNull(idx) ?: return
    this.isValidPreview = com.fitblock.game.board.PlacementValidator.canPlace(this.board, piece, x, y)
}
