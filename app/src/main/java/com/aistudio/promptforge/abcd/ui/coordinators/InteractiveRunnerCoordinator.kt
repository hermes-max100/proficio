package com.aistudio.promptforge.abcd.ui.coordinators

import com.aistudio.promptforge.abcd.api.SupportedModels
import com.aistudio.promptforge.abcd.data.AiResult
import com.aistudio.promptforge.abcd.data.GenerationMetrics
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.data.PromptStat
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.model.CURATED_PROMPT_REPOSITORY
import com.aistudio.promptforge.abcd.model.PromptRepositoryCategories
import com.aistudio.promptforge.abcd.model.ProvenanceStatus
import com.aistudio.promptforge.abcd.model.RepoPromptItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Modular coordinator managing interactive AI prompt execution,
 * dynamic search/filtering across curated and custom prompts,
 * analytics stats, and execution provenance logging.
 */
class InteractiveRunnerCoordinator(
    private val repository: PromptRepository,
    private val coroutineScope: CoroutineScope,
    savedPrompts: StateFlow<List<SavedPrompt>>
) {
    private val _repoSearchQuery = MutableStateFlow("")
    val repoSearchQuery: StateFlow<String> = _repoSearchQuery.asStateFlow()

    private val _repoSelectedCategory = MutableStateFlow(PromptRepositoryCategories.ALL)
    val repoSelectedCategory: StateFlow<String> = _repoSelectedCategory.asStateFlow()

    private val _repoSelectedModelFilter = MutableStateFlow("All")
    val repoSelectedModelFilter: StateFlow<String> = _repoSelectedModelFilter.asStateFlow()

    val favoritePromptIds: StateFlow<Set<String>> = repository.getFavoritePromptIds()
        .map { it.toSet() }
        .stateIn(coroutineScope, SharingStarted.Lazily, emptySet())

    val promptStatsMap: StateFlow<Map<String, PromptStat>> = repository.getAllPromptStats()
        .map { list -> list.associateBy { it.promptId } }
        .stateIn(coroutineScope, SharingStarted.Lazily, emptyMap())

    private val _isExecutingPrompt = MutableStateFlow(false)
    val isExecutingPrompt: StateFlow<Boolean> = _isExecutingPrompt.asStateFlow()

    private val _promptExecutionOutput = MutableStateFlow("")
    val promptExecutionOutput: StateFlow<String> = _promptExecutionOutput.asStateFlow()

    private val _promptExecutionMetrics = MutableStateFlow<GenerationMetrics?>(null)
    val promptExecutionMetrics: StateFlow<GenerationMetrics?> = _promptExecutionMetrics.asStateFlow()

    private val _promptExecutionNotice = MutableStateFlow<AppError?>(null)
    val promptExecutionNotice: StateFlow<AppError?> = _promptExecutionNotice.asStateFlow()

    val filteredRepoPrompts: StateFlow<List<RepoPromptItem>> = combine(
        savedPrompts,
        _repoSearchQuery,
        _repoSelectedCategory,
        _repoSelectedModelFilter,
        favoritePromptIds
    ) { saved, query, category, modelFilter, favorites ->
        val customItems = saved.map { sp ->
            RepoPromptItem(
                id = sp.id,
                title = sp.title,
                category = PromptRepositoryCategories.SAVED_CUSTOM,
                framework = sp.frameworkId.ifBlank { "Custom" },
                recommendedModel = SupportedModels.FLASH_LATEST,
                description = "Saved custom prompt (${sp.assembled.take(60)}...)",
                promptTemplate = sp.assembled,
                tags = listOf("#saved", "#custom"),
                isCustom = true,
                isFavorite = favorites.contains(sp.id),
                variables = emptyList()
            )
        }

        val allItems = CURATED_PROMPT_REPOSITORY.map { item ->
            item.copy(isFavorite = favorites.contains(item.id))
        } + customItems

        val cleanQuery = query.trim().lowercase()

        allItems.filter { item ->
            val matchesCategory = when (category) {
                PromptRepositoryCategories.ALL -> true
                PromptRepositoryCategories.SAVED_CUSTOM -> item.isCustom
                "Favorites" -> item.isFavorite
                else -> item.category.equals(category, ignoreCase = true)
            }

            val matchesModel = if (modelFilter == "All") true else item.recommendedModel == modelFilter

            val matchesSearch = if (cleanQuery.isBlank()) {
                true
            } else {
                item.title.lowercase().contains(cleanQuery) ||
                item.description.lowercase().contains(cleanQuery) ||
                item.promptTemplate.lowercase().contains(cleanQuery) ||
                item.framework.lowercase().contains(cleanQuery) ||
                item.tags.any { it.lowercase().contains(cleanQuery) }
            }

            matchesCategory && matchesModel && matchesSearch
        }
    }.stateIn(coroutineScope, SharingStarted.Lazily, CURATED_PROMPT_REPOSITORY)

    fun setSearchQuery(query: String) {
        _repoSearchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _repoSelectedCategory.value = category
    }

    fun setSelectedModelFilter(model: String) {
        _repoSelectedModelFilter.value = model
    }

    fun executePromptWithAi(
        promptText: String,
        systemInstruction: String? = null,
        model: String = SupportedModels.FLASH_LATEST,
        temperature: Float = 0.4f,
        maxTokens: Int = 1500,
        promptId: String? = null,
        promptTitle: String? = null,
        appliedVariables: Map<String, String> = emptyMap()
    ) {
        if (promptText.isBlank()) return
        _isExecutingPrompt.value = true
        _promptExecutionOutput.value = ""
        _promptExecutionMetrics.value = null
        _promptExecutionNotice.value = null

        coroutineScope.launch {
            val result = repository.generateComplete(
                system = systemInstruction,
                user = promptText,
                temperature = temperature,
                maxTokens = maxTokens,
                model = model
            )

            _isExecutingPrompt.value = false
            when (result) {
                is AiResult.Success -> {
                    _promptExecutionOutput.value = result.data
                    _promptExecutionMetrics.value = result.metrics
                    if (result.notice != null) {
                        _promptExecutionNotice.value = result.notice
                    }
                    if (promptId != null) {
                        repository.recordPromptExecution(promptId, result.metrics.latencyMs)
                    }
                    val effectiveId = promptId ?: UUID.randomUUID().toString()
                    val effectiveTitle = promptTitle?.ifBlank { null }
                        ?: promptText.take(40).replace("\n", " ").trim()
                    repository.provenanceRepository.recordRun(
                        promptId = effectiveId,
                        promptTitle = effectiveTitle,
                        selectedModel = model,
                        temperature = temperature,
                        maxTokens = maxTokens,
                        latencyMs = result.metrics.latencyMs,
                        tokensPrompt = result.metrics.promptTokens,
                        tokensOutput = result.metrics.outputTokens,
                        sanitizedOutput = result.data,
                        rawOutput = result.data,
                        resolvedVariables = appliedVariables,
                        status = if (result.isFallback) ProvenanceStatus.FALLBACK_LOCAL else ProvenanceStatus.SUCCESS
                    )
                }
                is AiResult.Error -> {
                    _promptExecutionOutput.value = "Error executing prompt: ${result.message}"
                    _promptExecutionNotice.value = result.appError ?: AppError.generic(result.message)
                    val effectiveId = promptId ?: UUID.randomUUID().toString()
                    val effectiveTitle = promptTitle?.ifBlank { null }
                        ?: promptText.take(40).replace("\n", " ").trim()
                    repository.provenanceRepository.recordRun(
                        promptId = effectiveId,
                        promptTitle = effectiveTitle,
                        selectedModel = model,
                        temperature = temperature,
                        maxTokens = maxTokens,
                        latencyMs = 0,
                        tokensPrompt = 0,
                        tokensOutput = 0,
                        sanitizedOutput = result.message,
                        rawOutput = result.message,
                        resolvedVariables = appliedVariables,
                        status = ProvenanceStatus.FAILED,
                        errorReason = result.message
                    )
                }
            }
        }
    }

    fun toggleFavoritePrompt(promptId: String) {
        coroutineScope.launch { repository.toggleFavorite(promptId) }
    }

    fun recordPromptCopy(promptId: String) {
        coroutineScope.launch { repository.recordPromptCopy(promptId) }
    }

    fun recordPromptShare(promptId: String) {
        coroutineScope.launch { repository.recordPromptShare(promptId) }
    }

    fun clearPromptExecution() {
        _promptExecutionOutput.value = ""
        _promptExecutionMetrics.value = null
        _promptExecutionNotice.value = null
    }

    fun createCustomRepoPrompt(
        title: String,
        framework: String,
        templateText: String
    ) {
        coroutineScope.launch {
            repository.insertSavedPrompt(
                SavedPrompt(
                    id = UUID.randomUUID().toString(),
                    title = title.ifBlank { "Custom Prompt: " + templateText.take(24) },
                    frameworkId = framework.ifBlank { "Custom" },
                    fieldsJson = "{}",
                    assembled = templateText,
                    system = "AutoForge Prompt Repository",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
