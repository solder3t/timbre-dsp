package com.timbre.dsp.data.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object AiClient {
    private const val TAG = "AiClient"

    const val DEFAULT_GEMINI_MODEL = "gemini-3.6-flash"
    const val DEFAULT_OPENAI_MODEL = "gpt-4o-mini"
    const val DEFAULT_CLAUDE_MODEL = "claude-haiku-4-5"
    const val DEFAULT_DEEPSEEK_MODEL = "deepseek-chat"
    const val DEFAULT_GROQ_MODEL = "openai/gpt-oss-120b"
    const val DEFAULT_OLLAMA_MODEL = "llama3.2"

    fun getDefaultModel(provider: AiProvider): String = when (provider) {
        AiProvider.GEMINI -> DEFAULT_GEMINI_MODEL
        AiProvider.OPENAI -> DEFAULT_OPENAI_MODEL
        AiProvider.CLAUDE -> DEFAULT_CLAUDE_MODEL
        AiProvider.DEEPSEEK -> DEFAULT_DEEPSEEK_MODEL
        AiProvider.GROQ -> DEFAULT_GROQ_MODEL
        AiProvider.OLLAMA -> DEFAULT_OLLAMA_MODEL
        AiProvider.NONE -> ""
    }

    fun getAvailableModels(provider: AiProvider): List<AiModelOption> = when (provider) {
        AiProvider.GEMINI -> listOf(
            AiModelOption("gemini-3.6-flash", "Gemini 3.6 Flash", "Fast, efficient multimodal DSP reasoning"),
            AiModelOption("gemini-3.7-flash", "Gemini 3.7 Flash", "Flagship Flash with hybrid reasoning"),
            AiModelOption("gemini-3.5-flash-lite", "Gemini 3.5 Flash-Lite", "Ultra low latency & high speed")
        )
        AiProvider.OPENAI -> listOf(
            AiModelOption("gpt-4o-mini", "GPT-4o Mini", "Fast and lightweight"),
            AiModelOption("gpt-4o", "GPT-4o", "Flagship model"),
            AiModelOption("o3-mini", "o3-mini", "High-speed reasoning model")
        )
        AiProvider.CLAUDE -> listOf(
            AiModelOption("claude-haiku-4-5", "Claude Haiku 4.5", "Fast frontier intelligence"),
            AiModelOption("claude-sonnet-4-5", "Claude Sonnet 4.5", "Balanced reasoning & acoustic analysis"),
            AiModelOption("claude-3-7-sonnet-latest", "Claude 3.7 Sonnet", "Hybrid reasoning model")
        )
        AiProvider.DEEPSEEK -> listOf(
            AiModelOption("deepseek-chat", "DeepSeek Chat (V3)", "General conversational & fast"),
            AiModelOption("deepseek-reasoner", "DeepSeek Reasoner (R1)", "Deep acoustic reasoning model")
        )
        AiProvider.GROQ -> listOf(
            AiModelOption("openai/gpt-oss-120b", "GPT OSS 120B", "Flagship open weights on Groq LPU"),
            AiModelOption("openai/gpt-oss-20b", "GPT OSS 20B", "Ultra-fast open weights on Groq LPU"),
            AiModelOption("deepseek-r1-distill-llama-70b", "DeepSeek R1 Distill 70B", "Reasoning on Groq LPU")
        )
        AiProvider.OLLAMA -> listOf(
            AiModelOption("llama3.2", "Llama 3.2", "Lightweight local model"),
            AiModelOption("llama3.3", "Llama 3.3", "70B open weights"),
            AiModelOption("phi4", "Phi-4", "High-reasoning small model"),
            AiModelOption("qwen2.5-coder", "Qwen 2.5 Coder", "Code and structured JSON model"),
            AiModelOption("deepseek-r1", "DeepSeek R1", "Local reasoning model")
        )
        AiProvider.NONE -> emptyList()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun sendPrompt(
        systemPrompt: String,
        userPrompt: String,
        provider: AiProvider,
        apiKey: String,
        serverUrl: String = "",
        model: String = ""
    ): AiResponse = withContext(Dispatchers.IO) {
        if (provider == AiProvider.NONE) {
            return@withContext AiResponse(text = "", errorCode = 400, errorMessage = "AI Provider is disabled")
        }

        val targetModel = if (model.isNotBlank()) model else getDefaultModel(provider)

        try {
            when (provider) {
                AiProvider.GEMINI -> callGemini(systemPrompt, userPrompt, apiKey, targetModel)
                AiProvider.OPENAI -> callOpenAiCompatible(
                    url = "https://api.openai.com/v1/chat/completions",
                    systemPrompt = systemPrompt,
                    userPrompt = userPrompt,
                    apiKey = apiKey,
                    model = targetModel
                )
                AiProvider.DEEPSEEK -> callOpenAiCompatible(
                    url = "https://api.deepseek.com/v1/chat/completions",
                    systemPrompt = systemPrompt,
                    userPrompt = userPrompt,
                    apiKey = apiKey,
                    model = targetModel
                )
                AiProvider.GROQ -> callOpenAiCompatible(
                    url = "https://api.groq.com/openai/v1/chat/completions",
                    systemPrompt = systemPrompt,
                    userPrompt = userPrompt,
                    apiKey = apiKey,
                    model = targetModel
                )
                AiProvider.CLAUDE -> callClaude(systemPrompt, userPrompt, apiKey, targetModel)
                AiProvider.OLLAMA -> callOllama(systemPrompt, userPrompt, serverUrl, targetModel)
                AiProvider.NONE -> AiResponse(text = "", errorCode = 400, errorMessage = "Provider disabled")
            }
        } catch (e: Exception) {
            Log.e(TAG, "AI request error with provider $provider", e)
            AiResponse(text = "", errorCode = 500, errorMessage = e.localizedMessage ?: "Unknown network error")
        }
    }

    private fun callGemini(
        systemPrompt: String,
        userPrompt: String,
        apiKey: String,
        model: String
    ): AiResponse {
        val cleanKey = apiKey.trim()
        if (cleanKey.isEmpty()) {
            return AiResponse(text = "", errorCode = 401, errorMessage = "Gemini API key is required")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$cleanKey"

        val root = JsonObject().apply {
            val systemInstruction = JsonObject().apply {
                val parts = JsonArray().apply {
                    add(JsonObject().apply { addProperty("text", systemPrompt) })
                }
                add("parts", parts)
            }
            add("system_instruction", systemInstruction)

            val contents = JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "user")
                    val parts = JsonArray().apply {
                        add(JsonObject().apply { addProperty("text", userPrompt) })
                    }
                    add("parts", parts)
                })
            }
            add("contents", contents)

            val genConfig = JsonObject().apply {
                addProperty("temperature", 0.3)
                addProperty("response_mime_type", "application/json")
            }
            add("generationConfig", genConfig)
        }

        val request = Request.Builder()
            .url(url)
            .post(root.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body.string()
            if (!response.isSuccessful) {
                return AiResponse(text = "", errorCode = response.code, errorMessage = "Gemini Error: $body")
            }

            val json = gson.fromJson(body, JsonObject::class.java)
            val text = json.getAsJsonArray("candidates")
                ?.get(0)?.asJsonObject
                ?.getAsJsonObject("content")
                ?.getAsJsonArray("parts")
                ?.get(0)?.asJsonObject
                ?.get("text")?.asString ?: ""

            val tokenCount = json.getAsJsonObject("usageMetadata")
                ?.get("totalTokenCount")?.asInt ?: 0

            return AiResponse(text = text, tokens = tokenCount)
        }
    }

    private fun callOpenAiCompatible(
        url: String,
        systemPrompt: String,
        userPrompt: String,
        apiKey: String,
        model: String
    ): AiResponse {
        val cleanKey = apiKey.trim()
        if (cleanKey.isEmpty()) {
            return AiResponse(text = "", errorCode = 401, errorMessage = "API key is required")
        }

        val root = JsonObject().apply {
            addProperty("model", model)
            val messages = JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "system")
                    addProperty("content", systemPrompt)
                })
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", userPrompt)
                })
            }
            add("messages", messages)
            addProperty("temperature", 0.3)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $cleanKey")
            .post(root.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body.string()
            if (!response.isSuccessful) {
                return AiResponse(text = "", errorCode = response.code, errorMessage = "API Error: $body")
            }

            val json = gson.fromJson(body, JsonObject::class.java)
            val text = json.getAsJsonArray("choices")
                ?.get(0)?.asJsonObject
                ?.getAsJsonObject("message")
                ?.get("content")?.asString ?: ""

            val tokens = json.getAsJsonObject("usage")
                ?.get("total_tokens")?.asInt ?: 0

            return AiResponse(text = text, tokens = tokens)
        }
    }

    private fun callClaude(
        systemPrompt: String,
        userPrompt: String,
        apiKey: String,
        model: String
    ): AiResponse {
        val cleanKey = apiKey.trim()
        if (cleanKey.isEmpty()) {
            return AiResponse(text = "", errorCode = 401, errorMessage = "Claude API key is required")
        }

        val url = "https://api.anthropic.com/v1/messages"
        val root = JsonObject().apply {
            addProperty("model", model)
            addProperty("max_tokens", 2048)
            addProperty("system", systemPrompt)
            val messages = JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", userPrompt)
                })
            }
            add("messages", messages)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("x-api-key", cleanKey)
            .addHeader("anthropic-version", "2023-06-01")
            .post(root.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body.string()
            if (!response.isSuccessful) {
                return AiResponse(text = "", errorCode = response.code, errorMessage = "Claude Error: $body")
            }

            val json = gson.fromJson(body, JsonObject::class.java)
            val text = json.getAsJsonArray("content")
                ?.get(0)?.asJsonObject
                ?.get("text")?.asString ?: ""

            val usage = json.getAsJsonObject("usage")
            val tokens = (usage?.get("input_tokens")?.asInt ?: 0) + (usage?.get("output_tokens")?.asInt ?: 0)

            return AiResponse(text = text, tokens = tokens)
        }
    }

    private fun callOllama(
        systemPrompt: String,
        userPrompt: String,
        serverUrl: String,
        model: String
    ): AiResponse {
        val baseUrl = if (serverUrl.isNotBlank()) serverUrl.trimEnd('/') else "http://localhost:11434"
        val url = "$baseUrl/api/chat"

        val root = JsonObject().apply {
            addProperty("model", model)
            addProperty("stream", false)
            val messages = JsonArray().apply {
                add(JsonObject().apply {
                    addProperty("role", "system")
                    addProperty("content", systemPrompt)
                })
                add(JsonObject().apply {
                    addProperty("role", "user")
                    addProperty("content", userPrompt)
                })
            }
            add("messages", messages)
        }

        val request = Request.Builder()
            .url(url)
            .post(root.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body.string()
            if (!response.isSuccessful) {
                return AiResponse(text = "", errorCode = response.code, errorMessage = "Ollama Error: $body")
            }

            val json = gson.fromJson(body, JsonObject::class.java)
            val text = json.getAsJsonObject("message")
                ?.get("content")?.asString ?: ""

            val promptEvalCount = json.get("prompt_eval_count")?.asInt ?: 0
            val evalCount = json.get("eval_count")?.asInt ?: 0

            return AiResponse(text = text, tokens = promptEvalCount + evalCount)
        }
    }
}
