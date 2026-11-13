### ✨ Added
- Initial **OS B∞ST** (`com.osboost`) native Android TV & Google TV RAM cleaner and memory analyzer (`minSdk 28`, `targetSdk 35`).
- 3-tier hybrid cleaning engine: `killBackgroundProcesses` (API 28–33), silent Shizuku/Root `am force-stop` (API 34+), and `System.gc()` diagnostic analysis fallback (API 34+).
- Smart protection rules excluding active TV Home Launchers, system/network/Bluetooth/GMS services, enabled keyboards (IMEs), and active audio/media playback.
- Dark Neon floating dialog UI (`#131922` / `#00E5FF` / `#1E50B5`) with Infinity Rocket launch animation, synthesized `AudioTrack` sound effects, and full D-Pad remote navigation.
- Automatic localization for English, Arabic (full RTL), and French.
- Silent GitHub Releases auto-updater (`ysfzone/osboost`) with `DownloadManager` + `FileProvider` APK installation.

### 🔄 Changed
- Standardized release artifact naming to `OS-BOOST-v<versionName>.apk`.

### 🐛 Fixed
- Handled translucent floating window lifecycle and auto-dismiss cleanly across Android TV launchers.
