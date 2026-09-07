package com.aistudio.promptforge.abcd.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aistudio.promptforge.abcd.api.ApiHealthStatus
import com.aistudio.promptforge.abcd.api.SupportedModels
import com.aistudio.promptforge.abcd.data.AutoForgePack
import com.aistudio.promptforge.abcd.data.GenerationMetrics
import com.aistudio.promptforge.abcd.data.PlaygroundRun
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.data.PromptStat
import com.aistudio.promptforge.abcd.data.SavedMcp
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.api.ConnectionTestResult
import com.aistudio.promptforge.abcd.data.LlmCredentialEntity
import com.aistudio.promptforge.abcd.model.ExecutionProvenanceRecord
import com.aistudio.promptforge.abcd.model.ProvenanceStatus
import com.aistudio.promptforge.abcd.util.ImportResult
import java.util.UUID
import com.aistudio.promptforge.abcd.data.SavedSkill
import com.aistudio.promptforge.abcd.data.DurableRunEntity
import com.aistudio.promptforge.abcd.model.ActionableFailure
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.model.AutoForgePackData
import com.aistudio.promptforge.abcd.model.CheckpointStage
import com.aistudio.promptforge.abcd.model.GeneratedMcp
import com.aistudio.promptforge.abcd.model.GeneratedSkill
import com.aistudio.promptforge.abcd.model.GoalPreset
import com.aistudio.promptforge.abcd.model.RecoveryAction
import com.aistudio.promptforge.abcd.model.RepoPromptItem
import com.aistudio.promptforge.abcd.model.RunState
import com.aistudio.promptforge.abcd.ui.coordinators.AutoForgeCoordinator
import com.aistudio.promptforge.abcd.ui.coordinators.InteractiveRunnerCoordinator
import com.aistudio.promptforge.abcd.ui.coordinators.PromptForgeCoordinator
import com.aistudio.promptforge.abcd.ui.coordinators.SkillPluginCoordinator
import com.aistudio.promptforge.abcd.ui.theme.ThemeManager
import com.aistudio.promptforge.abcd.util.RetryPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class EngineStage(val stepIndex: Int, val title: String, val description: String) {
    IDLE(0, "Ready", "Enter any goal or task to start AutoForge engine"),
    PROMPT_FORGING(1, "Prompt Forge", "Crafting 10/10 master prompt with personas & guardrails..."),
    SKILL_FORGING(2, "Skill Forge", "Scouring web/forums & autonomous coding of custom skills..."),
    PLUGIN_FORGING(3, "Plugin Forge", "Discovering MCPs & coding FastMCP Python/TypeScript tools..."),
    ASSEMBLY(4, "Agent Assembly", "Compiling autonomous Goal Execution Pack & verification test..."),
    READY(5, "Autonomous Engine Ready", "Full Agent Specification & MCP Pack synthesized successfully!"),
    ERROR(0, "Error", "Engine pipeline encountered an error")
}

/**
 * Lean architectural mediator coordinating domain-specific coordinators:
 * - [AutoForgeCoordinator]: Autonomous engine stages, execution, packs, and logs
 * - [PromptForgeCoordinator]: 10/10 prompt crafting, frameworks, and metrics
 * - [SkillPluginCoordinator]: Web/forum scouring, FastMCP & skill code generation
 * - [InteractiveRunnerCoordinator]: Live AI prompt execution, filtering, provenance
 */
