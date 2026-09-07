package com.aistudio.promptforge.abcd.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Perficio Theme Engine selectable theme modes:
 * - [OBSIDIAN]: New default theme — dark, modern, subtle, with a restrained Perficio teal accent.
 * - [DARK]: Preserved preset — deep charcoal surfaces with electric indigo/violet and teal cues.
 * - [LIGHT]: Preserved preset — crisp daylight workspace, soft off-white canvas with slate text.
 * - [NEON]: Preserved preset — builder mode, graphite base with cyan and violet state glow.
 * - [CYBERPUNK]: Preserved preset — night operator mode, deep navy with vivid magenta and acid-green.
 * - [CUSTOM]: Full user-engineered theme with live builder, appearance sliders, and token centralization.
 * - [SYSTEM]: Legacy compatibility mapping to default theme.
 */
enum class AppThemeMode(
    val title: String,
    val subtitle: String,
    val description: String
) {
    OBSIDIAN(
        title = "Perficio Obsidian",
        subtitle = "Default Studio",
        description = "Dark, modern, subtle obsidian with restrained Perficio teal accents"
    ),
    DARK(
        title = "Perficio Dark",
        subtitle = "Control Room",
        description = "Deep charcoal surfaces, electric violet accents, and clean automation cues"
    ),
    LIGHT(
        title = "Perficio Light",
        subtitle = "Clean Workspace",
        description = "Soft off-white canvas, slate text, and low visual noise for daylight focus"
    ),
    NEON(
        title = "Perficio Neon",
        subtitle = "Builder Mode",
        description = "Deep graphite with cyan, violet, and magenta highlights; state glow indicators"
    ),
    CYBERPUNK(
        title = "Perficio Cyberpunk",
        subtitle = "Night Operator",
        description = "Black/navy base, magenta and acid-green signals with high-contrast accents"
    ),
    CUSTOM(
        title = "Custom Theme",
        subtitle = "User Engineered",
        description = "Full custom theme with customizable semantic colors, surface styles, and sliders"
    ),
    SYSTEM(
        title = "System Default",
        subtitle = "Device Dynamic",
        description = "Legacy mode defaulting to Perficio Obsidian"
    );

    companion object {
        val presets = listOf(OBSIDIAN, DARK, LIGHT, NEON, CYBERPUNK)
    }
}

/**
 * Semantic theme tokens providing unified values across screens without hardcoding.
 * Contrast-safe and augmented with appearance controls, typography scaling, and density.
 */
data class AppThemeTokens(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val border: Color,
    val focusRing: Color,
    val onPrimary: Color = Color.White,
    val onSecondary: Color = Color.White,
    val onSurface: Color = textPrimary,
    val onBackground: Color = textPrimary,
    val isGlowEnabled: Boolean = false,
    val glowRunning: Color = accentSecondary,
    val glowGenerating: Color = accentPrimary,
    val glowSuccess: Color = success,
    // Appearance & Layout Controls
    val cornerRadius: Dp = 12.dp,
    val surfaceStyle: SurfaceStyle = SurfaceStyle.ELEVATED,
    val glowIntensity: Float = 0.5f,
    val backgroundEffect: BackgroundEffect = BackgroundEffect.SUBTLE_GRADIENT,
    val themeIntensity: Float = 0.75f, // 0.0f Clean to 1.0f Immersive
    val fontScale: Float = 1.0f,
    val density: ThemeDensity = ThemeDensity.COMFORTABLE
)

/**
 * Perficio Obsidian: Dark, modern, subtle, with a restrained Perficio teal accent.
 */
val ObsidianThemeTokens = AppThemeTokens(
    background = Color(0xFF090B10),
    surface = Color(0xFF11151F),
    surfaceElevated = Color(0xFF171D2B),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    accentPrimary = Color(0xFF00B4A0), // Restrained Perficio Teal
    accentSecondary = Color(0xFF38BDF8),
    success = Color(0xFF10B981),
    warning = Color(0xFFF59E0B),
    error = Color(0xFFEF4444),
    border = Color(0xFF1F293D),
    focusRing = Color(0xFF00B4A0),
    onPrimary = Color.White,
    onSecondary = Color(0xFF090B10),
    onSurface = Color(0xFFF1F5F9),
    onBackground = Color(0xFFF1F5F9),
    isGlowEnabled = true,
    glowIntensity = 0.4f,
    glowRunning = Color(0xFF00B4A0),
    glowGenerating = Color(0xFF38BDF8),
    glowSuccess = Color(0xFF10B981),
    cornerRadius = 12.dp,
    surfaceStyle = SurfaceStyle.ELEVATED,
    backgroundEffect = BackgroundEffect.SUBTLE_GRADIENT,
    themeIntensity = 0.7f,
    fontScale = 1.0f,
    density = ThemeDensity.COMFORTABLE
)

