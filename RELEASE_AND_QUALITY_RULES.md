> 🔗 **MANDATORY RULE CHAIN:** All UI footers, README badges, and release assets governed by this file MUST strictly follow the 7-color palette and `OS B∞ST` Infinity Rocket identity defined in [`BRAND_GUIDELINES.md`](BRAND_GUIDELINES.md) ([`assets/brand_sheet.jpeg`](assets/brand_sheet.jpeg)) and [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md).

# RELEASE_AND_QUALITY_RULES.md — OS B∞ST Governance, Versioning & QA Gate

---

## 1. Permanent Attribution & Version Footer (`By Ysfzone`)
- Always display the app version (`BuildConfig.VERSION_NAME`, e.g., `v1.0.0`) alongside **`By Ysfzone • X / GitHub: @Ysfzone`** elegantly at the bottom of the `OS B∞ST` popup UI (`activity_main.xml`):
  - Version (`v<versionName>`): `@color/brand_cyan_electric` (`#00E5FF`)
  - Attribution (`By Ysfzone • X / GitHub: @Ysfzone`): `@color/brand_silver_muted` (`#8A94A6`)
- Always include `By Ysfzone • X / GitHub: @Ysfzone` with direct links to `https://github.com/ysfzone` and `https://x.com/Ysfzone` in `README.md` and in any future website or graphic asset.

---

## 2. English-Only Technical GitHub Changelog
- Every GitHub Release must include a clean, structured technical Changelog in **English** containing ONLY software/codebase changes (never personal prompts or internal workflow rules), strictly categorized under:
  - `### ✨ Added`
  - `### 🔄 Changed`
  - `### 🐛 Fixed`

---

## 3. Explicit Release APK Naming
- Every release build (`app/build.gradle.kts` & `.github/workflows/release.yml`) must output and upload the exact signed filename:
  ```text
  OS-BOOST-v<versionName>.apk
  ```
  (For example: `OS-BOOST-v1.0.0.apk`.)

---

## 4. Synchronized README Version Badges
- Before every release push, automatically update the version badge(s) and download links in `README.md` (styled strictly in `#131922` and `#00E5FF`) to match `BuildConfig.VERSION_NAME`.

---

## 5. Mandatory Local Android Studio TV Emulator QA & Logcat Gate
- Before pushing or tagging ANY release:
  1. Build the signed release APK (`OS-BOOST-v<versionName>.apk`).
  2. Boot/use a local Android TV AVD via `adb` / `emulator`.
  3. Install `OS-BOOST-v<versionName>.apk` (`adb install -r`).
  4. Launch `com.osboost/.MainActivity`.
  5. Capture `adb logcat` for 4 seconds and verify **ZERO** exceptions or crashes before pushing the complete project to GitHub.

---

## 6. Strict Semantic Versioning (`MAJOR.MINOR.PATCH`)
- **MAJOR (`X.0.0`):** LOCKED — never change unless explicitly commanded by the user.
- **MINOR (`1.X.0`):** Increment ONLY when new features (`### ✨ Added`) are introduced (resetting PATCH to `0`).
- **PATCH (`1.0.X`):** Increment on EVERY release with bug fixes (`### 🐛 Fixed`) or minor changes/refinements (`### 🔄 Changed`).

