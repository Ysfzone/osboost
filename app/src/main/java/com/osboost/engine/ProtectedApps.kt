package com.osboost.engine

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.view.inputmethod.InputMethodManager

object ProtectedApps {

    private val CORE_SYSTEM_AND_TV_PACKAGES = setOf(
        "android",
        "com.android.systemui",
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.android.vending",
        "com.google.android.tv.remote.service",
        "com.google.android.leanbacklauncher",
        "com.google.android.tvlauncher",
        "com.google.android.apps.tv.launcherx",
        "com.android.tv.settings",
        "com.google.android.katniss",
        "com.android.bluetooth",
        "com.google.android.bluetooth",
        "com.android.networkstack",
        "com.google.android.networkstack",
        "com.android.wifi",
        "com.android.connectivity.resources",
        "com.android.providers.media",
        "com.google.android.providers.media.module",
        "moe.shizuku.privileged.api",
        "com.osboost"
    )

    fun getProtectedPackages(context: Context): Set<String> {
        val protected = HashSet<String>(CORE_SYSTEM_AND_TV_PACKAGES)
        protected.add(context.packageName)

        // Dynamically exclude active Home Launcher(s)
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val pm = context.packageManager
        pm.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo
            ?.packageName
            ?.let { protected.add(it) }

        pm.queryIntentActivities(homeIntent, 0).forEach { info ->
            info.activityInfo?.packageName?.let { protected.add(it) }
        }

        // Exclude enabled Input Methods / Keyboards
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.enabledInputMethodList?.forEach { inputMethod ->
            protected.add(inputMethod.packageName)
        }

        return protected
    }

    fun hasActiveMediaPlayback(context: Context): Boolean {
        val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return false
        return am.isMusicActive || am.activePlaybackConfigurations.isNotEmpty()
    }

    fun isProcessAudioOrForegroundProtected(
        processInfo: ActivityManager.RunningAppProcessInfo,
        mediaActive: Boolean
    ): Boolean {
        val importance = processInfo.importance
        if (importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
            return true
        }
        if (mediaActive && importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE) {
            return true
        }
        return false
    }
}

