
package com.fitblock.game.daily

import java.time.LocalDate

data class DailyChallenge(
    val date: LocalDate,
    val goalLines: Int = 20,
    val goalScore: Int = 10000,
    val rewardGems: Int = 10,
    val seed: Long = date.toEpochDay()
) {
    companion object {
        fun today(): DailyChallenge = DailyChallenge(LocalDate.now())
        fun forDate(date: LocalDate): DailyChallenge = DailyChallenge(date)
    }
}
