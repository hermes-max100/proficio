package com.aistudio.promptforge.abcd.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.promptforge.abcd.api.ConnectionTestResult
import com.aistudio.promptforge.abcd.data.LlmCredentialEntity
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun LlmCredentialManagerDialog(
    credentials: List<LlmCredentialEntity>,
    activeCredential: LlmCredentialEntity?,
    onSaveCredential: (LlmCredentialEntity) -> Unit,
    onSetActive: (String) -> Unit,
    onDelete: (String) -> Unit,
    onTestCredential: suspend (LlmCredentialEntity) -> ConnectionTestResult,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isAddingNew by remember { mutableStateOf(false) }

    // Form states
    var name by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("GEMINI") }
    var selectedAuthType by remember { mutableStateOf("API_KEY") }
    var credentialValue by remember { mutableStateOf("") }
    var endpointUrl by remember { mutableStateOf("") }
    var defaultModel by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var testStatus by remember { mutableStateOf<ConnectionTestResult?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
        {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAddingNew) "Add LLM Credential" else "BYOK & OAuth Credentials",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                if (!isAddingNew) {
                    Text(
                        text = "Configure custom API keys or OAuth Bearer tokens for Gemini, OpenAI, Claude, or custom LLMs. Active credentials are used for prompt execution and pipeline runs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            isAddingNew = true
                            name = ""
                            credentialValue = ""
                            endpointUrl = ""
                            defaultModel = "gemini-3.5-flash"
                            testStatus = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Provider / Key")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (credentials.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Using Default Gemini Direct API",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pre-configured system credentials are in use. Add your own API key or OAuth Bearer token above for high-throughput or alternate models.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(credentials, key = { it.id }) { cred ->
                                val isActive = cred.id == activeCredential?.id || (activeCredential == null && cred.isActive)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isActive) 1.5.dp else 0.5.dp,
                                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { onSetActive(cred.id) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive)
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                                        else
                                            MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isActive,
                                            onClick = { onSetActive(cred.id) }
                                        )

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = cred.name,
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = cred.providerType,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (cred.authType == "OAUTH_BEARER") "OAuth" else "API Key",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (cred.defaultModel.isNotBlank()) "Model: ${cred.defaultModel}" else "Default Model",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            if (cred.lastTestedAt > 0) {
                                                Text(
                                                    text = if (cred.isHealthy) "✓ Tested (${cred.lastLatencyMs}ms)" else "⚠ Test Failed",
                                                    fontSize = 11.sp,
                                                    color = if (cred.isHealthy) Color(0xFF10B981) else Color(0xFFEF4444)
                                                )
                                            }
                                        }

                                        IconButton(onClick = { onDelete(cred.id) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Add new form
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Text(
                                text = "Provider Type",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("GEMINI", "OPENAI", "ANTHROPIC", "CUSTOM").forEach { prov ->
                                    FilterChip(
                                        selected = selectedProvider == prov,
                                        onClick = {
                                            selectedProvider = prov
                                            defaultModel = when (prov) {
                                                "GEMINI" -> "gemini-3.5-flash"
                                                "OPENAI" -> "gpt-4o"
                                                "ANTHROPIC" -> "claude-3-5-sonnet-20241022"
                                                else -> "custom-model"
                                            }
                                        },
                                        label = { Text(prov, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Authentication Method",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedAuthType == "API_KEY",
                                    onClick = { selectedAuthType = "API_KEY" },
                                    label = { Text("API Key", fontSize = 12.sp) }
                                )
                                FilterChip(
                                    selected = selectedAuthType == "OAUTH_BEARER",
                                    onClick = { selectedAuthType = "OAUTH_BEARER" },
                                    label = { Text("OAuth Bearer Token", fontSize = 12.sp) }
                                )
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Credential Label") },
                                placeholder = { Text("e.g. Production Gemini Key") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = credentialValue,
                                onValueChange = { credentialValue = it },
                                label = { Text(if (selectedAuthType == "OAUTH_BEARER") "OAuth Access Token" else "API Key") },
                                placeholder = { Text(if (selectedAuthType == "OAUTH_BEARER") "Bearer ya29..." else "sk-...") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showPassword) "Hide" else "Show"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (selectedProvider == "CUSTOM" || selectedProvider == "OPENAI") {
                            item {
                                OutlinedTextField(
                                    value = endpointUrl,
                                    onValueChange = { endpointUrl = it },
                                    label = { Text("Custom Base URL (Optional)") },
                                    placeholder = { Text("https://api.groq.com/openai/v1/chat/completions") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = defaultModel,
                                onValueChange = { defaultModel = it },
                                label = { Text("Model Identifier") },
                                placeholder = { Text("e.g. gemini-3.5-flash or gpt-4o") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (testStatus != null) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (testStatus!!.isSuccess)
                                            Color(0xFF10B981).copy(alpha = 0.15f)
                                        else
                                            Color(0xFFEF4444).copy(alpha = 0.15f)
                                    )
                                ) {
                                    Text(
                                        text = testStatus!!.message,
                                        modifier = Modifier.padding(10.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (testStatus!!.isSuccess) Color(0xFF047857) else Color(0xFFB91C1C)
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        if (credentialValue.isNotBlank()) {
                                            isTesting = true
                                            val candidate = LlmCredentialEntity(
                                                id = UUID.randomUUID().toString(),
                                                name = name.ifBlank { "$selectedProvider Key" },
                                                providerType = selectedProvider,
                                                authType = selectedAuthType,
                                                credentialValue = credentialValue,
                                                endpointUrl = endpointUrl,
                                                defaultModel = defaultModel
                                            )
                                            coroutineScope.launch {
                                                testStatus = onTestCredential(candidate)
                                                isTesting = false
                                            }
                                        }
                                    },
                                    enabled = !isTesting && credentialValue.isNotBlank(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isTesting) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Test Ping", fontSize = 12.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        val newCred = LlmCredentialEntity(
                                            id = UUID.randomUUID().toString(),
                                            name = name.ifBlank { "$selectedProvider ($selectedAuthType)" },
                                            providerType = selectedProvider,
                                            authType = selectedAuthType,
                                            credentialValue = credentialValue,
                                            endpointUrl = endpointUrl,
                                            defaultModel = defaultModel,
                                            isActive = true,
                                            isHealthy = testStatus?.isSuccess == true,
                                            lastLatencyMs = testStatus?.latencyMs ?: 0
                                        )
                                        onSaveCredential(newCred)
                                        isAddingNew = false
                                    },
                                    enabled = credentialValue.isNotBlank(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Save & Activate", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isAddingNew) {
                TextButton(onClick = { isAddingNew = false }) {
                    Text("Back to List")
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("Done")
                }
            }
        }
    )
}
