package com.timbre.dsp.data

import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.EQMode
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.FilterType
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.regex.Pattern

data class ParsePreviewResult(
    val bandCount: Int,
    val preampGain: Float,
    val eqMode: EQMode,
    val isValid: Boolean
)

object EqualizerApoParser {

    private val preampPattern = Pattern.compile(
        "(?:Preamp|PreampGain|Pre-amp|Preamp_Gain)[:=]?\\s*([+-]?\\d+(?:\\.\\d+)?)\\s*(?:dB)?",
        Pattern.CASE_INSENSITIVE
    )

    // Standard Peace EQ / Equalizer APO line:
    // Filter 1: ON PK Fc 1000 Hz Gain 3.0 dB Q 1.41
    private val standardFilterPattern = Pattern.compile(
        "(?:Filter|Band)\\s*\\d*[:\\s]+(ON|OFF)?\\s*([A-Z0-9_-]+)?\\s*(?:Fc|Freq|Frequency)?[:=]?\\s*([\\d.]+)\\s*(Hz|kHz)?\\s*(?:Gain|GainDb)?[:=]?\\s*([+-]?[\\d.]+)\\s*(?:dB)?(?:\\s*(?:Q|Q-Factor|QFactor)?[:=]?\\s*([\\d.]+))?",
        Pattern.CASE_INSENSITIVE
    )

    fun parse(text: String, presetName: String = "Imported Preset"): EQPreset {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return EQPreset(
                id = "imported_${System.currentTimeMillis()}",
                name = presetName,
                isCustom = true,
                eqMode = EQMode.GRAPHIC_10,
                preampGain = 0f,
                bands = DSPSettings.default10Bands()
            )
        }

        // 1. Try parsing JSON format (Direct array or object)
        val jsonPreset = parseJson(trimmed, presetName)
        if (jsonPreset != null) return jsonPreset

        // 2. Try parsing GraphicEQ format
        val graphicEqPreset = parseGraphicEq(trimmed, presetName)
        if (graphicEqPreset != null) return graphicEqPreset

        // 3. Try parsing Peace / APO / AutoEQ text format
        var preamp = 0f
        val bands = mutableListOf<EQBand>()

        val lines = trimmed.lines()
        for (line in lines) {
            val lineTrimmed = line.trim()
            if (lineTrimmed.isEmpty() || lineTrimmed.startsWith("#") || lineTrimmed.startsWith("//")) continue

            val preampMatcher = preampPattern.matcher(lineTrimmed)
            if (preampMatcher.find()) {
                preamp = preampMatcher.group(1)?.toFloatOrNull() ?: preamp
                continue
            }

            val filterMatcher = standardFilterPattern.matcher(lineTrimmed)
            if (filterMatcher.find()) {
                val state = filterMatcher.group(1)?.uppercase(Locale.ROOT) ?: "ON"
                val typeStr = filterMatcher.group(2)?.uppercase(Locale.ROOT) ?: "PK"
                var fc = filterMatcher.group(3)?.toFloatOrNull() ?: 1000f
                val unit = filterMatcher.group(4)?.uppercase(Locale.ROOT)
                if (unit == "KHZ" || (fc < 30f && lineTrimmed.contains("kHz", ignoreCase = true))) {
                    fc *= 1000f
                }
                val gain = filterMatcher.group(5)?.toFloatOrNull() ?: 0f
                val q = filterMatcher.group(6)?.toFloatOrNull() ?: 1.414f

                val filterType = mapFilterType(typeStr)

                bands.add(
                    EQBand(
                        index = bands.size,
                        frequency = fc,
                        gain = gain,
                        q = q,
                        type = filterType,
                        enabled = state != "OFF"
                    )
                )
            }
        }

