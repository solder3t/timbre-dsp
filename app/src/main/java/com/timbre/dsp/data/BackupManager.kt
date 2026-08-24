package com.timbre.dsp.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.timbre.dsp.model.DSPSettings
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object BackupManager {

    private const val TAG = "BackupManager"

    fun exportBackup(context: Context, outputUri: Uri): Boolean {
        return try {
            val dspSettings = SettingsRepository.loadSettings(context)
            val customPresets = PresetRepository.customPresetsList

            val devicePrefs = context.getSharedPreferences("timbre_device_profiles_prefs", Context.MODE_PRIVATE)
            val deviceProfilesJson = devicePrefs.getString("key_device_profiles_json", "[]")

            val appPrefs = context.getSharedPreferences("timbre_app_profiles_prefs", Context.MODE_PRIVATE)
            val appProfilesJson = appPrefs.getString("key_app_profiles_json", "[]")

            val rootObj = JSONObject().apply {
                put("timbre_backup_version", 1)
                put("timestamp", System.currentTimeMillis())

                // 1. DSP Settings
                val dspObj = JSONObject().apply {
                    put("isEnabled", dspSettings.isEnabled)
                    put("routingMode", dspSettings.routingMode.name)
                    put("eqMode", dspSettings.eqMode.name)
                    put("currentPresetId", dspSettings.currentPresetId)
                    put("preampGain", dspSettings.preampGain.toDouble())
                    put("autoPreampEnabled", dspSettings.autoPreampEnabled)
                    put("targetCurve", dspSettings.targetCurve.name)

                    val bandsArray = JSONArray()
                    for (band in dspSettings.bands) {
                        val bObj = JSONObject().apply {
                            put("index", band.index)
                            put("frequency", band.frequency.toDouble())
                            put("gain", band.gain.toDouble())
                            put("q", band.q.toDouble())
                            put("type", band.type.name)
                            put("enabled", band.enabled)
                        }
                        bandsArray.put(bObj)
                    }
                    put("bands", bandsArray)

                    put("bassBoostEnabled", dspSettings.bassBoostEnabled)
                    put("bassBoostGain", dspSettings.bassBoostGain.toDouble())
                    put("virtualizerEnabled", dspSettings.virtualizerEnabled)
                    put("virtualizerStrength", dspSettings.virtualizerStrength.toDouble())
                    put("crossfeedEnabled", dspSettings.crossfeedEnabled)
                    put("crossfeedStrength", dspSettings.crossfeedStrength.toDouble())
                    put("clarityEnabled", dspSettings.clarityEnabled)
                    put("clarityGain", dspSettings.clarityGain.toDouble())
                    put("convolutionEnabled", dspSettings.convolutionEnabled)
                    put("convolutionWetDry", dspSettings.convolutionWetDry.toDouble())
                    put("activeConvolutionId", dspSettings.activeConvolutionId)
                    put("limiterEnabled", dspSettings.limiterEnabled)
                    put("channelBalance", dspSettings.channelBalance.toDouble())
                    put("isMono", dspSettings.isMono)
                }
                put("dspSettings", dspObj)

                // 2. Custom Presets
                val presetsArray = JSONArray()
                for (preset in customPresets) {
                    val pObj = JSONObject().apply {
                        put("id", preset.id)
                        put("name", preset.name)
                        put("eqMode", preset.eqMode.name)
                        put("preampGain", preset.preampGain.toDouble())
                        put("bassBoostGain", preset.bassBoostGain.toDouble())
                        put("clarityGain", preset.clarityGain.toDouble())
                        put("virtualizerStrength", preset.virtualizerStrength.toDouble())
                        put("crossfeedStrength", preset.crossfeedStrength.toDouble())

                        val pBands = JSONArray()
                        for (band in preset.bands) {
                            val bObj = JSONObject().apply {
                                put("index", band.index)
                                put("frequency", band.frequency.toDouble())
                                put("gain", band.gain.toDouble())
                                put("q", band.q.toDouble())
                                put("type", band.type.name)
                                put("enabled", band.enabled)
                            }
                            pBands.put(bObj)
                        }
                        put("bands", pBands)
                    }
                    presetsArray.put(pObj)
                }
                put("customPresets", presetsArray)

                // 3. Device & App Profiles
                put("deviceProfiles", JSONArray(deviceProfilesJson))
                put("appProfiles", JSONArray(appProfilesJson))
            }

            context.contentResolver.openOutputStream(outputUri)?.use { output ->
                OutputStreamWriter(output).use { writer ->
                    writer.write(rootObj.toString(2))
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed exporting backup", e)
            false
        }
    }

    fun importBackup(context: Context, inputUri: Uri): Boolean {
        return try {
            val jsonText = context.contentResolver.openInputStream(inputUri)?.use { input ->
                BufferedReader(InputStreamReader(input)).readText()
            } ?: return false

            val rootObj = JSONObject(jsonText)
            if (!rootObj.has("timbre_backup_version") && !rootObj.has("dspSettings")) return false

            // 1. Restore Custom Presets
            if (rootObj.has("customPresets")) {
                val presetsArray = rootObj.getJSONArray("customPresets")
                for (i in 0 until presetsArray.length()) {
                    val pObj = presetsArray.getJSONObject(i)
                    val name = pObj.getString("name")
                    val dsp = SettingsRepository.loadSettings(context).copy(
                        preampGain = pObj.optDouble("preampGain", 0.0).toFloat(),
                        bassBoostGain = pObj.optDouble("bassBoostGain", 0.0).toFloat(),
                        clarityGain = pObj.optDouble("clarityGain", 0.0).toFloat(),
                        virtualizerStrength = pObj.optDouble("virtualizerStrength", 0.0).toFloat(),
                        crossfeedStrength = pObj.optDouble("crossfeedStrength", 0.0).toFloat()
                    )
                    PresetRepository.saveCustomPreset(name, dsp)
                }
            }

            // 2. Restore Device Profiles
            if (rootObj.has("deviceProfiles")) {
                val deviceProfilesArray = rootObj.getJSONArray("deviceProfiles")
                val devicePrefs = context.getSharedPreferences("timbre_device_profiles_prefs", Context.MODE_PRIVATE)
                devicePrefs.edit().putString("key_device_profiles_json", deviceProfilesArray.toString()).apply()
            }

            // 3. Restore App Profiles
            if (rootObj.has("appProfiles")) {
                val appProfilesArray = rootObj.getJSONArray("appProfiles")
                val appPrefs = context.getSharedPreferences("timbre_app_profiles_prefs", Context.MODE_PRIVATE)
                appPrefs.edit().putString("key_app_profiles_json", appProfilesArray.toString()).apply()
            }

            // 4. Restore DSP Settings
            if (rootObj.has("dspSettings")) {
                val dspObj = rootObj.getJSONObject("dspSettings")
                val settingsPrefs = context.getSharedPreferences("timbre_dsp_settings_prefs", Context.MODE_PRIVATE)
                settingsPrefs.edit().putString("key_dsp_settings_json", dspObj.toString()).apply()
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed importing backup", e)
            false
        }
    }
}
