package com.aistudio.promptforge.abcd.data

import com.aistudio.promptforge.abcd.BuildConfig
import com.aistudio.promptforge.abcd.api.ApiCallResult
import com.aistudio.promptforge.abcd.api.ApiHealthStatus
import com.aistudio.promptforge.abcd.api.Content
import com.aistudio.promptforge.abcd.api.GeminiErrorResponse
import com.aistudio.promptforge.abcd.api.GenerateContentRequest
import com.aistudio.promptforge.abcd.api.GenerationConfig
import com.aistudio.promptforge.abcd.api.Part
import com.aistudio.promptforge.abcd.api.PromptForgeApiService
import com.aistudio.promptforge.abcd.api.RetrofitClient
import com.aistudio.promptforge.abcd.api.SupportedModels
import com.aistudio.promptforge.abcd.api.ConnectionTestResult
import com.aistudio.promptforge.abcd.api.UniversalLlmResult
import com.aistudio.promptforge.abcd.api.UniversalLlmService
import com.aistudio.promptforge.abcd.api.provider.GeminiDirectProvider
import com.aistudio.promptforge.abcd.api.provider.ProviderManager
import com.aistudio.promptforge.abcd.data.repository.PromptRevisionRepository
import com.aistudio.promptforge.abcd.data.repository.ProvenanceRepository
import com.aistudio.promptforge.abcd.data.repository.VaultDataRepository
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.model.AutoForgePackData
import com.aistudio.promptforge.abcd.model.GeneratedMcp
import com.aistudio.promptforge.abcd.model.GeneratedSkill
import com.aistudio.promptforge.abcd.util.AiOutputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.util.UUID
import kotlin.math.ceil

@kotlinx.serialization.Serializable
data class GenerationMetrics(
    val latencyMs: Long = 0,
    val promptTokens: Int = 0,
    val outputTokens: Int = 0,
    val totalTokens: Int = 0,
    val isEstimated: Boolean = false,
    val promptChars: Int = 0,
    val promptWords: Int = 0,
    val outputChars: Int = 0,
    val outputWords: Int = 0
) {
    val tokensPerSecond: Float
        get() = if (latencyMs > 0 && outputTokens > 0) {
            (outputTokens.toFloat() / (latencyMs.toFloat() / 1000f))
        } else 0f
}

sealed class AiResult<out T> {
    data class Success<out T>(
        val data: T,
        val metrics: GenerationMetrics,
        val isFallback: Boolean = false,
        val notice: AppError? = null
    ) : AiResult<T>()

    data class Error(
        val message: String,
        val code: Int? = null,
        val appError: AppError? = null
    ) : AiResult<Nothing>()
}

