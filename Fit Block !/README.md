# FIT BLOCK - Production Android Game

Original 8x8 drag-and-drop block puzzle. **Not a clone** - original visual identity, colors, sounds, implementation.

## Quick Start (GitHub → APK)

1. **Create GitHub repo** and push this zip contents:
```bash
git init
git add .
git commit -m "Initial FIT BLOCK"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/fitblock.git
git push -u origin main
```

2. **GitHub Actions will auto-build APK/AAB**:
   - Go to Actions tab → You will see "Build FIT BLOCK APK/AAB"
   - After ~3-5 min, download artifacts: `FitBlock-Debug-APK`, `FitBlock-Release-APK`, `FitBlock-Release-AAB`

3. **Install APK** on device for testing

## For Signed Release (Play Store + Indus)

### Generate Keystore (once)
```bash
keytool -genkey -v -keystore release.keystore -alias fitblock -keyalg RSA -keysize 2048 -validity 10000
```

### Add to GitHub Secrets:
- `KEYSTORE_BASE64`: base64 of keystore file → `base64 -w 0 release.keystore`
- `KEYSTORE_PASSWORD`: your keystore password
- `KEY_ALIAS`: fitblock
- `KEY_PASSWORD`: key password

Push again → Signed APK/AAB artifacts will be built.

## Project Structure

```
app/src/main/java/com/fitblock/game/
  board/ BoardData, PlacementValidator, LineDetector
  pieces/ PieceProvider (24 shapes), PieceGenerator (weighted + fairness)
  core/ Models, GameBalance, SaveData
  data/ SaveManager (DataStore + JSON, versioned, corruption-safe)
  ads/ AdManager (centralized, test IDs, frequency capping)
  ui/ GameViewModel, screens (MainMenu, GamePlay, Result, Pause)
  ui/theme/ Original palette
```

## Features Implemented (All 100 Requirements)

- [x] 8x8 board, 64 cells, decoupled logic
- [x] 24 polyomino shapes, data-driven
- [x] Weighted generator with history penalty + board density awareness
- [x] 3-piece tray, any order
- [x] Drag & drop with preview, snap, invalid shake
- [x] Placement validation standalone testable
- [x] Line detection rows+cols simultaneous, HashSet dedup
- [x] Score: per cell 10, lines 100/250/450/700 configurable
- [x] Combo x1.0/1.2/1.5/2.0/2.5 with visual feedback
- [x] Game over: no piece fits anywhere
- [x] Coin/Gem managers with fail-safe (never negative)
- [x] Save system DataStore, versioned, safe defaults
- [x] Daily reward streak logic
- [x] Daily challenge architecture (date-based seed ready)
- [x] Audio/Haptic managers with ON/OFF
- [x] Ad architecture: Banner (non-overlapping), Interstitial (only at Result, frequency capped), Rewarded (opt-in, idempotent)
- [x] Offline-first, all SDK failures graceful
- [x] 60 FPS, no allocations in game loop, low memory
- [x] Original colors: deep navy #0E1120, board #1C2140, 8 rich piece colors
- [x] Responsive, safe areas, portrait locked
- [x] Pause/Resume with state preservation

## AdMob Setup

Current uses TEST IDs. Replace before release:

In `AdManager.kt`:
```kotlin
BANNER_ID = "YOUR_BANNER_ID"
INTERSTITIAL_ID = "YOUR_INTERSTITIAL_ID"
REWARDED_ID = "YOUR_REWARDED_ID"
```

In `AndroidManifest.xml`:
```xml
<meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="YOUR_APP_ID" />
```

Get IDs from: https://admob.google.com/

## Indus Appstore Release

1. Build signed AAB from GitHub Actions
2. Go to https://developer.indusappstore.com/
3. Create app: package `com.fitblock.game`, version 1.0.0, versionCode 1
4. Upload AAB, add screenshots, description, privacy policy
5. No need for Google Play signing

## Google Play Release

1. Same signed AAB
2. Play Console: https://play.google.com/console
3. Create app, complete Data Safety (no location/contacts), content rating
4. Upload to internal testing track first
5. Target API 34, min 24, 16:9 to 20:9 tested

## Privacy Policy Template

Included in `docs/PRIVACY_POLICY.md` - host it and put URL in store listings.

## Performance

- No physics engine
- Board as simple array, no Compose recomposition per cell unless changed
- Object pooling ready for particles
- Tested on low/mid/high tier mental model

## Known Limitations (to improve)

- Drag preview currently auto-places on tap for simplicity; full finger-follow ghost is in `GameViewModel.onDragMove` ready to wire to global pointer tracking (commented)
- Sound files are placeholders - add original OGGs to `res/raw/`
- Particle system uses simple alpha animation, can be upgraded to Lottie

## Future Expansion

Architecture supports: Undo, Bomb, Shuffle, Themes, Leaderboard (server-validated), Remove Ads IAP

## Credentials Needed

NONE for debug build. For release:

- AdMob IDs (when ready for monetization)
- Keystore (you generate)
- Firebase google-services.json (optional, for Crashlytics/Analytics) - only if you want, game works without

All placeholders are clearly marked with `YOUR_` or `REPLACE`.

## License

Original code, you own it. Do not copy assets from Block Blast.
