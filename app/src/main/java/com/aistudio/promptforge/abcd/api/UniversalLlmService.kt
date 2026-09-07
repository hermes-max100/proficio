package com.aistudio.promptforge.abcd.api

import com.aistudio.promptforge.abcd.BuildConfig
import com.aistudio.promptforge.abcd.data.LlmCredentialEntity
import com.aistudio.promptforge.abcd.util.SafeLogger
import com.aistudio.promptforge.abcd.util.SecretMasker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

data class UniversalLlmResult(
    val isSuccess: Boolean,
    val text: String,
    val latencyMs: Long = 0,
    val promptTokens: Int = 0,
    val outputTokens: Int = 0,
    val totalTokens: Int = 0,
    val rawOutput: String = "",
    val errorMessage: String? = null,
    val provider: String = "GEMINI",
    val model: String = "gemini-3.5-flash"
)

data class ConnectionTestResult(
    val isSuccess: Boolean,
    val latencyMs: Long = 0,
    val message: String,
    val detectedModel: String = ""
)

object UniversalLlmService {
    private const val TAG = "UniversalLlmService"
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun execute(
        prompt: String,
        systemInstruction: String? = null,
        credential: LlmCredentialEntity?,
        overrideModel: String? = null,
        temperature: Float = 0.4f,
        maxTokens: Int = 1500
    ): UniversalLlmResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        // If no custom active credential, use Gemini Direct with BuildConfig
        val effectiveProvider = credential?.providerType ?: "GEMINI"
        val effectiveAuthType = credential?.authType ?: "API_KEY"
        val effectiveKey = credential?.credentialValue?.ifBlank { BuildConfig.GEMINI_API_KEY }
            ?: BuildConfig.GEMINI_API_KEY
        val effectiveModel = overrideModel?.ifBlank { null }
            ?: credential?.defaultModel?.ifBlank { null }
            ?: when (effectiveProvider) {
                "GEMINI" -> "gemini-3.5-flash"
                "OPENAI" -> "gpt-4o"
                "ANTHROPIC" -> "claude-3-5-sonnet-20241022"
                else -> "default-model"
            }

