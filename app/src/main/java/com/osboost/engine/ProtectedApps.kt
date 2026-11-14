package com.osboost.engine

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager

object ProtectedApps {

    private val CORE_SYSTEM_AND_TV_PACKAGES = setOf(
        "android",
        "com.osboost",
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
        "moe.shizuku.privileged.api",
        "com.topjohnwu.magisk"
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

    /**
     * Only foreground UI processes are protected; background media/audio/IPTV apps
     * are intentionally eligible for termination to maximize freed RAM.
     */
    fun isForegroundProtected(processInfo: ActivityManager.RunningAppProcessInfo): Boolean {
        return processInfo.importance <= ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }
}
