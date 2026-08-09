
package com.fitblock.game

import android.app.Application
import com.fitblock.game.ads.AdManager

class FitBlockApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AdManager.initialize(this)
    }
}
