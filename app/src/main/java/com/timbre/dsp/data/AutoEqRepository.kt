package com.timbre.dsp.data

import com.timbre.dsp.model.AutoEqProfile
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.FilterType

object AutoEqRepository {

    private val defaultFrequencies = listOf(31.25f, 62.5f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)

    private fun createProfile(
        brand: String,
        model: String,
        gains: List<Float>,
        preamp: Float = -4.5f,
        source: String = "AutoEq (Harman Target)"
    ): AutoEqProfile {
        val bands = defaultFrequencies.mapIndexed { index, freq ->
            EQBand(
                index = index,
                frequency = freq,
                gain = gains.getOrElse(index) { 0f },
                q = 1.414f,
                type = FilterType.PEAK
            )
        }
        return AutoEqProfile(
            model = model,
            brand = brand,
            source = source,
            bands = bands,
            preampGain = preamp
        )
    }

    val profiles: List<AutoEqProfile> = listOf(
        // Sony Over-Ear & TWS
        createProfile("Sony", "WH-1000XM4 (Over-Ear)", listOf(-4.8f, -4.2f, -1.8f, -0.6f, 0.4f, 1.2f, 3.4f, 1.8f, -3.2f, -1.0f), preamp = -4.0f),
        createProfile("Sony", "WH-1000XM5 (Over-Ear)", listOf(-3.6f, -3.2f, -1.5f, 0.2f, 0.8f, 1.6f, 2.8f, 0.5f, -2.4f, 0.0f), preamp = -3.5f),
        createProfile("Sony", "WH-1000XM3 (Over-Ear)", listOf(-5.2f, -4.5f, -2.0f, -0.8f, 0.2f, 1.0f, 3.0f, 1.5f, -3.8f, -1.2f), preamp = -4.2f),
        createProfile("Sony", "WH-CH720N", listOf(-2.5f, -1.8f, -0.5f, 0.2f, 0.5f, 1.2f, 2.0f, -0.5f, -1.8f, 0.0f), preamp = -3.0f),
        createProfile("Sony", "WF-1000XM4 (TWS)", listOf(-2.4f, -1.8f, 0.2f, 0.5f, 0.0f, 1.5f, 4.2f, -1.2f, -4.0f, -0.5f), preamp = -4.5f),
        createProfile("Sony", "WF-1000XM5 (TWS)", listOf(-1.8f, -1.2f, 0.0f, 0.2f, 0.5f, 1.8f, 3.5f, -0.8f, -2.5f, 0.5f), preamp = -4.0f),
        createProfile("Sony", "LinkBuds S", listOf(-1.2f, -0.8f, 0.0f, 0.4f, 0.6f, 1.4f, 2.8f, -1.0f, -2.0f, 0.2f), preamp = -3.0f),
        createProfile("Sony", "MDR-7506 (Studio)", listOf(1.5f, 0.5f, -1.0f, -0.5f, 0.2f, 0.8f, -1.2f, -4.5f, -3.2f, 1.0f), preamp = -3.0f),
        createProfile("Sony", "MDR-1AM2", listOf(-3.0f, -2.2f, -0.8f, 0.2f, 0.5f, 1.0f, 2.4f, -0.5f, -2.0f, 0.5f), preamp = -3.2f),
        createProfile("Sony", "IER-M9 (IEM)", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.2f, -0.8f, -1.0f, 0.2f), preamp = -2.0f),

        // Apple & Beats
        createProfile("Apple", "AirPods Pro (1st Gen)", listOf(2.4f, 1.8f, 0.5f, -0.2f, -0.8f, 0.0f, 1.2f, -1.5f, 2.8f, 1.0f), preamp = -3.2f),
        createProfile("Apple", "AirPods Pro 2 (USB-C / Lightning)", listOf(1.5f, 1.2f, 0.2f, -0.4f, -0.2f, 0.4f, 0.8f, -0.5f, 1.8f, 0.5f), preamp = -2.5f),
        createProfile("Apple", "AirPods Max", listOf(0.8f, 0.5f, -0.4f, -0.8f, -0.2f, 0.6f, 2.2f, 0.5f, -1.8f, 1.5f), preamp = -2.8f),
        createProfile("Apple", "AirPods 3", listOf(4.2f, 3.0f, 1.0f, -0.5f, -0.8f, 0.2f, 1.5f, -2.0f, -1.5f, 1.0f), preamp = -4.5f),
        createProfile("Apple", "AirPods 2", listOf(6.5f, 4.8f, 2.0f, -0.2f, -0.5f, 0.5f, 1.8f, -1.2f, -2.0f, 0.5f), preamp = -6.5f),
        createProfile("Beats", "Studio Pro", listOf(-1.8f, -1.2f, -0.5f, 0.0f, 0.4f, 1.2f, 2.5f, -0.8f, -2.2f, 0.5f), preamp = -3.0f),
        createProfile("Beats", "Fit Pro", listOf(1.0f, 0.8f, 0.0f, -0.4f, -0.2f, 0.5f, 1.5f, -1.0f, 1.5f, 0.2f), preamp = -2.5f),
        createProfile("Beats", "Solo 4 Wireless", listOf(-2.5f, -1.8f, -0.8f, 0.2f, 0.5f, 1.0f, 2.2f, -1.2f, -3.0f, 0.0f), preamp = -3.0f),