class MainViewModel(
    val repository: PromptRepository,
    val themeManager: ThemeManager? = null
) : ViewModel() {

    // ==========================================
    // PERSISTED VAULT FLOWS
    // ==========================================
    val savedPacks: StateFlow<List<AutoForgePack>> = repository.getAutoForgePacks()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedSkills: StateFlow<List<SavedSkill>> = repository.getSavedSkills()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedMcps: StateFlow<List<SavedMcp>> = repository.getSavedMcps()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val savedPrompts: StateFlow<List<SavedPrompt>> = repository.getSavedPrompts()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playgroundRuns: StateFlow<List<PlaygroundRun>> = repository.getPlaygroundRuns()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val llmCredentials: StateFlow<List<LlmCredentialEntity>> = repository.getAllLlmCredentials()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeLlmCredential: StateFlow<LlmCredentialEntity?> = repository.getActiveLlmCredential()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val executionHistory: StateFlow<List<ExecutionProvenanceRecord>> = repository.provenanceRepository.getAllProvenance()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ==========================================
    // ERROR HANDLING, RETRY & API STATUS
    // ==========================================
    private val _currentError = MutableStateFlow<AppError?>(null)
    val currentError: StateFlow<AppError?> = _currentError.asStateFlow()

    private val _apiHealthStatus = MutableStateFlow<ApiHealthStatus?>(null)
    val apiHealthStatus: StateFlow<ApiHealthStatus?> = _apiHealthStatus.asStateFlow()

    private val _selectedModel = MutableStateFlow(SupportedModels.FLASH_LATEST)
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isTestingApi = MutableStateFlow(false)
    val isTestingApi: StateFlow<Boolean> = _isTestingApi.asStateFlow()

    val isApiKeyConfigured: Boolean get() = repository.isApiKeyConfigured()

    private val _customApiKey = MutableStateFlow(repository.apiService.customApiKey)
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private var lastAction: (() -> Unit)? = null

    // ==========================================
    // DOMAIN COORDINATORS
    // ==========================================
    val autoForgeCoordinator = AutoForgeCoordinator(
        repository = repository,
        coroutineScope = viewModelScope,
        onError = { setError(it) }
    )

    val promptForgeCoordinator = PromptForgeCoordinator(
        repository = repository,
        coroutineScope = viewModelScope,
        onError = { setError(it) }
    )

    val skillPluginCoordinator = SkillPluginCoordinator(
        repository = repository,
        coroutineScope = viewModelScope,
        onError = { setError(it) }
    )

    val runnerCoordinator = InteractiveRunnerCoordinator(
        repository = repository,
        coroutineScope = viewModelScope,
        savedPrompts = savedPrompts
    )

    // ==========================================
    // DELEGATED AUTO FORGE FLOWS & DURABLE RUNS
    // ==========================================
    val goalInput: StateFlow<String> = autoForgeCoordinator.goalInput
    val engineStage: StateFlow<EngineStage> = autoForgeCoordinator.engineStage
    val isEngineRunning: StateFlow<Boolean> = autoForgeCoordinator.isEngineRunning
    val activePack: StateFlow<AutoForgePackData?> = autoForgeCoordinator.activePack
    val engineLogs: StateFlow<List<String>> = autoForgeCoordinator.engineLogs

    val durableRuns: StateFlow<List<DurableRunEntity>> = repository.getAllDurableRuns()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activeDurableRun: StateFlow<DurableRunEntity?> = autoForgeCoordinator.activeDurableRun
    val runState: StateFlow<RunState> = autoForgeCoordinator.runState
    val checkpointStage: StateFlow<CheckpointStage> = autoForgeCoordinator.checkpointStage
    val isGlobalPaused: StateFlow<Boolean> = autoForgeCoordinator.isGlobalPaused
    val currentFailure: StateFlow<ActionableFailure?> = autoForgeCoordinator.currentFailure

    // ==========================================
    // DELEGATED PROMPT FORGE FLOWS
    // ==========================================
    val promptForgeGoal: StateFlow<String> = promptForgeCoordinator.promptForgeGoal
    val prompt10OutOf10: StateFlow<String> = promptForgeCoordinator.prompt10OutOf10
    val promptMetrics: StateFlow<GenerationMetrics?> = promptForgeCoordinator.promptMetrics
    val isPromptBusy: StateFlow<Boolean> = promptForgeCoordinator.isPromptBusy
    val promptFramework: StateFlow<String> = promptForgeCoordinator.promptFramework

    // ==========================================
    // DELEGATED SKILL & MCP FLOWS
    // ==========================================
    val skillForgeQuery: StateFlow<String> = skillPluginCoordinator.skillForgeQuery
    val isSkillBusy: StateFlow<Boolean> = skillPluginCoordinator.isSkillBusy
    val currentSkills: StateFlow<List<GeneratedSkill>> = skillPluginCoordinator.currentSkills
    val skillScourStatus: StateFlow<String> = skillPluginCoordinator.skillScourStatus
    val mcpForgeQuery: StateFlow<String> = skillPluginCoordinator.mcpForgeQuery
    val isMcpBusy: StateFlow<Boolean> = skillPluginCoordinator.isMcpBusy
    val currentMcps: StateFlow<List<GeneratedMcp>> = skillPluginCoordinator.currentMcps

    // ==========================================
    // DELEGATED RUNNER & REPOSITORY FLOWS
    // ==========================================
    val repoSearchQuery: StateFlow<String> = runnerCoordinator.repoSearchQuery
    val repoSelectedCategory: StateFlow<String> = runnerCoordinator.repoSelectedCategory
    val repoSelectedModelFilter: StateFlow<String> = runnerCoordinator.repoSelectedModelFilter
    val favoritePromptIds: StateFlow<Set<String>> = runnerCoordinator.favoritePromptIds
    val promptStatsMap: StateFlow<Map<String, PromptStat>> = runnerCoordinator.promptStatsMap
    val isExecutingPrompt: StateFlow<Boolean> = runnerCoordinator.isExecutingPrompt
    val promptExecutionOutput: StateFlow<String> = runnerCoordinator.promptExecutionOutput
    val promptExecutionMetrics: StateFlow<GenerationMetrics?> = runnerCoordinator.promptExecutionMetrics
    val promptExecutionNotice: StateFlow<AppError?> = runnerCoordinator.promptExecutionNotice
    val filteredRepoPrompts: StateFlow<List<RepoPromptItem>> = runnerCoordinator.filteredRepoPrompts

    init {
        promptForgeCoordinator.setPromptForgeGoal(autoForgeCoordinator.goalInput.value)
    }

    // ==========================================
    // ERROR RECOVERY & RETRY ACTIONS
    // ==========================================
    fun clearError() {
        _currentError.value = null
    }

    fun setError(error: AppError) {
        _currentError.value = error
    }

    fun setSelectedModel(model: String) {
        _selectedModel.value = model
    }

    fun retryLastAction() {
        val action = lastAction
        _currentError.value = null
        action?.invoke()
    }

    fun testApiConnection() {
        _isTestingApi.value = true
        viewModelScope.launch {
            val status = repository.testApiHealth(_selectedModel.value)
            _apiHealthStatus.value = status
            _isTestingApi.value = false
            if (!status.isHealthy && status.error != null) {
                _currentError.value = status.error
            }
        }
    }

    // ==========================================
    // AUTO FORGE PIPELINE METHODS
    // ==========================================
    fun setGoalInput(value: String) = autoForgeCoordinator.setGoalInput(value)

    fun applyGoalPreset(preset: GoalPreset) {
        autoForgeCoordinator.applyGoalPreset(preset)
        promptForgeCoordinator.setPromptForgeGoal(preset.genericGoal)
    }

    fun runAutoForgePipeline(goal: String) {
        lastAction = { runAutoForgePipeline(goal) }
        _currentError.value = null
        autoForgeCoordinator.runAutoForgePipeline(
            goal = goal,
            selectedModel = _selectedModel.value,
            onSkillsSynthesized = { skillPluginCoordinator.setSkills(it) },
            onMcpsSynthesized = { skillPluginCoordinator.setMcps(it) },
            onPromptSynthesized = { promptForgeCoordinator.setPrompt10OutOf10(it) }
        )
    }

    fun toggleGlobalPause() = autoForgeCoordinator.toggleGlobalPause()

    fun setGlobalPause(paused: Boolean) = autoForgeCoordinator.setGlobalPause(paused)

    fun pauseEngine(reason: String = "User paused") = autoForgeCoordinator.pausePipeline(reason)

    fun resumeEngine(runId: String? = null) = autoForgeCoordinator.resumePipeline(runId)

    fun cancelEngine() = autoForgeCoordinator.cancelPipeline()

    fun retryEngine() = autoForgeCoordinator.retryPipeline()

    fun saveActivePackToVault(): Boolean = autoForgeCoordinator.saveActivePackToVault()

    fun deletePack(id: String) = autoForgeCoordinator.deletePack(id)

    fun loadPackIntoEngine(pack: AutoForgePack) {
        autoForgeCoordinator.loadPackIntoEngine(
            pack = pack,
            onSkillsLoaded = { skillPluginCoordinator.setSkills(it) },
            onMcpsLoaded = { skillPluginCoordinator.setMcps(it) },
            onPromptLoaded = { promptForgeCoordinator.setPrompt10OutOf10(it) }
        )
    }

    // ==========================================
    // PROMPT FORGE STANDALONE METHODS
    // ==========================================
    fun setPromptForgeGoal(value: String) = promptForgeCoordinator.setPromptForgeGoal(value)

    fun setPrompt10OutOf10(value: String) = promptForgeCoordinator.setPrompt10OutOf10(value)

    fun forge10OutOf10Prompt(goal: String) {
        lastAction = { forge10OutOf10Prompt(goal) }
        _currentError.value = null
        promptForgeCoordinator.forge10OutOf10Prompt(goal, _selectedModel.value)
    }

    fun savePromptToVault(title: String, promptText: String) =
        promptForgeCoordinator.savePromptToVault(title, promptText)

    fun loadSavedPromptIntoForge(prompt: SavedPrompt) {
        promptForgeCoordinator.loadSavedPrompt(prompt)
        _currentError.value = null
    }

    // ==========================================
    // SKILL FORGE STANDALONE METHODS
    // ==========================================
    fun setSkillForgeQuery(value: String) = skillPluginCoordinator.setSkillForgeQuery(value)

    fun scourAndCodeSkills(query: String) {
        lastAction = { scourAndCodeSkills(query) }
        _currentError.value = null
        skillPluginCoordinator.scourAndCodeSkills(query, _selectedModel.value)
    }

    fun saveSkillToVault(skill: GeneratedSkill) = skillPluginCoordinator.saveSkillToVault(skill)

    // ==========================================
    // PLUGIN FORGE STANDALONE METHODS
    // ==========================================
    fun setMcpForgeQuery(value: String) = skillPluginCoordinator.setMcpForgeQuery(value)

    fun synthesizeMcps(query: String) {
        lastAction = { synthesizeMcps(query) }
        _currentError.value = null
        skillPluginCoordinator.synthesizeMcps(query)
    }

    fun saveMcpToVault(mcp: GeneratedMcp) = skillPluginCoordinator.saveMcpToVault(mcp)

    // ==========================================
    // PROMPT REPOSITORY & INTERACTIVE RUNNER
    // ==========================================
    fun setRepoSearchQuery(query: String) = runnerCoordinator.setSearchQuery(query)

    fun setRepoSelectedCategory(category: String) = runnerCoordinator.setSelectedCategory(category)

    fun setRepoSelectedModelFilter(model: String) = runnerCoordinator.setSelectedModelFilter(model)

    fun updateCustomApiKey(key: String) {
        _customApiKey.value = key
        repository.setCustomApiKey(key)
    }

    fun executePromptWithGemini(
        promptText: String,
        systemInstruction: String? = null,
        model: String = _selectedModel.value,
        temperature: Float = 0.4f,
        maxTokens: Int = 1500,
        promptId: String? = null,
        promptTitle: String? = null,
        appliedVariables: Map<String, String> = emptyMap()
    ) {
        lastAction = {
            executePromptWithGemini(
                promptText = promptText,
                systemInstruction = systemInstruction,
                model = model,
                temperature = temperature,
                maxTokens = maxTokens,
                promptId = promptId,
                promptTitle = promptTitle,
                appliedVariables = appliedVariables
            )
        }
        _currentError.value = null
        runnerCoordinator.executePromptWithAi(
            promptText = promptText,
            systemInstruction = systemInstruction,
            model = model,
            temperature = temperature,
            maxTokens = maxTokens,
            promptId = promptId,
            promptTitle = promptTitle,
            appliedVariables = appliedVariables
        )
    }

    fun toggleFavoritePrompt(promptId: String) = runnerCoordinator.toggleFavoritePrompt(promptId)

    fun recordPromptCopy(promptId: String) = runnerCoordinator.recordPromptCopy(promptId)

    fun recordPromptShare(promptId: String) = runnerCoordinator.recordPromptShare(promptId)

    fun clearPromptExecution() = runnerCoordinator.clearPromptExecution()

    fun createCustomRepoPrompt(
        title: String,
        category: String,
        framework: String,
        templateText: String
    ) = runnerCoordinator.createCustomRepoPrompt(title, framework, templateText)

    // ==========================================
    // LLM CREDENTIALS & BYOK / OAUTH
    // ==========================================
    fun saveLlmCredential(credential: LlmCredentialEntity) = viewModelScope.launch {
        repository.saveLlmCredential(credential)
    }

    fun setActiveLlmCredential(id: String) = viewModelScope.launch {
        repository.setActiveLlmCredential(id)
    }

    fun deleteLlmCredential(id: String) = viewModelScope.launch {
        repository.deleteLlmCredential(id)
    }

    suspend fun testLlmCredential(credential: LlmCredentialEntity): ConnectionTestResult {
        return repository.testLlmCredential(credential)
    }

    // ==========================================
    // EXECUTION PROVENANCE & RUN HISTORY
    // ==========================================
    fun deleteExecutionProvenance(id: String) = viewModelScope.launch {
        repository.provenanceRepository.deleteProvenanceById(id)
    }

    fun clearAllExecutionHistory() = viewModelScope.launch {
        repository.provenanceRepository.clearAllProvenance()
    }

    // ==========================================
    // SINGLE ITEM IMPORT INTO ROOM DATABASE
    // ==========================================
    suspend fun importSinglePrompt(
        title: String,
        framework: String,
        templateText: String
    ) {
        val newPrompt = SavedPrompt(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Imported Prompt" },
            frameworkId = framework.ifBlank { "Custom" },
            fieldsJson = "{}",
            assembled = templateText,
            system = "You are a professional AI assistant.",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        repository.insertSavedPrompt(newPrompt)
    }

    suspend fun importSingleSkill(
        title: String,
        slug: String,
        category: String,
        trigger: String,
        code: String,
        markdown: String
    ) {
        val newSkill = SavedSkill(
            id = UUID.randomUUID().toString(),
            title = title.ifBlank { "Imported Skill" },
            slug = slug.ifBlank { title.lowercase().replace("\\s+".toRegex(), "-") },
            category = category.ifBlank { "Custom" },
            description = "Imported agent skill for $title",
            trigger = trigger.ifBlank { "custom, agent, workflow" },
            source = "Imported",
            implementationCode = code,
            skillMarkdown = markdown.ifBlank { "# $title\nImported custom agent skill." },
            createdAt = System.currentTimeMillis()
        )
        repository.insertSavedSkill(newSkill)
    }

    suspend fun importSingleMcp(
        name: String,
        category: String,
        description: String,
        configJson: String,
        code: String
    ) {
        val newMcp = SavedMcp(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Imported-MCP-Server" },
            category = category.ifBlank { "Custom" },
            description = description.ifBlank { "Imported FastMCP server tool" },
            toolsCount = 1,
            mcpJsonConfig = configJson.ifBlank { "{}" },
            serverCode = code,
            createdAt = System.currentTimeMillis()
        )
        repository.insertSavedMcp(newMcp)
    }

    suspend fun importSinglePack(pack: AutoForgePack) {
        repository.insertAutoForgePack(pack)
    }

    suspend fun importSingleHistoryRun(
        promptTitle: String,
        model: String,
        promptText: String,
        outputText: String,
        latencyMs: Long,
        tokensPrompt: Int,
        tokensOutput: Int
    ) {
        repository.provenanceRepository.recordRun(
            promptId = UUID.randomUUID().toString(),
            promptTitle = promptTitle.ifBlank { "Imported Execution" },
            selectedModel = model.ifBlank { SupportedModels.FLASH_LATEST },
            temperature = 0.4f,
            maxTokens = 1500,
            latencyMs = latencyMs,
            tokensPrompt = tokensPrompt,
            tokensOutput = tokensOutput,
            sanitizedOutput = outputText,
            rawOutput = outputText,
            resolvedVariables = emptyMap(),
            status = ProvenanceStatus.SUCCESS
        )
    }

    suspend fun importRawBundle(jsonString: String): ImportResult {
        return repository.vaultDataRepository.importBundle(jsonString)
    }

    // ==========================================
    // COMMON UTILITIES & PERSISTENCE HELPERS
    // ==========================================
    fun estimateTokens(text: String): Int = PromptRepository.estimateTokenCount(text)

    fun deleteSavedSkill(id: String) = viewModelScope.launch { repository.deleteSavedSkill(id) }

    fun deleteSavedMcp(id: String) = viewModelScope.launch { repository.deleteSavedMcp(id) }

    fun deleteSavedPrompt(id: String) = viewModelScope.launch { repository.deleteSavedPrompt(id) }

    fun clearRuns() = viewModelScope.launch { repository.clearPlaygroundRuns() }
}

class MainViewModelFactory(
    private val repository: PromptRepository,
    private val themeManager: ThemeManager? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, themeManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
