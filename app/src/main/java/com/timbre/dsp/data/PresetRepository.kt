package com.timbre.dsp.data

import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.FilterType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PresetRepository {

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
    private val _presets = MutableStateFlow<List<EQPreset>>(builtInPresets)
    val presets: StateFlow<List<EQPreset>> = _presets.asStateFlow()

    fun getPresetById(id: String): EQPreset? {
        return _presets.value.find { it.id == id }
    }

    fun saveCustomPreset(name: String, settings: DSPSettings): EQPreset {
        val newPreset = EQPreset(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            isCustom = true,
            preampGain = settings.preampGain,
            bands = settings.bands.map { it.copy() },
            bassBoostGain = settings.bassBoostGain,
            bassBoostFreq = settings.bassBoostCutoffFreq,
            virtualizerStrength = settings.virtualizerStrength,
            clarityGain = settings.clarityGain,
            crossfeedStrength = settings.crossfeedStrength,
            limiterEnabled = settings.limiterEnabled
        )
        customPresets.add(newPreset)
        _presets.value = builtInPresets + customPresets
        return newPreset
    }

    fun deleteCustomPreset(id: String) {
        customPresets.removeAll { it.id == id }
        _presets.value = builtInPresets + customPresets
    }
}
