> 🔗 **MANDATORY RULE CHAIN:** Before starting ANY task, UI edit, or release, you MUST read [`BRAND_GUIDELINES.md`](BRAND_GUIDELINES.md) (plus [`assets/brand_sheet.jpeg`](assets/brand_sheet.jpeg)) AND [`RELEASE_AND_QUALITY_RULES.md`](RELEASE_AND_QUALITY_RULES.md). Do not execute anything without obeying all three files together.

> **CRITICAL IMMUTABLE BRAND RULE:** Before creating or modifying ANY UI screen, popup, Android TV banner, launcher icon, GitHub README, web landing page, or promotional asset for `OS BOOST` (`com.osboost`), you MUST inspect `BRAND_GUIDELINES.md` and `assets/brand_sheet.jpeg`. You are strictly forbidden from altering the Infinity Rocket vector geometry, the `OS B∞ST` wordmark fusion, or introducing ANY hex color outside the 7 approved colors in `res/values/colors.xml`.

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
- **Exclusive 7-Color Palette (`res/values/colors.xml`):**
  - `brand_bg_obsidian`: `#0D1117`
  - `brand_bg_slate`: `#131922`
  - `brand_cyan_electric`: `#00E5FF`
  - `brand_blue_cobalt`: `#1E50B5`
  - `brand_white_pure`: `#FFFFFF`
  - `brand_black_pure`: `#000000`
  - `brand_silver_muted`: `#8A94A6`
- **3-Tier Hybrid Cleaning Engine (`com.osboost.engine`):**
  - **Tier 1 (API 28–33):** `ActivityManager.killBackgroundProcesses(pkg)` on non-protected apps.
  - **Tier 2 (API 34+ with Shizuku or Root):** Silent check for authorized Shizuku or `su`; executes `am force-stop <pkg>` on non-protected apps. No user prompts if absent.
  - **Tier 3 (API 34+ Standard Fallback):** `System.gc()` + accurate Before/After/Available RAM diagnostic analysis (`ANALYZED` badge).

## 2. File Map
- `BRAND_GUIDELINES.md` — Authoritative 7-color palette, Infinity Rocket geometry, and 6-variant brand usage specification.
- `RELEASE_AND_QUALITY_RULES.md` — Mandatory attribution, English changelog, APK naming, README badge sync, TV Emulator QA gate, and semver rules.
- `assets/brand_sheet.jpeg` — Visual source of truth (6-panel Brand Identity Sheet).
- `assets/logo.svg` — Master vector lockup (`OS B∞ST` + Infinity Rocket) for GitHub/web.
- `settings.gradle.kts` — Gradle plugin repositories, dependency resolution, root project `OS-BOOST`, includes `:app`.
- `build.gradle.kts` — Root Gradle build script declaring Android Application and Kotlin Android plugins.
- `gradle.properties` — JVM args, AndroidX, non-transitive R classes, Kotlin code style.
- `app/build.gradle.kts` — Module config (`com.osboost`, minSdk 28, targetSdk 35, R8 minification/shrinking, conditional release signing, BuildConfig, minimal deps).
- `app/proguard-rules.pro` — R8/ProGuard keep rules for Shizuku, coroutines, and `com.osboost.MainActivity`.
- `app/src/main/AndroidManifest.xml` — Leanback + Launcher intents, translucent theme, TV banner, adaptive launcher icons, permissions, `FileProvider`, `ShizukuProvider`.
- `app/src/main/res/values/colors.xml` — Exclusive 7-color brand palette.
- `app/src/main/res/values/themes.xml` — `Theme.OsBoost.Translucent` extending `@android:style/Theme.Translucent.NoTitleBar`.
- `app/src/main/res/values/strings.xml` — English strings (default).
- `app/src/main/res/values-ar/strings.xml` — Arabic strings (RTL).
- `app/src/main/res/values-fr/strings.xml` — French strings.
- `app/src/main/res/drawable/tv_banner.xml` — 320x180 Panel 5 horizontal TV banner lockup (`#131922` + Infinity Rocket + `OS B∞ST`).
- `app/src/main/res/drawable/ic_osboost_rocket.xml` — Master Infinity Rocket `VectorDrawable` (`#00E5FF` -> `#1E50B5` gradient).
- `app/src/main/res/drawable/ic_flame.xml` — Animated exhaust flame `VectorDrawable`.
- `app/src/main/res/drawable/ic_launcher_background.xml` & `ic_launcher_foreground.xml` — Adaptive icon layers (Panel 6).
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` & `ic_launcher_round.xml` — Adaptive launcher icons.
- `app/src/main/res/drawable/bg_popup_neon.xml` — Floating card shape (`#131922`, 20dp radius, 1.5dp `#00E5FF` border).
- `app/src/main/res/drawable/bg_badge_neon.xml` — Pill badge background for status/delta indicators.
- `app/src/main/res/drawable/bg_update_button.xml` — D-Pad focusable selector with `#00E5FF` focus ring.
- `app/src/main/res/drawable/progress_neon.xml` — Horizontal progress bar (`#1E50B5` -> `#00E5FF`).
- `app/src/main/res/xml/file_paths.xml` — `FileProvider` paths for APK self-update installation.
- `app/src/main/res/layout/activity_main.xml` — Centered `420dp` floating popup UI with `OS B∞ST` header, D-Pad support, and RTL-safe layout.
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
- [x] `PROJECT_CONTEXT.md` & `BRAND_GUIDELINES.md` locked with immutable brand rule
- [x] `assets/brand_sheet.jpeg` & `assets/logo.svg` archived
- [x] Exclusive 7-color palette enforced across `colors.xml`, drawables, layouts, and `MainActivity.kt`
- [x] Infinity Rocket (`ic_osboost_rocket.xml`), Panel 5 `tv_banner.xml`, and Panel 6 adaptive launcher icons
- [x] Release `v1.1.0`: Translucent glass UI, explicit engine mode pill, universal 3-tier escalation with one-time root prompt, and `By Ysfzone` footer
- [x] Release `v1.1.1`: Borderless full-bleed TV banner & launcher icons, +14% emblem scale boost, and multi-density PNG asset buckets (`mdpi`–`xxxhdpi`, `xhdpi`/`xxhdpi` banners, `512×512` master icon)
- [x] Release `v1.2.0`: Corner-glow borderless divider-free banner, top-right strategy pill, full-height rocket launch, pulsing update glow, background media cleanup, and unmanaged `su` root support
- [x] Release `v1.2.1`: Fixed-height popup card, rocket fly-away animation, and DONE completion badge
