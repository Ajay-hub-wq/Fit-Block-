
package com.fitblock.game.core
object GameBalance {
    fun getScoreForLines(lines: Int): Int = when(lines) {
        1 -> 100
        2 -> 300
        3 -> 600
        4 -> 1000
        else -> lines * 300
    }
    fun getComboMultiplier(combo: Int): Float = when {
        combo <=1 -> 1f
        combo ==2 -> 1.5f
        combo ==3 -> 2f
        else -> 2.5f
    }
}
