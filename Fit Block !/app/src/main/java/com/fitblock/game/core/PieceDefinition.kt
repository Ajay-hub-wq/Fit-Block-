
package com.fitblock.game.core
data class Point(val x: Int, val y: Int)
data class PieceDefinition(val id: Int, val cells: List<Point>, val colorIndex: Int) {
    val width: Int get() = (cells.maxOfOrNull { it.x } ?: 0) + 1
    val height: Int get() = (cells.maxOfOrNull { it.y } ?: 0) + 1
    val cellCount: Int get() = cells.size
}
