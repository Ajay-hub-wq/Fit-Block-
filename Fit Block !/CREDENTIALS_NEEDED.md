
# REQUIRED FROM OWNER - Credentials Checklist

Game builds and runs WITHOUT these (uses test IDs). Replace only when ready for release.

## 1. AdMob (for monetization)
Item: AdMob App ID, Banner, Interstitial, Rewarded IDs
Why: To show ads
Where: https://admob.google.com/ → Apps → Add app → Create ad units
What to send:
- Android App ID: ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
- Banner: ca-app-pub-.../...
- Interstitial: ca-app-pub-.../...
- Rewarded: ca-app-pub-.../...
Security: Not secret but treat as config, safe to commit but better in local
Replace in: app/src/main/java/com/fitblock/game/ads/AdManager.kt and AndroidManifest.xml

## 2. Signing Keystore (for Play Store & Indus)
Item: release.keystore file + passwords
Why: To sign AAB/APK for store
Where: Generate locally: keytool -genkey -v -keystore release.keystore -alias fitblock -keyalg RSA -keysize 2048 -validity 10000
What to send to GitHub Secrets: base64 of keystore + passwords
Security: SECRET - never commit keystore file

## 3. Firebase (OPTIONAL - for Crashlytics/Analytics)
Item: google-services.json
Why: Crash reporting, analytics, remote config
Where: https://console.firebase.google.com/ → New project → Add Android app → package com.fitblock.game → download json
What to send: Place file in app/ folder
Security: Contains API keys but okay to commit for Android (restricted), but keep private if possible

## 4. Package Name
Current: com.fitblock.game
If you want different: change in app/build.gradle.kts namespace and applicationId, and AndroidManifest package
Security: Public

## 5. Privacy Policy URL
Need to host docs/PRIVACY_POLICY.md somewhere and put URL in Play Console
Security: Public

ALL PLACEHOLDERS ARE MARKED WITH YOUR_ or TEST_
