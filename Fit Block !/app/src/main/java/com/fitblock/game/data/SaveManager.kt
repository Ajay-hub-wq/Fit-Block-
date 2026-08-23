package com.fitblock.game.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.fitblock.game.core.SaveData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "fitblock_prefs")

class SaveManager(private val context: Context) {

    suspend fun load(): SaveData {
        val prefs = context.dataStore.data.first()
        return SaveData(
            bestScore = prefs[intPreferencesKey("bestScore")]?: 0,
            coins = prefs[intPreferencesKey("coins")]?: 0,
            gems = prefs[intPreferencesKey("gems")]?: 0,
            musicEnabled = prefs[booleanPreferencesKey("musicEnabled")]?: true,
            sfxEnabled = prefs[booleanPreferencesKey("sfxEnabled")]?: true,
            vibrationEnabled = prefs[booleanPreferencesKey("vibrationEnabled")]?: true
        )
    }

    suspend fun save(data: SaveData) {
        context.dataStore.edit { prefs ->
            prefs[intPreferencesKey("bestScore")] = data.bestScore
            prefs[intPreferencesKey("coins")] = data.coins
            prefs[intPreferencesKey("gems")] = data.gems
            prefs[booleanPreferencesKey("musicEnabled")] = data.musicEnabled
            prefs[booleanPreferencesKey("sfxEnabled")] = data.sfxEnabled
            prefs[booleanPreferencesKey("vibrationEnabled")] = data.vibrationEnabled
        }
    }
}
