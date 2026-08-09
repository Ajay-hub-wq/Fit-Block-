
package com.fitblock.game.audio

import android.content.Context
import android.media.SoundPool
import com.fitblock.game.data.SaveManager
import kotlinx.coroutines.*

class AudioManager private constructor(context: Context) {
    private val soundPool = SoundPool.Builder().setMaxStreams(8).build()
    private var saveManager: SaveManager? = null
    // Placeholder sound IDs - add real OGGs to res/raw/
    // val placeSound = soundPool.load(context, R.raw.place, 1)

    fun playPlace() { /* soundPool.play(placeSound, 1f,1f,0,0,1f) */ }
    fun playClear(lines: Int) { }
    fun playInvalid() { }
    fun playGameOver() { }
    fun playCombo(combo: Int) { }

    companion object {
        @Volatile private var INSTANCE: AudioManager? = null
        fun getInstance(context: Context): AudioManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: AudioManager(context.applicationContext).also { INSTANCE = it }
        }
    }
}
