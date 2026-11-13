package com.osboost.engine

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.File

interface RootPromptStore {
    var hasAttemptedRootPrompt: Boolean
    var isRootGranted: Boolean
}

class SharedPrefsRootPromptStore(context: Context) : RootPromptStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override var hasAttemptedRootPrompt: Boolean
        get() = prefs.getBoolean(KEY_ATTEMPTED_ROOT, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ATTEMPTED_ROOT, value).apply()
        }

    override var isRootGranted: Boolean
        get() = prefs.getBoolean(KEY_ROOT_GRANTED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_ROOT_GRANTED, value).apply()
        }

    companion object {
        private const val PREFS_NAME = "osboost_prefs"
        private const val KEY_ATTEMPTED_ROOT = "has_attempted_root_prompt"
        private const val KEY_ROOT_GRANTED = "is_root_granted"
    }
}

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

    fun isRootBinaryPresent(): Boolean {
        if (SU_PATHS.any { File(it).exists() }) return true
        return runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            process.waitFor() == 0
        }.getOrDefault(false)
    }

    /**
     * One-time root prompt policy:
     * Prompts at most once when `su` exists; never prompts again if denied.
     */
    fun resolveRootAccess(
        store: RootPromptStore,
        suExists: () -> Boolean = ::isRootBinaryPresent,
        requestSuId: () -> Boolean = ::executeSuId
    ): Boolean {
        if (!suExists()) return false

        if (!store.hasAttemptedRootPrompt) {
            store.hasAttemptedRootPrompt = true
            val granted = requestSuId()
            store.isRootGranted = granted
            return granted
        }

        if (!store.isRootGranted) {
            return false
        }

        val stillGranted = requestSuId()
        if (!stillGranted) {
            store.isRootGranted = false
        }
        return stillGranted
    }

    fun deepCleanViaShizuku(packages: List<String>): Int {
        if (packages.isEmpty() || !isShizukuReady()) return 0
        return runCatching {
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
        }.getOrDefault(0)
    }

    fun deepCleanViaRoot(packages: List<String>): Int {
        return runCatching {
            val stopCmds = packages.joinToString("; ") { "am force-stop $it" }
            val dropCachesCmd = "sync && echo 3 > /proc/sys/vm/drop_caches"
            val fullScript = if (stopCmds.isNotEmpty()) {
                "$stopCmds; $dropCachesCmd"
            } else {
                dropCachesCmd
            }
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", fullScript))
            if (process.waitFor() == 0) packages.size else 0
        }.getOrDefault(0)
    }

    private fun executeSuId(): Boolean = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val output = process.inputStream.bufferedReader().use { it.readText() }
        process.waitFor() == 0 && output.contains("uid=0")
    }.getOrDefault(false)
}
