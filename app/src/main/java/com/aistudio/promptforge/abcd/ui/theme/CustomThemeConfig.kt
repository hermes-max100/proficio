package com.aistudio.promptforge.abcd.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.UUID

enum class SurfaceStyle(val label: String, val description: String) {
    FLAT("Flat", "Clean minimal single-plane surfaces"),
    ELEVATED("Elevated", "Layered depth with tonal elevation"),
    GLASS("Glassmorphic", "Frosted glass feel with translucent depth"),
    OUTLINE("Outline", "High-contrast technical borders")
}

enum class BackgroundEffect(val label: String, val description: String) {
    SOLID("Solid Canvas", "Pure consistent background color"),
    SUBTLE_GRADIENT("Subtle Gradient", "Smooth directional lighting gradient"),
    MESH_RADIAL("Radial Vignette", "Soft atmospheric radial glow"),
    DOT_GRID("Technical Grid", "Engineered dot-matrix background")
}

enum class ThemeDensity(val label: String, val itemSpacingDp: Int, val cardPaddingDp: Int) {
    COMPACT("Compact", 8, 10),
    COMFORTABLE("Comfortable", 12, 14),
    SPACIOUS("Spacious", 16, 18)
}

/**
 * Full configuration model for user-engineered custom themes in Perficio.
 */
