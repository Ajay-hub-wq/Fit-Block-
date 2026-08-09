
package com.fitblock.game.ads

import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Centralized Ad Manager - never call SDK directly from gameplay
 * PLACEHOLDERS MUST BE REPLACED BEFORE PRODUCTION
 * Test IDs are used by default
 */
object AdManager {
    // TODO: REPLACE WITH REAL IDs BEFORE RELEASE
    // Test IDs (Google official)
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    // YOUR REAL IDS - set via BuildConfig or here
    var BANNER_ID = TEST_BANNER_ID
    var INTERSTITIAL_ID = TEST_INTERSTITIAL_ID
    var REWARDED_ID = TEST_REWARDED_ID

    var bannerEnabled = true
    var interstitialEnabled = true
    var rewardedEnabled = true

    private var gamesSinceLastInterstitial = 0
    private var lastInterstitialTime = 0L
    private var interstitialThisSession = 0
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    fun initialize(context: Context) {
        MobileAds.initialize(context) {}
        loadInterstitial(context)
        loadRewarded(context)
    }

    fun canShowInterstitial(minGames: Int, minSeconds: Float, sessionCap: Int): Boolean {
        if (!interstitialEnabled) return false
        if (gamesSinceLastInterstitial < minGames) return false
        if (System.currentTimeMillis() - lastInterstitialTime < minSeconds*1000) return false
        if (interstitialThisSession >= sessionCap) return false
        return interstitialAd != null
    }

    fun loadInterstitial(context: Context) {
        val req = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_ID, req, object: InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) { interstitialAd = ad }
            override fun onAdFailedToLoad(err: LoadAdError) { interstitialAd = null }
        })
    }

    fun showInterstitial(context: Context, onClosed: ()->Unit, onFail: (String)->Unit) {
        val ad = interstitialAd
        if (ad == null) { onFail("No ad"); loadInterstitial(context); return }
        ad.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { interstitialAd=null; loadInterstitial(context); gamesSinceLastInterstitial=0; lastInterstitialTime=System.currentTimeMillis(); interstitialThisSession++; onClosed() }
            override fun onAdFailedToShowFullScreenContent(e: AdError) { interstitialAd=null; loadInterstitial(context); onFail(e.message) }
        }
        ad.show(context as android.app.Activity)
    }

    fun loadRewarded(context: Context) {
        val req = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_ID, req, object: RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) { rewardedAd = ad }
            override fun onAdFailedToLoad(err: LoadAdError) { rewardedAd = null }
        })
    }

    fun showRewarded(context: Context, onRewarded: ()->Unit, onFail: (String)->Unit) {
        val ad = rewardedAd
        if (ad == null) { onFail("No ad available"); loadRewarded(context); return }
        ad.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { rewardedAd=null; loadRewarded(context) }
            override fun onAdFailedToShowFullScreenContent(e: AdError) { rewardedAd=null; loadRewarded(context); onFail(e.message) }
        }
        ad.show(context as android.app.Activity) { onRewarded() }
    }

    fun notifyGameEnded() { gamesSinceLastInterstitial++ }
}
