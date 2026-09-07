package com.aistudio.promptforge.abcd

import com.aistudio.promptforge.abcd.api.UniversalLlmResult
import com.aistudio.promptforge.abcd.api.UniversalLlmService
import com.aistudio.promptforge.abcd.data.AutoForgePack
import com.aistudio.promptforge.abcd.data.LlmCredentialEntity
import com.aistudio.promptforge.abcd.data.SavedMcp
import com.aistudio.promptforge.abcd.data.SavedPrompt
import com.aistudio.promptforge.abcd.data.SavedSkill
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomAndUniversalLlmUnitTest {

    @Test
    fun testLlmCredentialEntityCreation() {
        val cred = LlmCredentialEntity(
            id = "cred_test_1",
            name = "Personal OpenAI",
            providerType = "OPENAI",
            authType = "API_KEY",
            credentialValue = "sk-test1234567890",
            endpointUrl = "https://api.openai.com/v1",
            defaultModel = "gpt-4o",
            isActive = true,
            isHealthy = true,
            lastLatencyMs = 320L
        )

        assertEquals("cred_test_1", cred.id)
        assertEquals("Personal OpenAI", cred.name)
        assertEquals("OPENAI", cred.providerType)
        assertEquals("API_KEY", cred.authType)
        assertEquals("sk-test1234567890", cred.credentialValue)
        assertEquals("gpt-4o", cred.defaultModel)
        assertTrue(cred.isActive)
        assertTrue(cred.isHealthy)
        assertEquals(320L, cred.lastLatencyMs)
    }

    @Test
    fun testLlmCredentialOAuthSupport() {
        val oauthCred = LlmCredentialEntity(
            id = "cred_oauth_1",
            name = "Enterprise Gateway OAuth",
            providerType = "CUSTOM",
            authType = "OAUTH_BEARER",
            credentialValue = "ya29.a0AfH6SM...",
            endpointUrl = "https://proxy.internal.corp/v1",
            defaultModel = "custom-deepseek",
            isActive = true
        )

        assertEquals("OAUTH_BEARER", oauthCred.authType)
        assertEquals("ya29.a0AfH6SM...", oauthCred.credentialValue)
        assertEquals("https://proxy.internal.corp/v1", oauthCred.endpointUrl)
    }

    @Test
    fun testSavedPromptEntityProperties() {
        val prompt = SavedPrompt(
            id = "p_1",
            title = "Test Prompt",
            frameworkId = "CREATE",
            fieldsJson = "{\"role\":\"Architect\"}",
            assembled = "Act as an Architect",
            system = "System prompt"
        )

        assertEquals("p_1", prompt.id)
        assertEquals("Test Prompt", prompt.title)
        assertEquals("CREATE", prompt.frameworkId)
        assertEquals("{\"role\":\"Architect\"}", prompt.fieldsJson)
        assertEquals("Act as an Architect", prompt.assembled)
        assertEquals("System prompt", prompt.system)
    }

    @Test
    fun testSavedSkillEntityProperties() {
        val skill = SavedSkill(
            id = "s_1",
            title = "Python Code Analysis",
            slug = "python-analysis",
            category = "Engineering",
            description = "Analyzes python AST",
            trigger = "python, analyze, lint",
            source = "User Import",
            implementationCode = "def analyze(): pass",
            skillMarkdown = "# Python Analysis Skill"
        )

        assertEquals("s_1", skill.id)
        assertEquals("Python Code Analysis", skill.title)
        assertEquals("Engineering", skill.category)
        assertEquals("python, analyze, lint", skill.trigger)
    }

    @Test
    fun testSavedMcpEntityProperties() {
        val mcp = SavedMcp(
            id = "mcp_1",
            name = "Database Tools",
            category = "Database",
            description = "SQL query execution tools",
            toolsCount = 4,
            mcpJsonConfig = "{\"tools\":[{\"name\":\"query\"}]}",
            serverCode = "server = FastMCP()"
        )

        assertEquals("mcp_1", mcp.id)
        assertEquals("Database Tools", mcp.name)
        assertEquals(4, mcp.toolsCount)
    }

    @Test
    fun testAutoForgePackEntityProperties() {
        val pack = AutoForgePack(
            id = "pack_1",
            goalTitle = "Full Stack Generator",
            goalInput = "Generate a full stack app",
            taskType = "Software Development",
            promptText = "Prompt instructions",
            skillsJson = "[]",
            mcpConfigJson = "[]",
            fullSpecMarkdown = "# Full Stack Generator Spec"
        )

        assertEquals("pack_1", pack.id)
        assertEquals("Full Stack Generator", pack.goalTitle)
        assertEquals("Software Development", pack.taskType)
    }

    @Test
    fun testUniversalLlmServiceOfflineFallback() = runBlocking {
        val cred = LlmCredentialEntity(
            id = "cred_test",
            name = "Test Provider",
            providerType = "OPENAI",
            authType = "API_KEY",
            credentialValue = "invalid-test-key",
            defaultModel = "gpt-4o"
        )

        // Without network or with dummy key, execute safely returns a structured result without throwing exceptions
        val result = UniversalLlmService.execute(
            prompt = "Generate a unit test prompt",
            systemInstruction = "You are a prompt engineer",
            credential = cred,
            overrideModel = null,
            temperature = 0.7f
        )
        assertNotNull(result)
        assertEquals("OPENAI", result.provider)
        assertEquals("gpt-4o", result.model)
        assertNotNull(result.errorMessage)
        assertFalse(result.isSuccess)
    }
}
