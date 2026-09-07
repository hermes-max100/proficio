package com.aistudio.promptforge.abcd.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.ui.theme.AppThemeMode
import com.aistudio.promptforge.abcd.ui.theme.BackgroundEffect
import com.aistudio.promptforge.abcd.ui.theme.ContrastSafety
import com.aistudio.promptforge.abcd.ui.theme.CustomThemeConfig
import com.aistudio.promptforge.abcd.ui.theme.LocalAppThemeTokens
import com.aistudio.promptforge.abcd.ui.theme.SurfaceStyle
import com.aistudio.promptforge.abcd.ui.theme.ThemeDensity
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager
import com.aistudio.promptforge.abcd.ui.theme.perficioBackground
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeBuilderScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val themeManager = viewModel.themeManager ?: return
    val savedThemes by themeManager.customThemes.collectAsState()
    val activeCustomTheme by themeManager.activeCustomTheme.collectAsState()
    val currentMode by themeManager.themeMode.collectAsState()
    val tokens = LocalAppThemeTokens.current

    // Local editable draft state initialized from active custom theme
    var draftConfig by remember(activeCustomTheme.id) { mutableStateOf(activeCustomTheme) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveNameInput by remember { mutableStateOf(draftConfig.name) }
    var showDeleteConfirmDialog by remember { mutableStateOf<String?>(null) }
    var selectedColorKey by remember { mutableStateOf<String?>("Primary Accent") }

    // Live preview tokens
    val previewTokens = remember(draftConfig) { draftConfig.toTokens() }
    val contrastRatio = remember(draftConfig) {
        ContrastSafety.calculateContrastRatio(previewTokens.textPrimary, previewTokens.surface)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Perficio Theme Studio",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Custom Design Engine & Token Configurator",
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.testTag("theme_builder_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = tokens.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            themeManager.resetCustomTheme(draftConfig.id)
                            draftConfig = themeManager.activeCustomTheme.value
                        },
                        modifier = Modifier.testTag("theme_builder_reset_button")
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Reset", tint = tokens.textSecondary)
                    }
                    IconButton(
                        onClick = {
                            val duplicated = themeManager.duplicateCustomTheme(draftConfig.id)
                            draftConfig = duplicated
                        },
                        modifier = Modifier.testTag("theme_builder_duplicate_button")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate", tint = tokens.textSecondary)
                    }
                    IconButton(
                        onClick = {
                            saveNameInput = "${draftConfig.name} Preset"
                            showSaveDialog = true
                        },
                        modifier = Modifier.testTag("theme_builder_save_preset_button")
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = "Save Preset", tint = tokens.accentPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = tokens.surface)
            )
        },
        bottomBar = {
            Surface(
                color = tokens.surfaceElevated,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("theme_builder_cancel_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, tokens.border)
                    ) {
                        Text("Cancel", color = tokens.textSecondary)
                    }

                    Button(
                        onClick = {
                            themeManager.saveCustomTheme(draftConfig)
                            themeManager.setThemeMode(AppThemeMode.CUSTOM)
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("theme_builder_apply_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = previewTokens.accentPrimary,
                            contentColor = previewTokens.onPrimary
                        )
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Apply Theme", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .perficioBackground(tokens)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // -----------------------------------------------------------------
            // 1. SAVED THEMES SELECTOR
            // -----------------------------------------------------------------
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Saved Custom Themes",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = tokens.textPrimary
                            )
                            IconButton(
                                onClick = {
                                    val newTheme = CustomThemeConfig(
                                        id = UUID.randomUUID().toString(),
                                        name = "Custom Preset #${savedThemes.size + 1}"
                                    )
                                    themeManager.saveCustomTheme(newTheme)
                                    draftConfig = newTheme
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("theme_builder_add_new_theme_button")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "New Theme", tint = tokens.accentPrimary)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(savedThemes) { themeItem ->
                                val isSelected = draftConfig.id == themeItem.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        draftConfig = themeItem
                                        themeManager.setActiveCustomThemeId(themeItem.id)
                                    },
                                    label = { Text(themeItem.name, fontSize = 12.sp) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    } else null,
                                    trailingIcon = if (savedThemes.size > 1) {
                                        {
                                            IconButton(
                                                onClick = { showDeleteConfirmDialog = themeItem.id },
                                                modifier = Modifier.size(16.dp)
                                            ) {
                                                Icon(Icons.Filled.Close, contentDescription = "Delete", modifier = Modifier.size(12.dp))
                                            }
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = tokens.accentPrimary,
                                        selectedLabelColor = tokens.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // 2. LIVE INTERACTIVE PREVIEW PANEL
            // -----------------------------------------------------------------
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = previewTokens.surface),
                    border = BorderStroke(1.5.dp, previewTokens.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("theme_builder_live_preview_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(previewTokens.accentPrimary)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Live Preview: ${draftConfig.name}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = previewTokens.textPrimary
                                )
                            }

                            // Contrast Safety Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (contrastRatio >= 4.5f) previewTokens.success.copy(alpha = 0.2f) else previewTokens.warning.copy(alpha = 0.2f),
                                border = BorderStroke(
                                    0.5.dp,
                                    if (contrastRatio >= 4.5f) previewTokens.success else previewTokens.warning
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        if (contrastRatio >= 4.5f) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = if (contrastRatio >= 4.5f) previewTokens.success else previewTokens.warning,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "WCAG AA ${ContrastSafety.formatRatio(contrastRatio)}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (contrastRatio >= 4.5f) previewTokens.success else previewTokens.warning
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Preview Child Surface with configured SurfaceStyle and CornerRadius
                        Card(
                            shape = RoundedCornerShape(previewTokens.cornerRadius),
                            colors = CardDefaults.cardColors(
                                containerColor = when (previewTokens.surfaceStyle) {
                                    SurfaceStyle.FLAT -> previewTokens.surface
                                    SurfaceStyle.ELEVATED -> previewTokens.surfaceElevated
                                    SurfaceStyle.GLASS -> previewTokens.surface.copy(alpha = 0.75f)
                                    SurfaceStyle.OUTLINE -> previewTokens.surface
                                }
                            ),
                            border = BorderStroke(
                                if (previewTokens.surfaceStyle == SurfaceStyle.OUTLINE) 1.5.dp else 1.dp,
                                previewTokens.border
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "Autonomous Agent Dispatcher",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = (16 * previewTokens.fontScale).sp
                                    ),
                                    color = previewTokens.textPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Synthesizing autonomous goal execution pack with zero telemetry loss and active contrast safety.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = (12 * previewTokens.fontScale).sp
                                    ),
                                    color = previewTokens.textSecondary
                                )

                                Spacer(Modifier.height(12.dp))

                                // Status Pills
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PreviewStatusPill("Online", previewTokens.success, previewTokens.surface)
                                    PreviewStatusPill("3 Retries", previewTokens.warning, previewTokens.surface)
                                    PreviewStatusPill("0 Errors", previewTokens.error, previewTokens.surface)
                                }

                                Spacer(Modifier.height(14.dp))

                                // Buttons in preview
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {},
                                        shape = RoundedCornerShape(previewTokens.cornerRadius),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = previewTokens.accentPrimary,
                                            contentColor = previewTokens.onPrimary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Primary CTA", fontSize = (12 * previewTokens.fontScale).sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {},
                                        shape = RoundedCornerShape(previewTokens.cornerRadius),
                                        border = BorderStroke(1.dp, previewTokens.border),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = previewTokens.accentSecondary
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Secondary", fontSize = (12 * previewTokens.fontScale).sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // 3. SEMANTIC COLORS CONFIGURATION
            // -----------------------------------------------------------------
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Palette, contentDescription = null, tint = tokens.accentPrimary, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Semantic Colors",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.textPrimary
                                )
                            }

                            // Dark / Light Base Switch
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        draftConfig = draftConfig.copy(
                                            isDark = !draftConfig.isDark,
                                            backgroundColorHex = if (draftConfig.isDark) 0xFFF8FAFC else 0xFF090B10,
                                            surfaceColorHex = if (draftConfig.isDark) 0xFFFFFFFF else 0xFF11151F,
                                            surfaceElevatedColorHex = if (draftConfig.isDark) 0xFFEFF1F6 else 0xFF171D2B,
                                            textPrimaryColorHex = if (draftConfig.isDark) 0xFF0F172A else 0xFFF1F5F9,
                                            textSecondaryColorHex = if (draftConfig.isDark) 0xFF64748B else 0xFF94A3B8,
                                            borderColorHex = if (draftConfig.isDark) 0xFFE2E8F0 else 0xFF1F293D
                                        )
                                    }
                                    .background(tokens.surfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    if (draftConfig.isDark) Icons.Filled.Nightlight else Icons.Filled.LightMode,
                                    contentDescription = null,
                                    tint = tokens.accentPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(if (draftConfig.isDark) "Dark Base" else "Light Base", fontSize = 11.sp, color = tokens.textPrimary)
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Select a semantic role below to assign from quick palette presets or custom color swatches:",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = tokens.textSecondary
                        )

                        Spacer(Modifier.height(12.dp))

                        // Semantic Color Role Selector Chips
                        val colorRoles = listOf(
                            "Primary Accent" to draftConfig.primaryColor,
                            "Secondary Accent" to draftConfig.secondaryColor,
                            "Background" to draftConfig.backgroundColor,
                            "Surface" to draftConfig.surfaceColor,
                            "Surface Elevated" to draftConfig.surfaceElevatedColor,
                            "Success" to draftConfig.successColor,
                            "Warning" to draftConfig.warningColor,
                            "Error" to draftConfig.errorColor,
                            "Border" to draftConfig.borderColor
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            colorRoles.forEach { (roleName, roleColor) ->
                                val isSelected = selectedColorKey == roleName
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) tokens.accentPrimary.copy(alpha = 0.15f) else tokens.surfaceElevated,
                                    border = BorderStroke(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) tokens.accentPrimary else tokens.border
                                    ),
                                    modifier = Modifier
                                        .clickable { selectedColorKey = roleName }
                                        .testTag("color_role_${roleName.replace(" ", "_").lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(roleColor)
                                                .border(0.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            roleName,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = tokens.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        // Palette Preset Selector for the chosen role
                        Text(
                            "Assign Palette Color to: $selectedColorKey",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = tokens.accentPrimary
                        )
                        Spacer(Modifier.height(8.dp))

                        val palettePresets = listOf(
                            0xFF00B4A0 to "Perficio Teal",
                            0xFF38BDF8 to "Ice Cyan",
                            0xFF7C5CFF to "Electric Violet",
                            0xFF10B981 to "Emerald",
                            0xFFF59E0B to "Amber Gold",
                            0xFFEF4444 to "Crimson",
                            0xFFFF2BD6 to "Neon Magenta",
                            0xFFB6FF00 to "Acid Lime",
                            0xFF090B10 to "Deep Obsidian",
                            0xFF11151F to "Obsidian Surface",
                            0xFF171D2B to "Elevated Slate",
                            0xFF1F293D to "Technical Border",
                            0xFFF8FAFC to "Crisp White",
                            0xFF0F172A to "Slate Black"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            palettePresets.forEach { (colorHex, label) ->
                                val color = Color(colorHex)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            draftConfig = when (selectedColorKey) {
                                                "Primary Accent" -> draftConfig.copy(primaryColorHex = colorHex)
                                                "Secondary Accent" -> draftConfig.copy(secondaryColorHex = colorHex)
                                                "Background" -> draftConfig.copy(backgroundColorHex = colorHex)
                                                "Surface" -> draftConfig.copy(surfaceColorHex = colorHex)
                                                "Surface Elevated" -> draftConfig.copy(surfaceElevatedColorHex = colorHex)
                                                "Success" -> draftConfig.copy(successColorHex = colorHex)
                                                "Warning" -> draftConfig.copy(warningColorHex = colorHex)
                                                "Error" -> draftConfig.copy(errorColorHex = colorHex)
                                                "Border" -> draftConfig.copy(borderColorHex = colorHex)
                                                else -> draftConfig
                                            }
                                        }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(color)
                                            .border(1.dp, tokens.border, RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val isCurrentSelected = when (selectedColorKey) {
                                            "Primary Accent" -> draftConfig.primaryColorHex == colorHex
                                            "Secondary Accent" -> draftConfig.secondaryColorHex == colorHex
                                            "Background" -> draftConfig.backgroundColorHex == colorHex
                                            "Surface" -> draftConfig.surfaceColorHex == colorHex
                                            "Surface Elevated" -> draftConfig.surfaceElevatedColorHex == colorHex
                                            "Success" -> draftConfig.successColorHex == colorHex
                                            "Warning" -> draftConfig.warningColorHex == colorHex
                                            "Error" -> draftConfig.errorColorHex == colorHex
                                            "Border" -> draftConfig.borderColorHex == colorHex
                                            else -> false
                                        }
                                        if (isCurrentSelected) {
                                            Icon(Icons.Filled.Check, contentDescription = null, tint = if (color.red + color.green + color.blue > 1.8f) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(label, fontSize = 9.sp, color = tokens.textSecondary, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // 4. APPEARANCE CONTROLS: SURFACE STYLE, RADIUS, GLOW, EFFECTS, INTENSITY
            // -----------------------------------------------------------------
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Tune, contentDescription = null, tint = tokens.accentPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Appearance Controls",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = tokens.textPrimary
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // A. Surface Style
                        Text("Surface Style", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = tokens.textPrimary)
                        Text("Defines layering, borders, and translucency", fontSize = 11.sp, color = tokens.textSecondary)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SurfaceStyle.values().forEach { style ->
                                val isSelected = draftConfig.surfaceStyle == style
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { draftConfig = draftConfig.copy(surfaceStyle = style) },
                                    label = { Text(style.label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = tokens.accentPrimary,
                                        selectedLabelColor = tokens.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // B. Corner Radius Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Corner Radius", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = tokens.textPrimary)
                            Text("${draftConfig.cornerRadiusDp} dp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.accentPrimary)
                        }
                        Slider(
                            value = draftConfig.cornerRadiusDp.toFloat(),
                            onValueChange = { draftConfig = draftConfig.copy(cornerRadiusDp = it.toInt()) },
                            valueRange = 0f..24f,
                            steps = 23,
                            colors = SliderDefaults.colors(
                                thumbColor = tokens.accentPrimary,
                                activeTrackColor = tokens.accentPrimary
                            ),
                            modifier = Modifier.testTag("theme_builder_corner_radius_slider")
                        )

                        Spacer(Modifier.height(12.dp))

                        // C. Glow Intensity Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Glow Intensity", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = tokens.textPrimary)
                            Text("${(draftConfig.glowIntensity * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.accentPrimary)
                        }
                        Slider(
                            value = draftConfig.glowIntensity,
                            onValueChange = { draftConfig = draftConfig.copy(glowIntensity = it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = tokens.accentPrimary,
                                activeTrackColor = tokens.accentPrimary
                            ),
                            modifier = Modifier.testTag("theme_builder_glow_slider")
                        )

                        Spacer(Modifier.height(12.dp))

                        // D. Background Effects
                        Text("Background Effect", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = tokens.textPrimary)
                        Text("Subtle atmospheric rendering behind views", fontSize = 11.sp, color = tokens.textSecondary)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BackgroundEffect.values().forEach { effect ->
                                val isSelected = draftConfig.backgroundEffect == effect
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { draftConfig = draftConfig.copy(backgroundEffect = effect) },
                                    label = { Text(effect.label, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = tokens.accentPrimary,
                                        selectedLabelColor = tokens.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // E. Theme Intensity Slider ("Clean" to "Immersive")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Theme Intensity", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = tokens.textPrimary)
                            val intensityLabel = when {
                                draftConfig.themeIntensity < 0.33f -> "Clean & Minimal"
                                draftConfig.themeIntensity < 0.66f -> "Balanced Focus"
                                else -> "Immersive Studio"
                            }
                            Text(intensityLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = tokens.accentPrimary)
                        }
                        Slider(
                            value = draftConfig.themeIntensity,
                            onValueChange = { draftConfig = draftConfig.copy(themeIntensity = it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = tokens.accentPrimary,
                                activeTrackColor = tokens.accentPrimary
                            ),
                            modifier = Modifier.testTag("theme_builder_intensity_slider")
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // 5. TYPOGRAPHY & DENSITY SETTINGS
            // -----------------------------------------------------------------
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = tokens.surface),
                    border = BorderStroke(1.dp, tokens.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.FormatSize, contentDescription = null, tint = tokens.accentPrimary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Typography & Density",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = tokens.textPrimary
                            )
                        }

                        Spacer(Modifier.height(14.dp))

                        // Font Scale
                        Text("Text Size Scale", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = tokens.textPrimary)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                0.85f to "Compact 85%",
                                1.00f to "Normal 100%",
                                1.15f to "Large 115%",
                                1.30f to "Extra 130%"
                            ).forEach { (scale, label) ->
                                val isSelected = (draftConfig.fontScale - scale) in -0.05f..0.05f
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { draftConfig = draftConfig.copy(fontScale = scale) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = tokens.accentPrimary,
                                        selectedLabelColor = tokens.onPrimary
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Density
                        Text("Layout Density", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = tokens.textPrimary)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ThemeDensity.values().forEach { densityMode ->
                                val isSelected = draftConfig.density == densityMode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { draftConfig = draftConfig.copy(density = densityMode) },
                                    label = { Text(densityMode.label, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = tokens.accentPrimary,
                                        selectedLabelColor = tokens.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Custom Preset") },
            text = {
                Column {
                    Text(
                        "Give this custom theme configuration a name to add it to your saved themes:",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = saveNameInput,
                        onValueChange = { saveNameInput = it },
                        singleLine = true,
                        label = { Text("Theme Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newPreset = draftConfig.copy(
                            id = UUID.randomUUID().toString(),
                            name = saveNameInput.trim().ifEmpty { "Custom Theme" },
                            createdAt = System.currentTimeMillis()
                        )
                        themeManager.saveCustomTheme(newPreset)
                        draftConfig = newPreset
                        showSaveDialog = false
                    }
                ) {
                    Text("Save Preset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { themeIdToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Custom Theme?") },
            text = { Text("This will permanently remove this theme preset from your saved list.") },
            confirmButton = {
                Button(
                    onClick = {
                        themeManager.deleteCustomTheme(themeIdToDelete)
                        showDeleteConfirmDialog = null
                        draftConfig = themeManager.activeCustomTheme.value
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = tokens.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PreviewStatusPill(label: String, color: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.2f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(4.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
