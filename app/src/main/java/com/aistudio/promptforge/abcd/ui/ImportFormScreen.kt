package com.aistudio.promptforge.abcd.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.promptforge.abcd.data.AutoForgePack
import kotlinx.coroutines.launch
import java.util.UUID

enum class ImportType(val title: String, val description: String) {
    PROMPT("Prompt", "Import a structured prompt template into Room"),
    SKILL("Skill", "Import an autonomous agent skill with Python instructions"),
    MCP("MCP Tool", "Import a FastMCP server or client tool configuration"),
    PACK("Agent Pack", "Import an entire Perficio agent specification pack"),
    HISTORY("History Log", "Import an external execution run into Room history"),
    BUNDLE("JSON Bundle", "Import a full Perficio data portability archive")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportFormScreen(
    viewModel: MainViewModel,
    onNavigateToVault: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedType by remember { mutableStateOf(ImportType.PROMPT) }

    // Common fields
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Custom") }
    var framework by remember { mutableStateOf("CO-STAR") }

    // Content fields
    var mainContent by remember { mutableStateOf("") }
    var secondaryContent by remember { mutableStateOf("") }
    var extraField1 by remember { mutableStateOf("") }
    var extraField2 by remember { mutableStateOf("") }

    var isImporting by remember { mutableStateOf(false) }
    var importSuccessMessage by remember { mutableStateOf<String?>(null) }

    fun loadSample() {
        when (selectedType) {
            ImportType.PROMPT -> {
                title = "Senior Architect Code Review"
                category = "Engineering"
                framework = "CO-STAR"
                mainContent = """
# Context
You are a Staff Principal Systems Architect reviewing a pull request in a high-concurrency distributed system.

# Objective
Review the provided Kotlin/Go code for safety, latency, memory leaks, and idempotency:
Code to review:
{code_diff}

# Style & Tone
Rigorous, constructive, actionable, and mathematically precise.

# Audience
Senior Backend Engineers.

# Response Format
1. Executive Summary & Verdict (Approve / Request Changes)
2. Critical Risks & Vulnerabilities
3. Performance & Memory Profiling Observations
4. Refactored Code Proposals
                """.trimIndent()
            }
            ImportType.SKILL -> {
                title = "Postgres Query Profiler"
                category = "Database"
                extraField1 = "postgres-profiler"
                extraField2 = "sql, postgres, explain, analyze, query"
                mainContent = """
def profile_sql_query(query: str, database_url: str = "") -> dict:
    '''Analyzes SQL query plan using EXPLAIN (ANALYZE, BUFFERS)'''
    import json
    return {
        "query": query,
        "execution_time_ms": 14.2,
        "planning_time_ms": 1.1,
        "scanned_rows": 1240,
        "index_used": "idx_user_created_at"
    }
                """.trimIndent()
                secondaryContent = """
# Postgres Query Profiler Skill
Provides deep inspection of SQL queries, identifying sequential scans, high cache-miss buffers, and unindexed foreign keys.
                """.trimIndent()
            }
            ImportType.MCP -> {
                title = "GitHub FastMCP Tool"
                category = "DevOps"
                extraField1 = "Automated PR triage and commit inspection MCP server"
                extraField2 = "{\"version\": \"1.0.0\", \"transport\": \"stdio\", \"timeout\": 30}"
                mainContent = """
from mcp.server.fastmcp import FastMCP

mcp = FastMCP("github-triage")

@mcp.tool()
def get_open_pull_requests(repo: str) -> list[str]:
    '''List all open pull requests for a repository.'''
    return [f"PR #101 in {repo}", f"PR #102 in {repo}"]
                """.trimIndent()
            }
            ImportType.PACK -> {
                title = "Full-Stack Security Audit Pack"
                category = "Security"
                extraField1 = "Autonomous agent pipeline that conducts static analysis and penetration testing audits"
                mainContent = "Perform an end-to-end SAST and DAST evaluation of the project repository."
                secondaryContent = "You are an autonomous offensive security research engineer. Uncover zero-days, injection flaws, and misconfigured permissions."
            }
            ImportType.HISTORY -> {
                title = "Architecture Decision Record Run"
                category = "gemini-3.5-flash"
                extraField1 = "Generate an ADR for adopting event-sourcing with Kafka."
                extraField2 = "480" // latency
                mainContent = """
# ADR-042: Event-Sourcing with Kafka

## Status: ACCEPTED
## Context:
System requires guaranteed auditability and replayable state.

## Decision:
We will transition from direct database mutations to event-driven Kafka logs.
                """.trimIndent()
                secondaryContent = "850" // tokens
            }
            ImportType.BUNDLE -> {
                title = "AutoForge Portable Archive"
                mainContent = """
{
  "version": 1,
  "exportedAt": ${System.currentTimeMillis()},
  "source": "AutoForge Platform",
  "prompts": [],
  "skills": [],
  "mcps": [],
  "autoforgePacks": [],
  "promptStats": [],
  "provenance": []
}
                """.trimIndent()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Import to Room Database",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selectedType.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { loadSample() }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Load Sample", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                // Import Type Selector
                ScrollableTabRow(
                    selectedTabIndex = selectedType.ordinal,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ImportType.values().forEach { type ->
                        Tab(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                importSuccessMessage = null
                            },
                            text = { Text(type.title, fontSize = 13.sp) }
                        )
                    }
                }
            }

            if (importSuccessMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF10B981).copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Import Successful!",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                                Text(
                                    text = importSuccessMessage!!,
                                    fontSize = 12.sp,
                                    color = Color(0xFF065F46)
                                )
                            }
                            if (selectedType == ImportType.HISTORY) {
                                TextButton(onClick = onNavigateToHistory) {
                                    Text("View History", fontSize = 12.sp)
                                }
                            } else {
                                TextButton(onClick = onNavigateToVault) {
                                    Text("View Vault", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Input fields based on type
            when (selectedType) {
                ImportType.PROMPT -> {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Prompt Title") },
                            placeholder = { Text("e.g. Senior Architect Code Review") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = framework,
                                onValueChange = { framework = it },
                                label = { Text("Framework") },
                                placeholder = { Text("CO-STAR / CARE") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") },
                                placeholder = { Text("Engineering") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        Text(
                            text = "Prompt Template Text (Supports {placeholders})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = mainContent,
                            onValueChange = { mainContent = it },
                            placeholder = { Text("Paste prompt template text here...") },
                            minLines = 8,
                            maxLines = 14,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                ImportType.SKILL -> {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Skill Name") },
                            placeholder = { Text("e.g. Postgres Query Profiler") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = extraField1,
                                onValueChange = { extraField1 = it },
                                label = { Text("Slug Identifier") },
                                placeholder = { Text("postgres-profiler") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") },
                                placeholder = { Text("Database") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = extraField2,
                            onValueChange = { extraField2 = it },
                            label = { Text("Trigger Keywords (comma-separated)") },
                            placeholder = { Text("sql, postgres, explain, analyze") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "Python Execution Code",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = mainContent,
                            onValueChange = { mainContent = it },
                            placeholder = { Text("def execute_skill(): ...") },
                            minLines = 6,
                            maxLines = 12,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "Markdown Documentation & Guide",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = secondaryContent,
                            onValueChange = { secondaryContent = it },
                            placeholder = { Text("# Skill Guide\nDescribe what this skill does...") },
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                ImportType.MCP -> {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("MCP Server Name") },
                            placeholder = { Text("e.g. GitHub FastMCP Tool") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Category") },
                                placeholder = { Text("DevOps") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = extraField2,
                                onValueChange = { extraField2 = it },
                                label = { Text("Config JSON") },
                                placeholder = { Text("{\"version\": \"1.0.0\"}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = extraField1,
                            onValueChange = { extraField1 = it },
                            label = { Text("Description") },
                            placeholder = { Text("Automated PR triage FastMCP tool") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "FastMCP Tool Code (Python / TypeScript)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = mainContent,
                            onValueChange = { mainContent = it },
                            placeholder = { Text("from mcp.server.fastmcp import FastMCP\n\nmcp = FastMCP(...)") },
                            minLines = 8,
                            maxLines = 14,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                ImportType.PACK -> {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Pack Title") },
                            placeholder = { Text("Full-Stack Security Audit Pack") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = extraField1,
                            onValueChange = { extraField1 = it },
                            label = { Text("Agent Goal & Purpose") },
                            placeholder = { Text("Conducts static analysis and vulnerability audits") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "System Prompt / Persona",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = secondaryContent,
                            onValueChange = { secondaryContent = it },
                            placeholder = { Text("You are an autonomous agent...") },
                            minLines = 4,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "Initial Master Prompt",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = mainContent,
                            onValueChange = { mainContent = it },
                            placeholder = { Text("Step 1: Inspect repository...") },
                            minLines = 6,
                            maxLines = 10,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                ImportType.HISTORY -> {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Prompt Title / Task") },
                            placeholder = { Text("e.g. Architecture Decision Record Run") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Model Name") },
                                placeholder = { Text("gemini-3.5-flash") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = extraField2,
                                onValueChange = { extraField2 = it },
                                label = { Text("Latency (ms)") },
                                placeholder = { Text("450") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = extraField1,
                            onValueChange = { extraField1 = it },
                            label = { Text("Input Prompt Excerpt") },
                            placeholder = { Text("Prompt text that was sent to the model...") },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text(
                            text = "Output Response Text",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = mainContent,
                            onValueChange = { mainContent = it },
                            placeholder = { Text("Model generation response...") },
                            minLines = 6,
                            maxLines = 12,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                ImportType.BUNDLE -> {
                    item {
                        Text(
                            text = "Paste AutoForge JSON Data Bundle",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = mainContent,
                            onValueChange = { mainContent = it },
                            placeholder = { Text("{\n  \"version\": 1,\n  \"prompts\": [...]\n}") },
                            minLines = 10,
                            maxLines = 16,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Action row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboardText = clipboardManager.getText()?.text
                            if (!clipboardText.isNullOrBlank()) {
                                mainContent = clipboardText
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paste Clipboard", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            isImporting = true
                            coroutineScope.launch {
                                try {
                                    when (selectedType) {
                                        ImportType.PROMPT -> {
                                            viewModel.importSinglePrompt(
                                                title = title.ifBlank { "Imported Prompt" },
                                                framework = framework,
                                                templateText = mainContent
                                            )
                                            importSuccessMessage = "Saved '$title' to Room prompt repository."
                                        }
                                        ImportType.SKILL -> {
                                            viewModel.importSingleSkill(
                                                title = title.ifBlank { "Imported Skill" },
                                                slug = extraField1,
                                                category = category,
                                                trigger = extraField2,
                                                code = mainContent,
                                                markdown = secondaryContent
                                            )
                                            importSuccessMessage = "Saved '$title' to Room skill vault."
                                        }
                                        ImportType.MCP -> {
                                            viewModel.importSingleMcp(
                                                name = title.ifBlank { "Imported MCP" },
                                                category = category,
                                                description = extraField1,
                                                configJson = extraField2,
                                                code = mainContent
                                            )
                                            importSuccessMessage = "Saved '$title' to Room MCP server vault."
                                        }
                                        ImportType.PACK -> {
                                            val pack = AutoForgePack(
                                                id = UUID.randomUUID().toString(),
                                                goalTitle = title.ifBlank { "Imported Agent Pack" },
                                                goalInput = extraField1.ifBlank { title },
                                                taskType = category.ifBlank { "Agent Workflow" },
                                                promptText = mainContent,
                                                skillsJson = "[]",
                                                mcpConfigJson = "[]",
                                                fullSpecMarkdown = secondaryContent.ifBlank { "# $title\n$mainContent" }
                                            )
                                            viewModel.importSinglePack(pack)
                                            importSuccessMessage = "Saved agent pack '$title' to Room database."
                                        }
                                        ImportType.HISTORY -> {
                                            val lat = extraField2.toLongOrNull() ?: 450L
                                            val promptTokens = (extraField1.length / 4).coerceAtLeast(10)
                                            val outputTokens = (mainContent.length / 4).coerceAtLeast(20)
                                            viewModel.importSingleHistoryRun(
                                                promptTitle = title.ifBlank { "Imported Run" },
                                                model = category.ifBlank { "gemini-3.5-flash" },
                                                promptText = extraField1,
                                                outputText = mainContent,
                                                latencyMs = lat,
                                                tokensPrompt = promptTokens,
                                                tokensOutput = outputTokens
                                            )
                                            importSuccessMessage = "Saved run record '$title' to execution history."
                                        }
                                        ImportType.BUNDLE -> {
                                            val result = viewModel.importRawBundle(mainContent)
                                            if (result.isSuccess) {
                                                importSuccessMessage = result.summaryMessage
                                            } else {
                                                snackbarHostState.showSnackbar("Error: ${result.error ?: "Invalid bundle"}")
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Failed to import: ${e.message}")
                                } finally {
                                    isImporting = false
                                }
                            }
                        },
                        enabled = !isImporting && (mainContent.isNotBlank() || title.isNotBlank()),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import to Room", fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
