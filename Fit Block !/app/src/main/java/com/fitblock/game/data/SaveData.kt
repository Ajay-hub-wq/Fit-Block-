
package com.fitblock.game.data
data class SaveData(
    val bestScore: Int = 0,
    val coins: Int = 0,
    val gems: Int = 0,
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
)
