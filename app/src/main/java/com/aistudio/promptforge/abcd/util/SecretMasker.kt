package com.aistudio.promptforge.abcd.util

import android.util.Log

/**
 * High-security utility ensuring that credentials, API keys, bearer tokens,
 * and sensitive secrets are NEVER leaked into application logs, traces, or UI strings.
 */
object SecretMasker {

    // Regex patterns identifying various API keys, OAuth tokens, and secrets
    private val PATTERNS = listOf(
        // Google Gemini / Cloud API keys: AIzaSy...
        Regex("""AIza[0-9A-Za-z_\-]{35}"""),
        // OpenAI API keys: sk-...
        Regex("""sk-[a-zA-Z0-9_\-]{20,}"""),
        // Anthropic API keys: sk-ant-...
        Regex("""sk-ant-[a-zA-Z0-9_\-]{20,}"""),
        // GitHub Personal Access Tokens
        Regex("""ghp_[a-zA-Z0-9]{36}"""),
        Regex("""github_pat_[a-zA-Z0-9_]{50,}"""),
        // Bearer tokens in headers or strings
        Regex("""(?i)Bearer\s+([a-zA-Z0-9_\-\.]{12,})"""),
        // URL query param secrets: ?key=..., &api_key=..., &token=...
        Regex("""(?i)([?&](?:key|api_key|token|access_token|secret|password)=)[^&\s]+"""),
        // JSON key-value secret fields
        Regex("""(?i)"(?:password|credentialValue|secretKey|client_secret|apiKey|api_key)":\s*"([^"]+)"""")
    )

    /**
     * Replaces any detected sensitive credentials or tokens with safe redaction masks.
     */
    fun sanitize(input: String?): String {
        if (input.isNullOrBlank()) return ""
        var result = input

        // Mask Bearer tokens first
        result = result.replace(Regex("""(?i)Bearer\s+([a-zA-Z0-9_\-\.]{8,})""")) { m ->
            val token = m.groupValues[1]
            "Bearer ••••${token.takeLast(4)}"
        }

        // Mask Google keys (AIza...)
        result = result.replace(Regex("""AIza[0-9A-Za-z_\-]{20,}""")) { m ->
            "AIza••••${m.value.takeLast(4)}"
        }

        // Mask OpenAI / Anthropic keys
        result = result.replace(Regex("""sk-(?:ant-)?[a-zA-Z0-9_\-]{16,}""")) { m ->
            "sk-••••${m.value.takeLast(4)}"
        }

        // Mask GitHub tokens
        result = result.replace(Regex("""ghp_[a-zA-Z0-9]{20,}""")) { m ->
            "ghp_••••${m.value.takeLast(4)}"
        }
        result = result.replace(Regex("""github_pat_[a-zA-Z0-9_]{30,}""")) { m ->
            "github_pat_••••${m.value.takeLast(4)}"
        }

        // Mask query param secrets in URLs
        result = result.replace(Regex("""(?i)([?&](?:key|api_key|token|access_token|secret|password)=)[^&\s]+""")) { m ->
            "${m.groupValues[1]}[REDACTED_SECRET]"
        }

        // Mask JSON fields
        result = result.replace(Regex("""(?i)("(?:password|credentialValue|secretKey|client_secret|apiKey|api_key)":\s*")([^"]+)(")""")) { m ->
            val prefix = m.groupValues[1]
            val secret = m.groupValues[2]
            val suffix = m.groupValues[3]
            val masked = if (secret.length <= 4) "••••••••" else "••••••••${secret.takeLast(4)}"
            "$prefix$masked$suffix"
        }

        return result
    }

    /**
     * Safely masks a standalone secret key for UI display (e.g. "••••••••1a2b").
     */
    fun maskSecret(secret: String?): String {
        if (secret.isNullOrBlank()) return ""
        return if (secret.length <= 6) {
            "••••••••"
        } else {
            "••••••••${secret.takeLast(4)}"
        }
    }

    /**
     * Checks whether a given string contains an unredacted secret pattern.
     */
    fun containsSecret(text: String?): Boolean {
        if (text.isNullOrBlank()) return false
        return PATTERNS.any { it.containsMatchIn(text) }
    }
}

/**
 * Drop-in safe logger that enforces zero secret leakage in Logcat output.
 */
object SafeLogger {

    fun d(tag: String, msg: String, tr: Throwable? = null) {
        val cleanMsg = SecretMasker.sanitize(msg)
        if (tr != null) {
            Log.d(tag, cleanMsg, sanitizeThrowable(tr))
        } else {
            Log.d(tag, cleanMsg)
        }
    }

    fun i(tag: String, msg: String, tr: Throwable? = null) {
        val cleanMsg = SecretMasker.sanitize(msg)
        if (tr != null) {
            Log.i(tag, cleanMsg, sanitizeThrowable(tr))
        } else {
            Log.i(tag, cleanMsg)
        }
    }

    fun w(tag: String, msg: String, tr: Throwable? = null) {
        val cleanMsg = SecretMasker.sanitize(msg)
        if (tr != null) {
            Log.w(tag, cleanMsg, sanitizeThrowable(tr))
        } else {
            Log.w(tag, cleanMsg)
        }
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        val cleanMsg = SecretMasker.sanitize(msg)
        if (tr != null) {
            Log.e(tag, cleanMsg, sanitizeThrowable(tr))
        } else {
            Log.e(tag, cleanMsg)
        }
    }

    private fun sanitizeThrowable(tr: Throwable): Throwable {
        val sanitizedMessage = SecretMasker.sanitize(tr.message)
        return Exception(sanitizedMessage, tr.cause)
    }
}
