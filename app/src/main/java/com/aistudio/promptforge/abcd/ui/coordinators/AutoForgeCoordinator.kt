package com.aistudio.promptforge.abcd.ui.coordinators

import com.aistudio.promptforge.abcd.data.AiResult
import com.aistudio.promptforge.abcd.data.AutoForgeEngine
import com.aistudio.promptforge.abcd.data.AutoForgePack
import com.aistudio.promptforge.abcd.data.DurableRunEntity
import com.aistudio.promptforge.abcd.data.PlaygroundRun
import com.aistudio.promptforge.abcd.data.PromptRepository
import com.aistudio.promptforge.abcd.model.ActionableFailure
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.model.AutoForgePackData
import com.aistudio.promptforge.abcd.model.CheckpointStage
import com.aistudio.promptforge.abcd.model.FailureModeMapper
import com.aistudio.promptforge.abcd.model.GeneratedMcp
import com.aistudio.promptforge.abcd.model.GeneratedSkill
import com.aistudio.promptforge.abcd.model.GoalPreset
import com.aistudio.promptforge.abcd.model.RecoveryAction
import com.aistudio.promptforge.abcd.model.RunCheckpointData
import com.aistudio.promptforge.abcd.model.RunState
import com.aistudio.promptforge.abcd.ui.EngineStage
import com.aistudio.promptforge.abcd.util.RetryPolicy
import com.aistudio.promptforge.abcd.util.SafeLogger
import com.aistudio.promptforge.abcd.util.SecretMasker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Hardened autonomous master coordinator managing:
 * - Durable run execution lifecycle with explicit [RunState]
 * - Checkpoint progression & resumption across stages
 * - Bounded retries with exponential backoff
 * - Idempotency enforcement to prevent duplicate parallel runs
 * - Graceful cancellation & Global Pause Perficio control
 * - Zero secrets in logs, traces, or telemetry
 */
