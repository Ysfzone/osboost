package com.osboost.engine

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class EngineMode {
    DEEP_ROOT,
    DEEP_SHIZUKU,
    STANDARD_BOOST,
    SMART_ANALYSIS
}

data class CleanResult(
    val stats: MemoryStats,
    val mode: EngineMode,
    val processesCleaned: Int,
    val isOptimized: Boolean
)

object SmartCleaner {

    fun selectEngineMode(
        rootGranted: Boolean,
        shizukuReady: Boolean,
        sdkInt: Int = Build.VERSION.SDK_INT
    ): EngineMode {
        return when {
            rootGranted -> EngineMode.DEEP_ROOT
            shizukuReady -> EngineMode.DEEP_SHIZUKU
            sdkInt <= Build.VERSION_CODES.TIRAMISU -> EngineMode.STANDARD_BOOST
            else -> EngineMode.SMART_ANALYSIS
        }
    }

    suspend fun cleanAndAnalyze(context: Context): CleanResult = withContext(Dispatchers.IO) {
        val before = MemoryManager.readMemorySnapshot(context)
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val protectedPackages = ProtectedApps.getProtectedPackages(context)
        val mediaActive = ProtectedApps.hasActiveMediaPlayback(context)
        val targetPackages = collectEligiblePackages(context, am, protectedPackages, mediaActive)

        val rootGranted = ShizukuHelper.resolveRootAccess(SharedPrefsRootPromptStore(context))
        val shizukuReady = !rootGranted && ShizukuHelper.isShizukuReady()
        val mode = selectEngineMode(rootGranted, shizukuReady)

        var cleanedCount = 0
        val optimized: Boolean

        when (mode) {
            EngineMode.DEEP_ROOT -> {
                optimized = true
                cleanedCount = ShizukuHelper.deepCleanViaRoot(targetPackages)
            }
            EngineMode.DEEP_SHIZUKU -> {
                optimized = true
                cleanedCount = ShizukuHelper.deepCleanViaShizuku(targetPackages)
            }
            EngineMode.STANDARD_BOOST -> {
                optimized = true
                targetPackages.forEach { pkg ->
                    runCatching {
                        am.killBackgroundProcesses(pkg)
                        cleanedCount++
                    }
                }
            }
            EngineMode.SMART_ANALYSIS -> {
                optimized = false
                System.gc()
            }
        }

        delay(250L)
        val after = MemoryManager.readMemorySnapshot(context)
        val stats = MemoryManager.calculateStats(before, after)

        CleanResult(
            stats = stats,
            mode = mode,
            processesCleaned = cleanedCount,
            isOptimized = optimized
        )
    }

    private fun collectEligiblePackages(
        context: Context,
        am: ActivityManager,
        protectedPackages: Set<String>,
        mediaActive: Boolean
    ): List<String> {
        val candidates = LinkedHashSet<String>()

        am.runningAppProcesses?.forEach { proc ->
            if (!ProtectedApps.isProcessAudioOrForegroundProtected(proc, mediaActive)) {
                proc.pkgList?.forEach { pkg ->
                    if (pkg !in protectedPackages) {
                        candidates.add(pkg)
                    }
                }
            }
        }

        if (!mediaActive) {
            context.packageManager.getInstalledApplications(0).forEach { appInfo ->
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                if (!isSystem && appInfo.packageName !in protectedPackages) {
                    candidates.add(appInfo.packageName)
                }
            }
        }

        return candidates.toList()
    }
}
