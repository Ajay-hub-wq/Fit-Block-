
package com.fitblock.game.data
import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore("fitblock")

class SaveManager(private val context: Context) {
    private val BEST = intPreferencesKey("best")
    private val COINS = intPreferencesKey("coins")
    private val GEMS = intPreferencesKey("gems")
    private val MUSIC = booleanPreferencesKey("music")
    private val SFX = booleanPreferencesKey("sfx")
    private val VIB = booleanPreferencesKey("vib")
    suspend fun load(): SaveData {
        val p = context.dataStore.data.first()
        return SaveData(
            bestScore = p[BEST] ?: 0,
            coins = p[COINS] ?: 0,
            gems = p[GEMS] ?: 0,
            musicEnabled = p[MUSIC] ?: true,
            sfxEnabled = p[SFX] ?: true,
            vibrationEnabled = p[VIB] ?: true
        )
    }
    suspend fun save(d: SaveData) {
        context.dataStore.edit {
            it[BEST] = d.bestScore
            it[COINS] = d.coins
            it[GEMS] = d.gems
            it[MUSIC] = d.musicEnabled
            it[SFX] = d.sfxEnabled
            it[VIB] = d.vibrationEnabled
        }
    }
}
