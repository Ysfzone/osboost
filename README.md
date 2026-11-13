# OS BOOST — Android TV RAM Cleaner & Analyzer

[![Release](https://img.shields.io/github/v/release/ysfzone/osboost?style=flat-square&color=00E5FF)](https://github.com/ysfzone/osboost/releases)
[![Android](https://img.shields.io/badge/Android-9%20to%2015-2979FF?style=flat-square)](https://github.com/ysfzone/osboost)

Ultra-lightweight (~245 KB), 100% native Kotlin RAM cleaner and memory analyzer built specifically for **Android TV**, **Google TV**, and **TV Boxes** (`minSdk 28`, `targetSdk 35`, package `com.osboost`).

> **Zero background services.** Launches on-demand from the Android TV Home banner, floats as a Dark Neon dialog over your launcher, optimizes or analyzes memory, and auto-dismisses.

---

## Key Features
- **3-Tier Hybrid Cleaning Engine:**
  - **Tier 1 (Android 9–13 / API 28–33):** Cleans eligible background processes via `ActivityManager.killBackgroundProcesses`.
  - **Tier 2 (Android 14+ / API 34+ with Shizuku or Root):** Silently detects authorized Shizuku or `su` and runs `am force-stop` for true deep cleaning—never nags if absent.
  - **Tier 3 (Android 14+ Standard Fallback):** Runs `System.gc()` and accurate Before/After RAM diagnostics (`ANALYZED` badge).
- **Smart Protection Rules:** Automatically excludes your active TV Home Launcher, `com.osboost`, SystemUI, Google Play Services, Android TV Remote, Bluetooth/Wi-Fi stack, active keyboards (IMEs), and any app currently playing audio/IPTV media.
- **TV-First Dark Neon UI:** `420dp` floating translucent card, vector rocket launch animation, synthesized `AudioTrack` sound effects (zero audio asset bloat), and full D-Pad / TV Remote navigation.
- **Localized Automatically:** English, Arabic (full RTL), and French.
- **Silent GitHub Auto-Updater:** Checks [`ysfzone/osboost` Releases](https://github.com/ysfzone/osboost/releases) asynchronously and surfaces a D-Pad-focused update button only when a newer APK is available.

---

## Download & Install
Download the latest signed APK from [GitHub Releases](https://github.com/ysfzone/osboost/releases/latest) and sideload via ADB:
```bash
adb install -r app-release.apk
```

---

## Android 14+ Deep Cleaning (ADB / Shizuku Guide)
On Android 14+ (API 34+), Android restricts third-party apps from killing other apps' background processes. To unlock **Tier 2 Deep Cleaning** without root:
1. Install **Shizuku** on your Android TV / TV Box.
2. Enable **Developer Options** -> **USB / Wireless Debugging** and pair/start Shizuku via ADB:
   ```bash
   adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
   ```
3. Grant **OS BOOST** permission inside Shizuku (once).
4. From then on, launching **OS BOOST** from your TV Home Screen automatically uses Tier 2 (`OPTIMIZED`).

---

## نظرة عامة (العربية)

تطبيق **OS BOOST** هو منظف ومحلل ذاكرة عشوائية (RAM) فائق الخفة (~245 كيلوبايت) مخصص لأجهزة **Android TV** و **Google TV** و **TV Box**، مبني بالكامل بلغة Kotlin الأصلية دون أي خدمات خلفية دائمة.

### المميزات الرئيسية
- **محرك تنظيف هجين من 3 مستويات:**
  - **المستوى 1 (أندرويد 9–13):** تنظيف العمليات الخلفية غير المحمية مباشرة.
  - **المستوى 2 (أندرويد 14+ مع Shizuku أو Root):** فحص صامت لوجود Shizuku أو صلاحيات الروت لتنفيذ إيقاف عميق (`am force-stop`) دون إزعاج المستخدم إذا لم تتوفر الصلاحيات.
  - **المستوى 3 (الوضع القياسي لأندرويد 14+):** تحليل دقيق للذاكرة المتاحة والمستخدمة قبل وبعد الفحص.
- **حماية ذكية:** يستثني تلقائياً واجهة التلفاز الرئيسية، خدمات النظام والشبكة والبلوتوث، لوحات المفاتيح، وأي تطبيق يشغّل صوتاً أو بث IPTV حالياً.
- **واجهة نيون داكنة سريعة:** نافذة عائمة بتصميم مخصص للتلفاز، دعم كامل لأزرار الريموت (D-Pad)، ودعم كامل للغة العربية (RTL) والإنجليزية والفرنسية.
- **تحديث صامت عبر GitHub Releases:** يفحص التحديثات في الخلفية بصمت ويظهر زر التحديث فقط عند توفر إصدار جديد.
