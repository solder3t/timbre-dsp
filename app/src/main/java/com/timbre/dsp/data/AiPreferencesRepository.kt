package com.timbre.dsp.data

import android.content.Context
import android.content.SharedPreferences
import com.timbre.dsp.data.api.AiClient
import com.timbre.dsp.data.api.AiProvider

data class AiPreferences(
    val provider: AiProvider = AiProvider.GEMINI,
    val apiKey: String = "",
    val model: String = "",
    val serverUrl: String = "http://localhost:11434"
)

object AiPreferencesRepository {
    private const val PREFS_NAME = "timbre_ai_preferences"
    private const val KEY_PROVIDER = "key_ai_provider"
    private const val KEY_API_KEY = "key_ai_api_key"
    private const val KEY_MODEL = "key_ai_model"
    private const val KEY_SERVER_URL = "key_ai_server_url"

    fun load(context: Context): AiPreferences {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val providerStr = prefs.getString(KEY_PROVIDER, AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name
        val provider = try {
            AiProvider.valueOf(providerStr)
        } catch (e: Exception) {
            AiProvider.GEMINI
        }
        val apiKey = prefs.getString(KEY_API_KEY, "") ?: ""
        val model = prefs.getString(KEY_MODEL, "") ?: ""
        val serverUrl = prefs.getString(KEY_SERVER_URL, "http://localhost:11434") ?: "http://localhost:11434"

        return AiPreferences(
            provider = provider,
            apiKey = apiKey,
            model = if (model.isBlank()) AiClient.getDefaultModel(provider) else model,
            serverUrl = serverUrl
        )
    }

    fun save(context: Context, prefs: AiPreferences) {
        val sp: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit()
            .putString(KEY_PROVIDER, prefs.provider.name)
            .putString(KEY_API_KEY, prefs.apiKey)
            .putString(KEY_MODEL, prefs.model)
            .putString(KEY_SERVER_URL, prefs.serverUrl)
            .apply()
    }
}
