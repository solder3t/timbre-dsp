package com.timbre.dsp.data

import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.EQMode
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.FilterType
import java.util.Locale
import java.util.regex.Pattern

object EqualizerApoParser {

    private val preampPattern = Pattern.compile("Preamp:\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*dB", Pattern.CASE_INSENSITIVE)
    // Filter 1: ON PK Fc 1000 Hz Gain 3.0 dB Q 1.41
    private val filterPattern = Pattern.compile(
        "Filter\\s*\\d*:\\s*(ON|OFF)\\s+([A-Z0-9]+)\\s+Fc\\s+([\\d.]+)\\s*Hz\\s+Gain\\s+([+-]?[\\d.]+)\\s*dB(?:\\s+Q\\s+([\\d.]+))?",
        Pattern.CASE_INSENSITIVE
    )

    fun parse(text: String, presetName: String = "Imported Preset"): EQPreset {
        var preamp = 0f
        val bands = mutableListOf<EQBand>()

        val lines = text.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("//")) continue

            val preampMatcher = preampPattern.matcher(trimmed)
            if (preampMatcher.find()) {
                preamp = preampMatcher.group(1)?.toFloatOrNull() ?: 0f
                continue
            }

            val filterMatcher = filterPattern.matcher(trimmed)
            if (filterMatcher.find()) {
                val state = filterMatcher.group(1)?.uppercase(Locale.ROOT) ?: "ON"
                val typeStr = filterMatcher.group(2)?.uppercase(Locale.ROOT) ?: "PK"
                val fc = filterMatcher.group(3)?.toFloatOrNull() ?: 1000f
                val gain = filterMatcher.group(4)?.toFloatOrNull() ?: 0f
                val q = filterMatcher.group(5)?.toFloatOrNull() ?: 1.414f

                val filterType = when (typeStr) {
                    "PK", "PEQ", "PEAK" -> FilterType.PEAK
                    "LSC", "LS", "LOWSHELF", "LOW_SHELF" -> FilterType.LOW_SHELF
                    "HSC", "HS", "HIGHSHELF", "HIGH_SHELF" -> FilterType.HIGH_SHELF
                    "LP", "LPQ", "LOWPASS", "LOW_PASS" -> FilterType.LOW_PASS
                    "HP", "HPQ", "HIGHPASS", "HIGH_PASS" -> FilterType.HIGH_PASS
                    "NO", "NOTCH" -> FilterType.NOTCH
                    "BP", "BANDPASS", "BAND_PASS" -> FilterType.BAND_PASS
                    else -> FilterType.PEAK
                }

                bands.add(
                    EQBand(
                        index = bands.size,
                        frequency = fc,
                        gain = gain,
                        q = q,
                        type = filterType,
                        enabled = state == "ON"
                    )
                )
            }
        }

        return EQPreset(
            id = "imported_${System.currentTimeMillis()}",
            name = presetName,
            isCustom = true,
            eqMode = if (bands.size == 10) EQMode.GRAPHIC_10 else EQMode.PARAMETRIC,
            preampGain = preamp,
            bands = if (bands.isNotEmpty()) bands else DSPSettings.default10Bands()
        )
    }

    fun exportToPeace(preset: EQPreset): String {
        val sb = StringBuilder()
        sb.appendLine("# Timbre DSP - Equalizer APO / Peace EQ Configuration")
        sb.appendLine("# Preset: ${preset.name}")
        sb.appendLine(String.format(Locale.US, "Preamp: %.1f dB", preset.preampGain))
        sb.appendLine()

        preset.bands.forEachIndexed { idx, band ->
            val typeStr = when (band.type) {
                FilterType.PEAK -> "PK"
                FilterType.LOW_SHELF -> "LSC"
                FilterType.HIGH_SHELF -> "HSC"
                FilterType.LOW_PASS -> "LP"
                FilterType.HIGH_PASS -> "HP"
                FilterType.NOTCH -> "NO"
                FilterType.BAND_PASS -> "BP"
            }
            val state = if (band.enabled) "ON" else "OFF"
            sb.appendLine(
                String.format(
                    Locale.US,
                    "Filter %d: %s %s Fc %.1f Hz Gain %.1f dB Q %.2f",
                    idx + 1,
                    state,
                    typeStr,
                    band.frequency,
                    band.gain,
                    band.q
                )
            )
        }

        return sb.toString()
    }
}
