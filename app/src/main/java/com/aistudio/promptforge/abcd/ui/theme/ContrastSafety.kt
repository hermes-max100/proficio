package com.aistudio.promptforge.abcd.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Contrast Safety Engine ensuring text and interactive elements are always legible
 * according to W3C Web Content Accessibility Guidelines (WCAG 2.1).
 */
object ContrastSafety {

    private const val WCAG_AA_MIN_RATIO = 4.5f
    private const val WCAG_LARGE_MIN_RATIO = 3.0f

    /**
     * Computes the relative luminance of a color according to W3C WCAG 2.1 specification.
     */
    fun calculateLuminance(color: Color): Float {
        val r = sRgbToLinear(color.red)
        val g = sRgbToLinear(color.green)
        val b = sRgbToLinear(color.blue)
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    private fun sRgbToLinear(channel: Float): Float {
        return if (channel <= 0.04045f) {
            channel / 12.92f
        } else {
            ((channel + 0.055f) / 1.055f).pow(2.4f)
        }
    }

    /**
     * Calculates the contrast ratio between two colors (ranging from 1.0 to 21.0).
     */
    fun calculateContrastRatio(foreground: Color, background: Color): Float {
        val l1 = calculateLuminance(foreground)
        val l2 = calculateLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    /**
     * Checks if two colors meet the WCAG AA contrast threshold.
     */
    fun isAccessible(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val requiredRatio = if (isLargeText) WCAG_LARGE_MIN_RATIO else WCAG_AA_MIN_RATIO
        return calculateContrastRatio(foreground, background) >= requiredRatio
    }

    /**
     * Ensures readable text by testing the given [textColor] against [backgroundColor].
     * If the contrast ratio is lower than [minRatio], automatically adjusts or flips
     * to a high-contrast tint (off-white for dark backgrounds, deep charcoal for light backgrounds)
     * so text is never washed out or unreadable.
     */
    fun ensureContrast(
        textColor: Color,
        backgroundColor: Color,
        minRatio: Float = WCAG_AA_MIN_RATIO
    ): Color {
        val currentRatio = calculateContrastRatio(textColor, backgroundColor)
        if (currentRatio >= minRatio) {
            return textColor
        }

        val bgLuminance = calculateLuminance(backgroundColor)
        val isBackgroundDark = bgLuminance < 0.35f

        return if (isBackgroundDark) {
            // Background is dark: lighten text towards pure readable white/off-white
            val brightFallback = Color(0xFFF8FAFC)
            if (calculateContrastRatio(brightFallback, backgroundColor) >= minRatio) {
                brightFallback
            } else {
                Color.White
            }
        } else {
            // Background is light: darken text towards deep crisp slate
            val darkFallback = Color(0xFF0F172A)
            if (calculateContrastRatio(darkFallback, backgroundColor) >= minRatio) {
                darkFallback
            } else {
                Color.Black
            }
        }
    }

    /**
     * Formats the contrast ratio as a human-readable string (e.g. "8.4:1").
     */
    fun formatRatio(ratio: Float): String {
        return String.format("%.1f:1", ratio)
    }
}
