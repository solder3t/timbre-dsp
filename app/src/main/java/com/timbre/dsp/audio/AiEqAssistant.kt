package com.timbre.dsp.audio

import android.util.Log
import com.google.gson.Gson
import com.timbre.dsp.data.api.AiClient
import com.timbre.dsp.data.api.AiEqResponse
import com.timbre.dsp.data.api.AiProvider
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.FilterType
import com.timbre.dsp.model.EQMode
import java.util.Locale

object AiEqAssistant {
    private const val TAG = "AiEqAssistant"
    private val gson = Gson()

    private fun buildSystemPrompt(settings: DSPSettings): String {
        val bandsStr = settings.bands.joinToString(", ") { "${it.frequency.toInt()}Hz: ${String.format(Locale.ROOT, "%.1f", it.gain)}dB" }
        val modeStr = if (settings.eqMode == EQMode.PARAMETRIC) "Parametric Equalizer" else "10-Band Graphic Equalizer"

        return """
            You are an expert audio mastering engineer and acoustician with decades of experience tuning studio monitors, high-end audiophile headphones, and DSP architectures.
            Your goal is to provide a master-grade frequency response and DSP tuning based on the user's natural language request.

            Current DSP State:
            - Mode: $modeStr
            - Current Preamp: ${settings.preampGain} dB
            - Current Bass Boost: ${settings.bassBoostGain} dB (Enabled: ${settings.bassBoostEnabled})
            - Current Clarity Exciter: ${settings.clarityGain} dB (Enabled: ${settings.clarityEnabled})
            - Active Bands: [$bandsStr]

            Target Output JSON Schema:
            {
              "title": "A short 2-4 word descriptive preset name",
              "description": "Brief explanation of the acoustic balance and sonic character",
              "bands": [-15.0 to +15.0, ... 10 floats for Graphic EQ: 31Hz, 62Hz, 125Hz, 250Hz, 500Hz, 1000Hz, 2000Hz, 4000Hz, 8000Hz, 16000Hz],
              "parametricBands": [
                {
                  "index": 0 to 9,
                  "frequency": 20.0 to 20000.0,
                  "gainDb": -15.0 to +15.0,
                  "q": 0.2 to 10.0,
                  "type": "PEAKING" | "LOW_SHELF" | "HIGH_SHELF" | "LOW_PASS" | "HIGH_PASS",
                  "enabled": true
                }
              ],
              "preamp": -15.0 to 0.0 (Preamp gain to prevent digital clipping),
              "bassBoost": 0 to 1000 (Optional dynamic bass enhancement),
              "clarityBoost": 0 to 1000 (Optional high-frequency clarity exciter),
              "irsRecommendation": "Optional acoustic space or impulse response recommendation"
            }

            Audio Engineering Rules:
            1. Headroom Safety: Always provide negative preamp if any positive gain boost is applied (\(\text{preamp} \le -\max(\text{gains})\)).
            2. Spectral Cleanliness: Prefer surgical cuts and moderate boosts (+1dB to +4.5dB) over extreme boosts to avoid distortion and phase smears.
            3. Target Curve Alignment: If asked for "Harman", "IEF Neutral", "Warm", "Punchy", "V-Shaped", or a specific headphone model, apply accurate acoustic compensation curves.
            4. FORMAT: Return ONLY the JSON object. No Markdown code fences, no conversational text.
        """.trimIndent()
    }

    suspend fun getAdjustment(
        userPrompt: String,
        currentSettings: DSPSettings,
        provider: AiProvider,
        apiKey: String,
        serverUrl: String = "",
        model: String = ""
    ): Pair<AiEqResponse?, String?> {
        if (provider == AiProvider.NONE) {
            return Pair(null, "AI Provider is disabled in settings.")
        }

        val systemPrompt = buildSystemPrompt(currentSettings)
        val response = AiClient.sendPrompt(
            systemPrompt = systemPrompt,
            userPrompt = userPrompt,
            provider = provider,
            apiKey = apiKey,
            serverUrl = serverUrl,
            model = model
        )

        if (response.errorCode != 0 || response.text.isBlank()) {
            return Pair(null, response.errorMessage ?: "Failed to get response from AI (Error ${response.errorCode})")
        }

        return try {
            val content = response.text.trim()
            val jsonStart = content.indexOf('{')
            val jsonEnd = content.lastIndexOf('}') + 1
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                return Pair(null, "AI response did not contain valid JSON.")
            }
            val jsonStr = content.substring(jsonStart, jsonEnd)
            val parsed = gson.fromJson(jsonStr, AiEqResponse::class.java)
            Pair(parsed, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing AI response: ${response.text}", e)
            Pair(null, "Failed to parse AI tuning profile: ${e.localizedMessage}")
        }
    }

    fun applyToSettings(response: AiEqResponse, current: DSPSettings): DSPSettings {
        var updated = current

        // 1. Update bands
        if (current.eqMode == EQMode.PARAMETRIC && !response.parametricBands.isNullOrEmpty()) {
            val newBands = response.parametricBands.mapIndexed { idx, peq ->
                val filterType = when (peq.type.uppercase()) {
                    "LOW_SHELF", "LOWSHELF", "LSC" -> FilterType.LOW_SHELF
                    "HIGH_SHELF", "HIGHSHELF", "HSC" -> FilterType.HIGH_SHELF
                    "LOW_PASS", "LOWPASS", "LP" -> FilterType.LOW_PASS
                    "HIGH_PASS", "HIGHPASS", "HP" -> FilterType.HIGH_PASS
                    "NOTCH" -> FilterType.NOTCH
                    "BAND_PASS", "BANDPASS" -> FilterType.BAND_PASS
                    else -> FilterType.PEAK
                }
                EQBand(
                    index = idx,
                    frequency = peq.frequency.coerceIn(20f, 20000f),
                    gain = peq.gainDb.coerceIn(-15f, 15f),
                    q = peq.q.coerceIn(0.1f, 10f),
                    type = filterType,
                    enabled = peq.enabled
                )
            }
            updated = updated.copy(bands = newBands)
        } else if (!response.bands.isNullOrEmpty()) {
            val frequencies = floatArrayOf(31f, 62f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
            val newBands = frequencies.mapIndexed { idx, freq ->
                val gain = response.bands.getOrNull(idx) ?: 0f
                EQBand(
                    index = idx,
                    frequency = freq,
                    gain = gain.coerceIn(-15f, 15f),
                    q = 1.414f,
                    type = FilterType.PEAK,
                    enabled = true
                )
            }
            updated = updated.copy(bands = newBands)
        }

        // 2. Preamp
        response.preamp?.let {
            updated = updated.copy(preampGain = it.coerceIn(-15f, 15f))
        }

        // 3. Bass Boost
        response.bassBoost?.let {
            val gain = (it / 1000f) * 12f
            updated = updated.copy(
                bassBoostEnabled = gain > 0.5f,
                bassBoostGain = gain
            )
        }

        // 4. Clarity Exciter
        response.clarityBoost?.let {
            val gain = (it / 1000f) * 12f
            updated = updated.copy(
                clarityEnabled = gain > 0.5f,
                clarityGain = gain
            )
        }

        // 5. Preset ID
        val customName = response.title ?: "AI Tuned"
        updated = updated.copy(currentPresetId = "ai_${customName.lowercase().replace(" ", "_")}")

        return updated
    }
}
