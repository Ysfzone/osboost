package com.osboost.engine

import android.app.ActivityManager
import android.content.Context
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

data class MemorySnapshot(
    val totalBytes: Long,
    val availBytes: Long
) {
    val usedBytes: Long
        get() = max(0L, totalBytes - availBytes)

    val usedPercentage: Int
        get() = if (totalBytes > 0L) {
            ((usedBytes.toDouble() / totalBytes.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
        } else {
            0
        }
}

data class MemoryStats(
    val before: MemorySnapshot,
    val after: MemorySnapshot,
    val deltaAvailableMB: Int,
    val availableFormatted: String,
    val usedFormatted: String,
    val totalFormatted: String
)

object MemoryManager {

    private const val BYTES_PER_MB = 1024.0 * 1024.0
    private const val BYTES_PER_GB = 1024.0 * 1024.0 * 1024.0

    fun readMemorySnapshot(context: Context): MemorySnapshot {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        return MemorySnapshot(
            totalBytes = memInfo.totalMem,
            availBytes = memInfo.availMem
        )
    }

    fun calculateStats(before: MemorySnapshot, after: MemorySnapshot): MemoryStats {
        val deltaBytes = max(0L, after.availBytes - before.availBytes)
        val deltaMB = (deltaBytes / BYTES_PER_MB).roundToInt()

        return MemoryStats(
            before = before,
            after = after,
            deltaAvailableMB = deltaMB,
            availableFormatted = formatBytes(after.availBytes),
            usedFormatted = formatBytes(after.usedBytes),
            totalFormatted = formatBytes(after.totalBytes)
        )
    }

    private fun formatBytes(bytes: Long): String {
        val gb = bytes / BYTES_PER_GB
        return if (gb >= 1.0) {
            String.format(Locale.US, "%.1f GB", gb)
        } else {
            val mb = (bytes / BYTES_PER_MB).roundToInt()
            "$mb MB"
        }
    }
}

