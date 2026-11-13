package com.ysfzone.goclean

import com.ysfzone.goclean.engine.MemoryManager
import com.ysfzone.goclean.engine.MemorySnapshot
import com.ysfzone.goclean.updater.UpdateChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreLogicTest {

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
        assertTrue(UpdateChecker.isNewerVersion("v1.0.1", "1.0.0"))
        assertTrue(UpdateChecker.isNewerVersion("v1.1.0", "1.0.9"))
        assertTrue(UpdateChecker.isNewerVersion("2.0.0", "1.9.9"))
        assertFalse(UpdateChecker.isNewerVersion("v1.0.0", "1.0.0"))
        assertFalse(UpdateChecker.isNewerVersion("v0.9.9", "1.0.0"))
    }
}

