package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudio.promptforge.abcd.model.GOAL_PRESETS
import com.aistudio.promptforge.abcd.model.GoalPreset
import com.aistudio.promptforge.abcd.ui.EngineStage
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.ui.Screen
import com.aistudio.promptforge.abcd.ui.components.ApiDiagnosticsDialog
import com.aistudio.promptforge.abcd.ui.components.ErrorBanner
import com.aistudio.promptforge.abcd.ui.components.LlmCredentialManagerDialog
import com.aistudio.promptforge.abcd.ui.components.ThemeSelectorDialog
import com.aistudio.promptforge.abcd.ui.theme.AppThemeMode
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    val engineStage by viewModel.engineStage.collectAsState()
    val isEngineRunning by viewModel.isEngineRunning.collectAsState()
    val goalInput by viewModel.goalInput.collectAsState()

    val savedPacks by viewModel.savedPacks.collectAsState()
    val savedPrompts by viewModel.savedPrompts.collectAsState()
    val savedSkills by viewModel.savedSkills.collectAsState()
    val savedMcps by viewModel.savedMcps.collectAsState()
    val currentError by viewModel.currentError.collectAsState()

    var activeInputGoal by remember { mutableStateOf(goalInput) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLlmCredentialDialog by remember { mutableStateOf(false) }

    val llmCredentials by viewModel.llmCredentials.collectAsState()
    val activeLlmCredential by viewModel.activeLlmCredential.collectAsState()
    val executionHistory by viewModel.executionHistory.collectAsState()

    val totalSavedCount = savedPacks.size + savedPrompts.size + savedSkills.size + savedMcps.size

    if (showLlmCredentialDialog) {
        LlmCredentialManagerDialog(
            credentials = llmCredentials,
            activeCredential = activeLlmCredential,
            onSaveCredential = { viewModel.saveLlmCredential(it) },
            onSetActive = { viewModel.setActiveLlmCredential(it) },
            onDelete = { viewModel.deleteLlmCredential(it) },
            onTestCredential = { viewModel.testLlmCredential(it) },
            onDismiss = { showLlmCredentialDialog = false }
        )
    }

    if (showDiagnosticsDialog) {
        ApiDiagnosticsDialog(
            viewModel = viewModel,
            onDismiss = { showDiagnosticsDialog = false }
        )
    }

    if (showThemeDialog) {
        val tm = viewModel.themeManager ?: remember { ThemeManager(context) }
        ThemeSelectorDialog(
            themeManager = tm,
            onOpenThemeBuilder = {
                navController.navigate(Screen.ThemeBuilder.route)
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Hub,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Perficio",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        "STUDIO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                "Autonomous Agent & Tooling Control Room",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showLlmCredentialDialog = true },
                        modifier = Modifier.testTag("dashboard_llm_credentials_button")
                    ) {
                        Icon(
                            Icons.Filled.Key,
                            contentDescription = "BYOK / Multi-LLM Credentials",
                            tint = if (activeLlmCredential != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.ImportForm.route) },
                        modifier = Modifier.testTag("dashboard_import_button")
                    ) {
                        Icon(
                            Icons.Filled.UploadFile,
                            contentDescription = "Import Data to Room",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.History.route) },
                        modifier = Modifier.testTag("dashboard_history_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (executionHistory.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = MaterialTheme.colorScheme.onSecondary
                                    ) {
                                        Text("${executionHistory.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Filled.History,
                                contentDescription = "Execution History",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("dashboard_theme_button")
                    ) {
                        Icon(
                            Icons.Filled.Palette,
                            contentDescription = "Appearance & Themes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showDiagnosticsDialog = true },
                        modifier = Modifier.testTag("dashboard_api_diagnostics_button")
                    ) {
                        Icon(
                            Icons.Filled.NetworkCheck,
                            contentDescription = "API Status",
                            tint = if (viewModel.isApiKeyConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Agent Vault Action Button
                    BadgedBox(
                        badge = {
                            if (totalSavedCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text("$totalSavedCount")
                                }
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        IconButton(
                            onClick = { navController.navigate(Screen.Vault.route) },
                            modifier = Modifier.testTag("dashboard_vault_button")
                        ) {
                            Icon(
                                Icons.Filled.Inventory,
                                contentDescription = "Agent Vault",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ErrorBanner(
                error = currentError,
                onDismiss = { viewModel.clearError() },
                onRetry = { viewModel.retryLastAction() }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
            // ----------------------------------------------------
            // WORKSPACE APPEARANCE & THEME PICKER SECTION
            // ----------------------------------------------------
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_appearance_section_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Workspace Appearance",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Active: ${viewModel.themeManager?.themeMode?.collectAsState()?.value?.title ?: "System Default"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Button(
                                onClick = { showThemeDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("dashboard_customize_theme_button")
                            ) {
                                Text("Customize", fontSize = 12.sp)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Quick Theme Switcher Chips
                        val currentMode = viewModel.themeManager?.themeMode?.collectAsState()?.value ?: AppThemeMode.SYSTEM
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                AppThemeMode.SYSTEM to "System",
                                AppThemeMode.DARK to "Dark",
                                AppThemeMode.LIGHT to "Light",
                                AppThemeMode.NEON to "Neon",
                                AppThemeMode.CYBERPUNK to "Cyberpunk"
                            ).forEach { (mode, label) ->
                                val isSelected = currentMode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.themeManager?.setThemeMode(mode)
                                    },
                                    label = {
                                        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                Icons.Filled.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("dashboard_quick_theme_${mode.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 1. ENGINE STATUS & PIPELINE STATE BANNER
            // ----------------------------------------------------
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isEngineRunning) Color(0xFFEAB308)
                                        else if (engineStage == EngineStage.READY) Color(0xFF10B981)
                                        else MaterialTheme.colorScheme.primary
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Pipeline Status: ${engineStage.title}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.Engine.route)
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Open Engine View",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // MULTI-LLM (BYOK) & ROOM DATA OPERATIONS
            // ----------------------------------------------------
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_multi_llm_room_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Key,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Multi-LLM & Room Operations",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        "BYOK / OAuth Multi-Provider • Local Room Database",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (activeLlmCredential != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = if (activeLlmCredential != null) activeLlmCredential!!.providerType else "GEMINI DIRECT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Active provider details banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (activeLlmCredential != null) "Active: ${activeLlmCredential!!.name}" else "Active: Default Gemini 3.5 Flash",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (activeLlmCredential != null) "${activeLlmCredential!!.defaultModel} • ${activeLlmCredential!!.authType}" else "Cloud Native • Direct API Key",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                TextButton(onClick = { showLlmCredentialDialog = true }) {
                                    Text("Change LLM", fontSize = 11.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Fast action buttons: History, Import, BYOK Manager
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { navController.navigate(Screen.History.route) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("History (${executionHistory.size})", fontSize = 11.sp, maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = { navController.navigate(Screen.ImportForm.route) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Filled.UploadFile, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Import Data", fontSize = 11.sp, maxLines = 1)
                            }

                            Button(
                                onClick = { showLlmCredentialDialog = true },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("BYOK Key", fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 2. UNIFIED INTENT DISPATCHER (RUN ANY ENGINE)
            // ----------------------------------------------------
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.RocketLaunch,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Unified Intent Dispatcher",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Type your high-level goal or query once, then choose which engine to run:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = activeInputGoal,
                            onValueChange = {
                                activeInputGoal = it
                                viewModel.setGoalInput(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .testTag("dashboard_goal_input"),
                            placeholder = {
                                Text("e.g. Build an autonomous PR reviewer with security analysis, SQLite logging, and Discord alerts...")
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        // Preset goal recommendation chips
                        Text(
                            "Or choose a preset goal:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GOAL_PRESETS.take(4).forEach { preset ->
                                FilterChip(
                                    selected = activeInputGoal == preset.genericGoal,
                                    onClick = {
                                        activeInputGoal = preset.genericGoal
                                        viewModel.applyGoalPreset(preset)
                                    },
                                    label = { Text(preset.title, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(12.dp))

                        Text(
                            "Select Engine Execution Mode:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(8.dp))

                        // Combined Auto Forge Execution Button
                        Button(
                            onClick = {
                                val target = activeInputGoal.ifBlank { goalInput }
                                viewModel.setGoalInput(target)
                                viewModel.runAutoForgePipeline(target)
                                navController.navigate(Screen.Engine.route)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("dashboard_run_autoforge_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Run Combined 'Auto Forge' Autonomous Engine",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Specialized Forge Quick Dispatch Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Prompt Forge button
                            OutlinedButton(
                                onClick = {
                                    val target = activeInputGoal.ifBlank { goalInput }
                                    viewModel.setPromptForgeGoal(target)
                                    viewModel.forge10OutOf10Prompt(target)
                                    navController.navigate(Screen.PromptForge.route)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("dashboard_launch_prompt_forge_button"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Prompt Forge", fontSize = 11.sp, maxLines = 1)
                            }

                            // Skill Forge button
                            OutlinedButton(
                                onClick = {
                                    val target = activeInputGoal.ifBlank { goalInput }
                                    viewModel.setSkillForgeQuery(target)
                                    viewModel.scourAndCodeSkills(target)
                                    navController.navigate(Screen.SkillForge.route)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("dashboard_launch_skill_forge_button"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Filled.Psychology, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Skill Forge", fontSize = 11.sp, maxLines = 1)
                            }

                            // Plugin Forge button
                            OutlinedButton(
                                onClick = {
                                    val target = activeInputGoal.ifBlank { goalInput }
                                    viewModel.setMcpForgeQuery(target)
                                    viewModel.synthesizeMcps(target)
                                    navController.navigate(Screen.PluginForge.route)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("dashboard_launch_plugin_forge_button"),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp)
                            ) {
                                Icon(Icons.Filled.Extension, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Plugin Forge", fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 3. CORE ENGINES NAVIGATION CARDS SECTION
            // ----------------------------------------------------
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Autonomous Execution Engines",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "4 Engine Modes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- 3A. COMBINED 'AUTO FORGE' AUTONOMOUS ENGINE (HERO CARD) ---
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .testTag("dashboard_engine_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.FlashOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            "ORCHESTRATOR • 4-STAGE PIPELINE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        "Combined 'Auto Forge' Engine",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "The master autonomous orchestrator. Decomposes any complex intent or vague task into a production-grade agent package. Runs Prompt Forge, Skill Forge, and Plugin Forge in a continuous 4-stage pipeline to compile the full agent specification.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(10.dp))

                        // Capability tags
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DashboardCapabilityTag("Intent Decomposition")
                            DashboardCapabilityTag("Prompt + Skill + Tool Fusion")
                            DashboardCapabilityTag("Autonomous Auto-Coder")
                            DashboardCapabilityTag("Executable Agent Pack")
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = { navController.navigate(Screen.Engine.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("dashboard_launch_autoforge"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Launch Auto Forge Autonomous Engine", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // --- 3B. 'PROMPT FORGE' CARD ---
            item {
                EngineDetailCard(
                    title = "Prompt Forge",
                    badge = "10/10 SYSTEM PROMPT ARCHITECTURE",
                    tagline = "Production-Grade System Prompt Synthesizer",
                    description = "Formulate bulletproof system prompts with explicit persona boundaries, step-by-step Chain-of-Thought reasoning directives, anti-hallucination guardrails, and rigid output contracts.",
                    capabilities = listOf("Role & Boundaries", "CoT Step Directives", "Guardrails & Output Schema", "Token Throughput Tracking"),
                    icon = Icons.Filled.AutoAwesome,
                    iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    iconTint = MaterialTheme.colorScheme.primary,
                    buttonText = "Launch Prompt Forge",
                    testTagCard = "dashboard_prompt_card",
                    testTagButton = "dashboard_card_launch_prompt_forge",
                    onClick = { navController.navigate(Screen.PromptForge.route) }
                )
            }

            // --- 3C. 'SKILL FORGE' CARD ---
            item {
                EngineDetailCard(
                    title = "Skill Forge",
                    badge = "AUTONOMOUS SKILL DISCOVERY & AUTO-CODER",
                    tagline = "Skill Registry Scourer & Python/TS Auto-Coder",
                    description = "Autonomously searches GitHub repositories, X.com, and agent forums for existing capabilities. When custom logic is missing, writes Python and TypeScript implementation code with complete SKILL.md specs and an interactive sandbox runner.",
                    capabilities = listOf("Web & Forum Scour", "Python & TS Auto-Coder", "SKILL.md Spec Generator", "Live Execution Sandbox"),
                    icon = Icons.Filled.Psychology,
                    iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    buttonText = "Launch Skill Forge",
                    testTagCard = "dashboard_skill_card",
                    testTagButton = "dashboard_card_launch_skill_forge",
                    onClick = { navController.navigate(Screen.SkillForge.route) }
                )
            }

            // --- 3D. 'PLUGIN FORGE' CARD ---
            item {
                EngineDetailCard(
                    title = "Plugin Forge",
                    badge = "MODEL CONTEXT PROTOCOL (MCP) ENGINE",
                    tagline = "MCP Server & FastMCP Tool Builder",
                    description = "Integrate standard Model Context Protocol servers (Filesystem, Brave Search, SQLite, GitHub) or autonomously code custom FastMCP servers with typed @mcp.tool() endpoints, parameters, and desktop JSON-RPC configurations.",
                    capabilities = listOf("FastMCP Python/TS Server", "Tool Schema Inspector", "Standard MCP Catalog", "JSON-RPC Config Generator"),
                    icon = Icons.Filled.Extension,
                    iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    buttonText = "Launch Plugin Forge",
                    testTagCard = "dashboard_plugin_card",
                    testTagButton = "dashboard_card_launch_plugin_forge",
                    onClick = { navController.navigate(Screen.PluginForge.route) }
                )
            }

            // --- 3E. 'PROMPT REPOSITORY' CARD ---
            item {
                EngineDetailCard(
                    title = "Prompt Repository",
                    badge = "PROMPT REPOSITORY & SEARCH",
                    tagline = "Curated Library & Full-Text Keyword Search",
                    description = "Browse, search, and run 18+ battle-tested prompt templates across Autonomous Agents, Clean Architecture, Reasoning CoT, and FastMCP. Run interactive tests with Gemini, and copy prompts with 1 tap.",
                    capabilities = listOf("Instant Keyword Search", "Category Filters", "Direct Gemini Runner", "1-Tap Copy & Vault"),
                    icon = Icons.Filled.Folder,
                    iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    buttonText = "Explore Prompt Repository",
                    testTagCard = "dashboard_prompt_repo_card",
                    testTagButton = "dashboard_card_launch_prompt_repo",
                    onClick = { navController.navigate(Screen.PromptRepository.route) }
                )
            }

            // ----------------------------------------------------
            // 4. INTERACTIVE ARCHITECTURE PIPELINE OVERVIEW
            // ----------------------------------------------------
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Hub,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "System Architecture Flow",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "How specialized forges interconnect into the Combined Auto Forge engine:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))

                        // Interactive Flow Diagram Nodes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FlowStepNode(
                                title = "Prompt Forge",
                                subtitle = "10/10 Prompt",
                                icon = Icons.Filled.AutoAwesome,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                tint = MaterialTheme.colorScheme.primary,
                                onClick = { navController.navigate(Screen.PromptForge.route) }
                            )

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )

                            FlowStepNode(
                                title = "Skill Forge",
                                subtitle = "Coded Skills",
                                icon = Icons.Filled.Psychology,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                tint = MaterialTheme.colorScheme.secondary,
                                onClick = { navController.navigate(Screen.SkillForge.route) }
                            )

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )

                            FlowStepNode(
                                title = "Plugin Forge",
                                subtitle = "MCP Tools",
                                icon = Icons.Filled.Extension,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                tint = MaterialTheme.colorScheme.tertiary,
                                onClick = { navController.navigate(Screen.PluginForge.route) }
                            )

                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )

                            FlowStepNode(
                                title = "Auto Forge",
                                subtitle = "Agent Pack",
                                icon = Icons.Filled.FlashOn,
                                containerColor = MaterialTheme.colorScheme.primary,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                onClick = { navController.navigate(Screen.Engine.route) }
                            )
                        }
                    }
                }
            }

            // ----------------------------------------------------
            // 5. AGENT VAULT & TELEMETRY HUB
            // ----------------------------------------------------
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Inventory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Agent Vault Repository",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            TextButtonVaultLink {
                                navController.navigate(Screen.Vault.route)
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Stats Grid (4 columns)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            VaultStatPill(
                                count = savedPacks.size,
                                label = "Goal Packs",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(Screen.Vault.route) }
                            )
                            VaultStatPill(
                                count = savedPrompts.size,
                                label = "Prompts",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(Screen.Vault.route) }
                            )
                            VaultStatPill(
                                count = savedSkills.size,
                                label = "Skills",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(Screen.Vault.route) }
                            )
                            VaultStatPill(
                                count = savedMcps.size,
                                label = "MCPs",
                                modifier = Modifier.weight(1f),
                                onClick = { navController.navigate(Screen.Vault.route) }
                            )
                        }

                        // Recent Pack Preview
                        if (savedPacks.isNotEmpty()) {
                            val latestPack = savedPacks.first()
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Latest Saved Pack: ${latestPack.goalTitle}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            latestPack.goalInput,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.loadPackIntoEngine(latestPack)
                                            navController.navigate(Screen.Engine.route)
                                            Toast.makeText(context, "Loaded into Auto Forge!", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Load", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Recent Saved Prompts Preview
                        if (savedPrompts.isNotEmpty()) {
                            val latestPrompt = savedPrompts.first()
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                "Saved Prompt: ${latestPrompt.title}",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    latestPrompt.frameworkId.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            latestPrompt.assembled,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.setPrompt10OutOf10(latestPrompt.assembled)
                                            viewModel.setPromptForgeGoal(latestPrompt.title)
                                            navController.navigate(Screen.PromptForge.route)
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text("Open", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun TextButtonVaultLink(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                "View All",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(Modifier.width(2.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EngineDetailCard(
    title: String,
    badge: String,
    tagline: String,
    description: String,
    capabilities: List<String>,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTint: Color,
    buttonText: String,
    testTagCard: String,
    testTagButton: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTagCard),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconContainerColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                badge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                tagline,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                capabilities.forEach { cap ->
                    DashboardCapabilityTag(cap)
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag(testTagButton),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = iconContainerColor,
                    contentColor = iconTint
                )
            ) {
                Text(buttonText, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun DashboardCapabilityTag(text: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun FlowStepNode(
    title: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(
            title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
            maxLines = 1
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
fun VaultStatPill(
    count: Int,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
