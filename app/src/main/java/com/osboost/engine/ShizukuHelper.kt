package com.osboost.engine

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.File
import java.util.concurrent.TimeUnit

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

    private const val SU_TIMEOUT_MS = 2500L

    internal val STANDARD_SU_PATHS = arrayOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/su/bin/su",
        "/vendor/bin/su",
        "/system/bin/.ext/.su"
    )

    fun isShizukuReady(): Boolean = runCatching {
        Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }.getOrDefault(false)

    fun isRootBinaryPresent(
        knownPaths: Array<String> = STANDARD_SU_PATHS,
        pathEnv: String? = System.getenv("PATH"),
        fileExists: (String) -> Boolean = { File(it).exists() }
    ): Boolean {
        if (knownPaths.any(fileExists)) return true

        if (!pathEnv.isNullOrBlank()) {
            val foundInPath = pathEnv.split(File.pathSeparatorChar)
                .filter { it.isNotBlank() }
                .any { dir -> fileExists(File(dir, "su").path) }
            if (foundInPath) return true
        }

        return runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val finished = process.waitFor(SU_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            if (!finished) {
                process.destroyForcibly()
                false
            } else {
                process.exitValue() == 0
            }
        }.getOrDefault(false)
    }

    /**
     * One-time root prompt policy with 2.5s non-blocking timeout:
     * - Factory pre-rooted / unmanaged `su` firmware escalates immediately.
     * - Managed root (Magisk/SuperSU) prompts at most once; never nags if denied.
     */
    fun resolveRootAccess(
        store: RootPromptStore,
        suExists: () -> Boolean = { isRootBinaryPresent() },
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

            process.waitFor(SU_TIMEOUT_MS, TimeUnit.MILLISECONDS)
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
            val finished = process.waitFor(5000L, TimeUnit.MILLISECONDS)
            if (!finished) {
                process.destroyForcibly()
                0
            } else if (process.exitValue() == 0) {
                packages.size
            } else {
                0
            }
        }.getOrDefault(0)
    }

    private fun executeSuId(): Boolean = runCatching {
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
        val finished = process.waitFor(SU_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        if (!finished) {
            process.destroyForcibly()
            return false
        }
        val output = process.inputStream.bufferedReader().use { it.readText() }
        process.exitValue() == 0 && output.contains("uid=0")
    }.getOrDefault(false)
}
