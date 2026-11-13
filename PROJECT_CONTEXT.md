# PROJECT_CONTEXT.md — OS BOOST (Android TV RAM Cleaner & Analyzer)

## 1. Core Architecture & Constraints
- **App Name:** OS BOOST (`OS B∞ST`)
- **Package Name:** `com.osboost`
- **GitHub Repo (Updater):** `https://api.github.com/repos/ysfzone/osboost/releases/latest`
- **Language:** 100% Native Kotlin
- **UI Framework:** Standard Android XML Views (NO Jetpack Compose, NO Flutter, NO Material3)
- **SDK Targets:** Min SDK 28 (Android 9), Target SDK 35 (Android 15), Compile SDK 35
- **Distribution:** GitHub Releases ONLY (APK size target < 1.5 MB via R8 `isMinifyEnabled = true`, `isShrinkResources = true`)
- **Lifecycle & Services:** Zero persistent background services, zero `BootReceiver`, zero settings screen, zero local database. Launches on-demand from TV Home banner -> translucent floating dialog -> scan/clean -> results -> auto-dismiss (`2.0s`).
- **TV Navigation:** Full D-Pad / TV Remote support (`focusable`, `nextFocus*`, neon focus ring).
- **Localization:** English (`values`), Arabic (`values-ar`, full RTL), French (`values-fr`).
- **Dark Neon Palette:**
  - Card Background: `#EE121622` (20dp radius, 1.5dp `#00E5FF` border)
  - Primary Neon Cyan: `#00E5FF`
  - Secondary Electric Blue: `#2979FF`
  - Positive Result Emerald: `#00E676`
  - Text Primary: `#FFFFFF`
  - Text Secondary: `#94A3B8`
- **3-Tier Hybrid Cleaning Engine (`com.osboost.engine`):**
  - **Tier 1 (API 28–33):** `ActivityManager.killBackgroundProcesses(pkg)` on non-protected apps.
  - **Tier 2 (API 34+ with Shizuku or Root):** Silent check for authorized Shizuku or `su`; executes `am force-stop <pkg>` on non-protected apps. No user prompts if absent.
  - **Tier 3 (API 34+ Standard Fallback):** `System.gc()` + accurate Before/After/Available RAM diagnostic analysis (`ANALYZED` badge).

## 2. File Map
- `settings.gradle.kts` — Gradle plugin repositories, dependency resolution, root project `OS-BOOST`, includes `:app`.
- `build.gradle.kts` — Root Gradle build script declaring Android Application and Kotlin Android plugins.
- `gradle.properties` — JVM args, AndroidX, non-transitive R classes, Kotlin code style.
- `app/build.gradle.kts` — Module config (`com.osboost`, minSdk 28, targetSdk 35, R8 minification/shrinking, conditional release signing, BuildConfig, minimal deps).
- `app/proguard-rules.pro` — R8/ProGuard keep rules for Shizuku, coroutines, and `com.osboost.MainActivity`.
- `app/src/main/AndroidManifest.xml` — Leanback + Launcher intents, translucent theme, TV banner, permissions, `FileProvider`, `ShizukuProvider`.
- `app/src/main/res/values/colors.xml` — Dark Neon color tokens.
- `app/src/main/res/values/themes.xml` — `Theme.OsBoost.Translucent` extending `@android:style/Theme.Translucent.NoTitleBar`.
- `app/src/main/res/values/strings.xml` — English strings (default).
- `app/src/main/res/values-ar/strings.xml` — Arabic strings (RTL).
- `app/src/main/res/values-fr/strings.xml` — French strings.
- `app/src/main/res/drawable/tv_banner.xml` — 320x180 vector banner with rocket icon and "OS BOOST" geometric vector lettering.
- `app/src/main/res/drawable/ic_rocket.xml` — Custom neon rocket `VectorDrawable`.
- `app/src/main/res/drawable/ic_flame.xml` — Animated exhaust flame `VectorDrawable`.
- `app/src/main/res/drawable/bg_popup_neon.xml` — Floating card shape (`#EE121622`, 20dp radius, 1.5dp `#00E5FF` border).
- `app/src/main/res/drawable/bg_badge_neon.xml` — Pill badge background for status/delta indicators.
- `app/src/main/res/drawable/bg_update_button.xml` — D-Pad focusable selector with neon cyan focus ring.
- `app/src/main/res/drawable/progress_neon.xml` — Custom neon cyan-to-electric-blue horizontal progress bar.
- `app/src/main/res/xml/file_paths.xml` — `FileProvider` paths for APK self-update installation.
- `app/src/main/res/layout/activity_main.xml` — Centered `420dp` floating neon popup UI with D-Pad support and RTL-safe constraints.
- `app/src/main/java/com/osboost/engine/MemoryManager.kt` — `readMemorySnapshot(context): MemorySnapshot`, `calculateStats(before, after): MemoryStats`
- `app/src/main/java/com/osboost/engine/ProtectedApps.kt` — `getProtectedPackages(context): Set<String>`, `hasActiveMediaPlayback(context): Boolean`, `isProcessAudioOrForegroundProtected(processInfo, mediaActive): Boolean`
- `app/src/main/java/com/osboost/engine/ShizukuHelper.kt` — `isShizukuReady(): Boolean`, `isRootAvailable(): Boolean`, `forceStopPackages(packages): Int`
- `app/src/main/java/com/osboost/engine/SmartCleaner.kt` — `suspend fun cleanAndAnalyze(context): CleanResult`
- `app/src/main/java/com/osboost/audio/SoundEffects.kt` — `playLaunchWhoosh(context)`, `playCompleteChime(context)`
- `app/src/main/java/com/osboost/updater/UpdateChecker.kt` — `suspend fun checkLatestRelease(): ReleaseInfo?`, `downloadAndInstall(context, release)`
- `app/src/main/java/com/osboost/MainActivity.kt` — Floating dialog lifecycle, 2.2s animation sequence, D-Pad handling, auto-dismiss.
- `app/src/test/java/com/osboost/CoreLogicTest.kt` — Unit tests for `MemoryManager.calculateStats` and `UpdateChecker.isNewerVersion`.
- `.github/workflows/release.yml` — GitHub Actions workflow to build, sign (via secrets), and attach the release APK on `v*` tag push.
- `.gitignore` — Excludes build artifacts, `local.properties`, and keystore files.

## 3. Progress Checklist
### Completed
- [x] `PROJECT_CONTEXT.md` initialized & updated for `OS BOOST` (`com.osboost`)
- [x] Gradle configuration (`settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `app/build.gradle.kts`, `app/proguard-rules.pro`, `local.properties`, Gradle 8.11.1 wrapper)
- [x] `AndroidManifest.xml` & Resource XMLs (`colors.xml`, `themes.xml`, `strings.xml` EN/AR/FR, drawables, `file_paths.xml`, `activity_main.xml`)
- [x] Kotlin Cleaning Engine, Sound Effects, GitHub Updater (`ysfzone/osboost`), and `MainActivity.kt`
- [x] Package & brand refactor to `OS BOOST` (`com.osboost`)
- [x] Unit tests & signed release build verification
