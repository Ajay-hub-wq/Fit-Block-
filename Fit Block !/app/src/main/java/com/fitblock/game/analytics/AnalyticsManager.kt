
package com.fitblock.game.analytics

import android.os.Bundle
import android.util.Log

object AnalyticsManager {
    fun logEvent(name: String, params: Map<String, Any> = emptyMap()) {
        // TODO: FirebaseAnalytics.getInstance(context).logEvent(name, Bundle)
        Log.d("Analytics", "$name $params")
    }
    fun logGameStart() = logEvent("game_started")
    fun logGameOver(score: Int, lines: Int) = logEvent("game_over", mapOf("score" to score, "lines" to lines))
    fun logLineClear(count: Int) = logEvent("line_cleared", mapOf("count" to count))
}
