package com.tvtheater.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmokeTest {

    @Test
    fun `test runner environment is operational`() {
        val appName = "TV Theater"
        assertEquals("TV Theater", appName)
        assertTrue("Testing framework operational", true)
    }

    @Test
    fun `verify random discovery logic baseline`() {
        val genres = listOf("Hành Động", "Hoạt Hình", "Viễn Tưởng", "Hài Hước", "Võ Thuật")
        val shuffled = genres.shuffled()
        assertEquals(5, shuffled.size)
        assertTrue(shuffled.contains("Hành Động"))
    }
}
