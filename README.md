# OfflineKit (v1.0.0 source)

Native Kotlin + Jetpack Compose utility toolkit with 12 offline tools, per the OfflineKit blueprint.
Package / applicationId: `com.offlinekit.app` (change it in `app/build.gradle.kts` and the `com/offlinekit/app` source folders if you want your own ID).

## STATUS: written, NOT yet compiled or run
This source was generated in an environment with no Android SDK, no Kotlin compiler and no network.
Nothing here has been built, unit-tested, installed or tested on a device. Treat the first Termux build as
the real verification and expect to fix small compile errors.

## Build in Termux
```bash
cd ~/OfflineKit
bash setup-wrapper.sh     # once: writes Gradle 8.9 wrapper using your global Gradle
bash build.sh             # runs unit tests, then assembleDebug, then lists the APK
# APK: app/build/outputs/apk/debug/app-debug.apk
```
Why a wrapper: AGP 8.7.3 needs Gradle 8.9.x; the global Gradle 9.8.2 is only used to write the wrapper files.
`gradle.properties` points `android.aapt2FromMavenOverride` at Termux's aapt2 (the Maven one is x86-64 and cannot run
on your phone). Remove that line on a desktop. First build downloads dependencies (needs internet).

Pinned toolchain: AGP 8.7.3, Kotlin 2.0.21 (Compose compiler plugin), Compose BOM 2024.10.01, compileSdk/targetSdk 35,
minSdk 26, JVM target 17 (runs on your JDK 21), build-tools 34.0.0 (AGP default), ZXing core 3.5.3, play-services-ads 23.5.0,
UMP 3.0.0. If the first error is a version mismatch, fix that one cause only and re-run.

## Layout
- `core/` Outcome, number formatting, strict parsing
- `domain/` pure logic (calculator, converters, percent, dates, time, text, password, notes/expense rules). No Android imports.
- `data/` atomic JSON file store (notes, expenses) + SharedPreferences (theme, favorites, recents). No Room/KSP on purpose, to keep Termux builds simple.
- `ads/` all AdMob + consent (UMP) code; tools never depend on it
- `ui/`, `navigation/`, `app/` Compose screens, bottom tabs (Home, Saved, Settings), tool host
- `app/src/test/` unit tests for the logic

## AdMob
Uses Google's TEST app ID (manifest) and TEST banner unit (`ads/Ads.kt`). Banner appears on Home only, in a reserved 50dp strip;
if consent/SDK/ad fails, the strip stays empty. Before release: replace both IDs, review UMP consent setup in AdMob, privacy policy, Data safety.
Never tap live ads.

## Known limitations / not done
- QR is shown on screen only (no PNG save/share in v1).
- Notes list uses a plain scrolling column (fine for hundreds of notes, not tens of thousands).
- Notes autosave ~0.6s after typing stops, and on Done/Back.
- No release signing config, no instrumented/UI tests, no accessibility audit, no tablet layouts verified.
- INTERNET permission is present only for the ad SDK.

## Release checklist
See section 13 of the blueprint. Nothing is checked off yet.
