package com.timbre.dsp.data.api

import com.google.gson.annotations.SerializedName

enum class AiProvider(val displayName: String) {
    GEMINI("Google Gemini"),
    OPENAI("OpenAI"),
    CLAUDE("Anthropic Claude"),
    DEEPSEEK("DeepSeek"),
    GROQ("Groq (Ultra-Fast)"),
    OLLAMA("Ollama (Local)"),
    NONE("Disabled")
}

data class AiModelOption(
    val id: String,
    val displayName: String,
    val description: String = ""
)

data class AiResponse(
    val text: String,
    val tokens: Int = 0,
    val errorCode: Int = 0,
    val errorMessage: String? = null
)

data class AiEqBand(
    @SerializedName("index") val index: Int = 0,
    @SerializedName("frequency") val frequency: Float = 1000f,
    @SerializedName("gainDb") val gainDb: Float = 0f,
    @SerializedName("q") val q: Float = 1.0f,
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("type") val type: String = "PEAKING"
)

data class AiEqResponse(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("bands") val bands: List<Float>? = null, // For 10-Band Graphic EQ (31, 62, 125, 250, 500, 1k, 2k, 4k, 8k, 16k)
    @SerializedName("parametricBands") val parametricBands: List<AiEqBand>? = null,
    @SerializedName("preamp") val preamp: Float? = null,
    @SerializedName("bassBoost") val bassBoost: Int? = null, // 0 - 1000
    @SerializedName("clarityBoost") val clarityBoost: Int? = null, // 0 - 1000
    @SerializedName("irsRecommendation") val irsRecommendation: String? = null
)
