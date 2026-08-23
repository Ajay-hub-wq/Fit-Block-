
package com.fitblock.game.pieces
import com.fitblock.game.core.PieceDefinition
import com.fitblock.game.core.Point
import kotlin.random.Random

object PieceProvider {
    private val all = listOf(
        listOf(Point(0,0)),
        listOf(Point(0,0), Point(1,0)),
        listOf(Point(0,0), Point(0,1)),
        listOf(Point(0,0), Point(1,0), Point(0,1)),
        listOf(Point(0,0), Point(1,0), Point(2,0)),
        listOf(Point(0,0), Point(0,1), Point(0,2)),
        listOf(Point(0,0), Point(1,0), Point(1,1)),
        listOf(Point(0,0), Point(0,1), Point(1,1)),
        listOf(Point(0,0), Point(1,0), Point(2,0), Point(0,1)),
        listOf(Point(0,0), Point(1,0), Point(2,0), Point(2,1)),
        listOf(Point(0,0), Point(1,0), Point(0,1), Point(1,1)),
        listOf(Point(0,0), Point(1,0), Point(2,0), Point(1,1)),
        listOf(Point(1,0), Point(0,1), Point(1,1), Point(2,1)),
        listOf(Point(0,0), Point(1,0), Point(2,0), Point(3,0)),
        listOf(Point(0,0), Point(0,1), Point(0,2), Point(0,3))
    )
    fun getThreePieces(): List<PieceDefinition> {
        return List(3) { i ->
            val shape = all.random()
            PieceDefinition(id = Random.nextInt(10000), cells = shape, colorIndex = Random.nextInt(8))
        }
    }
}
