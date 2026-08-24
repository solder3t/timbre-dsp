package com.timbre.dsp.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.EQMode
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.FilterType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

object PresetRepository {

    private const val TAG = "PresetRepository"
    private const val PREFS_NAME = "timbre_custom_presets_prefs"
    private const val KEY_CUSTOM_PRESETS = "key_custom_presets_json"

    private val defaultFrequencies = listOf(31.25f, 62.5f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)

    private fun createPreset(id: String, name: String, gains: List<Float>, bassBoostGain: Float = 0f, clarityGain: Float = 0f): EQPreset {
        val bands = defaultFrequencies.mapIndexed { index, freq ->
            EQBand(
                index = index,
                frequency = freq,
                gain = gains.getOrElse(index) { 0f },
                q = 1.414f,
                type = FilterType.PEAK
            )
        }
        return EQPreset(
            id = id,
            name = name,
            isCustom = false,
            preampGain = 0f,
            bands = bands,
            bassBoostGain = bassBoostGain,
            clarityGain = clarityGain
        )
    }

    val builtInPresets: List<EQPreset> = listOf(
        createPreset("flat", "Flat", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)),
        createPreset("bass_boost", "Bass Boost", listOf(6.5f, 5.5f, 4.0f, 2.5f, 1.0f, 0f, 0f, 0f, 0f, 0f), bassBoostGain = 6f),
        createPreset("bass_reducer", "Bass Reducer", listOf(-6.0f, -5.0f, -3.5f, -2.0f, -1.0f, 0f, 0f, 0f, 0f, 0f)),
        createPreset("acoustic", "Acoustic", listOf(4.5f, 4.0f, 2.5f, 1.0f, 1.5f, 2.0f, 3.5f, 4.0f, 3.0f, 2.0f)),
        createPreset("classical", "Classical", listOf(4.5f, 3.5f, 3.0f, 2.5f, -1.5f, -1.5f, 0f, 2.0f, 3.5f, 4.0f)),
        createPreset("electronic", "Electronic / EDM", listOf(5.5f, 4.5f, 2.0f, 0f, -2.0f, 2.0f, 1.0f, 2.5f, 4.5f, 5.0f), bassBoostGain = 4f),
        createPreset("hiphop", "Hip-Hop", listOf(6.0f, 5.0f, 2.5f, 1.5f, -1.0f, -1.0f, 1.5f, -0.5f, 2.5f, 3.5f), bassBoostGain = 5f),
        createPreset("jazz", "Jazz", listOf(3.5f, 2.5f, 1.0f, 2.0f, -1.5f, -1.5f, 0f, 1.5f, 3.0f, 4.0f)),
        createPreset("pop", "Pop", listOf(-1.5f, -1.0f, 1.0f, 3.0f, 4.5f, 4.0f, 2.0f, -1.0f, -1.5f, -2.0f), clarityGain = 2f),
        createPreset("rock", "Rock", listOf(5.0f, 3.5f, -1.5f, -3.0f, -1.0f, 2.5f, 5.5f, 7.0f, 7.0f, 7.0f)),
        createPreset("vocal_booster", "Vocal Booster", listOf(-2.0f, -3.0f, -1.5f, 1.5f, 4.0f, 5.0f, 4.5f, 2.5f, 0f, -2.0f), clarityGain = 4f),
        createPreset("spoken_word", "Podcast / Voice", listOf(-4.0f, -2.0f, 0f, 2.5f, 4.5f, 4.5f, 3.0f, 1.0f, -2.0f, -4.0f)),
        createPreset("treble_booster", "Treble Booster", listOf(0f, 0f, 0f, 0f, 0f, 1.5f, 3.0f, 5.0f, 7.0f, 8.5f), clarityGain = 5f),
        createPreset("rnb", "R&B", listOf(3.0f, 6.0f, 5.0f, 2.0f, -1.5f, 1.0f, 2.5f, 3.0f, 3.5f, 4.0f))
    )

    private val customPresets = mutableListOf<EQPreset>()
    val customPresetsList: List<EQPreset>
        get() = customPresets.toList()

    private val _presets = MutableStateFlow<List<EQPreset>>(builtInPresets)
    val presets: StateFlow<List<EQPreset>> = _presets.asStateFlow()

    fun init(context: Context) {
        loadCustomPresets(context)
    }

    fun loadCustomPresets(context: Context) {
        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_CUSTOM_PRESETS, null) ?: return
            val array = JSONArray(jsonStr)
            customPresets.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val eqMode = try { EQMode.valueOf(obj.optString("eqMode", "GRAPHIC_10")) } catch (e: Exception) { EQMode.GRAPHIC_10 }
                val preamp = obj.optDouble("preampGain", 0.0).toFloat()
                val bassBoostGain = obj.optDouble("bassBoostGain", 0.0).toFloat()
                val clarityGain = obj.optDouble("clarityGain", 0.0).toFloat()
                val virtualizerStrength = obj.optDouble("virtualizerStrength", 0.0).toFloat()
                val crossfeedStrength = obj.optDouble("crossfeedStrength", 0.0).toFloat()

                val bands = mutableListOf<EQBand>()
                val bandsArr = obj.optJSONArray("bands")
                if (bandsArr != null) {
                    for (b in 0 until bandsArr.length()) {
                        val bObj = bandsArr.getJSONObject(b)
                        bands.add(
                            EQBand(
                                index = bObj.optInt("index", b),
                                frequency = bObj.optDouble("frequency", 1000.0).toFloat(),
                                gain = bObj.optDouble("gain", 0.0).toFloat(),
                                q = bObj.optDouble("q", 1.414).toFloat(),
                                type = try { FilterType.valueOf(bObj.optString("type", "PEAK")) } catch (e: Exception) { FilterType.PEAK },
                                enabled = bObj.optBoolean("enabled", true)
                            )
                        )
                    }
                }
                customPresets.add(
                    EQPreset(
                        id = id,
                        name = name,
                        isCustom = true,
                        eqMode = eqMode,
                        preampGain = preamp,
                        bands = bands,
                        bassBoostGain = bassBoostGain,
                        clarityGain = clarityGain,
                        virtualizerStrength = virtualizerStrength,
                        crossfeedStrength = crossfeedStrength
                    )
                )
            }
            _presets.value = builtInPresets + customPresets
        } catch (e: Throwable) {
            Log.e(TAG, "Error loading custom presets", e)
        }
    }

    private fun persistToDisk(context: Context?) {
        if (context == null) return
        try {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val array = JSONArray()
            for (p in customPresets) {
                val obj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("eqMode", p.eqMode.name)
                    put("preampGain", p.preampGain.toDouble())
                    put("bassBoostGain", p.bassBoostGain.toDouble())
                    put("clarityGain", p.clarityGain.toDouble())
                    put("virtualizerStrength", p.virtualizerStrength.toDouble())
                    put("crossfeedStrength", p.crossfeedStrength.toDouble())

                    val bandsArr = JSONArray()
                    for (b in p.bands) {
                        val bObj = JSONObject().apply {
                            put("index", b.index)
                            put("frequency", b.frequency.toDouble())
                            put("gain", b.gain.toDouble())
                            put("q", b.q.toDouble())
                            put("type", b.type.name)
                            put("enabled", b.enabled)
                        }
                        bandsArr.put(bObj)
                    }
                    put("bands", bandsArr)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_CUSTOM_PRESETS, array.toString()).apply()
        } catch (e: Throwable) {
            Log.e(TAG, "Error persisting custom presets", e)
        }
    }

    fun getPresetById(id: String): EQPreset? {
        if (id.isBlank()) return null
        val direct = _presets.value.find { it.id == id }
        if (direct != null) return direct

        val builtIn = builtInPresets.find { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
        if (builtIn != null) return builtIn

        val custom = customPresets.find { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) }
        if (custom != null) return custom

        if (id.startsWith("autoeq_")) {
            val modelName = id.removePrefix("autoeq_")
            val autoEq = AutoEqRepository.profiles.find { it.model.equals(modelName, ignoreCase = true) }
            if (autoEq != null) {
                return EQPreset(
                    id = id,
                    name = autoEq.model,
                    bands = autoEq.bands,
                    preampGain = autoEq.preampGain
                )
            }
        }

        return null
    }

    fun refreshPresets(context: Context? = null) {
        if (context != null) {
            loadCustomPresets(context)
        }
        _presets.value = builtInPresets + customPresets
    }

    fun saveCustomPreset(name: String, settings: DSPSettings, context: Context? = null): EQPreset {
        val newPreset = EQPreset(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            eqMode = settings.eqMode,
            isCustom = true,
            preampGain = settings.preampGain,
            bands = settings.bands.map { it.copy() },
            bassBoostGain = settings.bassBoostGain,
            clarityGain = settings.clarityGain,
            virtualizerStrength = settings.virtualizerStrength,
            crossfeedStrength = settings.crossfeedStrength
        )
        customPresets.add(newPreset)
        _presets.value = builtInPresets + customPresets
        persistToDisk(context)
        return newPreset
    }

    fun addCustomPreset(preset: EQPreset, context: Context? = null): EQPreset {
        val toAdd = if (!preset.isCustom) preset.copy(isCustom = true) else preset
        customPresets.removeAll { it.id == toAdd.id || it.name.equals(toAdd.name, ignoreCase = true) }
        customPresets.add(toAdd)
        _presets.value = builtInPresets + customPresets
        persistToDisk(context)
        return toAdd
    }

    fun deleteCustomPreset(id: String, context: Context? = null) {
        customPresets.removeAll { it.id == id }
        _presets.value = builtInPresets + customPresets
        persistToDisk(context)
    }
}

