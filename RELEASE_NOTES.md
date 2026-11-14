### 🔄 Changed
- Refined the popup card container in `activity_main.xml` to `430dp` × `275dp` with uniform `20dp` padding, `8dp` vertical spacing, and optical centering of the shared central `FrameLayout` stage between header and telemetry bar.
- Replaced the `"DONE"` text badge with a symbol-only `34dp` × `34dp` circular checkmark badge (`bg_check_circle_cyan.xml` with `#2600E5FF` fill, `1dp` `#6600E5FF` stroke, and centered `#00E5FF` vector checkmark).
- Choreographed the completion lifecycle in `MainActivity.kt` to smoothly fade out the circular checkmark badge (`250ms` alpha `1f -> 0f`) `800ms` before auto-dismissal.
- Bumped app version to `v1.2.2` (`versionCode = 6`) and release APK artifact to `OS-BOOST-v1.2.2.apk`.
