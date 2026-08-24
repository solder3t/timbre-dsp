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
        category: String = "Over-Ear",
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
            preampGain = preamp,
            category = category
        )
    }

    private val rawProfiles: List<AutoEqProfile> = listOf(
        // ==========================================
        // 7Hz
        // ==========================================
        createProfile("7Hz", "Aurora", listOf(0.3f, 0.1f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.0f, -1.2f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("7Hz", "Legato", listOf(-2.5f, -1.8f, -0.6f, 0.2f, 0.5f, 1.2f, 2.0f, -1.2f, -2.5f, 0.4f), preamp = -3.0f, category = "IEM"),
        createProfile("7Hz", "Salnotes Zero", listOf(1.5f, 1.0f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.5f, -2.0f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("7Hz", "Salnotes Zero 2", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.2f, -1.5f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("7Hz", "Sonus", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.8f, -2.5f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("7Hz", "Timeless (Planar)", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.5f, 1.0f, -2.2f, -3.5f, 0.8f), preamp = -2.2f, category = "IEM"),

        // ==========================================
        // AFUL
        // ==========================================
        createProfile("AFUL", "Explorer", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.6f, 1.2f, -0.8f, -1.0f, 0.2f), preamp = -1.8f, category = "IEM"),
        createProfile("AFUL", "MagicOne", listOf(0.8f, 0.5f, 0.0f, 0.0f, 0.2f, 0.4f, 0.8f, -1.0f, -1.5f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("AFUL", "Performer 5", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.2f, -1.5f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("AFUL", "Performer 8", listOf(0.6f, 0.4f, 0.0f, 0.0f, 0.0f, 0.4f, 0.8f, -1.0f, -1.2f, 0.5f), preamp = -1.8f, category = "IEM"),

        // ==========================================
        // AKG
        // ==========================================
        createProfile("AKG", "K240 Studio", listOf(5.5f, 4.0f, 1.5f, -0.2f, -0.8f, -0.4f, 0.5f, 2.8f, -2.0f, -3.5f), preamp = -5.8f, category = "Over-Ear"),
        createProfile("AKG", "K371", listOf(0.2f, 0.0f, -0.5f, 0.0f, 0.2f, 0.4f, 0.8f, -0.5f, -1.0f, 0.5f), preamp = -1.5f, category = "Over-Ear"),
        createProfile("AKG", "K702", listOf(6.0f, 4.5f, 1.8f, 0.0f, -0.4f, 0.2f, 0.8f, -2.0f, -4.0f, -1.5f), preamp = -6.2f, category = "Over-Ear"),
        createProfile("AKG", "N5005", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.2f, -3.0f, 0.6f), preamp = -2.0f, category = "IEM"),

        // ==========================================
        // Anker / Soundcore
        // ==========================================
        createProfile("Anker", "Soundcore Boom 2", listOf(0.2f, -0.2f, -0.8f, -0.4f, 0.2f, 0.6f, 1.5f, -0.5f, -1.5f, 0.2f), preamp = -2.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Anker", "Soundcore Flare 2", listOf(1.5f, 0.8f, -0.5f, -0.2f, 0.2f, 0.6f, 1.2f, -0.6f, -1.5f, 0.2f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Anker", "Soundcore Glow", listOf(2.2f, 1.4f, -0.4f, -0.2f, 0.2f, 0.5f, 1.2f, -0.4f, -1.4f, 0.0f), preamp = -2.5f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Anker", "Soundcore Liberty 4 NC", listOf(-1.5f, -1.0f, 0.0f, 0.4f, 0.6f, 1.4f, 2.2f, -1.2f, -2.5f, 0.5f), preamp = -2.8f, category = "TWS Earbuds"),
        createProfile("Anker", "Soundcore Motion 300", listOf(1.8f, 1.0f, -0.4f, -0.2f, 0.2f, 0.6f, 1.2f, -0.8f, -1.8f, 0.4f), preamp = -2.4f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Anker", "Soundcore Motion Boom", listOf(0.5f, -0.2f, -1.0f, -0.5f, 0.2f, 0.8f, 1.8f, -0.5f, -1.5f, 0.2f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Anker", "Soundcore Motion+", listOf(1.2f, 0.5f, -0.6f, 0.0f, 0.2f, 0.5f, 1.0f, -1.2f, -2.2f, 0.5f), preamp = -2.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Anker", "Soundcore Space Q45", listOf(-2.8f, -2.0f, -0.8f, 0.2f, 0.5f, 1.2f, 2.5f, -1.0f, -2.8f, 0.2f), preamp = -3.2f, category = "Over-Ear"),

        // ==========================================
        // Apple
        // ==========================================
        createProfile("Apple", "AirPods (2nd Gen)", listOf(6.5f, 4.8f, 2.0f, -0.2f, -0.5f, 0.5f, 1.8f, -1.2f, -2.0f, 0.5f), preamp = -6.5f, category = "TWS Earbuds"),
        createProfile("Apple", "AirPods (3rd Gen)", listOf(4.2f, 3.0f, 1.0f, -0.5f, -0.8f, 0.2f, 1.5f, -2.0f, -1.5f, 1.0f), preamp = -4.5f, category = "TWS Earbuds"),
        createProfile("Apple", "AirPods Max", listOf(0.8f, 0.5f, -0.4f, -0.8f, -0.2f, 0.6f, 2.2f, 0.5f, -1.8f, 1.5f), preamp = -2.8f, category = "Over-Ear"),
        createProfile("Apple", "AirPods Pro (1st Gen)", listOf(2.4f, 1.8f, 0.5f, -0.2f, -0.8f, 0.0f, 1.2f, -1.5f, 2.8f, 1.0f), preamp = -3.2f, category = "TWS Earbuds"),
        createProfile("Apple", "AirPods Pro 2 (USB-C / Lightning)", listOf(1.5f, 1.2f, 0.2f, -0.4f, -0.2f, 0.4f, 0.8f, -0.5f, 1.8f, 0.5f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Apple", "EarPods (3.5mm / USB-C)", listOf(5.8f, 4.2f, 1.5f, -0.4f, -0.6f, 0.4f, 1.5f, -1.0f, -1.5f, 0.4f), preamp = -5.8f, category = "IEM"),

        // ==========================================
        // Artti
        // ==========================================
        createProfile("Artti", "T10 (Planar)", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.4f, 0.8f, -2.5f, -3.8f, 0.6f), preamp = -2.2f, category = "IEM"),

        // ==========================================
        // Audio-Technica
        // ==========================================
        createProfile("Audio-Technica", "ATH-M40x", listOf(1.2f, 0.4f, -1.2f, -0.8f, 0.0f, 0.6f, 0.8f, -2.2f, -1.5f, 1.0f), preamp = -2.5f, category = "Over-Ear"),
        createProfile("Audio-Technica", "ATH-M50x", listOf(0.5f, -0.8f, -2.5f, -1.8f, 0.2f, 0.8f, -1.2f, -3.5f, -2.0f, 1.5f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Audio-Technica", "ATH-R70x", listOf(4.2f, 3.2f, 1.2f, 0.0f, -0.2f, 0.2f, 0.6f, 1.8f, -1.0f, -2.0f), preamp = -4.5f, category = "Over-Ear"),

        // ==========================================
        // Bang & Olufsen (B&O)
        // ==========================================
        createProfile("Bang & Olufsen", "Beoplay EX", listOf(0.6f, 0.3f, -0.2f, 0.0f, 0.2f, 0.6f, 1.2f, -1.0f, -1.8f, 0.5f), preamp = -2.0f, category = "TWS Earbuds"),
        createProfile("Bang & Olufsen", "Beosound A1 (2nd Gen)", listOf(1.5f, 0.8f, -0.4f, -0.2f, 0.2f, 0.6f, 1.4f, -0.5f, -1.2f, 0.4f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),

        // ==========================================
        // Beats
        // ==========================================
        createProfile("Beats", "Fit Pro", listOf(1.0f, 0.8f, 0.0f, -0.4f, -0.2f, 0.5f, 1.5f, -1.0f, 1.5f, 0.2f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Beats", "Solo 4 Wireless", listOf(-2.5f, -1.8f, -0.8f, 0.2f, 0.5f, 1.0f, 2.2f, -1.2f, -3.0f, 0.0f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Beats", "Studio Pro", listOf(-1.8f, -1.2f, -0.5f, 0.0f, 0.4f, 1.2f, 2.5f, -0.8f, -2.2f, 0.5f), preamp = -3.0f, category = "Over-Ear"),

        // ==========================================
        // Beyerdynamic
        // ==========================================
        createProfile("Beyerdynamic", "DT 700 Pro X", listOf(-0.8f, -0.5f, 0.2f, 0.0f, 0.2f, 0.8f, 1.2f, -2.5f, -4.0f, -1.5f), preamp = -2.5f, category = "Over-Ear"),
        createProfile("Beyerdynamic", "DT 770 Pro (80 Ohm)", listOf(-1.5f, -0.8f, 1.2f, 0.5f, -0.2f, 0.8f, 1.5f, -4.5f, -6.8f, -2.5f), preamp = -3.5f, category = "Over-Ear"),
        createProfile("Beyerdynamic", "DT 770 Pro (250 Ohm)", listOf(-1.8f, -1.0f, 1.0f, 0.4f, -0.4f, 0.6f, 1.2f, -5.0f, -7.5f, -3.0f), preamp = -3.8f, category = "Over-Ear"),
        createProfile("Beyerdynamic", "DT 900 Pro X", listOf(1.5f, 1.0f, 0.2f, -0.2f, 0.0f, 0.5f, 1.0f, -3.0f, -4.5f, -2.0f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Beyerdynamic", "DT 990 Pro (250 Ohm)", listOf(3.2f, 2.5f, 0.8f, -0.5f, -0.2f, 0.5f, 1.2f, -5.2f, -8.0f, -4.0f), preamp = -4.0f, category = "Over-Ear"),
        createProfile("Beyerdynamic", "DT 1990 Pro", listOf(2.0f, 1.2f, 0.4f, -0.2f, 0.0f, 0.6f, 1.2f, -4.2f, -6.5f, -2.5f), preamp = -3.5f, category = "Over-Ear"),

        // ==========================================
        // Bose
        // ==========================================
        createProfile("Bose", "Noise Cancelling 700", listOf(1.8f, 1.2f, 0.2f, -0.4f, 0.0f, 0.8f, 2.2f, 0.4f, -2.0f, 0.5f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Bose", "QuietComfort 35 II", listOf(1.2f, 0.8f, 0.0f, -0.5f, 0.2f, 1.5f, 3.0f, 0.8f, -1.5f, 1.0f), preamp = -3.5f, category = "Over-Ear"),
        createProfile("Bose", "QuietComfort 45 / SE", listOf(2.0f, 1.5f, 0.2f, -0.2f, 0.0f, 0.8f, 1.5f, -2.8f, -4.5f, -1.0f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Bose", "QuietComfort Ultra (Earbuds)", listOf(0.8f, 0.5f, -0.2f, 0.0f, 0.2f, 0.8f, 1.6f, -1.2f, -2.0f, 0.5f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Bose", "QuietComfort Ultra (Headphones)", listOf(0.5f, 0.2f, -0.5f, 0.0f, 0.4f, 1.2f, 2.0f, -0.5f, -1.8f, 0.8f), preamp = -2.5f, category = "Over-Ear"),
        createProfile("Bose", "SoundLink Flex", listOf(1.8f, 1.0f, -0.6f, -0.4f, 0.2f, 0.8f, 1.5f, -0.5f, -1.5f, 0.5f), preamp = -2.5f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Bose", "SoundLink Mini II", listOf(1.2f, 0.5f, -1.0f, -0.8f, 0.2f, 0.8f, 1.8f, 0.0f, -1.2f, 0.5f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Bose", "SoundLink Revolve+ II", listOf(2.2f, 1.4f, -0.5f, -0.2f, 0.0f, 0.5f, 1.2f, -0.8f, -1.8f, 0.2f), preamp = -2.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),

        // ==========================================
        // CCA
        // ==========================================
        createProfile("CCA", "CRA / CRA+", listOf(-0.8f, -0.4f, -0.4f, 0.2f, 0.2f, 0.5f, 1.0f, -3.2f, -4.8f, -0.5f), preamp = -2.8f, category = "IEM"),
        createProfile("CCA", "Hydro", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.8f, -2.5f, 0.4f), preamp = -2.0f, category = "IEM"),
        createProfile("CCA", "Rhapsody", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.5f, -2.2f, 0.5f), preamp = -1.8f, category = "IEM"),

        // ==========================================
        // CMF by Nothing
        // ==========================================
        createProfile("CMF", "Buds", listOf(-1.5f, -1.0f, 0.0f, 0.2f, 0.5f, 1.2f, 1.8f, -1.0f, -2.2f, 0.5f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("CMF", "Buds Pro", listOf(-1.8f, -1.2f, -0.4f, 0.0f, 0.4f, 1.0f, 2.0f, -1.5f, -2.8f, 0.0f), preamp = -2.8f, category = "TWS Earbuds"),
        createProfile("CMF", "Buds Pro 2", listOf(-2.5f, -1.8f, -0.6f, 0.2f, 0.6f, 1.2f, 2.2f, -2.0f, -3.5f, 0.2f), preamp = -3.2f, category = "TWS Earbuds"),
        createProfile("CMF", "Neckband Pro", listOf(-2.0f, -1.5f, -0.5f, 0.2f, 0.4f, 1.0f, 2.4f, -1.8f, -3.0f, 0.0f), preamp = -3.0f, category = "TWS Earbuds"),

        // ==========================================
        // Dunu
        // ==========================================
        createProfile("Dunu", "DaVinci", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.6f, 1.0f, -1.0f, -1.2f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Dunu", "Falcon Ultra", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.5f, -2.0f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Dunu", "Kima", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, -1.6f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Dunu", "SA6 MKII", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.4f, 0.8f, -0.8f, -1.0f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Dunu", "Titan S", listOf(1.5f, 1.0f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, -1.8f, 0.5f), preamp = -2.0f, category = "IEM"),

        // ==========================================
        // Earfun
        // ==========================================
        createProfile("Earfun", "Air Pro 3", listOf(-1.8f, -1.2f, -0.2f, 0.2f, 0.5f, 1.2f, 2.0f, -1.5f, -2.5f, 0.2f), preamp = -2.8f, category = "TWS Earbuds"),
        createProfile("Earfun", "Air Pro 4", listOf(-1.2f, -0.8f, 0.0f, 0.2f, 0.4f, 1.0f, 1.8f, -1.2f, -2.2f, 0.4f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Earfun", "Free Pro 3", listOf(-1.4f, -0.9f, 0.0f, 0.2f, 0.4f, 1.0f, 1.8f, -1.0f, -2.0f, 0.4f), preamp = -2.5f, category = "TWS Earbuds"),

        // ==========================================
        // EPZ
        // ==========================================
        createProfile("EPZ", "Q1 Pro", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.2f, -1.5f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("EPZ", "Q5", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.0f, -2.8f, 0.5f), preamp = -2.0f, category = "IEM"),

        // ==========================================
        // Focal
        // ==========================================
        createProfile("Focal", "Bathys (Wireless)", listOf(0.4f, 0.0f, -0.6f, 0.0f, 0.2f, 0.8f, 1.5f, -0.8f, -1.5f, 0.5f), preamp = -2.2f, category = "Over-Ear"),
        createProfile("Focal", "Clear", listOf(2.5f, 1.8f, 0.5f, -0.2f, 0.0f, 0.4f, 1.0f, -2.5f, -1.8f, 0.5f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Focal", "Utopia (2022)", listOf(2.2f, 1.5f, 0.4f, -0.2f, 0.0f, 0.2f, 0.8f, -2.0f, -1.2f, 0.8f), preamp = -2.8f, category = "Over-Ear"),

        // ==========================================
        // Google
        // ==========================================
        createProfile("Google", "Pixel Buds A-Series", listOf(1.8f, 1.2f, 0.2f, -0.2f, 0.0f, 0.5f, 1.2f, -1.2f, -1.5f, 0.2f), preamp = -2.4f, category = "TWS Earbuds"),
        createProfile("Google", "Pixel Buds Pro", listOf(-1.5f, -1.0f, -0.2f, 0.2f, 0.4f, 1.0f, 2.0f, -1.0f, 1.5f, 0.0f), preamp = -2.8f, category = "TWS Earbuds"),
        createProfile("Google", "Pixel Buds Pro 2", listOf(0.5f, 0.2f, -0.4f, 0.0f, 0.2f, 0.6f, 1.2f, -0.8f, 1.2f, 0.4f), preamp = -2.0f, category = "TWS Earbuds"),

        // ==========================================
        // Harman Kardon
        // ==========================================
        createProfile("Harman Kardon", "Aura Studio 3", listOf(-0.5f, -0.2f, -0.6f, 0.0f, 0.2f, 0.6f, 1.2f, -0.5f, -1.2f, 0.2f), preamp = -1.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Harman Kardon", "Onyx Studio 7 / 8", listOf(-0.8f, -0.5f, -0.8f, 0.0f, 0.2f, 0.5f, 1.0f, -0.4f, -1.0f, 0.4f), preamp = -1.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),

        // ==========================================
        // Hidizs
        // ==========================================
        createProfile("Hidizs", "MP145 (Planar)", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.8f, -2.5f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Hidizs", "MS3", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.5f, -3.5f, 0.6f), preamp = -2.2f, category = "IEM"),

        // ==========================================
        // Hifiman
        // ==========================================
        createProfile("Hifiman", "Arya (Stealth)", listOf(2.0f, 1.4f, 0.4f, -0.2f, 0.0f, 0.5f, 1.2f, -1.0f, -2.2f, 1.5f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Hifiman", "Edition XS", listOf(2.5f, 1.8f, 0.5f, -0.2f, 0.0f, 0.4f, 1.0f, -1.2f, -2.0f, 1.2f), preamp = -3.2f, category = "Over-Ear"),
        createProfile("Hifiman", "HE400se", listOf(4.2f, 3.0f, 1.2f, 0.0f, -0.2f, 0.2f, 0.8f, -1.8f, -3.0f, 0.5f), preamp = -4.5f, category = "Over-Ear"),
        createProfile("Hifiman", "Sundara (2020)", listOf(3.8f, 2.8f, 1.0f, 0.0f, -0.2f, 0.4f, 1.2f, -1.5f, -2.8f, 0.8f), preamp = -4.2f, category = "Over-Ear"),

        // ==========================================
        // Huawei
        // ==========================================
        createProfile("Huawei", "FreeBuds 5i", listOf(-1.2f, -0.8f, 0.0f, 0.2f, 0.4f, 1.0f, 1.8f, -1.0f, -2.0f, 0.2f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Huawei", "FreeBuds Pro 3", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -0.8f, -1.2f, 0.4f), preamp = -1.8f, category = "TWS Earbuds"),
        createProfile("Huawei", "Sound Joy", listOf(2.0f, 1.2f, -0.5f, -0.2f, 0.2f, 0.6f, 1.4f, -0.4f, -1.5f, 0.2f), preamp = -2.4f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),

        // ==========================================
        // JBL
        // ==========================================
        createProfile("JBL", "Authentics 300", listOf(0.5f, 0.0f, -0.5f, 0.0f, 0.2f, 0.5f, 1.0f, -0.4f, -1.0f, 0.4f), preamp = -1.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "Boombox 3", listOf(0.5f, 0.0f, -0.8f, -0.4f, 0.2f, 0.4f, 0.8f, -0.5f, -1.0f, 0.2f), preamp = -1.5f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "Charge 5", listOf(2.5f, 1.5f, -0.8f, -0.6f, 0.0f, 0.6f, 1.2f, -0.5f, -1.5f, 0.2f), preamp = -3.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "Clip 4 / Clip 5", listOf(5.5f, 3.8f, 1.0f, -0.5f, -0.2f, 0.4f, 1.0f, 0.2f, -1.5f, 0.0f), preamp = -5.5f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "Flip 6", listOf(3.5f, 2.2f, -0.5f, -0.8f, 0.2f, 0.8f, 1.5f, 0.4f, -1.2f, 0.5f), preamp = -3.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "Go 3 / Go 4", listOf(6.0f, 4.5f, 1.5f, -0.8f, -0.4f, 0.2f, 1.2f, 0.5f, -1.8f, 0.0f), preamp = -6.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "PartyBox Stage 320", listOf(0.2f, -0.2f, -0.6f, 0.0f, 0.2f, 0.5f, 1.0f, -0.4f, -1.0f, 0.2f), preamp = -1.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "Tune 760NC", listOf(0.5f, 0.0f, -0.8f, 0.0f, 0.4f, 1.0f, 2.0f, -0.8f, -1.5f, 0.5f), preamp = -2.5f, category = "Over-Ear"),
        createProfile("JBL", "Xtreme 3 / 4", listOf(1.5f, 0.8f, -0.5f, 0.0f, 0.2f, 0.5f, 1.0f, -0.8f, -1.2f, 0.0f), preamp = -2.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),

        // ==========================================
        // Jabra
        // ==========================================
        createProfile("Jabra", "Elite 7 Pro", listOf(0.8f, 0.5f, 0.0f, -0.2f, 0.2f, 0.5f, 1.2f, -0.8f, -1.5f, 0.4f), preamp = -2.0f, category = "TWS Earbuds"),
        createProfile("Jabra", "Elite 8 Active", listOf(-1.2f, -0.8f, 0.0f, 0.2f, 0.4f, 1.0f, 1.8f, -1.0f, -2.0f, 0.2f), preamp = -2.6f, category = "TWS Earbuds"),
        createProfile("Jabra", "Elite 10", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.6f, 1.2f, -0.5f, -1.2f, 0.5f), preamp = -2.0f, category = "TWS Earbuds"),

        // ==========================================
        // Kefine
        // ==========================================
        createProfile("Kefine", "Delci", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.4f, -1.8f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Kefine", "Klean", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, -1.5f, 0.5f), preamp = -1.8f, category = "IEM"),

        // ==========================================
        // Kiwi Ears
        // ==========================================
        createProfile("Kiwi Ears", "Cadenza", listOf(0.6f, 0.4f, 0.0f, -0.2f, 0.0f, 0.5f, 1.2f, -1.5f, -1.8f, 0.4f), preamp = -2.0f, category = "IEM"),
        createProfile("Kiwi Ears", "KE4", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -0.8f, -1.0f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Kiwi Ears", "Melody (Planar)", listOf(-1.2f, -0.8f, -0.4f, 0.0f, 0.2f, 0.6f, 1.2f, -2.0f, -3.2f, 0.5f), preamp = -2.4f, category = "IEM"),
        createProfile("Kiwi Ears", "Orchestra Lite", listOf(1.0f, 0.6f, 0.0f, -0.2f, 0.0f, 0.2f, 0.6f, -0.8f, -1.2f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Kiwi Ears", "Quintet", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.0f, -2.8f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Kiwi Ears", "Singolo", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.5f, -2.0f, 0.4f), preamp = -1.8f, category = "IEM"),

        // ==========================================
        // KZ (Knowledge Zenith)
        // ==========================================
        createProfile("KZ", "Castor (Bass Enhanced)", listOf(-1.8f, -1.2f, -0.4f, 0.2f, 0.4f, 0.8f, 1.5f, -1.5f, -2.5f, 0.4f), preamp = -2.8f, category = "IEM"),
        createProfile("KZ", "Castor (Harman Target)", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.8f, -2.2f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("KZ", "EDX Pro", listOf(-1.5f, -0.8f, -0.8f, 0.2f, 0.2f, 0.5f, 1.0f, -4.5f, -6.8f, -1.8f), preamp = -3.5f, category = "IEM"),
        createProfile("KZ", "Krila", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -2.2f, -3.2f, 0.4f), preamp = -2.2f, category = "IEM"),
        createProfile("KZ", "PR2 (Planar)", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.0f, 0.4f, 0.8f, -3.5f, -5.5f, 0.2f), preamp = -2.5f, category = "IEM"),
        createProfile("KZ", "PR3 (Planar)", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.0f, 0.4f, 0.8f, -4.0f, -6.0f, 0.4f), preamp = -2.8f, category = "IEM"),
        createProfile("KZ", "ZS10 Pro X", listOf(-0.5f, -0.2f, -0.4f, 0.2f, 0.2f, 0.6f, 1.2f, -3.5f, -5.2f, -1.0f), preamp = -2.8f, category = "IEM"),
        createProfile("KZ", "ZSN Pro X", listOf(-1.0f, -0.5f, -0.8f, 0.2f, 0.2f, 0.5f, 1.0f, -4.2f, -6.5f, -1.5f), preamp = -3.2f, category = "IEM"),

        // ==========================================
        // Letshuoer
        // ==========================================
        createProfile("Letshuoer", "Galileo", listOf(0.8f, 0.5f, 0.0f, 0.0f, 0.2f, 0.4f, 0.8f, -0.8f, -1.0f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Letshuoer", "S12 / S12 Pro (Planar)", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.4f, 0.8f, -3.0f, -4.5f, 0.5f), preamp = -2.5f, category = "IEM"),

        // ==========================================
        // Marshall
        // ==========================================
        createProfile("Marshall", "Acton III", listOf(1.2f, 0.5f, -0.6f, -0.2f, 0.2f, 0.6f, 1.2f, -0.6f, -1.5f, 0.2f), preamp = -2.0f, category = "Bluetooth Speaker", source = "Rock & Acoustic Mastering"),
        createProfile("Marshall", "Emberton II", listOf(1.5f, 0.8f, -0.8f, -0.5f, 0.2f, 0.6f, 1.4f, -0.4f, -1.5f, 0.2f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Rock & Acoustic Mastering"),
        createProfile("Marshall", "Kilburn II", listOf(0.8f, 0.2f, -0.6f, -0.2f, 0.2f, 0.6f, 1.2f, -0.5f, -1.2f, 0.4f), preamp = -2.0f, category = "Bluetooth Speaker", source = "Rock & Acoustic Mastering"),
        createProfile("Marshall", "Middleton", listOf(1.0f, 0.4f, -0.6f, -0.2f, 0.2f, 0.5f, 1.2f, -0.5f, -1.4f, 0.2f), preamp = -2.0f, category = "Bluetooth Speaker", source = "Rock & Acoustic Mastering"),
        createProfile("Marshall", "Stanmore III", listOf(0.8f, 0.2f, -0.5f, 0.0f, 0.2f, 0.5f, 1.0f, -0.5f, -1.2f, 0.4f), preamp = -1.8f, category = "Bluetooth Speaker", source = "Rock & Acoustic Mastering"),
        createProfile("Marshall", "Willen", listOf(5.0f, 3.5f, 0.8f, -0.6f, -0.2f, 0.4f, 1.2f, 0.0f, -1.4f, 0.0f), preamp = -5.0f, category = "Bluetooth Speaker", source = "Rock & Acoustic Mastering"),

        // ==========================================
        // Moondrop
        // ==========================================
        createProfile("Moondrop", "Aria / Aria SE", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.5f, -2.0f, 1.0f), preamp = -2.0f, category = "IEM"),
        createProfile("Moondrop", "Blessing 2 / Dusk", listOf(2.0f, 1.5f, 0.5f, 0.0f, -0.2f, 0.2f, 0.8f, -1.0f, 1.5f, 0.5f), preamp = -2.5f, category = "IEM"),
        createProfile("Moondrop", "Blessing 3", listOf(1.2f, 0.8f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, 1.0f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Moondrop", "Chu II", listOf(0.8f, 0.5f, 0.0f, -0.2f, 0.0f, 0.4f, 1.2f, -2.0f, -1.5f, 0.8f), preamp = -2.0f, category = "IEM"),
        createProfile("Moondrop", "Dusk (DSP / Analog)", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.4f, 0.8f, -0.8f, 0.6f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Moondrop", "FreeDSP / May", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.2f, -1.5f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Moondrop", "Kato", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.2f, -1.2f, -1.8f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Moondrop", "Space Travel", listOf(0.8f, 0.5f, 0.0f, -0.2f, 0.0f, 0.4f, 1.2f, -1.0f, 1.2f, 0.2f), preamp = -2.0f, category = "TWS Earbuds"),
        createProfile("Moondrop", "Starfield", listOf(0.6f, 0.3f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.4f, -1.8f, 0.8f), preamp = -2.0f, category = "IEM"),
        createProfile("Moondrop", "Stellaris", listOf(0.5f, 0.2f, -0.2f, 0.0f, -0.4f, -0.8f, -1.5f, -4.5f, -6.0f, -1.0f), preamp = -2.5f, category = "IEM"),
        createProfile("Moondrop", "Variations", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.4f, 0.8f, -0.8f, 0.5f, 0.2f), preamp = -1.5f, category = "IEM"),

        // ==========================================
        // Nothing
        // ==========================================
        createProfile("Nothing", "Ear (1)", listOf(0.8f, 0.5f, -0.2f, -0.2f, 0.2f, 0.8f, 2.0f, -1.8f, -2.8f, 0.2f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Nothing", "Ear (2)", listOf(1.2f, 0.8f, 0.0f, -0.4f, -0.2f, 0.5f, 1.5f, -2.5f, -4.0f, 0.5f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Nothing", "Ear (2024)", listOf(0.6f, 0.4f, -0.4f, 0.0f, 0.2f, 0.6f, 1.2f, -1.8f, -2.5f, 0.4f), preamp = -2.2f, category = "TWS Earbuds"),
        createProfile("Nothing", "Ear (a)", listOf(-1.2f, -0.8f, -0.2f, 0.2f, 0.5f, 1.0f, 2.4f, -1.5f, -3.0f, 0.0f), preamp = -3.0f, category = "TWS Earbuds"),
        createProfile("Nothing", "Ear (stick)", listOf(6.5f, 5.0f, 2.2f, 0.0f, -0.5f, 0.2f, 1.0f, -1.5f, -2.0f, 0.5f), preamp = -6.5f, category = "TWS Earbuds"),
        createProfile("Nothing", "Head (1)", listOf(1.5f, 1.0f, 0.2f, -0.2f, 0.0f, 0.5f, 1.2f, -1.2f, -2.0f, 0.5f), preamp = -2.5f, category = "Over-Ear"),

        // ==========================================
        // OnePlus
        // ==========================================
        createProfile("OnePlus", "Buds Pro 2", listOf(-2.2f, -1.5f, -0.5f, 0.2f, 0.5f, 1.0f, 2.2f, -1.5f, -2.8f, 0.2f), preamp = -3.0f, category = "TWS Earbuds"),
        createProfile("OnePlus", "Buds Pro 3", listOf(-1.2f, -0.8f, -0.2f, 0.2f, 0.4f, 0.8f, 1.5f, -1.2f, -2.0f, 0.5f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("OnePlus", "Nord Buds 2", listOf(-2.8f, -2.0f, -0.6f, 0.2f, 0.4f, 1.2f, 2.5f, -1.8f, -3.2f, 0.0f), preamp = -3.2f, category = "TWS Earbuds"),

        // ==========================================
        // Realme
        // ==========================================
        createProfile("Realme", "Buds Air 5 Pro", listOf(-2.0f, -1.4f, -0.4f, 0.2f, 0.4f, 1.2f, 2.2f, -1.5f, -2.8f, 0.2f), preamp = -3.0f, category = "TWS Earbuds"),
        createProfile("Realme", "Buds Air 6 Pro", listOf(-1.8f, -1.2f, -0.2f, 0.2f, 0.4f, 1.0f, 2.0f, -1.4f, -2.5f, 0.4f), preamp = -2.8f, category = "TWS Earbuds"),

        // ==========================================
        // Samsung
        // ==========================================
        createProfile("Samsung", "Galaxy Buds 2 Pro", listOf(0.8f, 0.5f, 0.0f, -0.4f, 0.2f, 0.8f, 1.2f, -0.5f, 1.5f, -0.5f), preamp = -2.0f, category = "TWS Earbuds"),
        createProfile("Samsung", "Galaxy Buds 3 Pro", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.6f, 1.0f, -0.4f, 1.2f, 0.2f), preamp = -1.8f, category = "TWS Earbuds"),
        createProfile("Samsung", "Galaxy Buds FE", listOf(1.5f, 1.0f, 0.2f, -0.2f, 0.0f, 0.5f, 1.8f, -0.8f, 2.0f, 0.0f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Samsung", "Galaxy Buds Pro", listOf(0.5f, 0.2f, 0.0f, -0.2f, 0.4f, 1.0f, 1.8f, -1.0f, 1.2f, 0.0f), preamp = -2.2f, category = "TWS Earbuds"),

        // ==========================================
        // SeeAudio
        // ==========================================
        createProfile("SeeAudio", "Bravery", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.4f, 0.8f, -1.0f, -1.2f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("SeeAudio", "Yume II", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, -1.5f, 0.5f), preamp = -1.8f, category = "IEM"),

        // ==========================================
        // Sennheiser
        // ==========================================
        createProfile("Sennheiser", "HD 560S", listOf(3.5f, 2.8f, 1.0f, 0.0f, -0.4f, -0.8f, -0.2f, -1.8f, -2.5f, 0.5f), preamp = -4.0f, category = "Over-Ear"),
        createProfile("Sennheiser", "HD 600", listOf(5.8f, 4.5f, 2.2f, 0.4f, -0.5f, -0.8f, 0.2f, 2.4f, -1.5f, -3.0f), preamp = -6.0f, category = "Over-Ear"),
        createProfile("Sennheiser", "HD 650 / HD 6XX", listOf(6.2f, 5.0f, 2.0f, -0.2f, -1.0f, -0.6f, 0.5f, 3.2f, -0.8f, -2.5f), preamp = -6.5f, category = "Over-Ear"),
        createProfile("Sennheiser", "HD 660S2", listOf(4.2f, 3.5f, 1.5f, -0.2f, -0.6f, -0.4f, 0.8f, 2.8f, -1.0f, -2.0f), preamp = -5.0f, category = "Over-Ear"),
        createProfile("Sennheiser", "HD 800 S", listOf(5.0f, 3.8f, 1.5f, 0.0f, -0.2f, 0.4f, -0.5f, -4.2f, -6.5f, 1.5f), preamp = -5.5f, category = "Over-Ear"),
        createProfile("Sennheiser", "IE 200 (IEM)", listOf(1.2f, 0.8f, 0.0f, -0.4f, -0.2f, 0.5f, 1.5f, -1.8f, -2.2f, 0.5f), preamp = -2.5f, category = "IEM"),
        createProfile("Sennheiser", "IE 600 (IEM)", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.4f, 0.8f, -2.5f, -3.0f, 1.0f), preamp = -2.0f, category = "IEM"),
        createProfile("Sennheiser", "Momentum 4 Wireless", listOf(-4.5f, -3.8f, -1.2f, 0.5f, 0.8f, 1.2f, 2.5f, -1.0f, -3.5f, 0.0f), preamp = -3.5f, category = "Over-Ear"),
        createProfile("Sennheiser", "Momentum True Wireless 3", listOf(-1.0f, -0.5f, -0.2f, 0.0f, 0.2f, 0.8f, 1.8f, -1.0f, -2.2f, 0.4f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Sennheiser", "Momentum True Wireless 4", listOf(-0.5f, -0.2f, -0.2f, 0.0f, 0.2f, 0.6f, 1.5f, -0.8f, -1.8f, 0.5f), preamp = -2.2f, category = "TWS Earbuds"),

        // ==========================================
        // Shure
        // ==========================================
        createProfile("Shure", "SE215", listOf(-3.5f, -2.8f, -1.2f, 0.5f, 1.2f, 2.4f, 4.5f, 3.0f, -1.5f, -4.0f), preamp = -5.0f, category = "IEM"),
        createProfile("Shure", "SRH840A", listOf(0.5f, 0.2f, -0.4f, 0.0f, 0.2f, 0.5f, 1.0f, -1.2f, -1.8f, 0.4f), preamp = -2.0f, category = "Over-Ear"),

        // ==========================================
        // Simgot
        // ==========================================
        createProfile("Simgot", "EA500 LM", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.5f, -3.2f, 0.8f), preamp = -2.0f, category = "IEM"),
        createProfile("Simgot", "EA1000 Fermat", listOf(0.3f, 0.1f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.0f, -2.5f, 0.8f), preamp = -2.0f, category = "IEM"),
        createProfile("Simgot", "EM6L", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.2f, -1.6f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Simgot", "EW100P", listOf(0.6f, 0.4f, 0.0f, -0.2f, 0.0f, 0.4f, 0.8f, -1.5f, -1.8f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Simgot", "EW200", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.8f, -3.8f, 0.6f), preamp = -2.2f, category = "IEM"),
        createProfile("Simgot", "SuperMix 4", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.5f, -2.0f, 0.4f), preamp = -1.8f, category = "IEM"),

        // ==========================================
        // Sonos
        // ==========================================
        createProfile("Sonos", "Move 2", listOf(0.8f, 0.2f, -0.6f, -0.2f, 0.2f, 0.6f, 1.2f, -0.4f, -1.0f, 0.4f), preamp = -1.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Sonos", "Roam 2", listOf(2.4f, 1.5f, -0.5f, -0.2f, 0.2f, 0.6f, 1.2f, -0.5f, -1.4f, 0.2f), preamp = -2.5f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),

        // ==========================================
        // Sony
        // ==========================================
        createProfile("Sony", "IER-M9 (IEM)", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.2f, -0.8f, -1.0f, 0.2f), preamp = -2.0f, category = "IEM"),
        createProfile("Sony", "LinkBuds S", listOf(-1.2f, -0.8f, 0.0f, 0.4f, 0.6f, 1.4f, 2.8f, -1.0f, -2.0f, 0.2f), preamp = -3.0f, category = "TWS Earbuds"),
        createProfile("Sony", "MDR-1AM2", listOf(-3.0f, -2.2f, -0.8f, 0.2f, 0.5f, 1.0f, 2.4f, -0.5f, -2.0f, 0.5f), preamp = -3.2f, category = "Over-Ear"),
        createProfile("Sony", "MDR-7506 (Studio)", listOf(1.5f, 0.5f, -1.0f, -0.5f, 0.2f, 0.8f, -1.2f, -4.5f, -3.2f, 1.0f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Sony", "SRS-XB100 / XB13", listOf(4.5f, 3.2f, 0.5f, -0.5f, 0.0f, 0.6f, 1.5f, 0.2f, -1.8f, 0.0f), preamp = -4.5f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Sony", "SRS-XE200", listOf(2.5f, 1.8f, -0.4f, -0.2f, 0.2f, 0.8f, 1.6f, -0.8f, -2.0f, 0.2f), preamp = -3.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Sony", "SRS-XG300", listOf(1.0f, 0.4f, -0.6f, -0.2f, 0.2f, 0.5f, 1.2f, -0.5f, -1.4f, 0.2f), preamp = -2.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Sony", "ULT FIELD 1", listOf(1.2f, 0.5f, -0.8f, -0.4f, 0.2f, 0.6f, 1.5f, -0.5f, -1.8f, 0.2f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Sony", "ULT FIELD 7", listOf(0.4f, 0.0f, -0.6f, -0.2f, 0.2f, 0.5f, 1.0f, -0.5f, -1.2f, 0.4f), preamp = -1.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Sony", "WF-1000XM4 (TWS)", listOf(-2.4f, -1.8f, 0.2f, 0.5f, 0.0f, 1.5f, 4.2f, -1.2f, -4.0f, -0.5f), preamp = -4.5f, category = "TWS Earbuds"),
        createProfile("Sony", "WF-1000XM5 (TWS)", listOf(-1.8f, -1.2f, 0.0f, 0.2f, 0.5f, 1.8f, 3.5f, -0.8f, -2.5f, 0.5f), preamp = -4.0f, category = "TWS Earbuds"),
        createProfile("Sony", "WH-1000XM3 (Over-Ear)", listOf(-5.2f, -4.5f, -2.0f, -0.8f, 0.2f, 1.0f, 3.0f, 1.5f, -3.8f, -1.2f), preamp = -4.2f, category = "Over-Ear"),
        createProfile("Sony", "WH-1000XM4 (Over-Ear)", listOf(-4.8f, -4.2f, -1.8f, -0.6f, 0.4f, 1.2f, 3.4f, 1.8f, -3.2f, -1.0f), preamp = -4.0f, category = "Over-Ear"),
        createProfile("Sony", "WH-1000XM5 (Over-Ear)", listOf(-3.6f, -3.2f, -1.5f, 0.2f, 0.8f, 1.6f, 2.8f, 0.5f, -2.4f, 0.0f), preamp = -3.5f, category = "Over-Ear"),
        createProfile("Sony", "WH-CH720N", listOf(-2.5f, -1.8f, -0.5f, 0.2f, 0.5f, 1.2f, 2.0f, -0.5f, -1.8f, 0.0f), preamp = -3.0f, category = "Over-Ear"),

        // ==========================================
        // SoundPEATS
        // ==========================================
        createProfile("SoundPEATS", "Air4 Pro", listOf(-1.4f, -0.8f, 0.0f, 0.2f, 0.4f, 1.0f, 1.8f, -1.0f, -2.0f, 0.4f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("SoundPEATS", "Capsule3 Pro", listOf(-1.6f, -1.0f, -0.2f, 0.2f, 0.4f, 1.0f, 1.8f, -1.2f, -2.2f, 0.4f), preamp = -2.6f, category = "TWS Earbuds"),
        createProfile("SoundPEATS", "Engine 4", listOf(-1.8f, -1.2f, -0.2f, 0.2f, 0.4f, 1.0f, 2.0f, -1.5f, -2.8f, 0.2f), preamp = -2.8f, category = "TWS Earbuds"),

        // ==========================================
        // Tanchjim
        // ==========================================
        createProfile("Tanchjim", "4U", listOf(0.3f, 0.1f, -0.2f, 0.0f, 0.2f, 0.4f, 0.8f, -1.0f, -1.2f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Tanchjim", "Bunny (3.5mm / DSP)", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.4f, -1.8f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Tanchjim", "Kara", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.2f, -1.5f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Tanchjim", "Ola / Ola Bass", listOf(1.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.4f, 0.8f, -1.5f, -2.0f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Tanchjim", "One / One DSP", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, -1.5f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Tanchjim", "Origin", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.4f, 0.8f, -1.0f, -1.2f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Tanchjim", "Tanya / Tanya DSP", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.5f, 1.2f, -1.0f, -1.5f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Tanchjim", "Zero", listOf(0.8f, 0.4f, 0.0f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, -1.6f, 0.5f), preamp = -1.8f, category = "IEM"),

        // ==========================================
        // Tangzu
        // ==========================================
        createProfile("Tangzu", "Fudu Verse 1", listOf(0.4f, 0.2f, -0.4f, 0.0f, 0.2f, 0.5f, 1.2f, -1.2f, -1.5f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Tangzu", "Princess Chang Le", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.6f, 1.2f, -1.5f, -2.0f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Tangzu", "Wan'er S.G", listOf(0.8f, 0.5f, 0.0f, -0.2f, 0.0f, 0.4f, 1.0f, -1.8f, -2.0f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Tangzu", "Xuan NV", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.0f, -1.2f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Tangzu", "YuXuanJi", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.5f, -2.0f, 0.5f), preamp = -1.8f, category = "IEM"),

        // ==========================================
        // Technics
        // ==========================================
        createProfile("Technics", "EAH-AZ60M2", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.0f, -1.5f, 0.4f), preamp = -1.8f, category = "TWS Earbuds"),
        createProfile("Technics", "EAH-AZ80", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -0.8f, -1.2f, 0.5f), preamp = -1.8f, category = "TWS Earbuds"),

        // ==========================================
        // Thieaudio
        // ==========================================
        createProfile("Thieaudio", "Hype 2 / Hype 4", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.4f, 0.8f, -0.8f, -1.0f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Thieaudio", "Monarch MKIII", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.4f, 0.8f, -0.5f, 0.5f, 0.5f), preamp = -1.8f, category = "IEM"),

        // ==========================================
        // TinHiFi
        // ==========================================
        createProfile("TinHiFi", "C2 Punch", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.5f, -2.0f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("TinHiFi", "T2 Plus", listOf(0.8f, 0.4f, 0.0f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, -1.5f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("TinHiFi", "T3 Plus", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.2f, -1.6f, 0.4f), preamp = -1.8f, category = "IEM"),

        // ==========================================
        // Tribit
        // ==========================================
        createProfile("Tribit", "StormBox Blast", listOf(0.4f, 0.0f, -0.6f, -0.2f, 0.2f, 0.5f, 1.0f, -0.6f, -1.2f, 0.2f), preamp = -1.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Tribit", "StormBox Micro 2", listOf(3.8f, 2.5f, 0.0f, -0.5f, 0.2f, 0.8f, 1.4f, 0.0f, -1.5f, 0.2f), preamp = -3.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Tribit", "XSound Go", listOf(4.2f, 2.8f, 0.2f, -0.4f, 0.2f, 0.6f, 1.2f, 0.0f, -1.4f, 0.2f), preamp = -4.2f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),

        // ==========================================
        // Truthear
        // ==========================================
        createProfile("Truthear", "Gate", listOf(0.6f, 0.4f, 0.0f, -0.2f, 0.0f, 0.4f, 1.0f, -1.5f, -1.8f, 0.6f), preamp = -1.8f, category = "IEM"),
        createProfile("Truthear", "Hexa", listOf(1.2f, 0.8f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.0f, 1.2f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Truthear", "Hola", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.6f, 1.4f, -0.6f, -1.0f, 0.2f), preamp = -1.8f, category = "IEM"),
        createProfile("Truthear", "Nova", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.4f, 0.6f, -0.5f, 0.8f, 0.2f), preamp = -1.5f, category = "IEM"),
        createProfile("Truthear", "Zero:RED", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -0.8f, -1.2f, 0.4f), preamp = -1.8f, category = "IEM"),

        // ==========================================
        // Ultimate Ears (UE)
        // ==========================================
        createProfile("Ultimate Ears", "Boom 3", listOf(2.2f, 1.5f, -0.6f, -0.2f, 0.2f, 0.6f, 1.4f, -0.4f, -1.6f, 0.2f), preamp = -2.6f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Ultimate Ears", "Megaboom 3", listOf(1.5f, 0.8f, -0.6f, -0.2f, 0.2f, 0.5f, 1.2f, -0.5f, -1.4f, 0.2f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Ultimate Ears", "Wonderboom 3", listOf(2.5f, 1.8f, -0.5f, -0.4f, 0.0f, 0.5f, 1.2f, -0.2f, -1.4f, 0.0f), preamp = -2.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),

        // ==========================================
        // Xiaomi & Redmi
        // ==========================================
        createProfile("Xiaomi", "Bluetooth Speaker (Mini / Pro)", listOf(2.0f, 1.2f, -0.5f, -0.2f, 0.2f, 0.6f, 1.2f, -0.4f, -1.4f, 0.2f), preamp = -2.4f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Xiaomi", "Buds 4 Pro", listOf(-1.2f, -0.8f, 0.0f, 0.2f, 0.4f, 1.0f, 1.8f, -1.0f, -2.0f, 0.2f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Xiaomi", "Buds 5", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.6f, 1.2f, -0.8f, -1.2f, 0.4f), preamp = -1.8f, category = "TWS Earbuds"),
        createProfile("Xiaomi", "Mi Portable Bluetooth Speaker 16W", listOf(1.5f, 0.8f, -0.6f, -0.2f, 0.2f, 0.5f, 1.2f, -0.5f, -1.5f, 0.2f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Xiaomi", "Redmi Buds 4 Active", listOf(-2.4f, -1.8f, -0.5f, 0.2f, 0.4f, 1.0f, 2.2f, -1.5f, -2.8f, 0.0f), preamp = -3.0f, category = "TWS Earbuds"),
        createProfile("Xiaomi", "Redmi Buds 5 Pro", listOf(-1.5f, -1.0f, -0.2f, 0.2f, 0.4f, 1.0f, 2.0f, -1.2f, -2.2f, 0.4f), preamp = -2.6f, category = "TWS Earbuds"),
        createProfile("Xiaomi", "Redmi Buds 6 Active", listOf(-2.0f, -1.5f, -0.4f, 0.2f, 0.4f, 1.0f, 2.0f, -1.5f, -2.5f, 0.2f), preamp = -2.8f, category = "TWS Earbuds"),
        createProfile("Xiaomi", "Sound Outdoor (30W)", listOf(1.2f, 0.5f, -0.6f, -0.2f, 0.2f, 0.6f, 1.2f, -0.5f, -1.4f, 0.4f), preamp = -2.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Xiaomi", "Sound Pocket (5W)", listOf(4.8f, 3.5f, 0.5f, -0.6f, 0.0f, 0.4f, 1.2f, 0.0f, -1.5f, 0.0f), preamp = -4.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),

        // ==========================================
        // Ziigaat
        // ==========================================
        createProfile("Ziigaat", "Cincotres", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.0f, -1.2f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Ziigaat", "Doscinco", listOf(-0.5f, -0.2f, -0.4f, 0.0f, 0.2f, 0.5f, 0.8f, -1.2f, -1.5f, 0.4f), preamp = -2.0f, category = "IEM"),
        createProfile("Ziigaat", "Nuo", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.5f, -1.8f, 0.5f), preamp = -1.8f, category = "IEM")
    )

    // Automatically sorted alphabetically by Brand, then Model
    val profiles: List<AutoEqProfile> = rawProfiles.sortedWith(
        compareBy({ it.brand.lowercase() }, { it.model.lowercase() })
    )

    fun search(query: String, category: String = "All"): List<AutoEqProfile> {
        val q = query.trim().lowercase()
        return profiles.filter { profile ->
            val matchesCategory = when (category) {
                "All" -> true
                "Nothing / CMF" -> profile.brand.equals("Nothing", ignoreCase = true) || profile.brand.equals("CMF", ignoreCase = true)
                "IEMs" -> profile.category == "IEM"
                "TWS Earbuds" -> profile.category == "TWS Earbuds"
                "Over-Ear" -> profile.category == "Over-Ear"
                "Speakers" -> profile.category == "Bluetooth Speaker"
                else -> true
            }

            val matchesQuery = if (q.isBlank()) true else {
                profile.brand.lowercase().contains(q) ||
                profile.model.lowercase().contains(q) ||
                profile.category.lowercase().contains(q)
            }

            matchesCategory && matchesQuery
        }
    }
}
