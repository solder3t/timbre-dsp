package com.timbre.dsp.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.EQMode
import com.timbre.dsp.model.FilterType
import com.timbre.dsp.model.RoutingMode
import org.json.JSONArray
import org.json.JSONObject

object SettingsRepository {

    private const val TAG = "SettingsRepository"
    private const val PREFS_NAME = "timbre_dsp_settings_prefs"
    private const val KEY_SETTINGS_JSON = "key_dsp_settings_json"

    fun loadSettings(context: Context): DSPSettings {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_SETTINGS_JSON, null) ?: return DSPSettings()

        return try {
            val obj = JSONObject(jsonStr)
            val isEnabled = obj.optBoolean("isEnabled", true)
            val routingMode = try { RoutingMode.valueOf(obj.optString("routingMode", "AUTO")) } catch (e: Exception) { RoutingMode.AUTO }
            val eqMode = try { EQMode.valueOf(obj.optString("eqMode", "GRAPHIC_10")) } catch (e: Exception) { EQMode.GRAPHIC_10 }
            val currentPresetId = obj.optString("currentPresetId", "flat")
            val preampGain = obj.optDouble("preampGain", 0.0).toFloat()

            val bands = mutableListOf<EQBand>()
            val bandsArray = obj.optJSONArray("bands")
            if (bandsArray != null) {
                for (i in 0 until bandsArray.length()) {
                    val bObj = bandsArray.getJSONObject(i)
                    bands.add(
                        EQBand(
                            index = bObj.optInt("index", i),
                            frequency = bObj.optDouble("frequency", 1000.0).toFloat(),
                            gain = bObj.optDouble("gain", 0.0).toFloat(),
                            q = bObj.optDouble("q", 1.414).toFloat(),
                            type = try { FilterType.valueOf(bObj.optString("type", "PEAK")) } catch (e: Exception) { FilterType.PEAK }
                        )
                    )
                }
            } else {
                bands.addAll(DSPSettings.default10Bands())
            }

            val bassBoostEnabled = obj.optBoolean("bassBoostEnabled", false)
            val bassBoostGain = obj.optDouble("bassBoostGain", 0.0).toFloat()
            val bassBoostCutoffFreq = obj.optDouble("bassBoostCutoffFreq", 80.0).toFloat()

            val virtualizerEnabled = obj.optBoolean("virtualizerEnabled", false)
            val virtualizerStrength = obj.optDouble("virtualizerStrength", 0.0).toFloat()

            val crossfeedEnabled = obj.optBoolean("crossfeedEnabled", false)
            val crossfeedStrength = obj.optDouble("crossfeedStrength", 0.5).toFloat()

            val clarityEnabled = obj.optBoolean("clarityEnabled", false)
            val clarityGain = obj.optDouble("clarityGain", 0.0).toFloat()

            val limiterEnabled = obj.optBoolean("limiterEnabled", true)
            val channelBalance = obj.optDouble("channelBalance", 0.0).toFloat()
            val isMono = obj.optBoolean("isMono", false)
            val isVisualizerEnabled = obj.optBoolean("isVisualizerEnabled", true)

            DSPSettings(
                isEnabled = isEnabled,
                routingMode = routingMode,
                eqMode = eqMode,
                currentPresetId = currentPresetId,
                preampGain = preampGain,
                bands = bands,
                bassBoostEnabled = bassBoostEnabled,
                bassBoostGain = bassBoostGain,
                bassBoostCutoffFreq = bassBoostCutoffFreq,
                virtualizerEnabled = virtualizerEnabled,
                virtualizerStrength = virtualizerStrength,
                crossfeedEnabled = crossfeedEnabled,
                crossfeedStrength = crossfeedStrength,
                clarityEnabled = clarityEnabled,
                clarityGain = clarityGain,
                limiterEnabled = limiterEnabled,
                channelBalance = channelBalance,
                isMono = isMono,
                isVisualizerEnabled = isVisualizerEnabled
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Failed parsing saved DSPSettings", e)
            DSPSettings()
        }
    }

    fun saveSettings(context: Context, settings: DSPSettings) {
        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val obj = JSONObject().apply {
                put("isEnabled", settings.isEnabled)
                put("routingMode", settings.routingMode.name)
                put("eqMode", settings.eqMode.name)
                put("currentPresetId", settings.currentPresetId)
                put("preampGain", settings.preampGain.toDouble())

                val bandsArray = JSONArray()
                for (band in settings.bands) {
                    val bObj = JSONObject().apply {
                        put("index", band.index)
                        put("frequency", band.frequency.toDouble())
                        put("gain", band.gain.toDouble())
                        put("q", band.q.toDouble())
                        put("type", band.type.name)
                    }
                    bandsArray.put(bObj)
                }
                put("bands", bandsArray)

                put("bassBoostEnabled", settings.bassBoostEnabled)
                put("bassBoostGain", settings.bassBoostGain.toDouble())
                put("bassBoostCutoffFreq", settings.bassBoostCutoffFreq.toDouble())

                put("virtualizerEnabled", settings.virtualizerEnabled)
                put("virtualizerStrength", settings.virtualizerStrength.toDouble())

                put("crossfeedEnabled", settings.crossfeedEnabled)
                put("crossfeedStrength", settings.crossfeedStrength.toDouble())

                put("clarityEnabled", settings.clarityEnabled)
                put("clarityGain", settings.clarityGain.toDouble())

                put("limiterEnabled", settings.limiterEnabled)
                put("channelBalance", settings.channelBalance.toDouble())
                put("isMono", settings.isMono)
                put("isVisualizerEnabled", settings.isVisualizerEnabled)
            }
            prefs.edit().putString(KEY_SETTINGS_JSON, obj.toString()).apply()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed saving DSPSettings", e)
        }
    }
}
