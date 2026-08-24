package com.timbre.dsp.ui.main

import com.timbre.dsp.data.AutoEqRepository
import com.timbre.dsp.data.EqualizerApoParser
import com.timbre.dsp.data.PresetRepository
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.EQMode
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.FilterType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