        // Sennheiser
        createProfile("Sennheiser", "HD 600", listOf(5.8f, 4.5f, 2.2f, 0.4f, -0.5f, -0.8f, 0.2f, 2.4f, -1.5f, -3.0f), preamp = -6.0f),
        createProfile("Sennheiser", "HD 650 / HD 6XX", listOf(6.2f, 5.0f, 2.0f, -0.2f, -1.0f, -0.6f, 0.5f, 3.2f, -0.8f, -2.5f), preamp = -6.5f),
        createProfile("Sennheiser", "HD 560S", listOf(3.5f, 2.8f, 1.0f, 0.0f, -0.4f, -0.8f, -0.2f, -1.8f, -2.5f, 0.5f), preamp = -4.0f),
        createProfile("Sennheiser", "HD 660S2", listOf(4.2f, 3.5f, 1.5f, -0.2f, -0.6f, -0.4f, 0.8f, 2.8f, -1.0f, -2.0f), preamp = -5.0f),
        createProfile("Sennheiser", "HD 800 S", listOf(5.0f, 3.8f, 1.5f, 0.0f, -0.2f, 0.4f, -0.5f, -4.2f, -6.5f, 1.5f), preamp = -5.5f),
        createProfile("Sennheiser", "Momentum 4 Wireless", listOf(-4.5f, -3.8f, -1.2f, 0.5f, 0.8f, 1.2f, 2.5f, -1.0f, -3.5f, 0.0f), preamp = -3.5f),
        createProfile("Sennheiser", "IE 200 (IEM)", listOf(1.2f, 0.8f, 0.0f, -0.4f, -0.2f, 0.5f, 1.5f, -1.8f, -2.2f, 0.5f), preamp = -2.5f),
        createProfile("Sennheiser", "IE 600 (IEM)", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.4f, 0.8f, -2.5f, -3.0f, 1.0f), preamp = -2.0f),

        // Bose
        createProfile("Bose", "QuietComfort 35 II", listOf(1.2f, 0.8f, 0.0f, -0.5f, 0.2f, 1.5f, 3.0f, 0.8f, -1.5f, 1.0f), preamp = -3.5f),
        createProfile("Bose", "QuietComfort 45", listOf(2.0f, 1.5f, 0.2f, -0.2f, 0.0f, 0.8f, 1.5f, -2.8f, -4.5f, -1.0f), preamp = -3.0f),
        createProfile("Bose", "QuietComfort Ultra (Headphones)", listOf(0.5f, 0.2f, -0.5f, 0.0f, 0.4f, 1.2f, 2.0f, -0.5f, -1.8f, 0.8f), preamp = -2.5f),
        createProfile("Bose", "QuietComfort Ultra (Earbuds)", listOf(0.8f, 0.5f, -0.2f, 0.0f, 0.2f, 0.8f, 1.6f, -1.2f, -2.0f, 0.5f), preamp = -2.5f),
        createProfile("Bose", "Noise Cancelling 700", listOf(1.8f, 1.2f, 0.2f, -0.4f, 0.0f, 0.8f, 2.2f, 0.4f, -2.0f, 0.5f), preamp = -3.0f),

        // Samsung & Galaxy Buds
        createProfile("Samsung", "Galaxy Buds 2 Pro", listOf(0.8f, 0.5f, 0.0f, -0.4f, 0.2f, 0.8f, 1.2f, -0.5f, 1.5f, -0.5f), preamp = -2.0f),
        createProfile("Samsung", "Galaxy Buds FE", listOf(1.5f, 1.0f, 0.2f, -0.2f, 0.0f, 0.5f, 1.8f, -0.8f, 2.0f, 0.0f), preamp = -2.5f),
        createProfile("Samsung", "Galaxy Buds 3 Pro", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.6f, 1.0f, -0.4f, 1.2f, 0.2f), preamp = -1.8f),
        createProfile("Samsung", "Galaxy Buds Pro", listOf(0.5f, 0.2f, 0.0f, -0.2f, 0.4f, 1.0f, 1.8f, -1.0f, 1.2f, 0.0f), preamp = -2.2f),

        // Beyerdynamic
        createProfile("Beyerdynamic", "DT 770 Pro (80 Ohm)", listOf(-1.5f, -0.8f, 1.2f, 0.5f, -0.2f, 0.8f, 1.5f, -4.5f, -6.8f, -2.5f), preamp = -3.5f),
        createProfile("Beyerdynamic", "DT 770 Pro (250 Ohm)", listOf(-1.8f, -1.0f, 1.0f, 0.4f, -0.4f, 0.6f, 1.2f, -5.0f, -7.5f, -3.0f), preamp = -3.8f),
        createProfile("Beyerdynamic", "DT 990 Pro (250 Ohm)", listOf(3.2f, 2.5f, 0.8f, -0.5f, -0.2f, 0.5f, 1.2f, -5.2f, -8.0f, -4.0f), preamp = -4.0f),
        createProfile("Beyerdynamic", "DT 700 Pro X", listOf(-0.8f, -0.5f, 0.2f, 0.0f, 0.2f, 0.8f, 1.2f, -2.5f, -4.0f, -1.5f), preamp = -2.5f),
        createProfile("Beyerdynamic", "DT 900 Pro X", listOf(1.5f, 1.0f, 0.2f, -0.2f, 0.0f, 0.5f, 1.0f, -3.0f, -4.5f, -2.0f), preamp = -3.0f),
        createProfile("Beyerdynamic", "DT 1990 Pro (Balanced Pads)", listOf(2.0f, 1.2f, 0.4f, -0.2f, 0.0f, 0.6f, 1.2f, -4.2f, -6.5f, -2.5f), preamp = -3.5f),

        // Audio-Technica & AKG
        createProfile("Audio-Technica", "ATH-M50x", listOf(0.5f, -0.8f, -2.5f, -1.8f, 0.2f, 0.8f, -1.2f, -3.5f, -2.0f, 1.5f), preamp = -3.0f),
        createProfile("Audio-Technica", "ATH-M40x", listOf(1.2f, 0.4f, -1.2f, -0.8f, 0.0f, 0.6f, 0.8f, -2.2f, -1.5f, 1.0f), preamp = -2.5f),
        createProfile("Audio-Technica", "ATH-R70x", listOf(4.2f, 3.2f, 1.2f, 0.0f, -0.2f, 0.2f, 0.6f, 1.8f, -1.0f, -2.0f), preamp = -4.5f),
        createProfile("AKG", "K371", listOf(0.2f, 0.0f, -0.5f, 0.0f, 0.2f, 0.4f, 0.8f, -0.5f, -1.0f, 0.5f), preamp = -1.5f),
        createProfile("AKG", "K240 Studio", listOf(5.5f, 4.0f, 1.5f, -0.2f, -0.8f, -0.4f, 0.5f, 2.8f, -2.0f, -3.5f), preamp = -5.8f),
        createProfile("AKG", "K702", listOf(6.0f, 4.5f, 1.8f, 0.0f, -0.4f, 0.2f, 0.8f, -2.0f, -4.0f, -1.5f), preamp = -6.2f),

        // Hifiman Planar
        createProfile("Hifiman", "Sundara (2020)", listOf(3.8f, 2.8f, 1.0f, 0.0f, -0.2f, 0.4f, 1.2f, -1.5f, -2.8f, 0.8f), preamp = -4.2f),
        createProfile("Hifiman", "Edition XS", listOf(2.5f, 1.8f, 0.5f, -0.2f, 0.0f, 0.4f, 1.0f, -1.2f, -2.0f, 1.2f), preamp = -3.2f),
        createProfile("Hifiman", "Arya (Stealth Magnets)", listOf(2.0f, 1.4f, 0.4f, -0.2f, 0.0f, 0.5f, 1.2f, -1.0f, -2.2f, 1.5f), preamp = -3.0f),
        createProfile("Hifiman", "HE400se", listOf(4.2f, 3.0f, 1.2f, 0.0f, -0.2f, 0.2f, 0.8f, -1.8f, -3.0f, 0.5f), preamp = -4.5f),

        // Focal
        createProfile("Focal", "Bathys (Wireless)", listOf(0.4f, 0.0f, -0.6f, 0.0f, 0.2f, 0.8f, 1.5f, -0.8f, -1.5f, 0.5f), preamp = -2.2f),
        createProfile("Focal", "Clear", listOf(2.5f, 1.8f, 0.5f, -0.2f, 0.0f, 0.4f, 1.0f, -2.5f, -1.8f, 0.5f), preamp = -3.0f),
        createProfile("Focal", "Utopia (2022)", listOf(2.2f, 1.5f, 0.4f, -0.2f, 0.0f, 0.2f, 0.8f, -2.0f, -1.2f, 0.8f), preamp = -2.8f),

        // Chi-Fi & Audiophile IEMs
        createProfile("Moondrop", "Aria", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.5f, -2.0f, 1.0f), preamp = -2.0f),
        createProfile("Moondrop", "Blessing 2", listOf(2.0f, 1.5f, 0.5f, 0.0f, -0.2f, 0.2f, 0.8f, -1.0f, 1.5f, 0.5f), preamp = -2.5f),
        createProfile("Moondrop", "Blessing 3", listOf(1.2f, 0.8f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, 1.0f, 0.5f), preamp = -2.0f),
        createProfile("Moondrop", "Chu II", listOf(0.8f, 0.5f, 0.0f, -0.2f, 0.0f, 0.4f, 1.2f, -2.0f, -1.5f, 0.8f), preamp = -2.0f),
        createProfile("Moondrop", "Kato", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.2f, -1.2f, -1.8f, 0.5f), preamp = -2.0f),
        createProfile("Moondrop", "Variations", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.4f, 0.8f, -0.8f, 0.5f, 0.2f), preamp = -1.5f),
        createProfile("Truthear", "Zero:RED", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -0.8f, -1.2f, 0.4f), preamp = -1.8f),
        createProfile("Truthear", "Hexa", listOf(1.2f, 0.8f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.0f, 1.2f, 0.5f), preamp = -2.0f),
        createProfile("Truthear", "Nova", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.4f, 0.6f, -0.5f, 0.8f, 0.2f), preamp = -1.5f),
        createProfile("Tangzu", "Wan'er S.G", listOf(0.8f, 0.5f, 0.0f, -0.2f, 0.0f, 0.4f, 1.0f, -1.8f, -2.0f, 0.5f), preamp = -2.0f),
        createProfile("7Hz", "Salnotes Zero 2", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.2f, -1.5f, 0.5f), preamp = -1.8f),
        createProfile("Kiwi Ears", "Cadenza", listOf(0.6f, 0.4f, 0.0f, -0.2f, 0.0f, 0.5f, 1.2f, -1.5f, -1.8f, 0.4f), preamp = -2.0f),
        createProfile("Simgot", "EA500 LM", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.5f, -3.2f, 0.8f), preamp = -2.0f),
        createProfile("Shure", "SE215", listOf(-3.5f, -2.8f, -1.2f, 0.5f, 1.2f, 2.4f, 4.5f, 3.0f, -1.5f, -4.0f), preamp = -5.0f),

        // Anker & JBL
        createProfile("Anker", "Soundcore Space Q45", listOf(-2.8f, -2.0f, -0.8f, 0.2f, 0.5f, 1.2f, 2.5f, -1.0f, -2.8f, 0.2f), preamp = -3.2f),
        createProfile("Anker", "Soundcore Liberty 4 NC", listOf(-1.5f, -1.0f, 0.0f, 0.4f, 0.6f, 1.4f, 2.2f, -1.2f, -2.5f, 0.5f), preamp = -2.8f),
        createProfile("JBL", "Tune 760NC", listOf(0.5f, 0.0f, -0.8f, 0.0f, 0.4f, 1.0f, 2.0f, -0.8f, -1.5f, 0.5f), preamp = -2.5f)
    )

    fun search(query: String): List<AutoEqProfile> {
        if (query.isBlank()) return profiles
        val q = query.trim().lowercase()
        return profiles.filter {
            it.brand.lowercase().contains(q) || it.model.lowercase().contains(q)
        }
    }
}
