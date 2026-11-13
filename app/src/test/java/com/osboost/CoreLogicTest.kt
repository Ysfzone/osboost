package com.osboost

import com.osboost.engine.CleanResult
import com.osboost.engine.EngineMode
import com.osboost.engine.MemoryManager
import com.osboost.engine.MemorySnapshot
import com.osboost.engine.RootPromptStore
import com.osboost.engine.ShizukuHelper
import com.osboost.engine.SmartCleaner
import com.osboost.updater.UpdateChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreLogicTest {

    private class FakeRootPromptStore(
        override var hasAttemptedRootPrompt: Boolean = false,
        override var isRootGranted: Boolean = false
    ) : RootPromptStore

    @Test
    fun `calculateStats computes delta and formats GB and MB accurately`() {
        val mb = 1024L * 1024L
        val before = MemorySnapshot(
            totalBytes = 4096L * mb, // 4.0 GB
            availBytes = 1024L * mb  // 1.0 GB
        )
        val after = MemorySnapshot(
            totalBytes = 4096L * mb, // 4.0 GB
            availBytes = 1536L * mb  // 1.5 GB
        )

        val stats = MemoryManager.calculateStats(before, after)

        assertEquals(512, stats.deltaAvailableMB)
        assertEquals("1.5 GB", stats.availableFormatted)
        assertEquals("2.5 GB", stats.usedFormatted)
        assertEquals("4.0 GB", stats.totalFormatted)
        assertEquals(63, stats.after.usedPercentage)
    }

    @Test
    fun `calculateStats clamps negative delta to zero`() {
        val mb = 1024L * 1024L
        val before = MemorySnapshot(totalBytes = 2048L * mb, availBytes = 900L * mb)
        val after = MemorySnapshot(totalBytes = 2048L * mb, availBytes = 850L * mb)

        val stats = MemoryManager.calculateStats(before, after)

        assertEquals(0, stats.deltaAvailableMB)
        assertEquals("850 MB", stats.availableFormatted)
    }

    @Test
    fun `isNewerVersion handles semver tags accurately`() {
        assertTrue(UpdateChecker.isNewerVersion("v1.1.0", "1.0.0"))
        assertTrue(UpdateChecker.isNewerVersion("v1.0.1", "1.0.0"))
        assertTrue(UpdateChecker.isNewerVersion("2.0.0", "1.9.9"))
        assertFalse(UpdateChecker.isNewerVersion("v1.1.0", "1.1.0"))
        assertFalse(UpdateChecker.isNewerVersion("v0.9.9", "1.0.0"))
    }

    @Test
    fun `one-time root prompt asks once when su exists and never nags after denial`() {
        val store = FakeRootPromptStore()
        var promptCalls = 0

        // First launch: su exists, user denies
        val firstResult = ShizukuHelper.resolveRootAccess(
            store = store,
            suExists = { true },
            requestSuId = {
                promptCalls++
                false
            }
        )
        assertFalse(firstResult)
        assertTrue(store.hasAttemptedRootPrompt)
        assertFalse(store.isRootGranted)
        assertEquals(1, promptCalls)

        // Second launch: must NOT prompt again
        val secondResult = ShizukuHelper.resolveRootAccess(
            store = store,
            suExists = { true },
            requestSuId = {
                promptCalls++
                true
            }
        )
        assertFalse(secondResult)
        assertEquals(1, promptCalls)
    }

    @Test
    fun `one-time root prompt persists granted state`() {
        val store = FakeRootPromptStore()
        val granted = ShizukuHelper.resolveRootAccess(
            store = store,
            suExists = { true },
            requestSuId = { true }
        )
        assertTrue(granted)
        assertTrue(store.hasAttemptedRootPrompt)
        assertTrue(store.isRootGranted)
    }

    @Test
    fun `selectEngineMode resolves all 4 modes accurately`() {
        assertEquals(
            EngineMode.DEEP_ROOT,
            SmartCleaner.selectEngineMode(rootGranted = true, shizukuReady = true, sdkInt = 35)
        )
        assertEquals(
            EngineMode.DEEP_SHIZUKU,
            SmartCleaner.selectEngineMode(rootGranted = false, shizukuReady = true, sdkInt = 35)
        )
        assertEquals(
            EngineMode.STANDARD_BOOST,
            SmartCleaner.selectEngineMode(rootGranted = false, shizukuReady = false, sdkInt = 33)
        )
        assertEquals(
            EngineMode.SMART_ANALYSIS,
            SmartCleaner.selectEngineMode(rootGranted = false, shizukuReady = false, sdkInt = 34)
        )
    }

    @Test
    fun `CleanResult tracks closed app count accurately`() {
        val mb = 1024L * 1024L
        val stats = MemoryManager.calculateStats(
            MemorySnapshot(2048L * mb, 1000L * mb),
            MemorySnapshot(2048L * mb, 1320L * mb)
        )
        val result = CleanResult(
            stats = stats,
            mode = EngineMode.DEEP_SHIZUKU,
            processesCleaned = 6,
            isOptimized = true
        )
        assertEquals(6, result.processesCleaned)
        assertEquals(320, result.stats.deltaAvailableMB)
        assertEquals(EngineMode.DEEP_SHIZUKU, result.mode)
    }
}
