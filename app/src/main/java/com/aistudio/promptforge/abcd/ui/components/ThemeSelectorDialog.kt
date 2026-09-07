package com.aistudio.promptforge.abcd.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.promptforge.abcd.ui.theme.AppThemeMode
import com.aistudio.promptforge.abcd.ui.theme.AppThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.CyberpunkThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.DarkThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.LightThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.LocalAppThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.NeonThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.ObsidianThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager

@Composable
fun ThemeSelectorDialog(
    themeManager: ThemeManager,
    onOpenThemeBuilder: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val currentMode by themeManager.themeMode.collectAsState()
    val glowEnabled by themeManager.glowEffectsEnabled.collectAsState()
    val activeCustomTheme by themeManager.activeCustomTheme.collectAsState()
    val tokens = LocalAppThemeTokens.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(tokens.accentPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Palette,
                        contentDescription = "Perficio Theme Engine",
                        tint = tokens.accentPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Perficio Theme Engine",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Centralized token system with contrast safety",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Choose an appearance mode or build a custom theme with full slider control:",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary
                )

                // 1. Perficio Obsidian (New Default!)
                ThemeOptionCard(
                    mode = AppThemeMode.OBSIDIAN,
                    isSelected = currentMode == AppThemeMode.OBSIDIAN || currentMode == AppThemeMode.SYSTEM,
                    tokens = ObsidianThemeTokens,
                    icon = Icons.Filled.DarkMode,
                    onSelect = { themeManager.setThemeMode(AppThemeMode.OBSIDIAN) },
                    testTag = "theme_option_obsidian"
                )

                // 2. Perficio Dark (Preserved preset)
                ThemeOptionCard(
                    mode = AppThemeMode.DARK,
                    isSelected = currentMode == AppThemeMode.DARK,
                    tokens = DarkThemeTokens,
                    icon = Icons.Filled.DarkMode,
                    onSelect = { themeManager.setThemeMode(AppThemeMode.DARK) },
                    testTag = "theme_option_dark"
                )

                // 3. Perficio Light (Preserved preset)
                ThemeOptionCard(
                    mode = AppThemeMode.LIGHT,
                    isSelected = currentMode == AppThemeMode.LIGHT,
                    tokens = LightThemeTokens,
                    icon = Icons.Filled.LightMode,
                    onSelect = { themeManager.setThemeMode(AppThemeMode.LIGHT) },
                    testTag = "theme_option_light"
                )

                // 4. Perficio Neon (Preserved preset)
                ThemeOptionCard(
                    mode = AppThemeMode.NEON,
                    isSelected = currentMode == AppThemeMode.NEON,
                    tokens = NeonThemeTokens,
                    icon = Icons.Filled.FlashOn,
                    onSelect = { themeManager.setThemeMode(AppThemeMode.NEON) },
                    testTag = "theme_option_neon"
                )

                // 5. Perficio Cyberpunk (Preserved preset)
                ThemeOptionCard(
                    mode = AppThemeMode.CYBERPUNK,
                    isSelected = currentMode == AppThemeMode.CYBERPUNK,
                    tokens = CyberpunkThemeTokens,
                    icon = Icons.Filled.AutoAwesome,
                    onSelect = { themeManager.setThemeMode(AppThemeMode.CYBERPUNK) },
                    testTag = "theme_option_cyberpunk"
                )

                // 6. Custom Theme (Replaces fifth default option!)
                val isCustomSelected = currentMode == AppThemeMode.CUSTOM
                val customTokens = activeCustomTheme.toTokens()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            themeManager.setThemeMode(AppThemeMode.CUSTOM)
                            onDismiss()
                            onOpenThemeBuilder()
                        }
                        .testTag("theme_option_custom"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCustomSelected) {
                            tokens.accentPrimary.copy(alpha = 0.12f)
                        } else {
                            tokens.surfaceElevated
                        }
                    ),
                    border = BorderStroke(
                        width = if (isCustomSelected) 1.8.dp else 1.dp,
                        color = if (isCustomSelected) tokens.accentPrimary else tokens.border
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCustomSelected) tokens.accentPrimary else tokens.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Tune,
                                        contentDescription = null,
                                        tint = if (isCustomSelected) tokens.onPrimary else tokens.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Custom: ${activeCustomTheme.name}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (isCustomSelected) FontWeight.Bold else FontWeight.SemiBold
                                        ),
                                        color = tokens.textPrimary
                                    )
                                    Text(
                                        "User Engineered Theme Studio",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isCustomSelected) tokens.accentPrimary else tokens.textSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (isCustomSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(tokens.accentPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = tokens.onPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Configure semantic colors, surface styles, corner radius, glow, and theme intensity slider.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = tokens.textSecondary
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ColorSwatch(color = customTokens.background, label = "Bg", textColor = customTokens.textPrimary)
                                ColorSwatch(color = customTokens.surface, label = "Surface", textColor = customTokens.textPrimary)
                                ColorSwatch(color = customTokens.accentPrimary, label = "Primary", textColor = customTokens.onPrimary)
                                ColorSwatch(color = customTokens.accentSecondary, label = "Secondary", textColor = customTokens.onSecondary)
                            }

                            Button(
                                onClick = {
                                    themeManager.setThemeMode(AppThemeMode.CUSTOM)
                                    onDismiss()
                                    onOpenThemeBuilder()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tokens.accentPrimary,
                                    contentColor = tokens.onPrimary
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("theme_open_builder_button")
                            ) {
                                Text("Edit Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // State Glow Toggle
                Card(
                    colors = CardDefaults.cardColors(containerColor = tokens.surfaceElevated),
                    border = BorderStroke(1.dp, tokens.border),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Workflow State Glow",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = tokens.textPrimary
                            )
                            Text(
                                "Indicates active agent runs and generation telemetry",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = tokens.textSecondary
                            )
                        }
                        Switch(
                            checked = glowEnabled,
                            onCheckedChange = { themeManager.setGlowEffectsEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = tokens.accentPrimary,
                                checkedTrackColor = tokens.accentPrimary.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.testTag("theme_glow_toggle")
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("theme_dialog_close")
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            if (currentMode != AppThemeMode.OBSIDIAN) {
                OutlinedButton(
                    onClick = { themeManager.resetToDefault() },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.testTag("theme_reset_default_button")
                ) {
                    Icon(Icons.Filled.SettingsBrightness, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Default Obsidian", fontSize = 12.sp)
                }
            }
        }
    )
}

@Composable
private fun ThemeOptionCard(
    mode: AppThemeMode,
    isSelected: Boolean,
    tokens: AppThemeTokens,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onSelect: () -> Unit,
    testTag: String
) {
    val currentTheme = LocalAppThemeTokens.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSelect)
            .testTag(testTag),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                currentTheme.accentPrimary.copy(alpha = 0.12f)
            } else {
                currentTheme.surfaceElevated
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 1.8.dp else 1.dp,
            color = if (isSelected) currentTheme.accentPrimary else currentTheme.border
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) currentTheme.accentPrimary else currentTheme.surface
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isSelected) currentTheme.onPrimary else currentTheme.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            mode.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = currentTheme.textPrimary
                        )
                        Text(
                            mode.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) currentTheme.accentPrimary else currentTheme.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(currentTheme.accentPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = currentTheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                mode.description,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = currentTheme.textSecondary
            )

            // Swatch Chips Preview
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColorSwatch(color = tokens.background, label = "Bg", textColor = tokens.textPrimary)
                ColorSwatch(color = tokens.surface, label = "Surface", textColor = tokens.textPrimary)
                ColorSwatch(color = tokens.accentPrimary, label = "Primary", textColor = tokens.onPrimary)
                ColorSwatch(color = tokens.accentSecondary, label = "Secondary", textColor = tokens.onSecondary)
                if (tokens.isGlowEnabled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(tokens.accentPrimary.copy(alpha = 0.2f))
                            .border(1.dp, tokens.accentPrimary.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Glow", fontSize = 9.sp, color = tokens.accentPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    label: String,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 9.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}