class PromptRepository(
    private val dao: PromptDao,
    val apiService: PromptForgeApiService = PromptForgeApiService()
) {
    val revisionRepository = PromptRevisionRepository(dao)
    val provenanceRepository = ProvenanceRepository(dao)
    val vaultDataRepository = VaultDataRepository(dao, provenanceRepository)
    val providerManager = ProviderManager(
        geminiDirectProvider = GeminiDirectProvider(apiService)
    )

    // AutoForge Packs
    fun getAutoForgePacks(): Flow<List<AutoForgePack>> = dao.getAllAutoForgePacks()
    suspend fun insertAutoForgePack(pack: AutoForgePack) = dao.insertAutoForgePack(pack)
    suspend fun deleteAutoForgePack(id: String) = dao.deleteAutoForgePack(id)

    // Saved Skills
    fun getSavedSkills(): Flow<List<SavedSkill>> = dao.getAllSavedSkills()
    suspend fun insertSavedSkill(skill: SavedSkill) = dao.insertSavedSkill(skill)
    suspend fun deleteSavedSkill(id: String) = dao.deleteSavedSkill(id)

    // Saved MCPs
    fun getSavedMcps(): Flow<List<SavedMcp>> = dao.getAllSavedMcps()
    suspend fun insertSavedMcp(mcp: SavedMcp) = dao.insertSavedMcp(mcp)
    suspend fun deleteSavedMcp(id: String) = dao.deleteSavedMcp(id)

    // Saved Prompts
    fun getSavedPrompts(): Flow<List<SavedPrompt>> = dao.getAllSavedPrompts()
    fun searchSavedPrompts(query: String): Flow<List<SavedPrompt>> = dao.searchSavedPrompts(query)
    suspend fun insertSavedPrompt(prompt: SavedPrompt) = dao.insertSavedPrompt(prompt)
    suspend fun deleteSavedPrompt(id: String) = dao.deleteSavedPrompt(id)

    // Favorites
    fun getFavoritePromptIds(): Flow<List<String>> = dao.getAllFavoritePromptIds()
    suspend fun toggleFavorite(promptId: String): Boolean {
        return if (dao.isFavorite(promptId)) {
            dao.deleteFavorite(promptId)
            false
        } else {
            dao.insertFavorite(FavoritePrompt(promptId))
            true
        }
    }
    suspend fun isFavorite(promptId: String): Boolean = dao.isFavorite(promptId)

    // Prompt Stats
    fun getAllPromptStats(): Flow<List<PromptStat>> = dao.getAllPromptStats()
    suspend fun recordPromptExecution(promptId: String, latencyMs: Long) {
        val current = dao.getPromptStat(promptId) ?: PromptStat(promptId)
        dao.insertOrUpdatePromptStat(
            current.copy(
                executionCount = current.executionCount + 1,
                lastLatencyMs = latencyMs,
                lastUsedAt = System.currentTimeMillis()
            )
        )
    }
    suspend fun recordPromptCopy(promptId: String) {
        val current = dao.getPromptStat(promptId) ?: PromptStat(promptId)
        dao.insertOrUpdatePromptStat(
            current.copy(
                copyCount = current.copyCount + 1,
                lastUsedAt = System.currentTimeMillis()
            )
        )
    }
    suspend fun recordPromptShare(promptId: String) {
        val current = dao.getPromptStat(promptId) ?: PromptStat(promptId)
        dao.insertOrUpdatePromptStat(
            current.copy(
                shareCount = current.shareCount + 1,
                lastUsedAt = System.currentTimeMillis()
            )
        )
    }

    fun setCustomApiKey(key: String) {
        apiService.setCustomKey(key)
    }

    // LLM Credentials & BYOK / OAuth in Room
    fun getAllLlmCredentials(): Flow<List<LlmCredentialEntity>> = dao.getAllLlmCredentials()
    fun getActiveLlmCredential(): Flow<LlmCredentialEntity?> = dao.getActiveLlmCredential()
    suspend fun getActiveLlmCredentialSync(): LlmCredentialEntity? = dao.getActiveLlmCredentialSync()

    suspend fun saveLlmCredential(credential: LlmCredentialEntity) {
        if (credential.isActive) {
            dao.deactivateAllLlmCredentials()
        }
        dao.insertLlmCredential(credential)
        if (credential.providerType == "GEMINI" && credential.authType == "API_KEY") {
            apiService.setCustomKey(credential.credentialValue)
        }
    }

    suspend fun setActiveLlmCredential(id: String) {
        dao.deactivateAllLlmCredentials()
        dao.activateLlmCredential(id)
        val active = dao.getActiveLlmCredentialSync()
        if (active != null && active.providerType == "GEMINI" && active.authType == "API_KEY") {
            apiService.setCustomKey(active.credentialValue)
        }
    }

    suspend fun deleteLlmCredential(id: String) {
        dao.deleteLlmCredential(id)
    }

    suspend fun testLlmCredential(credential: LlmCredentialEntity): ConnectionTestResult {
        val result = UniversalLlmService.testCredential(credential)
        val updated = credential.copy(
            lastTestedAt = System.currentTimeMillis(),
            lastLatencyMs = result.latencyMs,
            isHealthy = result.isSuccess
        )
        dao.insertLlmCredential(updated)
        return result
    }

    // Playground Runs
    fun getPlaygroundRuns(): Flow<List<PlaygroundRun>> = dao.getAllPlaygroundRuns()
    suspend fun insertPlaygroundRun(run: PlaygroundRun) = dao.insertPlaygroundRun(run)
    suspend fun clearPlaygroundRuns() = dao.clearPlaygroundRuns()

    companion object {
        fun estimateTokenCount(text: String): Int {
            if (text.isBlank()) return 0
            val byChars = ceil(text.length / 4.0).toInt()
            val words = text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            val byWords = ceil(words / 0.75).toInt()
            return maxOf(byChars, byWords, 1)
        }
    }

    fun isApiKeyConfigured(): Boolean {
        return apiService.isApiKeyConfigured()
    }

    suspend fun testApiHealth(model: String = SupportedModels.FLASH_LATEST): ApiHealthStatus {
        return apiService.testConnection(model)
    }

    suspend fun generateComplete(
        system: String?,
        user: String,
        temperature: Float = 0.4f,
        maxTokens: Int = 1500,
        model: String = SupportedModels.FLASH_LATEST
    ): AiResult<String> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val promptCombined = if (!system.isNullOrBlank()) "$system\n$user" else user
        val promptWords = promptCombined.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
        val promptChars = promptCombined.length

        // Check if there is an active BYOK or OAuth credential in Room
        val activeCredential = dao.getActiveLlmCredentialSync()
        if (activeCredential != null && (activeCredential.providerType != "GEMINI" || activeCredential.authType != "API_KEY" || activeCredential.credentialValue.isNotBlank())) {
            val universalResult = UniversalLlmService.execute(
                prompt = user,
                systemInstruction = system,
                credential = activeCredential,
                overrideModel = if (model != SupportedModels.FLASH_LATEST) model else null,
                temperature = temperature,
                maxTokens = maxTokens
            )
            if (universalResult.isSuccess) {
                val sanitized = AiOutputValidator.sanitizeAndValidate(universalResult.text).sanitizedText
                val outputWords = sanitized.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                return@withContext AiResult.Success(
                    data = sanitized,
                    metrics = GenerationMetrics(
                        latencyMs = universalResult.latencyMs,
                        promptTokens = universalResult.promptTokens,
                        outputTokens = universalResult.outputTokens,
                        totalTokens = universalResult.totalTokens,
                        isEstimated = false,
                        promptChars = promptChars,
                        promptWords = promptWords,
                        outputChars = sanitized.length,
                        outputWords = outputWords
                    ),
                    isFallback = false
                )
            } else if (!apiService.isApiKeyConfigured()) {
                return@withContext AiResult.Error(
                    message = universalResult.errorMessage ?: "LLM generation failed",
                    appError = AppError.generic(universalResult.errorMessage ?: "LLM generation failed")
                )
            }
        }

        if (!apiService.isApiKeyConfigured()) {
            val fallbackText = AutoForgeEngine.generateLocalPrompt10OutOf10(user)
            val latency = System.currentTimeMillis() - startTime
            val outTokens = estimateTokenCount(fallbackText)
            val inTokens = estimateTokenCount(promptCombined)
            return@withContext AiResult.Success(
                data = fallbackText,
                metrics = GenerationMetrics(
                    latencyMs = latency,
                    promptTokens = inTokens,
                    outputTokens = outTokens,
                    totalTokens = inTokens + outTokens,
                    isEstimated = true,
                    promptChars = promptChars,
                    promptWords = promptWords,
                    outputChars = fallbackText.length,
                    outputWords = fallbackText.trim().split("\\s+".toRegex()).size
                ),
                isFallback = true,
                notice = AppError.apiKeyMissing()
            )
        }

        val apiResult = apiService.generateContent(
            prompt = user,
            systemInstruction = system,
            model = model,
            temperature = temperature,
            maxTokens = maxTokens
        )

        when (apiResult) {
            is ApiCallResult.Success -> {
                val response = apiResult.data
                val candidate = response.candidates?.firstOrNull()
                val rawText = candidate?.content?.parts?.firstOrNull()?.text ?: ""
                val sanitized = AiOutputValidator.sanitizeAndValidate(rawText).sanitizedText
                val outputWords = sanitized.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
                val outputChars = sanitized.length

                val promptTokens = response.usageMetadata?.promptTokenCount ?: estimateTokenCount(promptCombined)
                val outputTokens = response.usageMetadata?.candidatesTokenCount ?: estimateTokenCount(sanitized)
                val totalTokens = response.usageMetadata?.totalTokenCount ?: (promptTokens + outputTokens)
                val isEstimated = response.usageMetadata == null

                val metrics = GenerationMetrics(
                    latencyMs = apiResult.latencyMs,
                    promptTokens = promptTokens,
                    outputTokens = outputTokens,
                    totalTokens = totalTokens,
                    isEstimated = isEstimated,
                    promptChars = promptChars,
                    promptWords = promptWords,
                    outputChars = outputChars,
                    outputWords = outputWords
                )
                AiResult.Success(sanitized, metrics, isFallback = false)
            }
            is ApiCallResult.Failure -> {
                // Return fallback synthesis so the user is never blocked, but surface the AppError
                val fallbackText = AutoForgeEngine.generateLocalPrompt10OutOf10(user)
                val latency = System.currentTimeMillis() - startTime
                val outTokens = estimateTokenCount(fallbackText)
                val inTokens = estimateTokenCount(promptCombined)
                AiResult.Success(
                    data = fallbackText,
                    metrics = GenerationMetrics(
                        latencyMs = latency,
                        promptTokens = inTokens,
                        outputTokens = outTokens,
                        totalTokens = inTokens + outTokens,
                        isEstimated = true,
                        promptChars = promptChars,
                        promptWords = promptWords,
                        outputChars = fallbackText.length,
                        outputWords = fallbackText.trim().split("\\s+".toRegex()).size
                    ),
                    isFallback = true,
                    notice = apiResult.error
                )
            }
        }
    }

    suspend fun synthesize10OutOf10Prompt(goal: String, model: String = SupportedModels.FLASH_LATEST): AiResult<String> {
        val systemPrompt = """
You are AutoForge's Master Prompt Synthesizer.
Your mission is to take ANY broad, generic, or brief goal and engineer a flawless, comprehensive "10 out of 10" production prompt.
The generated prompt MUST include:
1. # SYSTEM PERSONA & EXPERTISE (Elite domain authority & operational standards)
2. # MISSION OBJECTIVE & SCOPE (Unambiguous boundary conditions)
3. # MULTI-STEP CHAIN-OF-THOUGHT PROTOCOL (Step 1 to Step 5 logical execution order)
4. # GUARDRAILS & ANTI-HALLUCINATION RULES (Zero unverified assumptions, rate limit handling)
5. # STRICT OUTPUT CONTRACT (Typed JSON schemas or structured Markdown sections)

Return ONLY the assembled 10/10 Prompt without conversational preamble.
""".trimIndent()

        return generateComplete(
            system = systemPrompt,
            user = "Goal/Task Intent: $goal",
            temperature = 0.3f,
            maxTokens = 1400,
            model = model
        )
    }

    suspend fun synthesizeSkillsForGoal(
        goal: String,
        prompt: String,
        model: String = SupportedModels.FLASH_LATEST
    ): List<GeneratedSkill> {
        if (!isApiKeyConfigured()) {
            return AutoForgeEngine.generateLocalSkills(goal)
        }

        // Try AI generation for custom skills, fallback to local engine if needed
        val system = """
You are AutoForge's Skill Engineer.
Analyze the user's goal and generate the Python code and SKILL.md for a custom autonomous skill.
Return format:
SKILL_NAME: <name>
CATEGORY: <category>
TRIGGER: <trigger_keyword>
DESCRIPTION: <brief description>
---CODE---
<executable python class code>
---MARKDOWN---
<SKILL.md documentation>
""".trimIndent()

        val aiResult = generateComplete(
            system = system,
            user = "Goal: $goal\nPrompt Context: $prompt",
            temperature = 0.3f,
            maxTokens = 1200
        )

        return if (aiResult is AiResult.Success && aiResult.data.contains("---CODE---")) {
            val text = aiResult.data
            try {
                val name = text.substringAfter("SKILL_NAME:").substringBefore("\n").trim().ifBlank { "Custom Agent Skill" }
                val category = text.substringAfter("CATEGORY:").substringBefore("\n").trim().ifBlank { "Custom Skill" }
                val trigger = text.substringAfter("TRIGGER:").substringBefore("\n").trim().ifBlank { "custom_skill" }
                val desc = text.substringAfter("DESCRIPTION:").substringBefore("---CODE---").trim()
                val code = text.substringAfter("---CODE---").substringBefore("---MARKDOWN---").trim()
                val md = text.substringAfter("---MARKDOWN---").trim()

                val generated = GeneratedSkill(
                    name = name,
                    slug = name.lowercase().replace("[^a-z0-9]+".toRegex(), "-").trim('-'),
                    category = category,
                    description = desc.ifBlank { "Auto-coded skill for $goal" },
                    source = "AutoForge Autonomous Code Synthesis",
                    triggers = listOf(trigger, "execute_$trigger"),
                    language = "python",
                    code = code,
                    skillMarkdown = md
                )
                listOf(generated) + AutoForgeEngine.generateLocalSkills(goal).take(2)
            } catch (_: Exception) {
                AutoForgeEngine.generateLocalSkills(goal)
            }
        } else {
            AutoForgeEngine.generateLocalSkills(goal)
        }
    }

    suspend fun synthesizeMcpsForGoal(goal: String, skills: List<GeneratedSkill>): List<GeneratedMcp> {
        return AutoForgeEngine.generateLocalMcps(goal, skills)
    }
}
