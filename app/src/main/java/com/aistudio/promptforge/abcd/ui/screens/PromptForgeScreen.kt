package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.ui.Screen
import com.aistudio.promptforge.abcd.ui.components.ApiDiagnosticsDialog
import com.aistudio.promptforge.abcd.ui.components.ErrorBanner
import com.aistudio.promptforge.abcd.ui.components.ThemeSelectorDialog
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptForgeScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val goalText by viewModel.promptForgeGoal.collectAsState()
    val promptResult by viewModel.prompt10OutOf10.collectAsState()
    val isBusy by viewModel.isPromptBusy.collectAsState()
    val metrics by viewModel.promptMetrics.collectAsState()
    val savedPrompts by viewModel.savedPrompts.collectAsState()
    val currentError by viewModel.currentError.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var promptSearchQuery by remember { mutableStateOf("") }
    var selectedFrameworkCategory by remember { mutableStateOf("All") }
    var showDiagnosticsDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var promptDetailToView by remember { mutableStateOf<SavedPrompt?>(null) }

    val frameworks = listOf("Auto-Agent", "GEPA Optimization", "CO-STAR", "CRAFT", "Few-Shot Chain")
    var selectedFramework by remember { mutableStateOf(frameworks[0]) }

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

    if (promptDetailToView != null) {
        val prompt = promptDetailToView!!
        AlertDialog(
            onDismissRequest = { promptDetailToView = null },
            modifier = Modifier.testTag("saved_prompt_detail_dialog"),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            prompt.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Framework: ${prompt.frameworkId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Token count: ~${PromptRepository.estimateTokenCount(prompt.assembled)} tokens",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(prompt.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    SelectionContainer {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                        ) {
                            Text(
                                text = prompt.assembled,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                ),
                                modifier = Modifier
                                    .padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loadSavedPromptIntoForge(prompt)
                        selectedTabIndex = 0
                        promptDetailToView = null
                        Toast.makeText(context, "Loaded into Prompt Forge", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("load_from_dialog_button")
                ) {
                    Text("Load into Forge")
                }
            },
            dismissButton = {
                Row {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(prompt.assembled))
                            Toast.makeText(context, "Copied prompt to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Copy")
                    }
                    Spacer(Modifier.width(6.dp))
                    TextButton(onClick = { promptDetailToView = null }) {
                        Text("Close")
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Prompt Studio",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Intent to 10/10 Production Prompt",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("prompt_forge_theme_selector_button")
                    ) {
                        Icon(
                            Icons.Filled.Palette,
                            contentDescription = "Workspace Theme & Appearance",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // API Diagnostics button
                    IconButton(
                        onClick = { showDiagnosticsDialog = true },
                        modifier = Modifier.testTag("prompt_forge_api_diagnostics_button")
                    ) {
                        Icon(
                            Icons.Filled.NetworkCheck,
                            contentDescription = "API Service Status",
                            tint = if (viewModel.isApiKeyConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Vault button with badge
                    BadgedBox(
                        badge = {
                            if (savedPrompts.isNotEmpty()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text("${savedPrompts.size}")
                                }
                            }
                        }
                    ) {
                        IconButton(
                            onClick = { navController.navigate(Screen.Vault.route) },
                            modifier = Modifier.testTag("prompt_forge_open_vault_button")
                        ) {
                            Icon(Icons.Filled.Inventory, contentDescription = "Agent Vault")
                        }
                    }

                    if (promptResult.isNotBlank()) {
                        IconButton(
                            onClick = {
                                viewModel.savePromptToVault("10/10 Prompt: " + goalText.take(30), promptResult)
                                Toast.makeText(context, "Saved Prompt to Vault!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("save_prompt_vault_button")
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = "Save Prompt")
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
            // Error banner at top of screen
            ErrorBanner(
                error = currentError,
                onDismiss = { viewModel.clearError() },
                onRetry = { viewModel.retryLastAction() }
            )

            // Primary Navigation Tabs (Forge vs Saved Prompts)
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Forge 10/10 Prompt") },
                    icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_forge_prompt")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Saved Prompts (${savedPrompts.size})") },
                    icon = { Icon(Icons.Filled.Inventory, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_saved_prompts")
                )
            }

            if (selectedTabIndex == 0) {
                // ==========================================
                // TAB 0: FORGE PROMPT VIEW
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                ) {
                    // Quick-load saved prompt strip if available
                    if (savedPrompts.isNotEmpty()) {
                        item {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Recent Saved Prompts",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        "View All (${savedPrompts.size})",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        modifier = Modifier
                                            .clickable { selectedTabIndex = 1 }
                                            .padding(4.dp)
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(savedPrompts.take(4)) { prompt ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainer,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                            ),
                                            modifier = Modifier
                                                .clickable {
                                                    viewModel.loadSavedPromptIntoForge(prompt)
                                                    Toast.makeText(context, "Loaded \"${prompt.title.take(24)}\"", Toast.LENGTH_SHORT).show()
                                                }
                                                .testTag("quick_load_prompt_${prompt.id.take(6)}")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Edit,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    prompt.title.take(28),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Input Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Goal or Intent to Forge",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = goalText,
                                    onValueChange = { viewModel.setPromptForgeGoal(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .testTag("prompt_forge_input"),
                                    placeholder = { Text("Enter a broad task, query, or draft prompt to refine to 10/10...") },
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = !isBusy
                                )
                                Spacer(Modifier.height(10.dp))

                                Text(
                                    "Optimization Framework:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(frameworks) { fw ->
                                        FilterChip(
                                            selected = selectedFramework == fw,
                                            onClick = { selectedFramework = fw },
                                            label = { Text(fw, fontSize = 12.sp) }
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                Button(
                                    onClick = { viewModel.forge10OutOf10Prompt(goalText) },
                                    enabled = !isBusy && goalText.isNotBlank(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("forge_10_out_of_10_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    if (isBusy) {
                                        CircularProgressIndicator(
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Forging 10/10 Prompt...")
                                    } else {
                                        Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Synthesize 10/10 Production Prompt", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Real-time Metrics Card
                    if (metrics != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    MetricItem(
                                        label = "Latency",
                                        value = "${metrics!!.latencyMs} ms",
                                        icon = Icons.Filled.Timer
                                    )
                                    MetricItem(
                                        label = "Tokens In/Out",
                                        value = "${metrics!!.promptTokens} / ${metrics!!.outputTokens}",
                                        icon = Icons.Filled.Psychology
                                    )
                                    MetricItem(
                                        label = "Throughput",
                                        value = "%.1f tok/s".format(metrics!!.tokensPerSecond),
                                        icon = Icons.Filled.Speed
                                    )
                                }
                            }
                        }
                    }

                    // Generated Output Card
                    if (promptResult.isNotBlank()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "10/10 Production Prompt",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Row {
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(promptResult))
                                                    Toast.makeText(context, "Copied prompt to clipboard!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.testTag("copy_forged_prompt_button")
                                            ) {
                                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Prompt")
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.savePromptToVault("10/10 Prompt: " + goalText.take(30), promptResult)
                                                    Toast.makeText(context, "Saved to Vault!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.testTag("save_forged_prompt_button")
                                            ) {
                                                Icon(Icons.Filled.Save, contentDescription = "Save Prompt")
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    SelectionContainer {
                                        Text(
                                            text = promptResult,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                lineHeight = 18.sp
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

                                    Spacer(Modifier.height(14.dp))

                                    // Primary Action Buttons: Copy Prompt & Save to Vault
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(promptResult))
                                                Toast.makeText(context, "Copied prompt to clipboard!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .testTag("prompt_forge_copy_button"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Copy Prompt", fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.savePromptToVault("10/10 Prompt: " + goalText.take(30), promptResult)
                                                Toast.makeText(context, "Saved to Vault!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .testTag("prompt_forge_save_button"),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Save to Vault")
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    // Action: Forward to Skill Forge
                                    Button(
                                        onClick = {
                                            viewModel.setSkillForgeQuery(goalText)
                                            viewModel.scourAndCodeSkills(goalText)
                                            navController.navigate("skill_forge")
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Text("Transfer & Forge Skills in Skill Forge")
                                        Spacer(Modifier.width(8.dp))
                                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // TAB 1: LIST SAVED PROMPTS VIEW
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(12.dp))

                    // Search and Filter Bar
                    OutlinedTextField(
                        value = promptSearchQuery,
                        onValueChange = { promptSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("saved_prompts_search_field"),
                        placeholder = { Text("Search saved prompts by title or keyword...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (promptSearchQuery.isNotBlank()) {
                                IconButton(onClick = { promptSearchQuery = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(8.dp))

                    // Category Filter Chips
                    val promptCategories = listOf("All", "Auto-Agent", "GEPA", "CO-STAR", "CRAFT", "Few-Shot")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(promptCategories) { category ->
                            FilterChip(
                                selected = selectedFrameworkCategory == category,
                                onClick = { selectedFrameworkCategory = category },
                                label = { Text(category, fontSize = 11.sp) },
                                modifier = Modifier.testTag("saved_prompt_category_$category")
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    val filteredPrompts = remember(savedPrompts, promptSearchQuery, selectedFrameworkCategory) {
                        val list = if (promptSearchQuery.isBlank()) {
                            savedPrompts
                        } else {
                            val q = promptSearchQuery.trim().lowercase()
                            savedPrompts.filter {
                                it.title.lowercase().contains(q) ||
                                it.assembled.lowercase().contains(q) ||
                                it.frameworkId.lowercase().contains(q)
                            }
                        }
                        if (selectedFrameworkCategory == "All") {
                            list
                        } else {
                            list.filter { it.frameworkId.contains(selectedFrameworkCategory, ignoreCase = true) }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Showing ${filteredPrompts.size} of ${savedPrompts.size} saved prompts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (savedPrompts.isNotEmpty()) {
                            Text(
                                "Total ~${savedPrompts.sumOf { PromptRepository.estimateTokenCount(it.assembled) }} tokens",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (filteredPrompts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    if (promptSearchQuery.isNotBlank()) "No Matching Prompts Found" else "No Saved Prompts in Vault",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    if (promptSearchQuery.isNotBlank()) {
                                        "Try adjusting your search query or clear the filter."
                                    } else {
                                        "Forge 10/10 prompts and save them to build your production prompt library."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 24.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                if (promptSearchQuery.isBlank()) {
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = { selectedTabIndex = 0 },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Forge Your First Prompt")
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 32.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredPrompts, key = { it.id }) { prompt ->
                                SavedPromptCard(
                                    prompt = prompt,
                                    onLoadIntoForge = {
                                        viewModel.loadSavedPromptIntoForge(prompt)
                                        selectedTabIndex = 0
                                        Toast.makeText(context, "Loaded \"${prompt.title}\" into Forge", Toast.LENGTH_SHORT).show()
                                    },
                                    onViewFull = {
                                        promptDetailToView = prompt
                                    },
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(prompt.assembled))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    onDelete = {
                                        viewModel.deleteSavedPrompt(prompt.id)
                                        Toast.makeText(context, "Deleted prompt", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavedPromptCard(
    prompt: SavedPrompt,
    onLoadIntoForge: () -> Unit,
    onViewFull: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val tokenCount = remember(prompt.assembled) {
        PromptRepository.estimateTokenCount(prompt.assembled)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("saved_prompt_card_${prompt.id.take(8)}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = prompt.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = prompt.frameworkId,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "•  ~$tokenCount tokens  •  ${prompt.assembled.length} chars",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp).testTag("copy_saved_prompt_${prompt.id.take(6)}")
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copy Prompt",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_saved_prompt_${prompt.id.take(6)}")
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete Prompt",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Snippet Preview
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onViewFull() }
            ) {
                Text(
                    text = prompt.assembled.take(180) + if (prompt.assembled.length > 180) "..." else "",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(8.dp),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewFull,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp).testTag("view_full_prompt_${prompt.id.take(6)}")
                ) {
                    Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("View Spec", style = MaterialTheme.typography.labelSmall)
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = onLoadIntoForge,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.height(34.dp).testTag("load_prompt_btn_${prompt.id.take(6)}")
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Load into Forge", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
    }
}