        // 4. If line-by-line standard pattern did not capture bands, try regex block parsing (Musaic block parser)
        if (bands.isEmpty()) {
            val blockRegex = Regex("(?i)(?:Band|Filter)\\s*\\d+[:\\s]+([\\s\\S]*?)(?=(?:Band|Filter)\\s*\\d+[:\\s]+|$)")
            val freqRegex = Regex("(?i)(?:Frequency|Fc|Freq)\\s*[:=]?\\s*([0-9.]+)\\s*(Hz|kHz)?")
            val gainRegex = Regex("(?i)(?:Gain|GainDb)\\s*[:=]?\\s*([+-]?[0-9.]+)\\s*(?:dB)?")
            val qRegex = Regex("(?i)(?:Q|Q-Factor|QFactor)\\s*[:=]?\\s*([0-9.]+)")
            val typeRegex = Regex("(?i)(?:Type|FilterType)\\s*[:=]?\\s*([A-Z0-9_-]+)")

            blockRegex.findAll(trimmed).forEach { matchResult ->
                val blockText = matchResult.groupValues[1]
                val freqMatch = freqRegex.find(blockText)
                val gainMatch = gainRegex.find(blockText)
                val qMatch = qRegex.find(blockText)
                val typeMatch = typeRegex.find(blockText)

                if (freqMatch != null && gainMatch != null) {
                    var freq = freqMatch.groupValues[1].toFloatOrNull() ?: 1000f
                    if (freqMatch.groupValues.getOrNull(2)?.equals("kHz", ignoreCase = true) == true || (freq < 30f && blockText.contains("kHz", ignoreCase = true))) {
                        freq *= 1000f
                    }
                    val gain = gainMatch.groupValues[1].toFloatOrNull() ?: 0f
                    val q = qMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 1.414f
                    val filterType = mapFilterType(typeMatch?.groupValues?.get(1) ?: "PK")

                    bands.add(
                        EQBand(
                            index = bands.size,
                            frequency = freq,
                            gain = gain,
                            q = q,
                            type = filterType,
                            enabled = true
                        )
                    )
                }
            }
        }

        // 5. If still empty, try CSV / Space-delimited rows: Freq Gain [Q] [Type]
        if (bands.isEmpty()) {
            for (line in lines) {
                val lineTrimmed = line.trim()
                if (lineTrimmed.isEmpty() || lineTrimmed.startsWith("#") || lineTrimmed.startsWith("//")) continue
                val parts = lineTrimmed.split(Regex("[,;\\s]+"))
                if (parts.size >= 2) {
                    val f = parts[0].toFloatOrNull()
                    val g = parts[1].toFloatOrNull()
                    val q = if (parts.size >= 3) parts[2].toFloatOrNull() ?: 1.414f else 1.414f
                    val t = if (parts.size >= 4) mapFilterType(parts[3]) else FilterType.PEAK
                    if (f != null && g != null && f in 10f..24000f && g in -30f..30f) {
                        bands.add(
                            EQBand(
                                index = bands.size,
                                frequency = f,
                                gain = g,
                                q = q,
                                type = t,
                                enabled = true
                            )
                        )
                    }
                }
            }
        }

        val finalBands = if (bands.isNotEmpty()) bands else DSPSettings.default10Bands()
        val eqMode = if (finalBands.size == 10 && finalBands.all { it.type == FilterType.PEAK }) EQMode.GRAPHIC_10 else EQMode.PARAMETRIC

