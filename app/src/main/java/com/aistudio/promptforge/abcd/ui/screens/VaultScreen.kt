package com.aistudio.promptforge.abcd.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.UploadFile
import com.aistudio.promptforge.abcd.data.AutoForgePack
import com.aistudio.promptforge.abcd.data.SavedMcp
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.data.SavedSkill
import com.aistudio.promptforge.abcd.model.RepoPromptItem
import com.aistudio.promptforge.abcd.ui.MainViewModel
import com.aistudio.promptforge.abcd.ui.Screen
import com.aistudio.promptforge.abcd.ui.components.DataPortabilityDialog
import com.aistudio.promptforge.abcd.ui.components.ErrorBanner
import com.aistudio.promptforge.abcd.ui.components.ExecutionProvenanceDialog
import com.aistudio.promptforge.abcd.ui.components.InteractiveGeminiRunnerDialog
import com.aistudio.promptforge.abcd.ui.components.PromptRevisionHistoryDialog
import com.aistudio.promptforge.abcd.ui.components.ProviderSettingsDialog
import com.aistudio.promptforge.abcd.ui.components.ThemeSelectorDialog
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager
import com.aistudio.promptforge.abcd.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val savedPacks by viewModel.savedPacks.collectAsState()
    val savedSkills by viewModel.savedSkills.collectAsState()
    val savedMcps by viewModel.savedMcps.collectAsState()
    val savedPrompts by viewModel.savedPrompts.collectAsState()
    val favoriteIds by viewModel.favoritePromptIds.collectAsState()
    val statsMap by viewModel.promptStatsMap.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Goal Packs, 1: Prompts, 2: Skills, 3: MCPs
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var packDetailToView by remember { mutableStateOf<AutoForgePack?>(null) }
    var runnerPromptItem by remember { mutableStateOf<RepoPromptItem?>(null) }

    var showProviderSettings by remember { mutableStateOf(false) }
    var showDataPortability by remember { mutableStateOf(false) }
    var showProvenanceLogs by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var revisionPromptToView by remember { mutableStateOf<SavedPrompt?>(null) }

    if (showThemeDialog) {
        val tm = viewModel.themeManager ?: remember { ThemeManager(context) }
        ThemeSelectorDialog(
            themeManager = tm,
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showProviderSettings) {
        ProviderSettingsDialog(
            viewModel = viewModel,
            onDismiss = { showProviderSettings = false }
        )
    }

    if (showDataPortability) {
        DataPortabilityDialog(
            viewModel = viewModel,
            onDismiss = { showDataPortability = false }
        )
    }

    if (showProvenanceLogs) {
        ExecutionProvenanceDialog(
            viewModel = viewModel,
            onDismiss = { showProvenanceLogs = false }
        )
    }

    if (revisionPromptToView != null) {
        PromptRevisionHistoryDialog(
            promptId = revisionPromptToView!!.id,
            promptTitle = revisionPromptToView!!.title,
            currentActiveText = revisionPromptToView!!.assembled,
            viewModel = viewModel,
            onRollbackApplied = { rolledBackText ->
                viewModel.savePromptToVault(revisionPromptToView!!.title, rolledBackText)
                revisionPromptToView = null
            },
            onDismiss = { revisionPromptToView = null }
        )
    }

    if (runnerPromptItem != null) {
        InteractiveGeminiRunnerDialog(
            promptItem = runnerPromptItem!!,
            viewModel = viewModel,
            onDismiss = { runnerPromptItem = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.testTag("vault_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
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
                                Icons.Filled.Inventory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Agent Vault",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Saved Goal Packs, Prompts, Skills & MCPs",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.testTag("vault_open_theme_dialog_button")
                    ) {
                        Icon(
                            Icons.Filled.Palette,
                            contentDescription = "Workspace Theme & Appearance",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showProviderSettings = true },
                        modifier = Modifier.testTag("vault_open_provider_settings_button")
                    ) {
                        Icon(
                            Icons.Filled.Security,
                            contentDescription = "AI Provider & Proxy Gateway",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.ImportForm.route) },
                        modifier = Modifier.testTag("vault_open_import_form_button")
                    ) {
                        Icon(
                            Icons.Filled.UploadFile,
                            contentDescription = "Import to Room Database",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.History.route) },
                        modifier = Modifier.testTag("vault_open_history_screen_button")
                    ) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = "Execution Provenance History",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    IconButton(
                        onClick = { showDataPortability = true },
                        modifier = Modifier.testTag("vault_open_data_portability_button")
                    ) {
                        Icon(
                            Icons.Filled.FolderZip,
                            contentDescription = "Data Portability & Factory Reset",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(
                        onClick = { showProvenanceLogs = true },
                        modifier = Modifier.testTag("vault_open_provenance_logs_button")
                    ) {
                        Icon(
                            Icons.Filled.Analytics,
                            contentDescription = "Execution Provenance Traces",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.PromptRepository.route) },
                        modifier = Modifier.testTag("vault_open_prompt_repository_button")
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = "Open Prompt Repository",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
            val currentError by viewModel.currentError.collectAsState()
            if (currentError != null) {
                ErrorBanner(
                    error = currentError,
                    onDismiss = { viewModel.clearError() },
                    onRetry = { viewModel.retryLastAction() },
                    onSwitchToLocal = {
                        viewModel.repository.providerManager.setProviderType(com.aistudio.promptforge.abcd.api.provider.ProviderType.LOCAL_AUTONOMOUS)
                        viewModel.clearError()
                        Toast.makeText(context, "Switched to Autonomous Local Mode", Toast.LENGTH_SHORT).show()
                    },
                    onOpenSettings = { showProviderSettings = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            val tabs = listOf(
                "Goal Packs (${savedPacks.size})",
                "Prompts (${savedPrompts.size})",
                "Skills (${savedSkills.size})",
                "MCPs (${savedMcps.size})"
            )
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = {
                            selectedTab = i
                            selectedCategory = "All"
                        },
                        text = { Text(title, fontSize = 11.sp, maxLines = 1) }
                    )
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("vault_search_input"),
                placeholder = {
                    Text(
                        when (selectedTab) {
                            0 -> "Search goal packs by goal, spec, or task type..."
                            1 -> "Search prompts by title, framework, or content..."
                            2 -> "Search skills by title, trigger, or language..."
                            3 -> "Search MCPs by name, tools, or description..."
                            else -> "Search vault..."
                        },
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category Filter Chips
            val categories = when (selectedTab) {
                0 -> listOf("All", "Full-Stack", "Mobile", "DevOps & Agent", "Data & AI")
                1 -> listOf("All", "CoT", "ReAct", "Role-Task", "Few-Shot", "System Prompt")
                2 -> listOf("All", "System", "Scraper", "Analysis", "Orchestrator", "Code")
                3 -> listOf("All", "Database", "API & Webhook", "Filesystem", "AI Tools", "Utilities")
                else -> listOf("All")
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        modifier = Modifier.testTag("category_chip_${cat.replace(" ", "_")}")
                    )
                }
            }

            // Filtered Items Lists
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                when (selectedTab) {
                    0 -> { // Goal Packs
                        val filteredPacks = savedPacks.filter { pack ->
                            val matchesSearch = searchQuery.isBlank() ||
                                pack.goalTitle.contains(searchQuery, ignoreCase = true) ||
                                pack.goalInput.contains(searchQuery, ignoreCase = true) ||
                                pack.promptText.contains(searchQuery, ignoreCase = true) ||
                                pack.taskType.contains(searchQuery, ignoreCase = true)
                            val matchesCat = selectedCategory == "All" || when (selectedCategory) {
                                "Full-Stack" -> pack.goalTitle.contains("full", true) || pack.goalInput.contains("web", true) || pack.taskType.contains("web", true)
                                "Mobile" -> pack.goalTitle.contains("android", true) || pack.goalInput.contains("mobile", true) || pack.goalInput.contains("compose", true)
                                "DevOps & Agent" -> pack.goalTitle.contains("devops", true) || pack.goalInput.contains("tool", true) || pack.goalInput.contains("pipeline", true)
                                "Data & AI" -> pack.goalTitle.contains("data", true) || pack.goalInput.contains("ai", true) || pack.goalInput.contains("agent", true)
                                else -> true
                            }
                            matchesSearch && matchesCat
                        }

                        if (filteredPacks.isEmpty()) {
                            item {
                                EmptyVaultPlaceholder(
                                    title = if (searchQuery.isNotBlank() || selectedCategory != "All") "No Matching Goal Packs" else "No Saved Goal Packs",
                                    subtitle = if (searchQuery.isNotBlank() || selectedCategory != "All") "Try clearing search filter or selecting 'All' category." else "Run AutoForge Engine to create and save complete autonomous goal packages."
                                )
                            }
                        } else {
                            items(filteredPacks) { pack ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Filled.FlashOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    pack.goalTitle,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    maxLines = 1
                                                )
                                            }
                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        val textToCopy = pack.promptText.ifBlank { pack.fullSpecMarkdown }
                                                        clipboardManager.setText(AnnotatedString(textToCopy))
                                                        Toast.makeText(context, "Goal pack prompt copied!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.testTag("vault_copy_pack_button")
                                                ) {
                                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Prompt")
                                                }
                                                IconButton(onClick = {
                                                    viewModel.loadPackIntoEngine(pack)
                                                    navController.navigate("engine")
                                                    Toast.makeText(context, "Loaded into Engine!", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Filled.FolderOpen, contentDescription = "Load")
                                                }
                                                IconButton(onClick = {
                                                    viewModel.deletePack(pack.id)
                                                    Toast.makeText(context, "Pack deleted", Toast.LENGTH_SHORT).show()
                                                }) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                                }
                                            }
                                        }
                                        Text(
                                            pack.goalInput,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                ) {
                                                    Text(
                                                        "Execution: ${pack.executionLatencyMs}ms",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                                    )
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Text(
                                                        pack.taskType.ifBlank { "Autonomous" },
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    )
                                                }
                                            }
                                            Button(
                                                onClick = { packDetailToView = pack },
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("View Spec", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> { // Prompts
                        // Prompt Repository Link Banner
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                                    .clickable { navController.navigate(Screen.PromptRepository.route) }
                                    .testTag("vault_open_repo_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            Icons.Filled.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                "Explore Curated Prompt Repository",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                "18+ battle-tested prompts with search, filters & Gemini runner",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Category Chips including Favorites
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                item {
                                    val isFavSelected = selectedCategory == "Favorites"
                                    val favCount = savedPrompts.count { favoriteIds.contains(it.id) }
                                    FilterChip(
                                        selected = isFavSelected,
                                        onClick = {
                                            selectedCategory = if (isFavSelected) "All" else "Favorites"
                                        },
                                        label = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    if (isFavSelected) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                                    contentDescription = null,
                                                    tint = if (isFavSelected) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    if (favCount > 0) "Favorites ($favCount)" else "Favorites",
                                                    fontSize = 12.sp
                                                )
                                            }
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFEF4444).copy(alpha = 0.16f),
                                            selectedLabelColor = Color(0xFFEF4444)
                                        ),
                                        modifier = Modifier.testTag("vault_filter_favorites")
                                    )
                                }

                                listOf("All", "RTF", "CREATE", "CARE", "TAG").forEach { cat ->
                                    val isSelected = selectedCategory == cat
                                    item {
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedCategory = cat },
                                            label = { Text(cat, fontSize = 12.sp) },
                                            modifier = Modifier.testTag("vault_filter_${cat.lowercase()}")
                                        )
                                    }
                                }
                            }
                        }

                        val filteredPrompts = savedPrompts.filter { prompt ->
                            val matchesSearch = searchQuery.isBlank() ||
                                prompt.title.contains(searchQuery, ignoreCase = true) ||
                                prompt.assembled.contains(searchQuery, ignoreCase = true) ||
                                prompt.frameworkId.contains(searchQuery, ignoreCase = true)
                            val matchesCat = when (selectedCategory) {
                                "All" -> true
                                "Favorites" -> favoriteIds.contains(prompt.id)
                                else -> prompt.frameworkId.contains(selectedCategory, ignoreCase = true) ||
                                        prompt.title.contains(selectedCategory, ignoreCase = true)
                            }
                            matchesSearch && matchesCat
                        }

                        if (filteredPrompts.isEmpty()) {
                            item {
                                EmptyVaultPlaceholder(
                                    title = if (searchQuery.isNotBlank() || selectedCategory != "All") "No Matching Prompts" else "No Saved Prompts",
                                    subtitle = if (searchQuery.isNotBlank() || selectedCategory != "All") "Try adjusting your search query or category filter." else "Forge 10/10 prompts in Prompt Forge and save them here."
                                )
                            }
                        } else {
                            items(filteredPrompts) { prompt ->
                                val isFavorite = favoriteIds.contains(prompt.id)
                                val stat = statsMap[prompt.id]
                                val wordCount = remember(prompt.assembled) {
                                    prompt.assembled.split("\\s+".toRegex()).count { it.isNotBlank() }
                                }
                                val tokenEst = remember(prompt.assembled) {
                                    (prompt.assembled.length / 4.0).toInt().coerceAtLeast(1)
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth().testTag("vault_prompt_card_${prompt.id.take(8)}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Filled.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(prompt.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                                                Spacer(Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                                ) {
                                                    Text(
                                                        prompt.frameworkId.uppercase(),
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Favorite button
                                                IconButton(
                                                    onClick = { viewModel.toggleFavoritePrompt(prompt.id) },
                                                    modifier = Modifier.size(32.dp).testTag("vault_fav_${prompt.id.take(8)}")
                                                ) {
                                                    Icon(
                                                        if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                                        contentDescription = if (isFavorite) "Favorited" else "Favorite",
                                                        tint = if (isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                // Share button
                                                IconButton(
                                                    onClick = {
                                                        ShareUtils.sharePrompt(
                                                            context = context,
                                                            title = prompt.title,
                                                            framework = prompt.frameworkId,
                                                            promptText = prompt.assembled
                                                        )
                                                        viewModel.recordPromptShare(prompt.id)
                                                    },
                                                    modifier = Modifier.size(32.dp).testTag("vault_share_${prompt.id.take(8)}")
                                                ) {
                                                    Icon(
                                                        Icons.Filled.Share,
                                                        contentDescription = "Share Prompt",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }

                                                // Copy button
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(prompt.assembled))
                                                        viewModel.recordPromptCopy(prompt.id)
                                                        Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.size(32.dp).testTag("vault_copy_prompt_button")
                                                ) {
                                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                                }

                                                // Delete button
                                                IconButton(
                                                    onClick = { viewModel.deleteSavedPrompt(prompt.id) },
                                                    modifier = Modifier.size(32.dp).testTag("vault_delete_${prompt.id.take(8)}")
                                                ) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(6.dp))

                                        // Prompt Statistics & Token estimate
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "~$tokenEst tokens • $wordCount words",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            if (stat != null && (stat.executionCount > 0 || stat.copyCount > 0 || stat.shareCount > 0)) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                                                ) {
                                                    Text(
                                                        text = "${stat.executionCount} runs • ${stat.copyCount} copies • ${stat.shareCount} shares",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, fontSize = 10.sp),
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                            prompt.assembled,
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                                            maxLines = 3,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Spacer(Modifier.height(10.dp))

                                        // Action buttons
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    runnerPromptItem = RepoPromptItem(
                                                        id = prompt.id,
                                                        title = prompt.title,
                                                        category = "Saved Prompts",
                                                        framework = prompt.frameworkId,
                                                        description = "Custom saved prompt from Vault",
                                                        promptTemplate = prompt.assembled,
                                                        isCustom = true
                                                    )
                                                },
                                                modifier = Modifier
                                                    .weight(1.2f)
                                                    .height(36.dp)
                                                    .testTag("vault_run_gemini_${prompt.id.take(8)}"),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(Modifier.width(3.dp))
                                                Text("Run Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(prompt.assembled))
                                                    viewModel.recordPromptCopy(prompt.id)
                                                    Toast.makeText(context, "Copied prompt!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .weight(0.9f)
                                                    .height(36.dp)
                                                    .testTag("vault_copy_btn_${prompt.id.take(8)}"),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(3.dp))
                                                Text("Copy", fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    ShareUtils.sharePrompt(
                                                        context = context,
                                                        title = prompt.title,
                                                        framework = prompt.frameworkId,
                                                        promptText = prompt.assembled
                                                    )
                                                    viewModel.recordPromptShare(prompt.id)
                                                },
                                                modifier = Modifier
                                                    .weight(0.9f)
                                                    .height(36.dp)
                                                    .testTag("vault_share_btn_${prompt.id.take(8)}"),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(3.dp))
                                                Text("Share", fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    revisionPromptToView = prompt
                                                },
                                                modifier = Modifier
                                                    .weight(0.9f)
                                                    .height(36.dp)
                                                    .testTag("vault_revisions_btn_${prompt.id.take(8)}"),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(3.dp))
                                                Text("Versions", fontSize = 10.sp)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.loadSavedPromptIntoForge(prompt)
                                                    navController.navigate(Screen.PromptForge.route)
                                                },
                                                modifier = Modifier
                                                    .weight(0.9f)
                                                    .height(36.dp)
                                                    .testTag("vault_forge_btn_${prompt.id.take(8)}"),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(3.dp))
                                                Text("Forge", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> { // Skills
                        val filteredSkills = savedSkills.filter { skill ->
                            val matchesSearch = searchQuery.isBlank() ||
                                skill.title.contains(searchQuery, ignoreCase = true) ||
                                skill.description.contains(searchQuery, ignoreCase = true) ||
                                skill.trigger.contains(searchQuery, ignoreCase = true) ||
                                skill.implementationCode.contains(searchQuery, ignoreCase = true)
                            val matchesCat = selectedCategory == "All" ||
                                skill.category.contains(selectedCategory, ignoreCase = true) ||
                                skill.title.contains(selectedCategory, ignoreCase = true)
                            matchesSearch && matchesCat
                        }

                        if (filteredSkills.isEmpty()) {
                            item {
                                EmptyVaultPlaceholder(
                                    title = if (searchQuery.isNotBlank() || selectedCategory != "All") "No Matching Skills" else "No Saved Custom Skills",
                                    subtitle = if (searchQuery.isNotBlank() || selectedCategory != "All") "Try adjusting your search query or skill category." else "Scour and code custom skills in Skill Forge and save them here."
                                )
                            }
                        } else {
                            items(filteredSkills) { skill ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Filled.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(skill.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                                                Spacer(Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.secondaryContainer
                                                ) {
                                                    Text(
                                                        skill.category.ifBlank { "Skill" },
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(skill.implementationCode))
                                                        Toast.makeText(context, "Skill code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.testTag("vault_copy_skill_button")
                                                ) {
                                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                                }
                                                IconButton(onClick = { viewModel.deleteSavedSkill(skill.id) }) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                                }
                                            }
                                        }
                                        Text(skill.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.height(6.dp))
                                        Text("Triggers: ${skill.trigger}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary))
                                    }
                                }
                            }
                        }
                    }
                    3 -> { // MCPs
                        val filteredMcps = savedMcps.filter { mcp ->
                            val matchesSearch = searchQuery.isBlank() ||
                                mcp.name.contains(searchQuery, ignoreCase = true) ||
                                mcp.description.contains(searchQuery, ignoreCase = true) ||
                                mcp.category.contains(searchQuery, ignoreCase = true) ||
                                mcp.mcpJsonConfig.contains(searchQuery, ignoreCase = true)
                            val matchesCat = selectedCategory == "All" ||
                                mcp.category.contains(selectedCategory, ignoreCase = true) ||
                                mcp.name.contains(selectedCategory, ignoreCase = true)
                            matchesSearch && matchesCat
                        }

                        if (filteredMcps.isEmpty()) {
                            item {
                                EmptyVaultPlaceholder(
                                    title = if (searchQuery.isNotBlank() || selectedCategory != "All") "No Matching MCPs" else "No Saved MCPs",
                                    subtitle = if (searchQuery.isNotBlank() || selectedCategory != "All") "Try adjusting your search query or MCP category." else "Configure MCP servers and FastMCP tools in Plugin Forge and save them here."
                                )
                            }
                        } else {
                            items(filteredMcps) { mcp ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(Icons.Filled.Extension, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(mcp.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1)
                                                Spacer(Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                                ) {
                                                    Text(
                                                        mcp.category.ifBlank { "Tool" },
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                            Row {
                                                IconButton(
                                                    onClick = {
                                                        clipboardManager.setText(AnnotatedString(mcp.mcpJsonConfig))
                                                        Toast.makeText(context, "MCP config copied to clipboard!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.testTag("vault_copy_mcp_button")
                                                ) {
                                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy")
                                                }
                                                IconButton(onClick = { viewModel.deleteSavedMcp(mcp.id) }) {
                                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                                }
                                            }
                                        }
                                        Text(mcp.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(Modifier.height(6.dp))
                                        Text("${mcp.toolsCount} Tools Configured", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.tertiary))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (packDetailToView != null) {
        val pack = packDetailToView!!
        AlertDialog(
            onDismissRequest = { packDetailToView = null },
            title = { Text(pack.goalTitle, fontWeight = FontWeight.Bold) },
            text = {
                SelectionContainer {
                    LazyColumn(modifier = Modifier.height(350.dp)) {
                        item {
                            Text(
                                text = pack.fullSpecMarkdown,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(pack.promptText.ifBlank { pack.fullSpecMarkdown }))
                            Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("dialog_copy_prompt_button")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Prompt", fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(pack.fullSpecMarkdown))
                            Toast.makeText(context, "Full Spec copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("dialog_copy_spec_button")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Spec", fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { packDetailToView = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun EmptyVaultPlaceholder(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Filled.Inventory,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.size(56.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
