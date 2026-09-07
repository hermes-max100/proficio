package com.aistudio.promptforge.abcd

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aistudio.promptforge.abcd.ui.theme.AppThemeMode
import com.aistudio.promptforge.abcd.ui.theme.BackgroundEffect
import com.aistudio.promptforge.abcd.ui.theme.ContrastSafety
import com.aistudio.promptforge.abcd.ui.theme.CustomThemeConfig
import com.aistudio.promptforge.abcd.ui.theme.CyberpunkThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.DarkThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.LightThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.NeonThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.ObsidianThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.SurfaceStyle
import com.aistudio.promptforge.abcd.ui.theme.ThemeDensity
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ThemeSystemUnitTest {

    @Test
    fun testAllThemeModesEnumValues() {
        val modes = AppThemeMode.entries
        assertTrue(modes.contains(AppThemeMode.OBSIDIAN))
        assertTrue(modes.contains(AppThemeMode.DARK))
        assertTrue(modes.contains(AppThemeMode.LIGHT))
        assertTrue(modes.contains(AppThemeMode.NEON))
        assertTrue(modes.contains(AppThemeMode.CYBERPUNK))
        assertTrue(modes.contains(AppThemeMode.CUSTOM))
        assertTrue(modes.contains(AppThemeMode.SYSTEM))
    }

    @Test
    fun testThemeModeEnumParsing() {
        assertEquals(AppThemeMode.OBSIDIAN, AppThemeMode.valueOf("OBSIDIAN"))
        assertEquals(AppThemeMode.DARK, AppThemeMode.valueOf("DARK"))
        assertEquals(AppThemeMode.LIGHT, AppThemeMode.valueOf("LIGHT"))
        assertEquals(AppThemeMode.NEON, AppThemeMode.valueOf("NEON"))
        assertEquals(AppThemeMode.CYBERPUNK, AppThemeMode.valueOf("CYBERPUNK"))
        assertEquals(AppThemeMode.CUSTOM, AppThemeMode.valueOf("CUSTOM"))
    }

    @Test
    fun testPerficioObsidianDefaultThemeTokens() {
        val obsidianTokens = ObsidianThemeTokens
        assertEquals(Color(0xFF090B10), obsidianTokens.background)
        assertEquals(Color(0xFF11151F), obsidianTokens.surface)
        assertEquals(Color(0xFF00B4A0), obsidianTokens.accentPrimary) // Restrained Perficio Teal
        assertTrue(obsidianTokens.isGlowEnabled)
        assertEquals(12.dp, obsidianTokens.cornerRadius)
        assertEquals(SurfaceStyle.ELEVATED, obsidianTokens.surfaceStyle)
        assertEquals(BackgroundEffect.SUBTLE_GRADIENT, obsidianTokens.backgroundEffect)
    }

    @Test
    fun testResolveTokensForExplicitModes() {
        val obsidianResolved = ThemeManager.resolveTokens(AppThemeMode.OBSIDIAN)
        assertEquals(ObsidianThemeTokens.background, obsidianResolved.background)
        assertEquals(ObsidianThemeTokens.accentPrimary, obsidianResolved.accentPrimary)

        val darkTokens = ThemeManager.resolveTokens(AppThemeMode.DARK)
        assertFalse(darkTokens.isGlowEnabled)
        assertEquals(DarkThemeTokens.background, darkTokens.background)
        assertEquals(DarkThemeTokens.accentPrimary, darkTokens.accentPrimary)

        val lightTokens = ThemeManager.resolveTokens(AppThemeMode.LIGHT)
        assertFalse(lightTokens.isGlowEnabled)
        assertEquals(LightThemeTokens.background, lightTokens.background)
        assertEquals(LightThemeTokens.accentPrimary, lightTokens.accentPrimary)

        val neonTokens = ThemeManager.resolveTokens(AppThemeMode.NEON)
        assertTrue(neonTokens.isGlowEnabled)
        assertEquals(NeonThemeTokens.background, neonTokens.background)
        assertEquals(Color(0xFF00E5FF), neonTokens.accentPrimary)

        val cyberpunkTokens = ThemeManager.resolveTokens(AppThemeMode.CYBERPUNK)
        assertTrue(cyberpunkTokens.isGlowEnabled)
        assertEquals(CyberpunkThemeTokens.background, cyberpunkTokens.background)
        assertEquals(Color(0xFFFF2BD6), cyberpunkTokens.accentPrimary)
    }

    @Test
    fun testCustomThemeResolutionAndConversion() {
        val customConfig = CustomThemeConfig(
            id = "custom_test_1",
            name = "Test Studio Theme",
            backgroundColorHex = 0xFF1A1A24,
            surfaceColorHex = 0xFF242436,
            surfaceElevatedColorHex = 0xFF2E2E44,
            primaryColorHex = 0xFF9333EA,
            secondaryColorHex = 0xFF06B6D4,
            successColorHex = 0xFF22C55E,
            warningColorHex = 0xFFEAB308,
            errorColorHex = 0xFFEF4444,
            borderColorHex = 0xFF3B3B54,
            surfaceStyle = SurfaceStyle.GLASS,
            cornerRadiusDp = 18,
            glowIntensity = 0.8f,
            backgroundEffect = BackgroundEffect.MESH_RADIAL,
            themeIntensity = 0.9f,
            fontScale = 1.15f,
            density = ThemeDensity.SPACIOUS
        )

        val tokens = customConfig.toTokens()
        assertEquals(Color(0xFF1A1A24), tokens.background)
        assertEquals(Color(0xFF242436), tokens.surface)
        assertEquals(Color(0xFF9333EA), tokens.accentPrimary)
        assertEquals(18.dp, tokens.cornerRadius)
        assertEquals(SurfaceStyle.GLASS, tokens.surfaceStyle)
        assertEquals(BackgroundEffect.MESH_RADIAL, tokens.backgroundEffect)
        assertEquals(0.9f, tokens.themeIntensity, 0.001f)
        assertEquals(1.15f, tokens.fontScale, 0.001f)
        assertEquals(ThemeDensity.SPACIOUS, tokens.density)

        val resolvedFromManager = ThemeManager.resolveTokens(AppThemeMode.CUSTOM, customConfig)
        assertEquals(tokens.background, resolvedFromManager.background)
        assertEquals(tokens.accentPrimary, resolvedFromManager.accentPrimary)
    }

    @Test
    fun testCustomThemeJsonSerializationRoundtrip() {
        val original = CustomThemeConfig(
            id = UUID.randomUUID().toString(),
            name = "Roundtrip Theme",
            backgroundColorHex = 0xFF080B10,
            primaryColorHex = 0xFF00B4A0,
            cornerRadiusDp = 16,
            glowIntensity = 0.65f,
            surfaceStyle = SurfaceStyle.OUTLINE,
            backgroundEffect = BackgroundEffect.DOT_GRID,
            themeIntensity = 0.85f,
            fontScale = 1.0f,
            density = ThemeDensity.COMPACT,
            isDark = true
        )

        val json = original.toJsonString()
        val restored = CustomThemeConfig.fromJsonString(json)

        assertEquals(original.id, restored.id)
        assertEquals(original.name, restored.name)
        assertEquals(original.backgroundColorHex, restored.backgroundColorHex)
        assertEquals(original.primaryColorHex, restored.primaryColorHex)
        assertEquals(original.cornerRadiusDp, restored.cornerRadiusDp)
        assertEquals(original.glowIntensity, restored.glowIntensity, 0.01f)
        assertEquals(original.surfaceStyle, restored.surfaceStyle)
        assertEquals(original.backgroundEffect, restored.backgroundEffect)
        assertEquals(original.themeIntensity, restored.themeIntensity, 0.01f)
        assertEquals(original.density, restored.density)
        assertEquals(original.isDark, restored.isDark)
    }

    @Test
    fun testCustomThemeListSerialization() {
        val list = listOf(
            CustomThemeConfig(id = "1", name = "Theme A"),
            CustomThemeConfig(id = "2", name = "Theme B")
        )
        val jsonStr = CustomThemeConfig.listToJson(list)
        val deserialized = CustomThemeConfig.listFromJson(jsonStr)

        assertEquals(2, deserialized.size)
        assertEquals("Theme A", deserialized[0].name)
        assertEquals("Theme B", deserialized[1].name)
    }

    @Test
    fun testContrastSafetyCalculations() {
        val black = Color.Black
        val white = Color.White
        val ratio = ContrastSafety.calculateContrastRatio(white, black)
        assertEquals(21.0f, ratio, 0.1f)
        assertTrue(ContrastSafety.isAccessible(white, black))

        // Same color should have 1:1 contrast ratio
        val sameColorRatio = ContrastSafety.calculateContrastRatio(white, white)
        assertEquals(1.0f, sameColorRatio, 0.01f)
        assertFalse(ContrastSafety.isAccessible(white, white))

        // Automatic contrast adjustment should rescue washed-out text
        val safeTextOnWhite = ContrastSafety.ensureContrast(
            textColor = Color(0xFFF0F0F0), // Almost white on white: fails contrast
            backgroundColor = Color.White,
            minRatio = 4.5f
        )
        assertTrue(ContrastSafety.calculateContrastRatio(safeTextOnWhite, Color.White) >= 4.5f)

        val safeTextOnBlack = ContrastSafety.ensureContrast(
            textColor = Color(0xFF101010), // Almost black on black: fails contrast
            backgroundColor = Color.Black,
            minRatio = 4.5f
        )
        assertTrue(ContrastSafety.calculateContrastRatio(safeTextOnBlack, Color.Black) >= 4.5f)
    }

    @Test
    fun testGlowEffectsCanBeDisabled() {
        val neonWithGlow = ThemeManager.resolveTokens(AppThemeMode.NEON, allowGlow = true)
        assertTrue(neonWithGlow.isGlowEnabled)

        val neonWithoutGlow = ThemeManager.resolveTokens(AppThemeMode.NEON, allowGlow = false)
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
