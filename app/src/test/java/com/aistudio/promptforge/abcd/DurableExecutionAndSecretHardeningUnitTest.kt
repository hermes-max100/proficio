package com.aistudio.promptforge.abcd

import com.aistudio.promptforge.abcd.data.DurableRunEntity
import com.aistudio.promptforge.abcd.data.LlmCredentialEntity
import com.aistudio.promptforge.abcd.model.ActionableFailure
import com.aistudio.promptforge.abcd.model.AppError
import com.aistudio.promptforge.abcd.model.CheckpointStage
import com.aistudio.promptforge.abcd.model.FailureModeMapper
import com.aistudio.promptforge.abcd.model.RecoveryAction
import com.aistudio.promptforge.abcd.model.RunState
import com.aistudio.promptforge.abcd.model.canCancel
import com.aistudio.promptforge.abcd.model.canResume
import com.aistudio.promptforge.abcd.model.isActive
import com.aistudio.promptforge.abcd.model.isTerminal
import com.aistudio.promptforge.abcd.util.SafeLogger
import com.aistudio.promptforge.abcd.util.SecretMasker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class DurableExecutionAndSecretHardeningUnitTest {

    // =========================================================================
    // 1. SECRET MASKING & ZERO-LEAKAGE TESTS
    // =========================================================================

    @Test
    fun testGeminiApiKeyMasking() {
        val rawGeminiKey = "AIzaSyD9876543210abcdefghijklmnopq123"
        val sanitized = SecretMasker.sanitize(rawGeminiKey)

        assertFalse("Sanitized output must not contain full raw key", sanitized.contains(rawGeminiKey))
        assertTrue("Sanitized key should preserve AIza prefix", sanitized.startsWith("AIza••••"))
        assertTrue("Sanitized key should preserve last 4 chars", sanitized.endsWith(rawGeminiKey.takeLast(4)))
    }

    @Test
    fun testOpenAiApiKeyMasking() {
        val rawOpenAiKey = "sk-proj-1234567890abcdefghijklmnopqrstuvwxyz"
        val sanitized = SecretMasker.sanitize(rawOpenAiKey)

        assertFalse("Sanitized output must not contain full raw key", sanitized.contains(rawOpenAiKey))
        assertTrue("Sanitized key should preserve sk- prefix", sanitized.startsWith("sk-••••"))
        assertTrue("Sanitized key should preserve last 4 chars", sanitized.endsWith(rawOpenAiKey.takeLast(4)))
    }

    @Test
    fun testJsonPayloadSecretSanitization() {
        val sensitiveJson = """{"apiKey":"AIzaSySecretApiKey123456789012345678","credentialValue":"sk-proj-TopSecretVal12345"}"""
        val sanitized = SecretMasker.sanitize(sensitiveJson)

        assertFalse("Sanitized string must not leak Gemini key", sanitized.contains("AIzaSySecretApiKey123456789012345678"))
        assertFalse("Sanitized string must not leak OpenAI key", sanitized.contains("sk-proj-TopSecretVal12345"))
        assertTrue("Sanitized string must have bullets", sanitized.contains("••••"))
    }

    @Test
    fun testAuthorizationHeaderSanitization() {
        val headerLog = "HTTP GET https://api.openai.com - Header: Bearer sk-ant-api03-abcdefghijklmn987654321"
        val sanitized = SecretMasker.sanitize(headerLog)

        assertFalse("Bearer token must not leak in logs", sanitized.contains("sk-ant-api03-abcdefghijklmn987654321"))
        assertTrue("Sanitized log should show masked Bearer token", sanitized.contains("Bearer ••••"))
    }

    @Test
    fun testMaskSecretDirect() {
        val rawKey = "AIzaSy123456789"
        val masked = SecretMasker.maskSecret(rawKey)
        assertFalse(masked.contains("123456789"))
        assertEquals("••••••••6789", masked)
    }

    @Test
    fun testLlmCredentialEntityMaskedProperty() {
        val cred = LlmCredentialEntity(
            id = "cred-1",
            name = "Production Gemini",
            providerType = "GEMINI",
            authType = "API_KEY",
            credentialValue = "AIzaSySampleKeyForTesting12345",
            endpointUrl = "",
            defaultModel = "gemini-3.5-flash",
            isActive = true
        )

        val masked = cred.maskedValue
        assertFalse("Entity maskedValue must not equal raw key", masked == cred.credentialValue)
        assertTrue("Entity maskedValue must contain bullets", masked.contains("••••••••"))
        assertTrue("Entity maskedValue preserves last 4 chars", masked.endsWith("2345"))
    }

    // =========================================================================
    // 2. DURABLE RUN STATE MACHINE TRANSITIONS
    // =========================================================================

    @Test
    fun testRunStateTransitionsAndProperties() {
        // Active states
        assertTrue(RunState.RUNNING.isActive())
        assertTrue(RunState.PENDING.isActive())
        assertFalse(RunState.PAUSED.isActive())
        assertFalse(RunState.COMPLETED.isActive())
        assertFalse(RunState.FAILED.isActive())

        // Resumable states
        assertTrue(RunState.PAUSED.canResume())
        assertTrue(RunState.FAILED.canResume())
        assertFalse(RunState.RUNNING.canResume())
        assertFalse(RunState.COMPLETED.canResume())
        assertFalse(RunState.CANCELLED.canResume())

        // Cancellable states
        assertTrue(RunState.RUNNING.canCancel())
        assertTrue(RunState.PENDING.canCancel())
        assertTrue(RunState.PAUSED.canCancel())
        assertFalse(RunState.COMPLETED.canCancel())
        assertFalse(RunState.CANCELLED.canCancel())

        // Terminal states
        assertTrue(RunState.COMPLETED.isTerminal())
        assertTrue(RunState.CANCELLED.isTerminal())
        assertFalse(RunState.RUNNING.isTerminal())
        assertFalse(RunState.PAUSED.isTerminal())
    }

    // =========================================================================
    // 3. CHECKPOINT STAGES & IDEMPOTENCY
    // =========================================================================

    @Test
    fun testCheckpointStageProgression() {
        assertTrue(CheckpointStage.INITIALIZATION.stepIndex < CheckpointStage.PROMPT_SYNTHESIS.stepIndex)
        assertTrue(CheckpointStage.PROMPT_SYNTHESIS.stepIndex < CheckpointStage.SKILL_SYNTHESIS.stepIndex)
        assertTrue(CheckpointStage.SKILL_SYNTHESIS.stepIndex < CheckpointStage.PLUGIN_SYNTHESIS.stepIndex)
        assertTrue(CheckpointStage.PLUGIN_SYNTHESIS.stepIndex < CheckpointStage.AGENT_ASSEMBLY.stepIndex)
        assertTrue(CheckpointStage.AGENT_ASSEMBLY.stepIndex < CheckpointStage.COMPLETED.stepIndex)
    }

    @Test
    fun testCheckpointResumeIdempotency() {
        // If run checkpoint is SKILL_SYNTHESIS, steps 1 and 2 are already done
        val currentCheckpoint = CheckpointStage.SKILL_SYNTHESIS
        val shouldExecutePrompt = currentCheckpoint.stepIndex < CheckpointStage.PROMPT_SYNTHESIS.stepIndex
        val shouldExecuteSkills = currentCheckpoint.stepIndex < CheckpointStage.SKILL_SYNTHESIS.stepIndex
        val shouldExecutePlugins = currentCheckpoint.stepIndex < CheckpointStage.PLUGIN_SYNTHESIS.stepIndex
        val shouldExecuteAssembly = currentCheckpoint.stepIndex < CheckpointStage.AGENT_ASSEMBLY.stepIndex

        assertFalse("Prompt stage should be skipped on resume from SKILL_SYNTHESIS", shouldExecutePrompt)
        assertFalse("Skills stage should be skipped on resume from SKILL_SYNTHESIS", shouldExecuteSkills)
        assertTrue("Plugins stage should execute on resume from SKILL_SYNTHESIS", shouldExecutePlugins)
        assertTrue("Assembly stage should execute on resume from SKILL_SYNTHESIS", shouldExecuteAssembly)
    }

    // =========================================================================
    // 4. FAILURE MODE MAPPER DIRECTIVES
    // =========================================================================

    @Test
    fun testAuthErrorMapsToEditDirective() {
        val authError = AppError.apiKeyInvalid(401)
        val actionableFailure = FailureModeMapper.mapFailure(
            appError = authError,
            currentStep = CheckpointStage.PROMPT_SYNTHESIS
        )

        assertEquals(RecoveryAction.EDIT, actionableFailure.action)
        assertTrue(actionableFailure.actionLabel.contains("Provider") || actionableFailure.actionLabel.contains("Key"))
        assertTrue(actionableFailure.userMessage.contains("API") || actionableFailure.userMessage.contains("Authentication"))
    }

    @Test
    fun testNetworkTimeoutMapsToRetryDirective() {
        val timeoutError = AppError.timeout("Read timed out")
        val actionableFailure = FailureModeMapper.mapFailure(
            appError = timeoutError,
            currentStep = CheckpointStage.SKILL_SYNTHESIS,
            hasCheckpoint = false
        )

        assertEquals(RecoveryAction.RETRY, actionableFailure.action)
        assertTrue(actionableFailure.actionLabel.contains("Retry"))
    }

    @Test
    fun testPauseInterruptionWithCheckpointMapsToResumeDirective() {
        val pauseError = AppError.generic("Execution paused by user")
        val actionableFailure = FailureModeMapper.mapFailure(
            appError = pauseError,
            currentStep = CheckpointStage.PLUGIN_SYNTHESIS,
            hasCheckpoint = true
        )

        assertEquals(RecoveryAction.RESUME, actionableFailure.action)
        assertTrue(actionableFailure.actionLabel.contains("Resume"))
    }

    @Test
    fun testContentSafetyBlockMapsToEditDirective() {
        val safetyError = AppError.safetyBlocked("HARM_CATEGORY_HARASSMENT")
        val actionableFailure = FailureModeMapper.mapFailure(
            appError = safetyError,
            currentStep = CheckpointStage.PROMPT_SYNTHESIS
        )

        assertEquals(RecoveryAction.EDIT, actionableFailure.action)
        assertTrue(actionableFailure.actionLabel.contains("Goal") || actionableFailure.actionLabel.contains("Edit"))
    }

    // =========================================================================
    // 5. DURABLE RUN ENTITY ROOM MODEL
    // =========================================================================

    @Test
    fun testDurableRunEntityProperties() {
        val runId = UUID.randomUUID().toString()
        val runEntity = DurableRunEntity(
            id = runId,
            idempotencyKey = "key-$runId",
            goalTitle = "Crypto Sentiment",
            goalInput = "Scrape crypto sentiment and synthesize report",
            selectedModel = "gemini-3.5-flash",
            state = "PAUSED",
            currentStepIndex = 2,
            totalSteps = 4,
            currentStepName = "Skill Forge Completed",
            retryCount = 1,
            maxRetries = 3,
            lastError = null,
            suggestedAction = "RESUME",
            checkpointDataJson = "{\"promptText\":\"Forged master prompt\"}"
        )

        assertEquals(runId, runEntity.id)
        assertEquals("PAUSED", runEntity.state)
        assertEquals(2, runEntity.currentStepIndex)
        assertEquals(1, runEntity.retryCount)
        assertEquals("RESUME", runEntity.suggestedAction)
        assertTrue(runEntity.checkpointDataJson.contains("master prompt"))
    }
}
