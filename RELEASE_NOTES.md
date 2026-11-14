### ✨ Added
- Borderless dual-corner radial glow lighting (`#1E50B5` / `#00E5FF` on `#131922` → `#0D1117`) across the Android TV banner, adaptive icon background, and multi-density PNG assets (`mdpi`–`xxxhdpi`, `xhdpi`/`xxhdpi` banners, `512×512` master icon).
- Full support for factory pre-rooted / unmanaged `su` firmware across standard TV-box paths (`/system/bin/.ext/.su`) and `PATH` lookup with a safe 2.5-second non-blocking timeout.
- Continuous pulsing glow animation on the "Update Available" button when a newer GitHub release is detected.

### 🔄 Changed
- Moved the active engine strategy pill (`DEEP BOOST • ROOT`, `DEEP BOOST • SHIZUKU`, `STANDARD BOOST`, `SMART ANALYSIS`) permanently to the top-right header corner, replacing the duplicate status badge.
- Repositioned the localized `Cleaning…` status pill below the Infinity Rocket and hid it cleanly once results appear.
- Extended the Infinity Rocket launch trajectory to ascend to the top inner edge of the popup card.
- Simplified completion telemetry to display freed/available RAM and `RAM OPTIMIZED` (EN) / `RAM OPTIMISÉE` (FR) / `الذاكرة محسّنة` (AR).
- Removed the background audio/media exemption so background IPTV, video, and music apps are stopped during cleaning while preserving core system, launcher, and privilege manager packages.
- Removed the vertical divider from `tv_banner.xml` and updated release artifact to `OS-BOOST-v1.2.0.apk`.
