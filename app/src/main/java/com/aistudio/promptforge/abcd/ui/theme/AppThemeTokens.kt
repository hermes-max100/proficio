package com.aistudio.promptforge.abcd.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Multiple selectable theme modes as a unified design system:
 * - [SYSTEM]: Follows Android system dynamic light/dark preference.
 * - [DARK]: Default workspace — "AI control room" with charcoal surfaces, electric blue/violet accent, teal signals.
 * - [LIGHT]: Accessibility, daylight use — "clean professional workspace" with soft off-white canvas, slate text.
 * - [NEON]: Builder mode — deep graphite with cyan, violet, and magenta highlights; subtle state glow.
 * - [CYBERPUNK]: Night operator mode — deep black/navy base, magenta and acid-green telemetry signals.
 */
enum class AppThemeMode(
    val title: String,
    val subtitle: String,
    val description: String
) {
    SYSTEM(
        title = "System Default",
        subtitle = "Device Dynamic",
        description = "Automatically follow Android system light or dark setting"
    ),
    DARK(
        title = "AutoFlow Dark",
        subtitle = "AI Control Room",
        description = "Charcoal surfaces, electric blue/violet accents, and teal automation cues"
    ),
    LIGHT(
        title = "AutoFlow Light",
        subtitle = "Clean Workspace",
        description = "Soft off-white canvas, slate text, and low visual noise for daylight focus"
    ),
    NEON(
        title = "AutoFlow Neon",
        subtitle = "Builder Mode",
        description = "Deep graphite with cyan, violet, and magenta highlights; state glow indicators"
    ),
    CYBERPUNK(
        title = "AutoFlow Cyberpunk",
        subtitle = "Night Operator",
        description = "Black/navy base, magenta and acid-green signals with high-contrast accents"
    )
}

/**
 * Semantic theme tokens providing unified values across screens without hardcoding.
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
    val isGlowEnabled: Boolean,
    val glowRunning: Color = accentSecondary,
    val glowGenerating: Color = accentPrimary,
    val glowSuccess: Color = success
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
    isGlowEnabled = false,
    glowRunning = Color(0xFF24D6B5),
    glowGenerating = Color(0xFF7C5CFF),
    glowSuccess = Color(0xFF24D6B5)
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
    isGlowEnabled = false,
    glowRunning = Color(0xFF008A73),
    glowGenerating = Color(0xFF5B3FD1),
    glowSuccess = Color(0xFF008A73)
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
    isGlowEnabled = true,
    glowRunning = Color(0xFF00E5FF),
    glowGenerating = Color(0xFFA855F7),
    glowSuccess = Color(0xFF00E5FF)
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
    isGlowEnabled = true,
    glowRunning = Color(0xFFB6FF00),
    glowGenerating = Color(0xFFFF2BD6),
    glowSuccess = Color(0xFFB6FF00)
)

val LocalAppThemeTokens = staticCompositionLocalOf { DarkThemeTokens }
val LocalThemeMode = compositionLocalOf { AppThemeMode.SYSTEM }