val DarkThemeTokens = AppThemeTokens(
    background = Color(0xFF0B0D12),
    surface = Color(0xFF151925),
    surfaceElevated = Color(0xFF1E2435),
    textPrimary = Color(0xFFF3F5FF),
    textSecondary = Color(0xFFA7AEC2),
    accentPrimary = Color(0xFF7C5CFF),
    accentSecondary = Color(0xFF24D6B5),
    success = Color(0xFF24D6B5),
    warning = Color(0xFFFFB300),
    error = Color(0xFFFF6B6B),
    border = Color(0xFF283049),
    focusRing = Color(0xFF7C5CFF),
    onPrimary = Color.White,
    onSecondary = Color(0xFF0B0D12),
    onSurface = Color(0xFFF3F5FF),
    onBackground = Color(0xFFF3F5FF),
    isGlowEnabled = false,
    glowIntensity = 0.3f,
    glowRunning = Color(0xFF24D6B5),
    glowGenerating = Color(0xFF7C5CFF),
    glowSuccess = Color(0xFF24D6B5),
    cornerRadius = 12.dp,
    surfaceStyle = SurfaceStyle.ELEVATED,
    backgroundEffect = BackgroundEffect.SUBTLE_GRADIENT,
    themeIntensity = 0.7f
)

val LightThemeTokens = AppThemeTokens(
    background = Color(0xFFF7F8FC),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFEFF1F8),
    textPrimary = Color(0xFF151722),
    textSecondary = Color(0xFF5E667A),
    accentPrimary = Color(0xFF5B3FD1),
    accentSecondary = Color(0xFF008A73),
    success = Color(0xFF008A73),
    warning = Color(0xFFD97706),
    error = Color(0xFFC62828),
    border = Color(0xFFDDE1EB),
    focusRing = Color(0xFF5B3FD1),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onSurface = Color(0xFF151722),
    onBackground = Color(0xFF151722),
    isGlowEnabled = false,
    glowIntensity = 0.0f,
    glowRunning = Color(0xFF008A73),
    glowGenerating = Color(0xFF5B3FD1),
    glowSuccess = Color(0xFF008A73),
    cornerRadius = 10.dp,
    surfaceStyle = SurfaceStyle.FLAT,
    backgroundEffect = BackgroundEffect.SOLID,
    themeIntensity = 0.4f
)

val NeonThemeTokens = AppThemeTokens(
    background = Color(0xFF070812),
    surface = Color(0xFF111327),
    surfaceElevated = Color(0xFF1A1D3B),
    textPrimary = Color(0xFFEAFBFF),
    textSecondary = Color(0xFFAFC9DF),
    accentPrimary = Color(0xFF00E5FF),
    accentSecondary = Color(0xFFA855F7),
    success = Color(0xFF00E5FF),
    warning = Color(0xFFFFC000),
    error = Color(0xFFFF4D8D),
    border = Color(0xFF2A3F6D),
    focusRing = Color(0xFF00E5FF),
    onPrimary = Color(0xFF070812),
    onSecondary = Color.White,
    onSurface = Color(0xFFEAFBFF),
    onBackground = Color(0xFFEAFBFF),
    isGlowEnabled = true,
    glowIntensity = 0.85f,
    glowRunning = Color(0xFF00E5FF),
    glowGenerating = Color(0xFFA855F7),
    glowSuccess = Color(0xFF00E5FF),
    cornerRadius = 14.dp,
    surfaceStyle = SurfaceStyle.GLASS,
    backgroundEffect = BackgroundEffect.MESH_RADIAL,
    themeIntensity = 0.95f
)

val CyberpunkThemeTokens = AppThemeTokens(
    background = Color(0xFF05030B),
    surface = Color(0xFF120A20),
    surfaceElevated = Color(0xFF1E1035),
    textPrimary = Color(0xFFFFF1FC),
    textSecondary = Color(0xFFBFAFC8),
    accentPrimary = Color(0xFFFF2BD6),
    accentSecondary = Color(0xFFB6FF00),
    success = Color(0xFFB6FF00),
    warning = Color(0xFFFFB300),
    error = Color(0xFFFF4F81),
    border = Color(0xFF56265B),
    focusRing = Color(0xFFFF2BD6),
    onPrimary = Color.White,
    onSecondary = Color(0xFF05030B),
    onSurface = Color(0xFFFFF1FC),
    onBackground = Color(0xFFFFF1FC),
    isGlowEnabled = true,
    glowIntensity = 0.9f,
    glowRunning = Color(0xFFB6FF00),
    glowGenerating = Color(0xFFFF2BD6),
    glowSuccess = Color(0xFFB6FF00),
    cornerRadius = 8.dp,
    surfaceStyle = SurfaceStyle.OUTLINE,
    backgroundEffect = BackgroundEffect.DOT_GRID,
    themeIntensity = 1.0f
)

val LocalAppThemeTokens = staticCompositionLocalOf { ObsidianThemeTokens }
val LocalThemeMode = compositionLocalOf { AppThemeMode.OBSIDIAN }
