package com.ysfzone.goclean.engine

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class CleanTier {
    TIER_1_LEGACY,
    TIER_2_DEEP,
    TIER_3_ANALYZE
}

data class CleanResult(
    val stats: MemoryStats,
    val tier: CleanTier,
    val processesCleaned: Int,
    val isOptimized: Boolean
)

object SmartCleaner {

    suspend fun cleanAndAnalyze(context: Context): CleanResult = withContext(Dispatchers.IO) {
        val before = MemoryManager.readMemorySnapshot(context)
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val protectedPackages = ProtectedApps.getProtectedPackages(context)
        val mediaActive = ProtectedApps.hasActiveMediaPlayback(context)
        val targetPackages = collectEligiblePackages(context, am, protectedPackages, mediaActive)

        var cleanedCount = 0
        val tier: CleanTier
        val optimized: Boolean

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            // Tier 1: Android 9–13 (API 28–33)
            tier = CleanTier.TIER_1_LEGACY
            optimized = true
            targetPackages.forEach { pkg ->
                runCatching {
                    am.killBackgroundProcesses(pkg)
                    cleanedCount++
                }
            }
        } else if (ShizukuHelper.isShizukuReady() || ShizukuHelper.isRootAvailable()) {
            // Tier 2: Android 14+ (API 34+) with Shizuku or Root
            tier = CleanTier.TIER_2_DEEP
            optimized = true
            cleanedCount = ShizukuHelper.forceStopPackages(targetPackages)
        } else {
            // Tier 3: Android 14+ Standard Diagnostic Fallback
            tier = CleanTier.TIER_3_ANALYZE
            optimized = false
            System.gc()
        }

        delay(250L)
        val after = MemoryManager.readMemorySnapshot(context)
        val stats = MemoryManager.calculateStats(before, after)

        CleanResult(
            stats = stats,
            tier = tier,
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

        // Also include non-system installed apps for Tier 1/Tier 2 background sweeping
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

