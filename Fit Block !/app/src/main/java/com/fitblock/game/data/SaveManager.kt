package com.fitblock.game.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

// Sahi import - kotlin.io nahi, kotlinx.serialization
@Serializable
data class GameSaveData(
    val highScore: Int = 0,
    val coins: Int = 0
)

private val Context.dataStore by preferencesDataStore(name = "fitblock_prefs")

class SaveManager(private val context: Context) {
    private val HIGH_SCORE_KEY = intPreferencesKey("high_score")
    private val COINS_KEY = intPreferencesKey("coins")

    val highScoreFlow: Flow<Int> = context.dataStore.data.map { it[HIGH_SCORE_KEY] ?: 0 }
    val coinsFlow: Flow<Int> = context.dataStore.data.map { it[COINS_KEY] ?: 0 }

    suspend fun saveHighScore(score: Int) {
        context.dataStore.edit { it[HIGH_SCORE_KEY] = score }
    }
    suspend fun saveCoins(coins: Int) {
        context.dataStore.edit { it[COINS_KEY] = coins }
    }
}
