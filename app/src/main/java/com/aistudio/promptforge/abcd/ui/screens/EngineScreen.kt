package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aistudio.promptforge.abcd.model.GOAL_PRESETS
import com.aistudio.promptforge.abcd.model.GeneratedMcp
import com.aistudio.promptforge.abcd.model.GeneratedSkill
import com.aistudio.promptforge.abcd.model.GoalPreset
import com.aistudio.promptforge.abcd.ui.EngineStage
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.ui.Screen
import com.aistudio.promptforge.abcd.ui.components.ApiDiagnosticsDialog
import com.aistudio.promptforge.abcd.ui.components.ErrorBanner
import com.aistudio.promptforge.abcd.ui.components.ThemeSelectorDialog
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val goalInput by viewModel.goalInput.collectAsState()
    val engineStage by viewModel.engineStage.collectAsState()
    val isRunning by viewModel.isEngineRunning.collectAsState()
    val activePack by viewModel.activePack.collectAsState()
    val engineLogs by viewModel.engineLogs.collectAsState()
    val currentError by viewModel.currentError.collectAsState()
    val runState by viewModel.runState.collectAsState()
    val isGlobalPaused by viewModel.isGlobalPaused.collectAsState()
    val activeDurableRun by viewModel.activeDurableRun.collectAsState()
    val currentFailure by viewModel.currentFailure.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedInspectTab by remember { mutableIntStateOf(0) }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

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
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.FlashOn,
                                contentDescription = "Perficio Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Perficio",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                "Autonomous Goal & Pipeline Engine",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleGlobalPause() },
                        modifier = Modifier.testTag("engine_global_pause_button")
                    ) {
                        Icon(
                            if (isGlobalPaused) Icons.Filled.PlayCircle else Icons.Filled.PauseCircle,
                            contentDescription = if (isGlobalPaused) "Resume Perficio" else "Pause Perficio",
                            tint = if (isGlobalPaused) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("engine_theme_selector_button")
                    ) {
                        Icon(
                            Icons.Filled.Palette,
                            contentDescription = "Workspace Theme & Appearance",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showDiagnosticsDialog = true },
                        modifier = Modifier.testTag("engine_api_diagnostics_button")
                    ) {
                        Icon(
                            Icons.Filled.NetworkCheck,
                            contentDescription = "API Status",
                            tint = if (viewModel.isApiKeyConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.Vault.route) },
                        modifier = Modifier.testTag("engine_open_vault_button")
                    ) {
                        Icon(Icons.Filled.Inventory, contentDescription = "Agent Vault")
                    }
                    if (activePack != null) {
                        IconButton(
                            onClick = {
                                val saved = viewModel.saveActivePackToVault()
                                if (saved) {
                                    Toast.makeText(context, "Saved to Agent Vault!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.testTag("save_pack_button")
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = "Save to Vault")
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

            // Global Pause Notification Banner
            if (isGlobalPaused) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("global_pause_banner"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.PauseCircle,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Perficio Global Pause Active",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    "Autonomous runs paused at checkpoints. Tap Resume to unpause.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.setGlobalPause(false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("resume_global_pause_button")
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Resume", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
            ) {
            // 1. Goal Engine Input Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Define Goal or Task Intent",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Enter a broad, generic goal. Perficio will forge a 10/10 prompt, scour & code required skills, and construct custom FastMCP tools.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = goalInput,
                            onValueChange = { viewModel.setGoalInput(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("goal_input_field"),
                            placeholder = { Text("e.g. Scrape crypto market news, compute sentiment score with Gemini, store in SQLite, and post summary to Discord...") },
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isRunning
                        )

                        Spacer(Modifier.height(12.dp))

                        // Quick Presets Carousel
                        Text(
                            "Quick Goal Presets:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(GOAL_PRESETS) { preset ->
                                FilterChip(
                                    selected = goalInput == preset.genericGoal,
                                    onClick = { viewModel.applyGoalPreset(preset) },
                                    label = {
                                        Text("${preset.iconEmoji} ${preset.title}", maxLines = 1)
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Launch Button
                        Button(
                            onClick = { viewModel.runAutoForgePipeline(goalInput) },
                            enabled = !isRunning && goalInput.isNotBlank() && !isGlobalPaused,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("ignite_engine_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isRunning) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text("Forging Autonomous Pipeline...")
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isGlobalPaused) "Engine Paused (Unpause to run)" else "Ignite Perficio Pipeline",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Running Controls: Pause & Cancel
                        if (isRunning) {
                            Spacer(Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.pauseEngine() },
                                    modifier = Modifier.weight(1f).testTag("pause_running_pipeline_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Pause Run")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.cancelEngine() },
                                    modifier = Modifier.weight(1f).testTag("cancel_running_pipeline_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Cancel Run")
                                }
                            }
                        }
                    }
                }
            }

            // 1.5 Durable Run Checkpoint / Paused Banner
            if (runState == com.aistudio.promptforge.abcd.model.RunState.PAUSED) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkpoint_paused_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.PauseCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Autonomous Run Paused at Checkpoint",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Intermediate progress safely preserved: ${activeDurableRun?.currentStepName ?: "Checkpoint"}. Completed stages will be skipped upon resuming.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.resumeEngine() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("resume_paused_run_button")
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Resume Run", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.cancelEngine() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("cancel_paused_run_button")
                                ) {
                                    Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Cancel Run")
                                }
                            }
                        }
                    }
                }
            }

            // 1.6 Actionable Failure Directive Card
            if (currentFailure != null && !isRunning) {
                item {
                    val failure = currentFailure!!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("actionable_failure_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    failure.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                failure.userMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        when (failure.action) {
                                            com.aistudio.promptforge.abcd.model.RecoveryAction.RESUME -> viewModel.resumeEngine()
                                            com.aistudio.promptforge.abcd.model.RecoveryAction.RETRY -> viewModel.retryEngine()
                                            com.aistudio.promptforge.abcd.model.RecoveryAction.EDIT -> {
                                                navController.navigate(Screen.Vault.route)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("failure_action_directive_button")
                                ) {
                                    Text(failure.actionLabel, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.cancelEngine() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("failure_cancel_button")
                                ) {
                                    Text("Dismiss / Cancel")
                                }
                            }
                        }
                    }
                }
            }

            // 2. Autonomous Pipeline Stepper Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Forge Pipeline Execution",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = CircleShape,
                                color = if (engineStage == EngineStage.READY) Color(0xFF10B981) else if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ) {
                                Text(
                                    text = engineStage.title,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (engineStage == EngineStage.READY) Color.White else MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        if (isRunning) {
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        Spacer(Modifier.height(12.dp))

                        // Stages Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PipelineStageItem(
                                title = "1. Prompt Forge",
                                subtitle = "10/10 Master Prompt",
                                isActive = engineStage == EngineStage.PROMPT_FORGING,
                                isDone = engineStage.stepIndex > 1
                            )
                            PipelineStageItem(
                                title = "2. Skill Forge",
                                subtitle = "Scour & Auto-Code",
                                isActive = engineStage == EngineStage.SKILL_FORGING,
                                isDone = engineStage.stepIndex > 2
                            )
                            PipelineStageItem(
                                title = "3. Plugin Forge",
                                subtitle = "MCPs & FastMCP",
                                isActive = engineStage == EngineStage.PLUGIN_FORGING,
                                isDone = engineStage.stepIndex > 3
                            )
                            PipelineStageItem(
                                title = "4. Agent Assembly",
                                subtitle = "Complete Package",
                                isActive = engineStage == EngineStage.ASSEMBLY,
                                isDone = engineStage == EngineStage.READY
                            )
                        }
                    }
                }
            }

            // 3. Live Logs / Execution Trace
            if (engineLogs.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F172A)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Terminal,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Live Engine Telemetry",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Text(
                                    "${engineLogs.size} events",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFF334155))
                            Spacer(Modifier.height(8.dp))

                            engineLogs.takeLast(6).forEach { log ->
                                Text(
                                    text = log,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = if (log.contains("Error")) Color(0xFFF87171) else if (log.contains("✅") || log.contains("🚀")) Color(0xFF34D399) else Color(0xFFCBD5E1)
                                    ),
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Output Inspector (When Ready)
            if (activePack != null) {
                item {
                    val pack = activePack!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        "Autonomous Agent Package",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        "${pack.skills.size} Skills • ${pack.mcps.size} MCPs • ${pack.executionLatencyMs}ms",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(pack.fullSpecMarkdown))
                                            Toast.makeText(context, "Full Spec copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.saveActivePackToVault()
                                            Toast.makeText(context, "Saved to Vault!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Filled.Save, contentDescription = "Save")
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Sub tabs
                            val tabs = listOf("10/10 Prompt", "Skills (${pack.skills.size})", "MCPs (${pack.mcps.size})", "Full Spec")
                            PrimaryTabRow(selectedTabIndex = selectedInspectTab) {
                                tabs.forEachIndexed { index, title ->
                                    Tab(
                                        selected = selectedInspectTab == index,
                                        onClick = { selectedInspectTab = index },
                                        text = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            when (selectedInspectTab) {
                                0 -> { // 10/10 Prompt
                                    SelectionContainer {
                                        Text(
                                            text = pack.prompt10OutOf10,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(12.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.setPrompt10OutOf10(pack.prompt10OutOf10)
                                            viewModel.setPromptForgeGoal(pack.goalInput)
                                            navController.navigate("prompt_forge")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Open in Standalone Prompt Studio")
                                        Spacer(Modifier.width(6.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                                1 -> { // Skills
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        pack.skills.forEach { skill ->
                                            SkillItemCard(skill = skill, onCopy = {
                                                clipboardManager.setText(AnnotatedString(skill.code))
                                                Toast.makeText(context, "Skill code copied!", Toast.LENGTH_SHORT).show()
                                            })
                                        }
                                        OutlinedButton(
                                            onClick = { navController.navigate("skill_forge") },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Open in Standalone Skill Forge")
                                            Spacer(Modifier.width(6.dp))
                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                2 -> { // MCPs
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        pack.mcps.forEach { mcp ->
                                            McpItemCard(mcp = mcp, onCopy = {
                                                clipboardManager.setText(AnnotatedString(mcp.mcpJsonConfig))
                                                Toast.makeText(context, "MCP JSON Config copied!", Toast.LENGTH_SHORT).show()
                                            })
                                        }
                                        OutlinedButton(
                                            onClick = { navController.navigate("plugin_forge") },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Open in Standalone Plugin Forge")
                                            Spacer(Modifier.width(6.dp))
                                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                3 -> { // Full Spec
                                    SelectionContainer {
                                        Text(
                                            text = pack.fullSpecMarkdown,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surface,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(12.dp)
                                        )
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
fun PipelineStageItem(
    title: String,
    subtitle: String,
    isActive: Boolean,
    isDone: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (isDone) Color(0xFF10B981)
                    else if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceContainerHigh
                )
                .border(
                    width = if (isActive) 2.dp else 1.dp,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else if (isActive) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
                fontSize = 10.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SkillItemCard(skill: GeneratedSkill, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        skill.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Code", modifier = Modifier.size(16.dp))
                }
            }
            Text(
                skill.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        skill.source,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        skill.language.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun McpItemCard(mcp: GeneratedMcp, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Extension,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        mcp.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy JSON", modifier = Modifier.size(16.dp))
                }
            }
            Text(
                mcp.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Tools: ${mcp.tools.joinToString(", ") { it.name }}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
