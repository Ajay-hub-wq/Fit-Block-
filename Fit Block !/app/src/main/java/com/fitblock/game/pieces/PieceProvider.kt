
package com.fitblock.game.pieces

import com.fitblock.game.core.CellOffset
import com.fitblock.game.core.PieceDefinition
import com.fitblock.game.board.BoardData
import com.fitblock.game.board.PlacementValidator
import kotlin.random.Random

object PieceProvider {
    fun getAllDefinitions(): List<PieceDefinition> {
        var id=0
        fun make(cells: List<Pair<Int,Int>>, weight: Float=1f): PieceDefinition {
            val offsets = cells.map { CellOffset(it.first, it.second) }
            val maxX = offsets.maxOf { it.x }
            val maxY = offsets.maxOf { it.y }
            return PieceDefinition(id++, offsets, maxX+1, maxY+1, colorIndex = id%7, weight=weight)
        }
        return listOf(
            make(listOf(0 to 0)), // single
            make(listOf(0 to 0, 1 to 0)), // domino h
            make(listOf(0 to 0, 0 to 1)), // domino v
            make(listOf(0 to 0, 1 to 0, 2 to 0)), // 3h
            make(listOf(0 to 0, 0 to 1, 0 to 2)), // 3v
            make(listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0)), // 4h
            make(listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3)), // 4v
            make(listOf(0 to 0, 1 to 0, 0 to 1, 1 to 1)), // 2x2
            make(listOf(0 to 0, 0 to 1, 0 to 2, 1 to 2)), // L
            make(listOf(1 to 0, 1 to 1, 1 to 2, 0 to 2)), // L rev
            make(listOf(0 to 0, 0 to 1, 1 to 1, 2 to 1)), // L small
            make(listOf(0 to 0, 1 to 0, 2 to 0, 2 to 1)), // L
            make(listOf(0 to 0, 1 to 0, 2 to 0, 1 to 1)), // T
            make(listOf(1 to 0, 0 to 1, 1 to 1, 2 to 1)), // T
            make(listOf(0 to 1, 1 to 1, 1 to 0, 2 to 0)), // S
            make(listOf(0 to 0, 1 to 0, 1 to 1, 2 to 1)), // Z
            make(listOf(0 to 0, 1 to 0, 2 to 0, 0 to 1, 1 to 1, 2 to 1)), // 3x2
            make(listOf(0 to 0, 1 to 0, 2 to 0, 0 to 1, 0 to 2)), // corner
            make(listOf(0 to 0, 1 to 0, 1 to 1)), // small L
            make(listOf(0 to 0, 1 to 0, 0 to 1)), // small L
            make(listOf(0 to 0, 0 to 1, 1 to 1)), // small L
            make(listOf(1 to 0, 0 to 1, 1 to 1)), // small L
            make(listOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0), 0.6f), // 5 line rare
            make(listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1, 0 to 2, 1 to 2), 0.8f), // 3x2 missing
        )
    }
}

class PieceGenerator(private val definitions: List<PieceDefinition> = PieceProvider.getAllDefinitions()) {
    private val history = ArrayDeque<Int>()
    private val historySize = 6
    private var rng = Random.Default

    fun setSeed(seed: Long) { rng = Random(seed) }

    fun generateOne(board: BoardData? = null): PieceDefinition {
        val weights = definitions.map { def ->
            var w = def.weight.coerceAtLeast(0.1f)
            if (history.contains(def.id)) w *= 0.25f
            if (board != null) {
                val fill = board.occupiedCount() / BoardData.TOTAL.toFloat()
                if (fill > 0.6f && def.cellCount > 4) w *= 0.45f
                if (fill > 0.75f && def.cellCount > 3) w *= 0.25f
            }
            w
        }
        val total = weights.sum()
        var roll = rng.nextFloat() * total
        for (i in definitions.indices) {
            roll -= weights[i]
            if (roll <= 0f) { pushHistory(definitions[i].id); return definitions[i] }
        }
        val fallback = definitions.random(rng)
        pushHistory(fallback.id)
        return fallback
    }

    fun generateBatch(count: Int, board: BoardData): List<PieceDefinition> {
        val list = MutableList(count) { generateOne(board) }
        val anyPlaceable = list.any { PlacementValidator.hasAnyValidPlacement(board, it) }
        if (!anyPlaceable && board.occupiedCount() < 50) {
            val single = definitions.firstOrNull { it.cellCount == 1 } ?: definitions.first()
            if (PlacementValidator.hasAnyValidPlacement(board, single)) {
                list[rng.nextInt(count)] = single
            }
        }
        return list
    }

    private fun pushHistory(id: Int) {
        history.addLast(id)
        if (history.size > historySize) history.removeFirst()
    }
}