data class CustomThemeConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Custom Theme",
    // Semantic Colors (Stored as 32-bit ARGB Longs)
    val backgroundColorHex: Long = 0xFF090B10,
    val surfaceColorHex: Long = 0xFF11151F,
    val surfaceElevatedColorHex: Long = 0xFF171D2B,
    val primaryColorHex: Long = 0xFF00B4A0,      // Perficio Teal
    val secondaryColorHex: Long = 0xFF38BDF8,    // Ice Slate
    val successColorHex: Long = 0xFF10B981,
    val warningColorHex: Long = 0xFFF59E0B,
    val errorColorHex: Long = 0xFFEF4444,
    val borderColorHex: Long = 0xFF1F293D,
    val textPrimaryColorHex: Long = 0xFFF1F5F9,
    val textSecondaryColorHex: Long = 0xFF94A3B8,
    // Appearance Controls
    val surfaceStyle: SurfaceStyle = SurfaceStyle.ELEVATED,
    val cornerRadiusDp: Int = 12,
    val glowIntensity: Float = 0.5f,
    val backgroundEffect: BackgroundEffect = BackgroundEffect.SUBTLE_GRADIENT,
    val themeIntensity: Float = 0.75f, // 0.0f = Clean, 1.0f = Immersive
    // Typography & Density
    val fontScale: Float = 1.0f,
    val density: ThemeDensity = ThemeDensity.COMFORTABLE,
    val isDark: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {

    val backgroundColor: Color get() = Color(backgroundColorHex)
    val surfaceColor: Color get() = Color(surfaceColorHex)
    val surfaceElevatedColor: Color get() = Color(surfaceElevatedColorHex)
    val primaryColor: Color get() = Color(primaryColorHex)
    val secondaryColor: Color get() = Color(secondaryColorHex)
    val successColor: Color get() = Color(successColorHex)
    val warningColor: Color get() = Color(warningColorHex)
    val errorColor: Color get() = Color(errorColorHex)
    val borderColor: Color get() = Color(borderColorHex)

    /**
     * Resolves complete AppThemeTokens with automatic WCAG AA contrast safety.
     */
    fun toTokens(): AppThemeTokens {
        val safeTextPrimary = ContrastSafety.ensureContrast(
            textColor = Color(textPrimaryColorHex),
            backgroundColor = surfaceColor
        )
        val safeTextSecondary = ContrastSafety.ensureContrast(
            textColor = Color(textSecondaryColorHex),
            backgroundColor = surfaceColor,
            minRatio = 3.0f
        )
        val safeOnPrimary = ContrastSafety.ensureContrast(
            textColor = if (isDark) Color.White else Color.Black,
            backgroundColor = primaryColor,
            minRatio = 3.5f
        )
        val safeOnSecondary = ContrastSafety.ensureContrast(
            textColor = if (isDark) Color.White else Color.Black,
            backgroundColor = secondaryColor,
            minRatio = 3.5f
        )
        val safeOnSurface = ContrastSafety.ensureContrast(
            textColor = Color(textPrimaryColorHex),
            backgroundColor = surfaceColor
        )
        val safeOnBackground = ContrastSafety.ensureContrast(
            textColor = Color(textPrimaryColorHex),
            backgroundColor = backgroundColor
        )

        return AppThemeTokens(
            background = backgroundColor,
            surface = surfaceColor,
            surfaceElevated = surfaceElevatedColor,
            textPrimary = safeTextPrimary,
            textSecondary = safeTextSecondary,
            accentPrimary = primaryColor,
            accentSecondary = secondaryColor,
            success = successColor,
            warning = warningColor,
            error = errorColor,
            border = borderColor,
            focusRing = primaryColor,
            onPrimary = safeOnPrimary,
            onSecondary = safeOnSecondary,
            onSurface = safeOnSurface,
            onBackground = safeOnBackground,
            isGlowEnabled = glowIntensity > 0.05f,
            glowRunning = secondaryColor,
            glowGenerating = primaryColor,
            glowSuccess = successColor,
            cornerRadius = cornerRadiusDp.dp,
            surfaceStyle = surfaceStyle,
            glowIntensity = glowIntensity,
            backgroundEffect = backgroundEffect,
            themeIntensity = themeIntensity,
            fontScale = fontScale,
            density = density
        )
    }

    fun toJsonString(): String {
        return buildString {
            append("{")
            append("\"id\":\"").append(escapeJson(id)).append("\",")
            append("\"name\":\"").append(escapeJson(name)).append("\",")
            append("\"backgroundColorHex\":").append(backgroundColorHex).append(",")
            append("\"surfaceColorHex\":").append(surfaceColorHex).append(",")
            append("\"surfaceElevatedColorHex\":").append(surfaceElevatedColorHex).append(",")
            append("\"primaryColorHex\":").append(primaryColorHex).append(",")
            append("\"secondaryColorHex\":").append(secondaryColorHex).append(",")
            append("\"successColorHex\":").append(successColorHex).append(",")
            append("\"warningColorHex\":").append(warningColorHex).append(",")
            append("\"errorColorHex\":").append(errorColorHex).append(",")
            append("\"borderColorHex\":").append(borderColorHex).append(",")
            append("\"textPrimaryColorHex\":").append(textPrimaryColorHex).append(",")
            append("\"textSecondaryColorHex\":").append(textSecondaryColorHex).append(",")
            append("\"surfaceStyle\":\"").append(surfaceStyle.name).append("\",")
            append("\"cornerRadiusDp\":").append(cornerRadiusDp).append(",")
            append("\"glowIntensity\":").append(glowIntensity).append(",")
            append("\"backgroundEffect\":\"").append(backgroundEffect.name).append("\",")
            append("\"themeIntensity\":").append(themeIntensity).append(",")
            append("\"fontScale\":").append(fontScale).append(",")
            append("\"density\":\"").append(density.name).append("\",")
            append("\"isDark\":").append(isDark).append(",")
            append("\"createdAt\":").append(createdAt)
            append("}")
        }
    }

    companion object {
        private fun escapeJson(str: String): String {
            return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
        }

        fun fromJsonString(jsonStr: String): CustomThemeConfig {
            val map = mutableMapOf<String, String>()
            val pattern = "\"([^\"]+)\"\\s*:\\s*(\"[^\"]*\"|[^,}]+)".toRegex()
            for (match in pattern.findAll(jsonStr)) {
                val key = match.groupValues[1]
                val rawValue = match.groupValues[2].trim()
                val value = if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
                    rawValue.substring(1, rawValue.length - 1)
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("\\n", "\n")
                        .replace("\\r", "\r")
                } else {
                    rawValue
                }
                map[key] = value
            }

            return CustomThemeConfig(
                id = map["id"] ?: UUID.randomUUID().toString(),
                name = map["name"] ?: "Custom Theme",
                backgroundColorHex = map["backgroundColorHex"]?.toLongOrNull() ?: 0xFF090B10,
                surfaceColorHex = map["surfaceColorHex"]?.toLongOrNull() ?: 0xFF11151F,
                surfaceElevatedColorHex = map["surfaceElevatedColorHex"]?.toLongOrNull() ?: 0xFF171D2B,
                primaryColorHex = map["primaryColorHex"]?.toLongOrNull() ?: 0xFF00B4A0,
                secondaryColorHex = map["secondaryColorHex"]?.toLongOrNull() ?: 0xFF38BDF8,
                successColorHex = map["successColorHex"]?.toLongOrNull() ?: 0xFF10B981,
                warningColorHex = map["warningColorHex"]?.toLongOrNull() ?: 0xFFF59E0B,
                errorColorHex = map["errorColorHex"]?.toLongOrNull() ?: 0xFFEF4444,
                borderColorHex = map["borderColorHex"]?.toLongOrNull() ?: 0xFF1F293D,
                textPrimaryColorHex = map["textPrimaryColorHex"]?.toLongOrNull() ?: 0xFFF1F5F9,
                textSecondaryColorHex = map["textSecondaryColorHex"]?.toLongOrNull() ?: 0xFF94A3B8,
                surfaceStyle = map["surfaceStyle"]?.let { runCatching { SurfaceStyle.valueOf(it) }.getOrNull() } ?: SurfaceStyle.ELEVATED,
                cornerRadiusDp = map["cornerRadiusDp"]?.toIntOrNull() ?: 12,
                glowIntensity = map["glowIntensity"]?.toFloatOrNull() ?: 0.5f,
                backgroundEffect = map["backgroundEffect"]?.let { runCatching { BackgroundEffect.valueOf(it) }.getOrNull() } ?: BackgroundEffect.SUBTLE_GRADIENT,
                themeIntensity = map["themeIntensity"]?.toFloatOrNull() ?: 0.75f,
                fontScale = map["fontScale"]?.toFloatOrNull() ?: 1.0f,
                density = map["density"]?.let { runCatching { ThemeDensity.valueOf(it) }.getOrNull() } ?: ThemeDensity.COMFORTABLE,
                isDark = map["isDark"]?.toBooleanStrictOrNull() ?: true,
                createdAt = map["createdAt"]?.toLongOrNull() ?: System.currentTimeMillis()
            )
        }

        fun listToJson(list: List<CustomThemeConfig>): String {
            return list.joinToString(prefix = "[", postfix = "]", separator = ",") { it.toJsonString() }
        }

        fun listFromJson(jsonStr: String): List<CustomThemeConfig> {
            if (jsonStr.isBlank()) return emptyList()
            val results = mutableListOf<CustomThemeConfig>()
            val objectPattern = "\\{[^{}]*\\}".toRegex()
            for (match in objectPattern.findAll(jsonStr)) {
                results.add(fromJsonString(match.value))
            }
            return results
        }

        /**
         * Starter custom presets ready for instant customization.
         */
        val StarterCustomThemes = listOf(
            CustomThemeConfig(
                id = "custom_perficio_teal",
                name = "Perficio Studio Teal",
                backgroundColorHex = 0xFF080B10,
                surfaceColorHex = 0xFF101622,
                surfaceElevatedColorHex = 0xFF182030,
                primaryColorHex = 0xFF00B4A0,      // Perficio Teal
                secondaryColorHex = 0xFF22D3EE,    // Vivid Cyan
                successColorHex = 0xFF10B981,
                warningColorHex = 0xFFF59E0B,
                errorColorHex = 0xFFEF4444,
                borderColorHex = 0xFF212E46,
                cornerRadiusDp = 12,
                glowIntensity = 0.55f,
                surfaceStyle = SurfaceStyle.ELEVATED,
                backgroundEffect = BackgroundEffect.SUBTLE_GRADIENT,
                themeIntensity = 0.8f
            ),
            CustomThemeConfig(
                id = "custom_nordic_slate",
                name = "Nordic Slate",
                backgroundColorHex = 0xFF0F172A,
                surfaceColorHex = 0xFF1E293B,
                surfaceElevatedColorHex = 0xFF334155,
                primaryColorHex = 0xFF38BDF8,      // Sky Blue
                secondaryColorHex = 0xFF818CF8,    // Indigo
                successColorHex = 0xFF34D399,
                warningColorHex = 0xFFFBBF24,
                errorColorHex = 0xFFF87171,
                borderColorHex = 0xFF475569,
                cornerRadiusDp = 8,
                glowIntensity = 0.25f,
                surfaceStyle = SurfaceStyle.FLAT,
                backgroundEffect = BackgroundEffect.SOLID,
                themeIntensity = 0.5f
            ),
            CustomThemeConfig(
                id = "custom_emerald_oasis",
                name = "Emerald Matrix",
                backgroundColorHex = 0xFF05120E,
                surfaceColorHex = 0xFF0B211A,
                surfaceElevatedColorHex = 0xFF123328,
                primaryColorHex = 0xFF10B981,      // Emerald
                secondaryColorHex = 0xFF6EE7B7,    // Mint
                successColorHex = 0xFF059669,
                warningColorHex = 0xFFD97706,
                errorColorHex = 0xFFDC2626,
                borderColorHex = 0xFF1C4D3D,
                cornerRadiusDp = 14,
                glowIntensity = 0.7f,
                surfaceStyle = SurfaceStyle.GLASS,
                backgroundEffect = BackgroundEffect.MESH_RADIAL,
                themeIntensity = 0.9f
            )
        )
    }
}
