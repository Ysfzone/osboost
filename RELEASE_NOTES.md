### 🔄 Changed
- Locked the popup card container in `activity_main.xml` to fixed `440dp` × `310dp` dimensions with a shared `FrameLayout` central stage so the card height never jumps between cleaning and results states.
- Replaced the instant stage swap with a smooth rocket fly-away and gradual fade-out animation (`AccelerateInterpolator` upward translation + alpha fade for the Infinity Rocket, aura, and `Cleaning…` pill).
- Added a minimalist `DONE` completion badge (`DONE` / `TERMINÉ` / `تم`) with an Electric Cyan (`#00E5FF`) vector checkmark (`ic_check_cyan.xml`) centered above the available RAM hero metric.
- Bumped app version to `v1.2.1` (`versionCode = 5`) and release APK artifact to `OS-BOOST-v1.2.1.apk`.
