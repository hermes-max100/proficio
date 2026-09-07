package com.aistudio.promptforge.abcd.model

import com.aistudio.promptforge.abcd.util.SecretMasker

/**
 * Maps system exceptions and AI failure types into clear, empathetic user messages
 * paired with a concrete actionable recovery directive: RESUME, RETRY, or EDIT.
 */
object FailureModeMapper {

    fun mapFailure(
        throwable: Throwable? = null,
        appError: AppError? = null,
        currentStep: CheckpointStage = CheckpointStage.PROMPT_SYNTHESIS,
        retryCount: Int = 0,
        maxRetries: Int = 3,
        hasCheckpoint: Boolean = false
    ): ActionableFailure {
        val errorType = appError?.type ?: AiErrorType.UNKNOWN
        val rawMessage = throwable?.message ?: appError?.message ?: "An unexpected execution error occurred"
        val cleanMessage = SecretMasker.sanitize(rawMessage)

        return when {
            // Max retries reached
            retryCount >= maxRetries -> {
                ActionableFailure(
                    title = "Maximum Retries Reached ($retryCount/$maxRetries)",
                    userMessage = "Execution paused after $maxRetries unsuccessful attempts during ${currentStep.title}. Your intermediate artifacts are safely preserved in the checkpoint.",
                    action = if (hasCheckpoint) RecoveryAction.RESUME else RecoveryAction.RETRY,
                    actionLabel = if (hasCheckpoint) "Resume from Checkpoint" else "Retry Step",
                    secondaryActionLabel = "Edit Goal / Parameters",
                    technicalSummary = SecretMasker.sanitize(appError?.technicalDetails ?: cleanMessage),
                    retryCount = retryCount,
                    maxRetries = maxRetries,
                    checkpointStage = currentStep.title
                )
            }

            // Network / Timeout
            errorType == AiErrorType.NETWORK_UNAVAILABLE || errorType == AiErrorType.TIMEOUT -> {
                ActionableFailure(
                    title = "Network Connection Lost",
                    userMessage = "The connection timed out while running ${currentStep.title}. Perficio safely captured your checkpoint so you don't lose progress.",
                    action = if (hasCheckpoint) RecoveryAction.RESUME else RecoveryAction.RETRY,
                    actionLabel = if (hasCheckpoint) "Resume Run" else "Retry Connection",
                    secondaryActionLabel = "Switch to Local Offline Engine",
                    technicalSummary = "Network timeout at ${currentStep.title}",
                    retryCount = retryCount,
                    maxRetries = maxRetries,
                    checkpointStage = currentStep.title
                )
            }

            // Rate limit (HTTP 429)
            errorType == AiErrorType.RATE_LIMIT_EXCEEDED -> {
                ActionableFailure(
                    title = "Provider Rate Limit Reached (429)",
                    userMessage = "The LLM provider requested a cooldown period. Your pipeline is paused at ${currentStep.title} and can be resumed momentarily.",
                    action = RecoveryAction.RESUME,
                    actionLabel = "Resume Pipeline",
                    secondaryActionLabel = "Switch Active Provider",
                    technicalSummary = "HTTP 429 Resource Exhausted",
                    retryCount = retryCount,
                    maxRetries = maxRetries,
                    checkpointStage = currentStep.title
                )
            }

            // Missing / Invalid API Key
            errorType == AiErrorType.API_KEY_MISSING || errorType == AiErrorType.API_KEY_INVALID -> {
                ActionableFailure(
                    title = "Authentication Required",
                    userMessage = "No valid API credential was found. Configure an API key or select an active credential in Provider Settings to continue.",
                    action = RecoveryAction.EDIT,
                    actionLabel = "Configure Provider / Key",
                    secondaryActionLabel = "Run Local Autonomous Engine",
                    technicalSummary = "Missing or unauthorized API credentials",
                    retryCount = retryCount,
                    maxRetries = maxRetries,
                    checkpointStage = currentStep.title
                )
            }

            // Safety blocked
            errorType == AiErrorType.SAFETY_BLOCKED -> {
                ActionableFailure(
                    title = "Content Safety Filter Triggered",
                    userMessage = "The request was flagged by provider safety filters during ${currentStep.title}. Edit your goal wording to proceed.",
                    action = RecoveryAction.EDIT,
                    actionLabel = "Edit Goal Description",
                    secondaryActionLabel = "Retry",
                    technicalSummary = "Safety filter policy applied at ${currentStep.title}",
                    retryCount = retryCount,
                    maxRetries = maxRetries,
                    checkpointStage = currentStep.title
                )
            }

            // Upstream Server Error (HTTP 500, 503)
            errorType == AiErrorType.SERVER_ERROR -> {
                ActionableFailure(
                    title = "Upstream Provider Unavailable",
                    userMessage = "The AI model service returned a temporary server error. Intermediate progress is saved in your run checkpoint.",
                    action = if (hasCheckpoint) RecoveryAction.RESUME else RecoveryAction.RETRY,
                    actionLabel = if (hasCheckpoint) "Resume Pipeline" else "Retry Request",
                    secondaryActionLabel = "Switch to Local Mode",
                    technicalSummary = SecretMasker.sanitize(appError?.technicalDetails ?: "HTTP 5xx server error"),
                    retryCount = retryCount,
                    maxRetries = maxRetries,
                    checkpointStage = currentStep.title
                )
            }

            // Default fallback failure
            else -> {
                ActionableFailure(
                    title = "Pipeline Interrupted",
                    userMessage = "Execution paused during ${currentStep.title}. $cleanMessage. Checkpoint has been preserved.",
                    action = if (hasCheckpoint) RecoveryAction.RESUME else RecoveryAction.RETRY,
                    actionLabel = if (hasCheckpoint) "Resume from Checkpoint" else "Retry",
                    secondaryActionLabel = "Edit Goal",
                    technicalSummary = SecretMasker.sanitize(appError?.technicalDetails ?: cleanMessage),
                    retryCount = retryCount,
                    maxRetries = maxRetries,
                    checkpointStage = currentStep.title
                )
            }
        }
    }
}
