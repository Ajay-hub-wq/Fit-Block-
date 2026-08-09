
package com.fitblock.game.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.fitblock.game.core.SaveData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.Serializable

@Serializable
private data class SaveDataSerializable(
    val version: Int = 4,
    val bestScore: Int = 0,
    val coins: Int = 0,
    val gems: Int = 0,
    val musicEnabled: Boolean = true,
    val sfxEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val musicVolume: Float = 0.25f,
    val sfxVolume: Float = 0.7f,
    val dailyStreak: Int = 0,
    val lastRewardDate: String = "",
    val tutorialCompleted: Boolean = false,
    val totalGamesPlayed: Int = 0,
    val totalLinesCleared: Int = 0
)

private val Context.dataStore by preferencesDataStore(name = "fitblock_save")

class SaveManager(private val context: Context) {
    private val KEY_SAVE = stringPreferencesKey("save_json")

    suspend fun load(): SaveData {
        return try {
            val prefs = context.dataStore.data.first()
            val json = prefs[KEY_SAVE] ?: return SaveData()
            val ser = Json.decodeFromString<SaveDataSerializable>(json)
            SaveData(
                version = ser.version,
                bestScore = ser.bestScore.coerceAtLeast(0),
                coins = ser.coins.coerceAtLeast(0),
                gems = ser.gems.coerceAtLeast(0),
                musicEnabled = ser.musicEnabled,
                sfxEnabled = ser.sfxEnabled,
                vibrationEnabled = ser.vibrationEnabled,
                musicVolume = ser.musicVolume.coerceIn(0f,1f),
                sfxVolume = ser.sfxVolume.coerceIn(0f,1f),
                dailyStreak = ser.dailyStreak,
                lastRewardDate = ser.lastRewardDate,
                tutorialCompleted = ser.tutorialCompleted,
                totalGamesPlayed = ser.totalGamesPlayed,
                totalLinesCleared = ser.totalLinesCleared
            )
        } catch (e: Exception) {
            SaveData()
        }
    }

    suspend fun save(data: SaveData) {
        try {
            val ser = SaveDataSerializable(
                version = data.version,
                bestScore = data.bestScore,
                coins = data.coins,
                gems = data.gems,
                musicEnabled = data.musicEnabled,
                sfxEnabled = data.sfxEnabled,
                vibrationEnabled = data.vibrationEnabled,
                musicVolume = data.musicVolume,
                sfxVolume = data.sfxVolume,
                dailyStreak = data.dailyStreak,
                lastRewardDate = data.lastRewardDate,
                tutorialCompleted = data.tutorialCompleted,
                totalGamesPlayed = data.totalGamesPlayed,
                totalLinesCleared = data.totalLinesCleared
            )
            val json = Json.encodeToString(ser)
            context.dataStore.edit { it[KEY_SAVE] = json }
        } catch (e: Exception) {
            // fail safe
        }
    }
}