        return EQPreset(
            id = "imported_${System.currentTimeMillis()}",
            name = presetName,
            isCustom = true,
            eqMode = eqMode,
            preampGain = preamp,
            bands = finalBands
        )
    }

    private fun mapFilterType(typeStr: String): FilterType {
        return when (typeStr.uppercase(Locale.ROOT)) {
            "PK", "PEQ", "PEAK" -> FilterType.PEAK
            "LSC", "LS", "LOWSHELF", "LOW_SHELF" -> FilterType.LOW_SHELF
            "HSC", "HS", "HIGHSHELF", "HIGH_SHELF" -> FilterType.HIGH_SHELF
            "LP", "LPQ", "LOWPASS", "LOW_PASS" -> FilterType.LOW_PASS
            "HP", "HPQ", "HIGHPASS", "HIGH_PASS" -> FilterType.HIGH_PASS
            "NO", "NOTCH" -> FilterType.NOTCH
            "BP", "BANDPASS", "BAND_PASS" -> FilterType.BAND_PASS
            else -> FilterType.PEAK
        }
    }

    private fun parseJson(text: String, presetName: String): EQPreset? {
        if (!text.startsWith("{") && !text.startsWith("[")) return null
        return try {
            val element = com.google.gson.JsonParser.parseString(text)
            if (element.isJsonArray) {
                val array = element.asJsonArray
                val bands = mutableListOf<EQBand>()
                for (i in 0 until array.size()) {
                    val item = array.get(i)
                    if (item.isJsonObject) {
                        val bObj = item.asJsonObject
                        val freq = if (bObj.has("frequency")) bObj.get("frequency").asFloat else 1000f
                        val gain = if (bObj.has("gain")) bObj.get("gain").asFloat else if (bObj.has("gainDb")) bObj.get("gainDb").asFloat else 0f
                        val q = if (bObj.has("q")) bObj.get("q").asFloat else 1.414f
                        val typeStr = if (bObj.has("type")) bObj.get("type").asString else "PEAK"
                        val enabled = if (bObj.has("enabled")) bObj.get("enabled").asBoolean else true
                        val index = if (bObj.has("index")) bObj.get("index").asInt else i
                        bands.add(
                            EQBand(
                                index = index,
                                frequency = freq,
                                gain = gain,
                                q = q,
                                type = mapFilterType(typeStr),
                                enabled = enabled
                            )
                        )
                    }
                }
                if (bands.isNotEmpty()) {
                    EQPreset(
                        id = "imported_${System.currentTimeMillis()}",
                        name = presetName,
                        isCustom = true,
                        eqMode = EQMode.PARAMETRIC,
                        preampGain = 0f,
                        bands = bands
                    )
                } else null
            } else if (element.isJsonObject) {
                val obj = element.asJsonObject
                val name = if (obj.has("name")) obj.get("name").asString else presetName
                val preamp = if (obj.has("preamp")) obj.get("preamp").asFloat else if (obj.has("preampGain")) obj.get("preampGain").asFloat else 0f
                val bands = mutableListOf<EQBand>()

                val bandsArray = if (obj.has("bands")) obj.getAsJsonArray("bands")
                else if (obj.has("parametricBands")) obj.getAsJsonArray("parametricBands")
                else null

                if (bandsArray != null) {
                    for (i in 0 until bandsArray.size()) {
                        val item = bandsArray.get(i)
                        if (item.isJsonObject) {
                            val bObj = item.asJsonObject
                            val freq = if (bObj.has("frequency")) bObj.get("frequency").asFloat else 1000f
                            val gain = if (bObj.has("gain")) bObj.get("gain").asFloat else if (bObj.has("gainDb")) bObj.get("gainDb").asFloat else 0f
                            val q = if (bObj.has("q")) bObj.get("q").asFloat else 1.414f
                            val typeStr = if (bObj.has("type")) bObj.get("type").asString else "PEAK"
                            val enabled = if (bObj.has("enabled")) bObj.get("enabled").asBoolean else true
                            val index = if (bObj.has("index")) bObj.get("index").asInt else i
                            bands.add(
                                EQBand(
                                    index = index,
                                    frequency = freq,
                                    gain = gain,
                                    q = q,
                                    type = mapFilterType(typeStr),
                                    enabled = enabled
                                )
                            )
                        } else if (item.isJsonPrimitive && item.asJsonPrimitive.isNumber) {
                            val defaultFreqs = listOf(31.25f, 62.5f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
                            bands.add(
                                EQBand(
                                    index = i,
                                    frequency = defaultFreqs.getOrElse(i) { 1000f },
                                    gain = item.asFloat,
                                    q = 1.414f,
                                    type = FilterType.PEAK,
                                    enabled = true
                                )
                            )
                        }
                    }
                }

                if (bands.isNotEmpty()) {
                    EQPreset(
                        id = "imported_${System.currentTimeMillis()}",
                        name = name,
                        isCustom = true,
                        eqMode = if (bands.size == 10) EQMode.GRAPHIC_10 else EQMode.PARAMETRIC,
                        preampGain = preamp,
                        bands = bands
                    )
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun parseGraphicEq(text: String, presetName: String): EQPreset? {
        if (!text.contains("GraphicEQ:", ignoreCase = true)) return null
        return try {
            val idx = text.indexOf("GraphicEQ:", ignoreCase = true)
            val content = text.substring(idx + "GraphicEQ:".length).trim()
            val points = content.split(";")
            val bands = mutableListOf<EQBand>()
            for (point in points) {
                val parts = point.trim().split(Regex("\\s+"))
                if (parts.size >= 2) {
                    val f = parts[0].toFloatOrNull()
                    val g = parts[1].toFloatOrNull()
                    if (f != null && g != null) {
                        bands.add(
                            EQBand(
                                index = bands.size,
                                frequency = f,
                                gain = g,
                                q = 1.414f,
                                type = FilterType.PEAK,
                                enabled = true
                            )
                        )
                    }
                }
            }
            if (bands.isNotEmpty()) {
                EQPreset(
                    id = "imported_${System.currentTimeMillis()}",
                    name = presetName,
                    isCustom = true,
                    eqMode = EQMode.PARAMETRIC,
                    preampGain = 0f,
                    bands = bands
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun parsePreview(text: String): ParsePreviewResult {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return ParsePreviewResult(0, 0f, EQMode.PARAMETRIC, isValid = false)
        }
        val preset = parse(trimmed)
        val hasBands = preset.bands.isNotEmpty()
        return ParsePreviewResult(
            bandCount = preset.bands.size,
            preampGain = preset.preampGain,
            eqMode = preset.eqMode,
            isValid = hasBands
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

