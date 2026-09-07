package com.aistudio.promptforge.abcd.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Maps semantic tokens to a full Material 3 ColorScheme ensuring all standard
 * M3 controls (Cards, Buttons, TextFields, TopAppBar, NavigationBar) automatically
 * render with the selected theme aesthetics.
 */
fun AppThemeTokens.toColorScheme(isDark: Boolean): ColorScheme {
    val onPrimaryColor = if (isDark) {
        if (accentPrimary == Color(0xFF00E5FF)) Color(0xFF070812) else Color.White
    } else {
        Color.White
    }

    val onSecondaryColor = if (accentSecondary == Color(0xFFB6FF00) || accentSecondary == Color(0xFF24D6B5)) {
        Color(0xFF05030B)
    } else {
        Color.White
    }

    return if (isDark) {
        darkColorScheme(
            primary = accentPrimary,
            onPrimary = onPrimaryColor,
            primaryContainer = accentPrimary.copy(alpha = 0.22f),
            onPrimaryContainer = accentPrimary,
            secondary = accentSecondary,
            onSecondary = onSecondaryColor,
            secondaryContainer = accentSecondary.copy(alpha = 0.20f),
            onSecondaryContainer = accentSecondary,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = surfaceElevated,
            onSurfaceVariant = textSecondary,
            surfaceContainer = surfaceElevated,
            surfaceContainerHigh = surfaceElevated,
            surfaceContainerHighest = surfaceElevated,
            outline = border,
            outlineVariant = border.copy(alpha = 0.5f),
            error = error,
            onError = Color.White,
            errorContainer = error.copy(alpha = 0.2f),
            onErrorContainer = error
        )
    } else {
        lightColorScheme(
            primary = accentPrimary,
            onPrimary = onPrimaryColor,
            primaryContainer = accentPrimary.copy(alpha = 0.15f),
            onPrimaryContainer = accentPrimary,
            secondary = accentSecondary,
            onSecondary = onSecondaryColor,
            secondaryContainer = accentSecondary.copy(alpha = 0.15f),
            onSecondaryContainer = accentSecondary,
            background = background,
            onBackground = textPrimary,
            surface = surface,
            onSurface = textPrimary,
            surfaceVariant = surfaceElevated,
            onSurfaceVariant = textSecondary,
            surfaceContainer = surfaceElevated,
            surfaceContainerHigh = surfaceElevated,
            surfaceContainerHighest = surfaceElevated,
            outline = border,
            outlineVariant = border.copy(alpha = 0.5f),
            error = error,
            onError = Color.White,
            errorContainer = error.copy(alpha = 0.12f),
            onErrorContainer = error
        )
    }
}

/**
 * Root theme composable supporting Dark, Light, Neon, Cyberpunk, and System modes.
 */
@Composable
fun AutoFlowTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    glowEnabled: Boolean = true,
    isSystemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val tokens = ThemeManager.resolveTokens(themeMode, isSystemDark, glowEnabled)
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK, AppThemeMode.NEON, AppThemeMode.CYBERPUNK -> true
    }
    val colorScheme = tokens.toColorScheme(isDark)

    CompositionLocalProvider(
        LocalAppThemeTokens provides tokens,
        LocalThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
            content = content
        )
    }
}

/**
 * Semantic modifier for subtle state-based glow (e.g. actively running workflow or AI generation).
 * Glow indicates state, not decoration. Disabled when tokens.isGlowEnabled is false or user toggles off.
 */
fun Modifier.stateGlow(
    color: Color,
    enabled: Boolean = true,
    radius: Dp = 10.dp,
    shapeRadius: Dp = 12.dp
): Modifier = if (enabled) {
    this.drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = color.copy(alpha = 0.35f).toArgb()
            frameworkPaint.maskFilter = android.graphics.BlurMaskFilter(
                radius.toPx(),
                android.graphics.BlurMaskFilter.Blur.NORMAL
            )
            canvas.drawRoundRect(
                left = 0f,
                top = 0f,
                right = size.width,
                bottom = size.height,
                radiusX = shapeRadius.toPx(),
                radiusY = shapeRadius.toPx(),
                paint = paint
            )
        }
    }
} else {
    this
}

/**
 * Semantic Card component utilizing LocalAppThemeTokens.
 */
@Composable
fun SemanticCard(
    modifier: Modifier = Modifier,
    isElevated: Boolean = false,
    isRunning: Boolean = false,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    val tokens = LocalAppThemeTokens.current
    val containerColor = if (isElevated) tokens.surfaceElevated else tokens.surface
    val glowColor = tokens.glowRunning

    Box(
        modifier = modifier
            .then(
                if (isRunning && tokens.isGlowEnabled) {
                    Modifier.stateGlow(glowColor, enabled = true, shapeRadius = 12.dp)
                } else Modifier
            )
    ) {
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(
                width = if (isRunning && tokens.isGlowEnabled) 1.5.dp else 1.dp,
                color = if (isRunning) tokens.accentSecondary else tokens.border
            )
        ) {
            content()
        }
    }
}
