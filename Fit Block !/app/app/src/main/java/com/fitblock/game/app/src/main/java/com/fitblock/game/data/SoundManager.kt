
package com.fitblock.game.data
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundManager(private val context: Context) {
    fun playPlace(isEnabled: Boolean) { if(!isEnabled) return; vibrate(20) }
    fun playClear(lines: Int, isEnabled: Boolean) { if(!isEnabled) return; vibrate(if(lines>1) 80 else 50) }
    fun playGameOver(isEnabled: Boolean) { if(!isEnabled) return; vibrate(200) }
    fun playClick(isEnabled: Boolean) { if(!isEnabled) return; vibrate(15) }
    private fun vibrate(ms: Long) {
        try {
            val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vib.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            else @Suppress("DEPRECATION") vib.vibrate(ms)
        } catch(e: Exception) {}
    }
}
