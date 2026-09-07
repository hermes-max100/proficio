package com.aistudio.promptforge.abcd

import androidx.compose.ui.graphics.Color
import com.aistudio.promptforge.abcd.ui.theme.AppThemeMode
import com.aistudio.promptforge.abcd.ui.theme.CyberpunkThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.DarkThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.LightThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.NeonThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeSystemUnitTest {

    @Test
    fun testAllThemeModesEnumValues() {
        val modes = AppThemeMode.entries
        assertEquals(5, modes.size)
        assertTrue(modes.contains(AppThemeMode.SYSTEM))
        assertTrue(modes.contains(AppThemeMode.DARK))
        assertTrue(modes.contains(AppThemeMode.LIGHT))
        assertTrue(modes.contains(AppThemeMode.NEON))
        assertTrue(modes.contains(AppThemeMode.CYBERPUNK))
    }

    @Test
    fun testThemeModeEnumParsing() {
        assertEquals(AppThemeMode.SYSTEM, AppThemeMode.valueOf("SYSTEM"))
        assertEquals(AppThemeMode.DARK, AppThemeMode.valueOf("DARK"))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.valueOf("LIGHT"))
        assertEquals(AppThemeMode.NEON, AppThemeMode.valueOf("NEON"))
        assertEquals(AppThemeMode.CYBERPUNK, AppThemeMode.valueOf("CYBERPUNK"))
    }

    @Test
    fun testResolveTokensForExplicitModes() {
        val darkTokens = ThemeManager.resolveTokens(AppThemeMode.DARK, isSystemDark = true)
        assertFalse(darkTokens.isGlowEnabled)
        assertEquals(DarkThemeTokens.background, darkTokens.background)
        assertEquals(DarkThemeTokens.accentPrimary, darkTokens.accentPrimary)

        val lightTokens = ThemeManager.resolveTokens(AppThemeMode.LIGHT, isSystemDark = false)
        assertFalse(lightTokens.isGlowEnabled)
        assertEquals(LightThemeTokens.background, lightTokens.background)
        assertEquals(LightThemeTokens.accentPrimary, lightTokens.accentPrimary)

        val neonTokens = ThemeManager.resolveTokens(AppThemeMode.NEON, isSystemDark = true)
        assertTrue(neonTokens.isGlowEnabled)
        assertEquals(NeonThemeTokens.background, neonTokens.background)
        assertEquals(Color(0xFF00E5FF), neonTokens.accentPrimary)

        val cyberpunkTokens = ThemeManager.resolveTokens(AppThemeMode.CYBERPUNK, isSystemDark = true)
        assertTrue(cyberpunkTokens.isGlowEnabled)
        assertEquals(CyberpunkThemeTokens.background, cyberpunkTokens.background)
        assertEquals(Color(0xFFFF2BD6), cyberpunkTokens.accentPrimary)
    }

    @Test
    fun testSystemThemeModeAdaptsToSystemDarkSetting() {
        val systemDark = ThemeManager.resolveTokens(AppThemeMode.SYSTEM, isSystemDark = true)
        assertEquals(DarkThemeTokens.background, systemDark.background)

        val systemLight = ThemeManager.resolveTokens(AppThemeMode.SYSTEM, isSystemDark = false)
        assertEquals(LightThemeTokens.background, systemLight.background)
    }

    @Test
    fun testGlowEffectsCanBeDisabled() {
        val neonWithGlow = ThemeManager.resolveTokens(AppThemeMode.NEON, isSystemDark = true, allowGlow = true)
        assertTrue(neonWithGlow.isGlowEnabled)

        val neonWithoutGlow = ThemeManager.resolveTokens(AppThemeMode.NEON, isSystemDark = true, allowGlow = false)
        assertFalse(neonWithoutGlow.isGlowEnabled)
    }

    @Test
    fun testStateGlowColorsAreDistinct() {
        val darkTokens = DarkThemeTokens
        assertNotEquals(darkTokens.glowRunning, darkTokens.glowGenerating)

        val cyberpunkTokens = CyberpunkThemeTokens
        assertNotEquals(cyberpunkTokens.glowRunning, cyberpunkTokens.glowGenerating)
        assertEquals(cyberpunkTokens.accentSecondary, cyberpunkTokens.glowRunning)
        assertEquals(cyberpunkTokens.accentPrimary, cyberpunkTokens.glowGenerating)
    }

    @Test
    fun testAllThemesHaveDescriptiveTitlesAndSubtitles() {
        for (mode in AppThemeMode.entries) {
            assertTrue(mode.title.isNotBlank())
            assertTrue(mode.subtitle.isNotBlank())
            assertTrue(mode.description.isNotBlank())
        }
    }
}
