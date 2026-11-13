package com.ysfzone.goclean.engine

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.File

object ShizukuHelper {

    private val SU_PATHS = arrayOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/su/bin/su",
        "/vendor/bin/su"
    )

    fun isShizukuReady(): Boolean = runCatching {
        Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    fun isRootAvailable(): Boolean {
        if (SU_PATHS.any { File(it).exists() }) return true
        return runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            process.waitFor() == 0
        }.getOrDefault(false)
    }

    fun forceStopPackages(packages: List<String>): Int {
        if (packages.isEmpty()) return 0

        if (isShizukuReady()) {
            val count = executeViaShizuku(packages)
            if (count >= 0) return count
        }

        if (isRootAvailable()) {
            return executeViaRoot(packages)
        }

        return 0
    }

    private fun executeViaShizuku(packages: List<String>): Int = runCatching {
        val script = packages.joinToString("; ") { "am force-stop $it" }
        val newProcessMethod = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        ).apply { isAccessible = true }

        val process = newProcessMethod.invoke(
            null,
            arrayOf("sh", "-c", script),
            null,
            null
        ) as Process

        process.waitFor()
        packages.size
    }.getOrDefault(-1)

    private fun executeViaRoot(packages: List<String>): Int = runCatching {
        val script = packages.joinToString("; ") { "am force-stop $it" }
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", script))
        process.waitFor()
        packages.size
    }.getOrDefault(0)
}

