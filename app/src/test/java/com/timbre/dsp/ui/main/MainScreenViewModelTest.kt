package com.timbre.dsp.ui.main

import com.timbre.dsp.audio.WavReader
import com.timbre.dsp.data.AutoEqRepository
import com.timbre.dsp.data.EqualizerApoParser
import com.timbre.dsp.data.PresetRepository
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.EQMode
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.FilterType
import com.timbre.dsp.model.TargetCurve
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DSPRepositoryTest {

  @Test
  fun testBuiltInPresetsExist() {
    val presets = PresetRepository.builtInPresets
    assertTrue(presets.isNotEmpty())
    
    val flat = PresetRepository.getPresetById("flat")
    assertNotNull(flat)
    assertEquals(10, flat!!.bands.size)
    assertTrue(flat.bands.all { it.gain == 0f })
  }

  @Test
  fun testCustomPresetCreationAndDeletion() {
    val customSettings = DSPSettings(preampGain = 3.5f)
    val saved = PresetRepository.saveCustomPreset("Test Custom", customSettings)
    assertNotNull(saved)
    assertEquals("Test Custom", saved.name)
    assertEquals(3.5f, saved.preampGain)

    val fetched = PresetRepository.getPresetById(saved.id)
    assertNotNull(fetched)

    PresetRepository.deleteCustomPreset(saved.id)
    val afterDelete = PresetRepository.getPresetById(saved.id)
    assertEquals(null, afterDelete)
  }

  @Test
  fun testAutoEqSearch() {
    val all = AutoEqRepository.profiles
    assertTrue(all.isNotEmpty())

    val sonyMatches = AutoEqRepository.search("Sony")
    assertTrue(sonyMatches.isNotEmpty())
    assertTrue(sonyMatches.all { it.brand.equals("Sony", ignoreCase = true) })

    val airpodsMatches = AutoEqRepository.search("AirPods")
    assertTrue(airpodsMatches.isNotEmpty())
  }

  @Test
  fun testEqualizerApoParser() {
    val sampleApo = """
      Preamp: -3.5 dB
      Filter 1: ON PK Fc 31.0 Hz Gain 4.5 dB Q 1.41
      Filter 2: ON LSC Fc 105.0 Hz Gain 6.0 dB Q 0.71
      Filter 3: ON HSC Fc 10000.0 Hz Gain -2.5 dB Q 0.71
      Filter 4: OFF NO Fc 60.0 Hz Gain 0.0 dB Q 10.0
    """.trimIndent()

    val parsed = EqualizerApoParser.parse(sampleApo, "Test Preset")
    assertEquals("Test Preset", parsed.name)
    assertEquals(-3.5f, parsed.preampGain, 0.01f)
    assertEquals(4, parsed.bands.size)

    assertEquals(FilterType.PEAK, parsed.bands[0].type)
    assertEquals(31.0f, parsed.bands[0].frequency, 0.01f)
    assertEquals(4.5f, parsed.bands[0].gain, 0.01f)
    assertTrue(parsed.bands[0].enabled)

    assertEquals(FilterType.LOW_SHELF, parsed.bands[1].type)
    assertEquals(105.0f, parsed.bands[1].frequency, 0.01f)
    assertEquals(6.0f, parsed.bands[1].gain, 0.01f)

    assertEquals(FilterType.HIGH_SHELF, parsed.bands[2].type)
    assertEquals(FilterType.NOTCH, parsed.bands[3].type)
    assertEquals(false, parsed.bands[3].enabled)
  }

  @Test
  fun testEqualizerApoExport() {
    val preset = EQPreset(
      id = "export_test",
      name = "Export Test",
      preampGain = -2.0f,
      bands = listOf(
        EQBand(0, 100f, 3.0f, 1.414f, FilterType.PEAK),
        EQBand(1, 10000f, -1.5f, 0.71f, FilterType.HIGH_SHELF)
      )
    )

    val exported = EqualizerApoParser.exportToPeace(preset)
    assertTrue(exported.contains("Preamp: -2.0 dB"))
    assertTrue(exported.contains("Filter 1: ON PK Fc 100.0 Hz Gain 3.0 dB Q 1.41"))
    assertTrue(exported.contains("Filter 2: ON HSC Fc 10000.0 Hz Gain -1.5 dB Q 0.71"))
  }

  @Test
  fun testTargetCurveValues() {
    val curves = TargetCurve.values()
    assertTrue(curves.contains(TargetCurve.HARMAN_OVER_EAR))
    assertTrue(curves.contains(TargetCurve.HARMAN_IN_EAR))
    assertTrue(curves.contains(TargetCurve.IEF_NEUTRAL))
    assertTrue(curves.contains(TargetCurve.DIFFUSE_FIELD))
  }

  @Test
  fun testWavReaderParsing16BitPcm() {
    // Generate valid RIFF WAVE buffer
    val numSamples = 64
    val dataBytes = numSamples * 2 * 2 // 2 channels, 16-bit (2 bytes)
    val totalSize = 36 + dataBytes

    val buffer = ByteBuffer.allocate(44 + dataBytes).order(ByteOrder.LITTLE_ENDIAN)
    buffer.put("RIFF".toByteArray())
    buffer.putInt(totalSize)
    buffer.put("WAVE".toByteArray())
    buffer.put("fmt ".toByteArray())
    buffer.putInt(16) // chunk size
    buffer.putShort(1) // PCM format
    buffer.putShort(2) // 2 channels
    buffer.putInt(48000) // sample rate
    buffer.putInt(48000 * 4) // byte rate
    buffer.putShort(4) // block align
    buffer.putShort(16) // bits per sample
    buffer.put("data".toByteArray())
    buffer.putInt(dataBytes)

    // Write samples (sine / impulse)
    for (i in 0 until numSamples) {
      val sample = if (i == 0) 16000.toShort() else 0.toShort()
      buffer.putShort(sample) // Left
      buffer.putShort(sample) // Right
    }

    val inputStream = ByteArrayInputStream(buffer.array())
    val parsed = WavReader.parseWav(inputStream)

    assertNotNull(parsed)
    assertEquals(48000, parsed!!.sampleRate)
    assertEquals(2, parsed.numChannels)
    assertEquals(numSamples, parsed.leftChannel.size)
    assertEquals(numSamples, parsed.rightChannel.size)
    assertTrue(parsed.leftChannel[0] > 0.5f) // Normalized peak near 0.89
  }

  @Test
  fun testAiClientDefaultsAndModels() {
    val geminiDefault = com.timbre.dsp.data.api.AiClient.getDefaultModel(com.timbre.dsp.data.api.AiProvider.GEMINI)
    assertEquals("gemini-3.6-flash", geminiDefault)

    val groqModels = com.timbre.dsp.data.api.AiClient.getAvailableModels(com.timbre.dsp.data.api.AiProvider.GROQ)
    assertTrue(groqModels.any { it.id.contains("gpt-oss-120b") })
  }

  @Test
  fun testAiEqAssistantApplyToSettings() {
    val aiResponse = com.timbre.dsp.data.api.AiEqResponse(
      title = "Warm Studio Acoustic",
      description = "Rich velvety bass with crystal vocals",
      bands = listOf(3.5f, 2.0f, 1.0f, 0.0f, 0.0f, 0.5f, 1.5f, 2.0f, 2.5f, 3.0f),
      preamp = -3.5f,
      bassBoost = 500,
      clarityBoost = 400
    )

    val initialSettings = DSPSettings(eqMode = EQMode.GRAPHIC_10)
    val applied = com.timbre.dsp.audio.AiEqAssistant.applyToSettings(aiResponse, initialSettings)

    assertEquals(-3.5f, applied.preampGain, 0.01f)
    assertEquals(3.5f, applied.bands[0].gain, 0.01f)
    assertTrue(applied.bassBoostEnabled)
    assertEquals(6.0f, applied.bassBoostGain, 0.01f)
    assertTrue(applied.clarityEnabled)
    assertEquals(4.8f, applied.clarityGain, 0.01f)
    assertTrue(applied.currentPresetId.contains("warm_studio_acoustic"))
  }
}