        try {
            when (effectiveProvider.uppercase()) {
                "GEMINI" -> executeGemini(
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    authType = effectiveAuthType,
                    apiKeyOrToken = effectiveKey,
                    model = effectiveModel,
                    temperature = temperature,
                    maxTokens = maxTokens,
                    startTime = startTime
                )
                "OPENAI", "CUSTOM" -> executeOpenAiCompatible(
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    authType = effectiveAuthType,
                    token = effectiveKey,
                    endpointUrl = credential?.endpointUrl?.ifBlank { "https://api.openai.com/v1/chat/completions" }
                        ?: "https://api.openai.com/v1/chat/completions",
                    model = effectiveModel,
                    temperature = temperature,
                    maxTokens = maxTokens,
                    startTime = startTime
                )
                "ANTHROPIC" -> executeAnthropic(
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    authType = effectiveAuthType,
                    token = effectiveKey,
                    endpointUrl = credential?.endpointUrl?.ifBlank { "https://api.anthropic.com/v1/messages" }
                        ?: "https://api.anthropic.com/v1/messages",
                    model = effectiveModel,
                    temperature = temperature,
                    maxTokens = maxTokens,
                    startTime = startTime
                )
                else -> executeGemini(
                    prompt = prompt,
                    systemInstruction = systemInstruction,
                    authType = effectiveAuthType,
                    apiKeyOrToken = effectiveKey,
                    model = effectiveModel,
                    temperature = temperature,
                    maxTokens = maxTokens,
                    startTime = startTime
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            SafeLogger.e(TAG, "Execution failed: ${SecretMasker.sanitize(e.message)}", e)
            UniversalLlmResult(
                isSuccess = false,
                text = "",
                latencyMs = latency,
                errorMessage = SecretMasker.sanitize(e.message) ?: "Unknown communication error",
                provider = effectiveProvider,
                model = effectiveModel
            )
        }
    }

    private fun executeGemini(
        prompt: String,
        systemInstruction: String?,
        authType: String,
        apiKeyOrToken: String,
        model: String,
        temperature: Float,
        maxTokens: Int,
        startTime: Long
    ): UniversalLlmResult {
        val cleanModel = if (model.startsWith("models/")) model.removePrefix("models/") else model
        val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:generateContent"
        val requestUrl = if (authType == "API_KEY") "$baseUrl?key=$apiKeyOrToken" else baseUrl

        val bodyJson = buildJsonObject {
            putJsonArray("contents") {
                addJsonObject {
                    put("role", "user")
                    putJsonArray("parts") {
                        addJsonObject {
                            put("text", prompt)
                        }
                    }
                }
            }
            if (!systemInstruction.isNullOrBlank()) {
                putJsonObject("systemInstruction") {
                    putJsonArray("parts") {
                        addJsonObject {
                            put("text", systemInstruction)
                        }
                    }
                }
            }
            putJsonObject("generationConfig") {
                put("temperature", temperature)
                put("maxOutputTokens", maxTokens)
            }
        }.toString()

        val requestBuilder = Request.Builder()
            .url(requestUrl)
            .post(bodyJson.toRequestBody(JSON_MEDIA_TYPE))

        if (authType == "OAUTH_BEARER" && apiKeyOrToken.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $apiKeyOrToken")
        }

        httpClient.newCall(requestBuilder.build()).execute().use { response ->
            val latency = System.currentTimeMillis() - startTime
            val responseString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val cleanResponse = SecretMasker.sanitize(responseString)
                return UniversalLlmResult(
                    isSuccess = false,
                    text = "",
                    latencyMs = latency,
                    errorMessage = "Gemini API HTTP ${response.code}: $cleanResponse",
                    provider = "GEMINI",
                    model = cleanModel
                )
            }

            val parsed = json.parseToJsonElement(responseString).jsonObject
            val candidates = parsed["candidates"]?.jsonArray
            val firstPartText = candidates?.getOrNull(0)?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("parts")?.jsonArray
                ?.getOrNull(0)?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: ""

            val usage = parsed["usageMetadata"]?.jsonObject
            val promptTokens = usage?.get("promptTokenCount")?.jsonPrimitive?.content?.toIntOrNull() ?: (prompt.length / 4)
            val outputTokens = usage?.get("candidatesTokenCount")?.jsonPrimitive?.content?.toIntOrNull() ?: (firstPartText.length / 4)
            val totalTokens = usage?.get("totalTokenCount")?.jsonPrimitive?.content?.toIntOrNull() ?: (promptTokens + outputTokens)

            return UniversalLlmResult(
                isSuccess = true,
                text = firstPartText,
                latencyMs = latency,
                promptTokens = promptTokens,
                outputTokens = outputTokens,
                totalTokens = totalTokens,
                rawOutput = responseString,
                provider = "GEMINI",
                model = cleanModel
            )
        }
    }

