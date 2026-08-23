package com.fitblock.game.ads

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * FIT BLOCK - Ad Manager
 * Production Ready for Google Play & Indus Appstore
 * 
 * HOW TO GO LIVE:
 * 1. Replace all TEST IDs with your REAL Production IDs from AdMob dashboard
 * 2. Change IS_TEST_MODE to false
 * 3. Update APPLICATION_ID in AndroidManifest.xml
 */
object AdManager {

    // ==================== CONFIGURATION - EDIT THIS SECTION ====================
    
    // Set to false before uploading to Play Store / Indus
    private const val IS_TEST_MODE = true

    // ----> PASTE YOUR REAL PRODUCTION AD UNIT IDs HERE <----
    // Get these from: AdMob Console -> Apps -> Ad Units
    
    // Banner Ad (shown on Game Over screen)
    private const val PROD_BANNER_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX" // TODO: Replace with your real Banner ID
    private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"

    // Interstitial Ad (shown every 3 game overs)
    private const val PROD_INTERSTITIAL_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX" // TODO: Replace with your real Interstitial ID
    private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    // Rewarded Ad (for extra coins / continue)
    private const val PROD_REWARDED_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX" // TODO: Replace with your real Rewarded ID
    private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    // Public getters - automatically returns test or prod based on flag
    val bannerAdUnitId get() = if (IS_TEST_MODE) TEST_BANNER_ID else PROD_BANNER_ID
    val interstitialAdUnitId get() = if (IS_TEST_MODE) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID
    val rewardedAdUnitId get() = if (IS_TEST_MODE) TEST_REWARDED_ID else PROD_REWARDED_ID

    // ==================== END CONFIGURATION ====================

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        MobileAds.initialize(context) {}
        isInitialized = true
        loadInterstitial(context)
        loadRewarded(context)
    }

    fun loadInterstitial(context: Context) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, interstitialAdUnitId, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
    }

    fun showInterstitial(activity: android.app.Activity, onDismissed: () -> Unit) {
        val ad = interstitialAd
        if (ad != null) {
            ad.show(activity)
            interstitialAd = null
            loadInterstitial(activity)
            onDismissed()
        } else {
            onDismissed()
            loadInterstitial(activity)
        }
    }

    fun loadRewarded(context: Context) {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, rewardedAdUnitId, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            })
    }

    fun isRewardedReady(): Boolean = rewardedAd != null

    fun showRewarded(activity: android.app.Activity, onRewarded: (Int) -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            ad.show(activity) { reward ->
                onRewarded(reward.amount)
            }
            rewardedAd = null
            loadRewarded(activity)
        }
    }
}
