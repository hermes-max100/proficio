package com.aistudio.promptforge.abcd.model

import kotlinx.serialization.Serializable

/**
 * Explicit durable lifecycle states for autonomous Perficio runs.
 */
enum class RunState {
    PENDING,       // Queued or initialized
    RUNNING,       // Actively processing execution steps
    PAUSED,        // Checkpointed and paused (user pause or global pause)
    FAILED,        // Terminated due to error; checkpoint preserved for resume
    CANCELLED,     // Explicitly cancelled by user
    COMPLETED      // Successfully finished all stages
}

fun RunState.isActive(): Boolean = this == RunState.RUNNING || this == RunState.PENDING
fun RunState.canResume(): Boolean = this == RunState.PAUSED || this == RunState.FAILED
fun RunState.canCancel(): Boolean = this == RunState.RUNNING || this == RunState.PENDING || this == RunState.PAUSED
fun RunState.isTerminal(): Boolean = this == RunState.COMPLETED || this == RunState.CANCELLED

/**
 * Sequential execution stages with durable checkpoint progression.
 */
enum class CheckpointStage(val stepIndex: Int, val title: String, val shortName: String) {
    INITIALIZATION(0, "Initialization", "init"),
    PROMPT_SYNTHESIS(1, "Prompt Forge (10/10 Prompt)", "prompt"),
    SKILL_SYNTHESIS(2, "Skill Forge (Auto-Coded Skills)", "skills"),
    PLUGIN_SYNTHESIS(3, "Plugin Forge (MCP Tools)", "mcps"),
    AGENT_ASSEMBLY(4, "Agent Assembly & Spec", "assembly"),
    COMPLETED(5, "Autonomous Execution Complete", "done")
}

/**
 * Concrete actionable recovery directive mapped from failures.
 */
enum class RecoveryAction {
    RESUME,  // Resume execution from saved checkpoint
    RETRY,   // Retry the current step with fresh bounded backoff
    EDIT     // Edit inputs, parameters, or credentials
}

/**
 * Encapsulates intermediate synthesized artifacts safely serializable in Room.
 */
@Serializable
data class RunCheckpointData(
    val promptText: String? = null,
    val skillsJson: String? = null,
    val mcpsJson: String? = null,
    val assembledSpecMarkdown: String? = null,
    val packId: String? = null,
    val executionLatencyMs: Long = 0L,
    val stepTimestamps: Map<String, Long> = emptyMap()
)

/**
 * Mapped actionable error report for user clarity.
 */
@Serializable
data class ActionableFailure(
    val title: String,
    val userMessage: String,
    val action: RecoveryAction,
    val actionLabel: String,
    val secondaryActionLabel: String? = null,
    val technicalSummary: String? = null,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val checkpointStage: String = "Prompt Forge"
)
