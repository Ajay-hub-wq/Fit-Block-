
package com.fitblock.game.core
enum class GameState { MainMenu, Playing, Dragging, Paused, GameOver }
data class ClearResult(val rows: List<Int>, val cols: List<Int>, val cellsToClear: Set<Pair<Int,Int>>)
