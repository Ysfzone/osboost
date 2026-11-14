### 🔄 Changed
- Redesigned the RAM progress bar (`bg_progress_bar.xml` & `activity_main.xml`) into an ultra-minimalist `3dp` `Aurora Hairline` pill (`999dp` radius) with a translucent `#268A94A6` track and a clean Deep Cobalt (`#1E50B5`) to Electric Cyan (`#00E5FF`) gradient fill animated with `DecelerateInterpolator`.
- Accelerated the symbol-only circular checkmark (`✓`) fade-out to `200ms` (`AccelerateInterpolator`), triggered `1100ms` before auto-dismissal.
- Replaced the Update button's scale bounce with a stationary `1800ms`-cycle ambient breathing glow (`#4D00E5FF` ↔ `#CC00E5FF` border stroke and `0.78f` ↔ `1.0f` opacity).
- Bumped app version to `v1.2.3` (`versionCode = 7`) and release APK artifact to `OS-BOOST-v1.2.3.apk`.