class AutoForgeCoordinator(
    private val repository: PromptRepository,
    private val coroutineScope: CoroutineScope,
    private val onError: (AppError) -> Unit
) {
    private val TAG = "AutoForgeCoordinator"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _goalInput = MutableStateFlow("Build an autonomous market intelligence agent that scours news daily, computes sentiment scores with Gemini, maintains a historical SQLite database, and delivers a Discord briefing.")
    val goalInput: StateFlow<String> = _goalInput.asStateFlow()

    private val _engineStage = MutableStateFlow(EngineStage.IDLE)
    val engineStage: StateFlow<EngineStage> = _engineStage.asStateFlow()

    private val _checkpointStage = MutableStateFlow(CheckpointStage.INITIALIZATION)
    val checkpointStage: StateFlow<CheckpointStage> = _checkpointStage.asStateFlow()

    private val _runState = MutableStateFlow(RunState.PENDING)
    val runState: StateFlow<RunState> = _runState.asStateFlow()

    private val _isEngineRunning = MutableStateFlow(false)
    val isEngineRunning: StateFlow<Boolean> = _isEngineRunning.asStateFlow()

    // Global Pause Perficio Control
    private val _isGlobalPaused = MutableStateFlow(false)
    val isGlobalPaused: StateFlow<Boolean> = _isGlobalPaused.asStateFlow()

    private val _activeDurableRun = MutableStateFlow<DurableRunEntity?>(null)
    val activeDurableRun: StateFlow<DurableRunEntity?> = _activeDurableRun.asStateFlow()

    private val _currentFailure = MutableStateFlow<ActionableFailure?>(null)
    val currentFailure: StateFlow<ActionableFailure?> = _currentFailure.asStateFlow()

    private val _activePack = MutableStateFlow<AutoForgePackData?>(null)
    val activePack: StateFlow<AutoForgePackData?> = _activePack.asStateFlow()

    private val _engineLogs = MutableStateFlow<List<String>>(emptyList())
    val engineLogs: StateFlow<List<String>> = _engineLogs.asStateFlow()

    private var activeJob: Job? = null
    private var lastRunConfig: RunConfig? = null

    data class RunConfig(
        val goal: String,
        val selectedModel: String,
        val onSkillsSynthesized: (List<GeneratedSkill>) -> Unit,
        val onMcpsSynthesized: (List<GeneratedMcp>) -> Unit,
        val onPromptSynthesized: (String) -> Unit
    )

    fun setGoalInput(value: String) {
        _goalInput.value = value
    }

    fun applyGoalPreset(preset: GoalPreset) {
        _goalInput.value = preset.genericGoal
    }

    /**
     * Appends a telemetry log line, strictly sanitizing any embedded secrets or tokens.
     */
    fun addLog(log: String) {
        val sanitized = SecretMasker.sanitize(log)
        _engineLogs.value = _engineLogs.value + sanitized
        SafeLogger.d(TAG, sanitized)
    }

    /**
     * Toggles the Global Pause Perficio control.
     * Pausing immediately freezes the active execution at its current checkpoint.
     */
    fun toggleGlobalPause() {
        setGlobalPause(!_isGlobalPaused.value)
    }

    fun setGlobalPause(paused: Boolean) {
        _isGlobalPaused.value = paused
        if (paused) {
            addLog("⏸️ [Global Pause] Perficio has been paused. Active execution preserved at checkpoint.")
            if (_isEngineRunning.value) {
                pausePipeline("Global Pause engaged")
            }
        } else {
            addLog("▶️ [Global Pause] Perficio resumed. Ready for autonomous execution.")
        }
    }

    /**
     * Generates a deterministic idempotency key for a goal execution.
     */
    fun calculateIdempotencyKey(goal: String, model: String): String {
        val normalized = goal.trim().lowercase().replace("\\s+".toRegex(), " ")
        return "autoforge_${normalized.hashCode()}_${model.hashCode()}"
    }

    /**
     * Initiates the autonomous pipeline with full durable lifecycle support,
     * idempotency checks, bounded retries, and checkpointing.
     */
    fun runAutoForgePipeline(
        goal: String,
        selectedModel: String,
        onSkillsSynthesized: (List<GeneratedSkill>) -> Unit,
        onMcpsSynthesized: (List<GeneratedMcp>) -> Unit,
        onPromptSynthesized: (String) -> Unit,
        forceNew: Boolean = false
    ) {
        val targetGoal = goal.trim().ifBlank { _goalInput.value }
        if (targetGoal.isBlank()) return

        if (_isGlobalPaused.value) {
            addLog("⚠️ Cannot start execution: Global Pause is active. Please resume Perficio first.")
            return
        }

        lastRunConfig = RunConfig(targetGoal, selectedModel, onSkillsSynthesized, onMcpsSynthesized, onPromptSynthesized)
        val idempotencyKey = calculateIdempotencyKey(targetGoal, selectedModel)

        activeJob?.cancel()
        activeJob = coroutineScope.launch {
            // Check idempotency: is there already an active/paused run for this goal?
            if (!forceNew) {
                val existingActive = repository.findActiveRunByIdempotencyKey(idempotencyKey)
                if (existingActive != null) {
                    if (existingActive.state == RunState.PAUSED.name || existingActive.state == RunState.FAILED.name) {
                        addLog("⚡ [Idempotency] Found existing run checkpoint (${existingActive.currentStepName}). Resuming...")
                        resumePipeline(existingActive.id)
                        return@launch
                    } else if (existingActive.state == RunState.RUNNING.name) {
                        addLog("⚡ [Idempotency] Run already executing. Attaching to existing stream.")
                        _activeDurableRun.value = existingActive
                        return@launch
                    }
                }
            }

            executePipelineInternal(
                runId = UUID.randomUUID().toString(),
                idempotencyKey = idempotencyKey,
                targetGoal = targetGoal,
                selectedModel = selectedModel,
                existingCheckpoint = null,
                startStep = 1,
                onSkillsSynthesized = onSkillsSynthesized,
                onMcpsSynthesized = onMcpsSynthesized,
                onPromptSynthesized = onPromptSynthesized
            )
        }
    }

    /**
     * Resumes execution of a paused or checkpointed run.
     */
    fun resumePipeline(runId: String? = null) {
        if (_isGlobalPaused.value) {
            _isGlobalPaused.value = false
            addLog("▶️ [Global Pause] Perficio resumed by user action.")
        }

        val targetRunId = runId ?: _activeDurableRun.value?.id
        val config = lastRunConfig

        activeJob?.cancel()
        activeJob = coroutineScope.launch {
            val entity = if (targetRunId != null) {
                repository.getDurableRunByIdSync(targetRunId)
            } else {
                _activeDurableRun.value
            }

            if (entity == null) {
                if (config != null) {
                    runAutoForgePipeline(
                        goal = config.goal,
                        selectedModel = config.selectedModel,
                        onSkillsSynthesized = config.onSkillsSynthesized,
                        onMcpsSynthesized = config.onMcpsSynthesized,
                        onPromptSynthesized = config.onPromptSynthesized,
                        forceNew = true
                    )
                }
                return@launch
            }

            val checkpoint = try {
                json.decodeFromString<RunCheckpointData>(entity.checkpointDataJson)
            } catch (_: Exception) {
                RunCheckpointData()
            }

            // Restore intermediate artifacts to caller if available
            val promptCallback = config?.onPromptSynthesized ?: {}
            val skillsCallback = config?.onSkillsSynthesized ?: {}
            val mcpsCallback = config?.onMcpsSynthesized ?: {}

            if (!checkpoint.promptText.isNullOrBlank()) {
                promptCallback(checkpoint.promptText)
            }
            if (!checkpoint.skillsJson.isNullOrBlank()) {
                try {
                    val skills = json.decodeFromString<List<GeneratedSkill>>(checkpoint.skillsJson)
                    skillsCallback(skills)
                } catch (_: Exception) {}
            }
            if (!checkpoint.mcpsJson.isNullOrBlank()) {
                try {
                    val mcps = json.decodeFromString<List<GeneratedMcp>>(checkpoint.mcpsJson)
                    mcpsCallback(mcps)
                } catch (_: Exception) {}
            }

            val nextStep = (entity.currentStepIndex + 1).coerceAtMost(4)
            addLog("🔄 [Resume] Resuming run from Checkpoint ${entity.currentStepIndex} (${entity.currentStepName}). Skipping completed stages.")

            executePipelineInternal(
                runId = entity.id,
                idempotencyKey = entity.idempotencyKey,
                targetGoal = entity.goalInput,
                selectedModel = entity.selectedModel,
                existingCheckpoint = checkpoint,
                startStep = nextStep,
                onSkillsSynthesized = skillsCallback,
                onMcpsSynthesized = mcpsCallback,
                onPromptSynthesized = promptCallback
            )
        }
    }

    /**
     * Pauses the currently running pipeline gracefully and saves state in Room.
     */
    fun pausePipeline(reason: String = "User requested pause") {
        val currentRun = _activeDurableRun.value ?: return
        coroutineScope.launch {
            activeJob?.cancel()
            _isEngineRunning.value = false
            _runState.value = RunState.PAUSED
            val updated = currentRun.copy(
                state = RunState.PAUSED.name,
                updatedAt = System.currentTimeMillis()
            )
            _activeDurableRun.value = updated
            repository.insertOrUpdateDurableRun(updated)
            addLog("⏸️ [Perficio Engine] Pipeline execution safely paused: $reason. Checkpoint preserved.")
        }
    }

    /**
     * Cancels the currently running pipeline and records the cancellation state.
     */
    fun cancelPipeline() {
        val currentRun = _activeDurableRun.value
        activeJob?.cancel()
        _isEngineRunning.value = false
        _runState.value = RunState.CANCELLED
        _engineStage.value = EngineStage.IDLE
        _checkpointStage.value = CheckpointStage.INITIALIZATION
        _currentFailure.value = null

        coroutineScope.launch {
            if (currentRun != null) {
                val updated = currentRun.copy(
                    state = RunState.CANCELLED.name,
                    updatedAt = System.currentTimeMillis()
                )
                repository.insertOrUpdateDurableRun(updated)
            }
            addLog("🛑 [Perficio Engine] Pipeline execution cancelled by user.")
        }
    }

    /**
     * Retries the failed step or starts fresh bounded execution.
     */
    fun retryPipeline() {
        _currentFailure.value = null
        val active = _activeDurableRun.value
        if (active != null && active.currentStepIndex > 0) {
            resumePipeline(active.id)
        } else if (lastRunConfig != null) {
            val cfg = lastRunConfig!!
            runAutoForgePipeline(
                goal = cfg.goal,
                selectedModel = cfg.selectedModel,
                onSkillsSynthesized = cfg.onSkillsSynthesized,
                onMcpsSynthesized = cfg.onMcpsSynthesized,
                onPromptSynthesized = cfg.onPromptSynthesized,
                forceNew = true
            )
        }
    }

    /**
     * Internal sequential stage pipeline with explicit checkpoints,
     * bounded retries, and failure mode mapping.
     */
    private suspend fun executePipelineInternal(
        runId: String,
        idempotencyKey: String,
        targetGoal: String,
        selectedModel: String,
        existingCheckpoint: RunCheckpointData?,
        startStep: Int,
        onSkillsSynthesized: (List<GeneratedSkill>) -> Unit,
        onMcpsSynthesized: (List<GeneratedMcp>) -> Unit,
        onPromptSynthesized: (String) -> Unit
    ) {
        _isEngineRunning.value = true
        _runState.value = RunState.RUNNING
        _currentFailure.value = null
        val startTime = System.currentTimeMillis()

        var promptText: String = existingCheckpoint?.promptText ?: ""
        var skillsList: List<GeneratedSkill> = try {
            existingCheckpoint?.skillsJson?.let { json.decodeFromString(it) } ?: emptyList()
        } catch (_: Exception) { emptyList() }
        var mcpsList: List<GeneratedMcp> = try {
            existingCheckpoint?.mcpsJson?.let { json.decodeFromString(it) } ?: emptyList()
        } catch (_: Exception) { emptyList() }

        var currentCheckpointData = existingCheckpoint ?: RunCheckpointData()

        var durableEntity = DurableRunEntity(
            id = runId,
            idempotencyKey = idempotencyKey,
            goalTitle = targetGoal.take(48).ifBlank { "Autonomous Goal Pack" },
            goalInput = targetGoal,
            selectedModel = selectedModel,
            state = RunState.RUNNING.name,
            currentStepIndex = startStep - 1,
            totalSteps = 4,
            currentStepName = if (startStep > 1) "Checkpoint Restored (${startStep - 1}/4)" else "Initialization",
            checkpointDataJson = json.encodeToString(currentCheckpointData),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        _activeDurableRun.value = durableEntity
        repository.insertOrUpdateDurableRun(durableEntity)

        try {
            // Check Global Pause before each step
            checkPauseGate()

            // ----------------------------------------------------
            // STAGE 1: PROMPT FORGE
            // ----------------------------------------------------
            if (startStep <= 1 || promptText.isBlank()) {
                _engineStage.value = EngineStage.PROMPT_FORGING
                _checkpointStage.value = CheckpointStage.PROMPT_SYNTHESIS
                addLog("⚡ [AutoForge Engine] Stage 1/4: Synthesizing 10/10 master prompt with personas & guardrails...")

                promptText = executeStepWithRetry(
                    stepName = "Prompt Forge",
                    maxRetries = 3
                ) { attempt ->
                    delay(300)
                    val result = repository.synthesize10OutOf10Prompt(targetGoal, selectedModel)
                    if (result is AiResult.Success) {
                        result.data
                    } else {
                        AutoForgeEngine.generateLocalPrompt10OutOf10(targetGoal)
                    }
                }

                onPromptSynthesized(promptText)
                currentCheckpointData = currentCheckpointData.copy(promptText = promptText)

                // Persist Checkpoint 1
                durableEntity = durableEntity.copy(
                    currentStepIndex = 1,
                    currentStepName = "Prompt Forge Completed",
                    checkpointDataJson = json.encodeToString(currentCheckpointData),
                    updatedAt = System.currentTimeMillis()
                )
                _activeDurableRun.value = durableEntity
                repository.insertOrUpdateDurableRun(durableEntity)
                addLog("✅ [Checkpoint 1 Saved] 10/10 Prompt forged (${promptText.length} chars). Checkpoint committed.")
            } else {
                addLog("⏭️ [Checkpoint 1 Reused] Using verified prompt from checkpoint (${promptText.length} chars).")
            }

            checkPauseGate()

            // ----------------------------------------------------
            // STAGE 2: SKILL FORGE
            // ----------------------------------------------------
            if (startStep <= 2 || skillsList.isEmpty()) {
                _engineStage.value = EngineStage.SKILL_FORGING
                _checkpointStage.value = CheckpointStage.SKILL_SYNTHESIS
                addLog("🧠 [AutoForge Engine] Stage 2/4: Scouring skill registries & synthesizing custom code...")

                skillsList = executeStepWithRetry(
                    stepName = "Skill Forge",
                    maxRetries = 3
                ) { attempt ->
                    delay(300)
                    repository.synthesizeSkillsForGoal(targetGoal, promptText, selectedModel)
                }

                onSkillsSynthesized(skillsList)
                currentCheckpointData = currentCheckpointData.copy(skillsJson = json.encodeToString(skillsList))

                // Persist Checkpoint 2
                durableEntity = durableEntity.copy(
                    currentStepIndex = 2,
                    currentStepName = "Skill Forge Completed",
                    checkpointDataJson = json.encodeToString(currentCheckpointData),
                    updatedAt = System.currentTimeMillis()
                )
                _activeDurableRun.value = durableEntity
                repository.insertOrUpdateDurableRun(durableEntity)
                addLog("✅ [Checkpoint 2 Saved] Synthesized ${skillsList.size} custom skills including \"${skillsList.firstOrNull()?.name ?: "Custom Skill"}\".")
            } else {
                addLog("⏭️ [Checkpoint 2 Reused] Restored ${skillsList.size} skills from checkpoint.")
            }

            checkPauseGate()

            // ----------------------------------------------------
            // STAGE 3: PLUGIN FORGE (MCPs)
            // ----------------------------------------------------
            if (startStep <= 3 || mcpsList.isEmpty()) {
                _engineStage.value = EngineStage.PLUGIN_FORGING
                _checkpointStage.value = CheckpointStage.PLUGIN_SYNTHESIS
                addLog("🔌 [AutoForge Engine] Stage 3/4: Discovering MCP servers & generating FastMCP endpoints...")

                mcpsList = executeStepWithRetry(
                    stepName = "Plugin Forge",
                    maxRetries = 3
                ) { attempt ->
                    delay(300)
                    repository.synthesizeMcpsForGoal(targetGoal, skillsList)
                }

                onMcpsSynthesized(mcpsList)
                currentCheckpointData = currentCheckpointData.copy(mcpsJson = json.encodeToString(mcpsList))

                // Persist Checkpoint 3
                durableEntity = durableEntity.copy(
                    currentStepIndex = 3,
                    currentStepName = "Plugin Forge Completed",
                    checkpointDataJson = json.encodeToString(currentCheckpointData),
                    updatedAt = System.currentTimeMillis()
                )
                _activeDurableRun.value = durableEntity
                repository.insertOrUpdateDurableRun(durableEntity)
                addLog("✅ [Checkpoint 3 Saved] Configured ${mcpsList.size} MCP servers with ${mcpsList.sumOf { it.tools.size }} executable tool endpoints.")
            } else {
                addLog("⏭️ [Checkpoint 3 Reused] Restored ${mcpsList.size} MCP configurations from checkpoint.")
            }

            checkPauseGate()

            // ----------------------------------------------------
            // STAGE 4: AGENT ASSEMBLY & FINAL SPEC
            // ----------------------------------------------------
            _engineStage.value = EngineStage.ASSEMBLY
            _checkpointStage.value = CheckpointStage.AGENT_ASSEMBLY
            addLog("📦 [AutoForge Engine] Stage 4/4: Compiling complete Autonomous Goal Pack & Agent Spec...")

            val fullSpec = AutoForgeEngine.assembleCompleteSpec(targetGoal, promptText, skillsList, mcpsList)
            val duration = System.currentTimeMillis() - startTime

            val packData = AutoForgePackData(
                id = UUID.randomUUID().toString(),
                goalTitle = targetGoal.take(48).ifBlank { "Autonomous Goal Pack" },
                goalInput = targetGoal,
                taskType = "Autonomous Goal Engine",
                systemRole = "Elite Autonomous Agent",
                prompt10OutOf10 = promptText,
                skills = skillsList,
                mcps = mcpsList,
                fullSpecMarkdown = fullSpec,
                executionLatencyMs = duration,
                createdAt = System.currentTimeMillis()
            )

            currentCheckpointData = currentCheckpointData.copy(
                assembledSpecMarkdown = fullSpec,
                packId = packData.id,
                executionLatencyMs = duration
            )

            // Persist Final Completed State
            durableEntity = durableEntity.copy(
                state = RunState.COMPLETED.name,
                currentStepIndex = 4,
                currentStepName = "Completed",
                checkpointDataJson = json.encodeToString(currentCheckpointData),
                completedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            _activeDurableRun.value = durableEntity
            repository.insertOrUpdateDurableRun(durableEntity)

            _activePack.value = packData
            _engineStage.value = EngineStage.READY
            _checkpointStage.value = CheckpointStage.COMPLETED
            _runState.value = RunState.COMPLETED
            _isEngineRunning.value = false
            addLog("🚀 [AutoForge Engine] Autonomous Goal Engine successfully built in ${duration}ms! Ready to deploy.")

            // Log playground record
            repository.insertPlaygroundRun(
                PlaygroundRun(
                    id = UUID.randomUUID().toString(),
                    forgeType = "AutoForge Engine",
                    input = targetGoal,
                    output = "Goal Pack Created: ${packData.goalTitle} (${skillsList.size} skills, ${mcpsList.size} MCPs)",
                    latencyMs = duration,
                    promptTokens = PromptRepository.estimateTokenCount(targetGoal),
                    outputTokens = PromptRepository.estimateTokenCount(fullSpec),
                    totalTokens = PromptRepository.estimateTokenCount(targetGoal) + PromptRepository.estimateTokenCount(fullSpec)
                )
            )

        } catch (c: CancellationException) {
            SafeLogger.d(TAG, "Pipeline cancelled or paused cleanly: ${c.message}")
        } catch (e: Exception) {
            handlePipelineFailure(e, durableEntity, currentCheckpointData)
        }
    }

    private fun checkPauseGate() {
        if (_isGlobalPaused.value) {
            pausePipeline("Global Pause active")
            throw CancellationException("Execution paused by Global Pause")
        }
    }

    /**
     * Executes a step with bounded retry, exponential backoff, and secret-safe logging.
     */
    private suspend fun <T> executeStepWithRetry(
        stepName: String,
        maxRetries: Int = 3,
        block: suspend (attempt: Int) -> T
    ): T {
        var lastException: Throwable? = null
        for (attempt in 0..maxRetries) {
            try {
                return block(attempt)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                lastException = t
                if (attempt >= maxRetries || !RetryPolicy.isRetryableException(t)) {
                    throw t
                }
                val delayMs = (500L * Math.pow(2.0, attempt.toDouble())).toLong().coerceAtMost(4000L)
                addLog("⚠️ [Bounded Retry] Transient issue in $stepName (Attempt ${attempt + 1}/$maxRetries). Retrying in ${delayMs}ms...")
                delay(delayMs)
            }
        }
        throw lastException ?: IllegalStateException("Retry exhausted in $stepName")
    }

    /**
     * Handles failure gracefully by updating Room entity, mapping the error to an
     * actionable user directive (RESUME, RETRY, EDIT), and preserving the checkpoint.
     */
    private fun handlePipelineFailure(
        e: Exception,
        currentEntity: DurableRunEntity,
        checkpoint: RunCheckpointData
    ) {
        _engineStage.value = EngineStage.ERROR
        _runState.value = RunState.FAILED
        _isEngineRunning.value = false

        val appErr = repository.apiService.classifyError(e)
        val hasCheckpoint = !checkpoint.promptText.isNullOrBlank()
        val actionableFailure = FailureModeMapper.mapFailure(
            throwable = e,
            appError = appErr,
            currentStep = _checkpointStage.value,
            retryCount = currentEntity.retryCount + 1,
            maxRetries = 3,
            hasCheckpoint = hasCheckpoint
        )
        _currentFailure.value = actionableFailure

        val updated = currentEntity.copy(
            state = RunState.FAILED.name,
            lastError = SecretMasker.sanitize(appErr.message),
            lastErrorCode = appErr.httpCode?.toString() ?: appErr.type.name,
            suggestedAction = actionableFailure.action.name,
            retryCount = currentEntity.retryCount + 1,
            updatedAt = System.currentTimeMillis()
        )
        _activeDurableRun.value = updated

        coroutineScope.launch {
            repository.insertOrUpdateDurableRun(updated)
        }

        onError(appErr)
        addLog("❌ [${actionableFailure.title}] ${actionableFailure.userMessage}")
        addLog("💡 Action Directive: [${actionableFailure.actionLabel}]")
    }

    fun saveActivePackToVault(): Boolean {
        val pack = _activePack.value ?: return false
        coroutineScope.launch {
            val entity = AutoForgePack(
                id = pack.id,
                goalTitle = pack.goalTitle,
                goalInput = pack.goalInput,
                taskType = pack.taskType,
                promptText = pack.prompt10OutOf10,
                skillsJson = Json.encodeToString(pack.skills),
                mcpConfigJson = Json.encodeToString(pack.mcps),
                fullSpecMarkdown = pack.fullSpecMarkdown,
                executionLatencyMs = pack.executionLatencyMs,
                createdAt = pack.createdAt,
                updatedAt = System.currentTimeMillis()
            )
            repository.insertAutoForgePack(entity)
        }
        return true
    }

    fun deletePack(id: String) {
        coroutineScope.launch { repository.deleteAutoForgePack(id) }
    }

    fun loadPackIntoEngine(
        pack: AutoForgePack,
        onSkillsLoaded: (List<GeneratedSkill>) -> Unit,
        onMcpsLoaded: (List<GeneratedMcp>) -> Unit,
        onPromptLoaded: (String) -> Unit
    ) {
        _goalInput.value = pack.goalInput
        onPromptLoaded(pack.promptText)
        val loadedSkills = try {
            Json.decodeFromString<List<GeneratedSkill>>(pack.skillsJson)
        } catch (_: Exception) {
            emptyList()
        }
        val loadedMcps = try {
            Json.decodeFromString<List<GeneratedMcp>>(pack.mcpConfigJson)
        } catch (_: Exception) {
            emptyList()
        }
        onSkillsLoaded(loadedSkills)
        onMcpsLoaded(loadedMcps)
        _activePack.value = AutoForgePackData(
            id = pack.id,
            goalTitle = pack.goalTitle,
            goalInput = pack.goalInput,
            taskType = pack.taskType,
            systemRole = "Elite Autonomous Agent",
            prompt10OutOf10 = pack.promptText,
            skills = loadedSkills,
            mcps = loadedMcps,
            fullSpecMarkdown = pack.fullSpecMarkdown,
            executionLatencyMs = pack.executionLatencyMs,
            createdAt = pack.createdAt
        )
        _engineStage.value = EngineStage.READY
        _runState.value = RunState.COMPLETED
    }
}
