
package com.fitblock.game.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(private val context: Context) {
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun light(enabled: Boolean) { if (!enabled) return; vibrate(40) }
    fun medium(enabled: Boolean) { if (!enabled) return; vibrate(80) }
    fun heavy(enabled: Boolean) { if (!enabled) return; vibrate(120) }
    fun combo(enabled: Boolean, combo: Int) { if (!enabled) return; vibrate((50 + combo*20).coerceAtMost(200).toLong()) }

    private fun vibrate(ms: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(ms)
        }
    }
}