    private fun executeOpenAiCompatible(
        prompt: String,
        systemInstruction: String?,
        authType: String,
        token: String,
        endpointUrl: String,
        model: String,
        temperature: Float,
        maxTokens: Int,
        startTime: Long
    ): UniversalLlmResult {
        val bodyJson = buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                if (!systemInstruction.isNullOrBlank()) {
                    addJsonObject {
                        put("role", "system")
                        put("content", systemInstruction)
                    }
                }
                addJsonObject {
                    put("role", "user")
                    put("content", prompt)
                }
            }
            put("temperature", temperature)
            put("max_tokens", maxTokens)
        }.toString()

        val requestBuilder = Request.Builder()
            .url(endpointUrl)
            .post(bodyJson.toRequestBody(JSON_MEDIA_TYPE))

        if (token.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        httpClient.newCall(requestBuilder.build()).execute().use { response ->
            val latency = System.currentTimeMillis() - startTime
            val responseString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val cleanResponse = SecretMasker.sanitize(responseString)
                return UniversalLlmResult(
                    isSuccess = false,
                    text = "",
                    latencyMs = latency,
                    errorMessage = "LLM API HTTP ${response.code}: $cleanResponse",
                    provider = "OPENAI",
                    model = model
                )
            }

            val parsed = json.parseToJsonElement(responseString).jsonObject
            val choices = parsed["choices"]?.jsonArray
            val text = choices?.getOrNull(0)?.jsonObject
                ?.get("message")?.jsonObject
                ?.get("content")?.jsonPrimitive?.content ?: ""

            val usage = parsed["usage"]?.jsonObject
            val promptTokens = usage?.get("prompt_tokens")?.jsonPrimitive?.content?.toIntOrNull() ?: (prompt.length / 4)
            val outputTokens = usage?.get("completion_tokens")?.jsonPrimitive?.content?.toIntOrNull() ?: (text.length / 4)
            val totalTokens = usage?.get("total_tokens")?.jsonPrimitive?.content?.toIntOrNull() ?: (promptTokens + outputTokens)

            return UniversalLlmResult(
                isSuccess = true,
                text = text,
                latencyMs = latency,
                promptTokens = promptTokens,
                outputTokens = outputTokens,
                totalTokens = totalTokens,
                rawOutput = responseString,
                provider = "OPENAI",
                model = model
            )
        }
    }

    private fun executeAnthropic(
        prompt: String,
        systemInstruction: String?,
        authType: String,
        token: String,
        endpointUrl: String,
        model: String,
        temperature: Float,
        maxTokens: Int,
        startTime: Long
    ): UniversalLlmResult {
        val bodyJson = buildJsonObject {
            put("model", model)
            put("max_tokens", maxTokens)
            put("temperature", temperature)
            if (!systemInstruction.isNullOrBlank()) {
                put("system", systemInstruction)
            }
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    put("content", prompt)
                }
            }
        }.toString()

        val requestBuilder = Request.Builder()
            .url(endpointUrl)
            .addHeader("anthropic-version", "2023-06-01")
            .post(bodyJson.toRequestBody(JSON_MEDIA_TYPE))

        if (token.isNotBlank()) {
            if (authType == "API_KEY") {
                requestBuilder.addHeader("x-api-key", token)
            } else {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }

        httpClient.newCall(requestBuilder.build()).execute().use { response ->
            val latency = System.currentTimeMillis() - startTime
            val responseString = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val cleanResponse = SecretMasker.sanitize(responseString)
                return UniversalLlmResult(
                    isSuccess = false,
                    text = "",
                    latencyMs = latency,
                    errorMessage = "Anthropic API HTTP ${response.code}: $cleanResponse",
                    provider = "ANTHROPIC",
                    model = model
                )
            }

            val parsed = json.parseToJsonElement(responseString).jsonObject
            val contentArray = parsed["content"]?.jsonArray
            val text = contentArray?.getOrNull(0)?.jsonObject
                ?.get("text")?.jsonPrimitive?.content ?: ""

            val usage = parsed["usage"]?.jsonObject
            val promptTokens = usage?.get("input_tokens")?.jsonPrimitive?.content?.toIntOrNull() ?: (prompt.length / 4)
            val outputTokens = usage?.get("output_tokens")?.jsonPrimitive?.content?.toIntOrNull() ?: (text.length / 4)
            val totalTokens = promptTokens + outputTokens

            return UniversalLlmResult(
                isSuccess = true,
                text = text,
                latencyMs = latency,
                promptTokens = promptTokens,
                outputTokens = outputTokens,
                totalTokens = totalTokens,
                rawOutput = responseString,
                provider = "ANTHROPIC",
                model = model
            )
        }
    }

    suspend fun testCredential(credential: LlmCredentialEntity): ConnectionTestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val testPrompt = "Ping! Reply with 'ONLINE'."
        try {
            val result = execute(
                prompt = testPrompt,
                systemInstruction = "You are a test agent. Keep answer under 10 chars.",
                credential = credential,
                maxTokens = 20
            )
            val latency = System.currentTimeMillis() - startTime
            if (result.isSuccess) {
                ConnectionTestResult(
                    isSuccess = true,
                    latencyMs = latency,
                    message = "Connection successful (${latency}ms) - Response: ${result.text.take(30)}",
                    detectedModel = result.model
                )
            } else {
                ConnectionTestResult(
                    isSuccess = false,
                    latencyMs = latency,
                    message = SecretMasker.sanitize(result.errorMessage ?: "Authentication or network failure"),
                    detectedModel = credential.defaultModel
                )
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ConnectionTestResult(
                isSuccess = false,
                latencyMs = latency,
                message = "Failed: ${SecretMasker.sanitize(e.message)}",
                detectedModel = credential.defaultModel
            )
        }
    }
}
