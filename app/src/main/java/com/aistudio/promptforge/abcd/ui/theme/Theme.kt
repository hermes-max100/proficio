package com.aistudio.promptforge.abcd.ui.theme

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Maps semantic tokens to a full Material 3 ColorScheme ensuring all standard
 * M3 controls automatically render with the selected theme aesthetics.
 */
fun AppThemeTokens.toColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = accentPrimary,
            onPrimary = onPrimary,
            primaryContainer = accentPrimary.copy(alpha = 0.22f),
            onPrimaryContainer = accentPrimary,
            secondary = accentSecondary,
            onSecondary = onSecondary,
            secondaryContainer = accentSecondary.copy(alpha = 0.20f),
            onSecondaryContainer = accentSecondary,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceElevated,
            onSurfaceVariant = textSecondary,
            surfaceContainer = surfaceElevated,
            surfaceContainerHigh = surfaceElevated,
            surfaceContainerHighest = surfaceElevated,
            outline = border,
            outlineVariant = border.copy(alpha = 0.6f),
            error = error,
            onError = Color.White,
            errorContainer = error.copy(alpha = 0.2f),
            onErrorContainer = error
        )
    } else {
        lightColorScheme(
            primary = accentPrimary,
            onPrimary = onPrimary,
            primaryContainer = accentPrimary.copy(alpha = 0.15f),
            onPrimaryContainer = accentPrimary,
            secondary = accentSecondary,
            onSecondary = onSecondary,
            secondaryContainer = accentSecondary.copy(alpha = 0.15f),
            onSecondaryContainer = accentSecondary,
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
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
 * Builds scaled Typography respecting the user's custom fontScale preference.
 */
fun buildScaledTypography(fontScale: Float): Typography {
    val base = Typography()
    if (fontScale == 1.0f) return base

    fun scale(style: TextStyle): TextStyle = style.copy(
        fontSize = (style.fontSize.value * fontScale).sp,
        lineHeight = (style.lineHeight.value * fontScale).sp
    )

    return Typography(
        displayLarge = scale(base.displayLarge),
        displayMedium = scale(base.displayMedium),
        displaySmall = scale(base.displaySmall),
        headlineLarge = scale(base.headlineLarge),
        headlineMedium = scale(base.headlineMedium),
        headlineSmall = scale(base.headlineSmall),
        titleLarge = scale(base.titleLarge),
        titleMedium = scale(base.titleMedium),
        titleSmall = scale(base.titleSmall),
        bodyLarge = scale(base.bodyLarge),
        bodyMedium = scale(base.bodyMedium),
        bodySmall = scale(base.bodySmall),
        labelLarge = scale(base.labelLarge),
        labelMedium = scale(base.labelMedium),
        labelSmall = scale(base.labelSmall)
    )
}

/**
 * Root Perficio Theme Composable with full custom engine support.
 */
@Composable
fun PerficioTheme(
    themeMode: AppThemeMode = AppThemeMode.OBSIDIAN,
    customConfig: CustomThemeConfig? = null,
    glowEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val tokens = ThemeManager.resolveTokens(
        mode = themeMode,
        customConfig = customConfig,
        allowGlow = glowEnabled
    )

    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.CUSTOM -> customConfig?.isDark ?: true
        else -> true
    }

    val colorScheme = tokens.toColorScheme(isDark)
    val typography = buildScaledTypography(tokens.fontScale)

    CompositionLocalProvider(
        LocalAppThemeTokens provides tokens,
        LocalThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

/**
 * Backward compatibility alias for AutoFlowTheme.
 */
@Composable
fun AutoFlowTheme(
    themeMode: AppThemeMode = AppThemeMode.OBSIDIAN,
    customConfig: CustomThemeConfig? = null,
    glowEnabled: Boolean = true,
    isSystemDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    PerficioTheme(
        themeMode = themeMode,
        customConfig = customConfig,
        glowEnabled = glowEnabled,
        content = content
    )
}

/**
 * Background effects modifier applying solid, gradient, radial, or technical dot grid.
 */
fun Modifier.perficioBackground(tokens: AppThemeTokens): Modifier {
    return when (tokens.backgroundEffect) {
        BackgroundEffect.SOLID -> this.background(tokens.background)
        BackgroundEffect.SUBTLE_GRADIENT -> this.background(
            Brush.verticalGradient(
                colors = listOf(
                    tokens.background,
                    tokens.surface.copy(alpha = 0.85f),
                    tokens.background
                )
            )
        )
        BackgroundEffect.MESH_RADIAL -> this.drawBehind {
            drawRect(tokens.background)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        tokens.accentPrimary.copy(alpha = 0.15f * tokens.themeIntensity),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, 0f),
                    radius = size.width * 0.85f
                )
            )
        }
        BackgroundEffect.DOT_GRID -> this.drawBehind {
            drawRect(tokens.background)
            val dotSpacing = 28.dp.toPx()
            val dotRadius = 1.2.dp.toPx()
            val dotColor = tokens.border.copy(alpha = 0.35f)
            var x = dotSpacing / 2
            while (x < size.width) {
                var y = dotSpacing / 2
                while (y < size.height) {
                    drawCircle(dotColor, dotRadius, Offset(x, y))
                    y += dotSpacing
                }
                x += dotSpacing
            }
        }
    }
}

/**
 * Semantic modifier for subtle state-based glow (e.g. actively running workflow or AI generation).
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
            frameworkPaint.maskFilter = BlurMaskFilter(
                radius.toPx(),
                BlurMaskFilter.Blur.NORMAL
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
 * Semantic Card component utilizing LocalAppThemeTokens with surface style and corner radius.
 */
@Composable
fun SemanticCard(
    modifier: Modifier = Modifier,
    isElevated: Boolean = false,
    isRunning: Boolean = false,
    shape: RoundedCornerShape? = null,
    content: @Composable () -> Unit
) {
    val tokens = LocalAppThemeTokens.current
    val effectiveShape = shape ?: RoundedCornerShape(tokens.cornerRadius)

    val containerColor = when (tokens.surfaceStyle) {
        SurfaceStyle.FLAT -> if (isElevated) tokens.surfaceElevated else tokens.surface
        SurfaceStyle.ELEVATED -> if (isElevated) tokens.surfaceElevated else tokens.surface
        SurfaceStyle.GLASS -> tokens.surface.copy(alpha = 0.78f)
        SurfaceStyle.OUTLINE -> tokens.surface
    }

    val borderWidth = when {
        isRunning && tokens.isGlowEnabled -> 1.5.dp
        tokens.surfaceStyle == SurfaceStyle.OUTLINE -> 1.5.dp
        else -> 1.dp
    }

    val borderColor = when {
        isRunning -> tokens.accentSecondary
        tokens.surfaceStyle == SurfaceStyle.OUTLINE -> tokens.border.copy(alpha = 0.9f)
        else -> tokens.border
    }

    Box(
        modifier = modifier
            .then(
                if (isRunning && tokens.isGlowEnabled) {
                    Modifier.stateGlow(
                        tokens.glowRunning,
                        enabled = true,
                        radius = (10 * tokens.glowIntensity).dp,
                        shapeRadius = tokens.cornerRadius
                    )
                } else Modifier
            )
    ) {
        Card(
            shape = effectiveShape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = BorderStroke(width = borderWidth, color = borderColor)
        ) {
            content()
        }
    }
}
