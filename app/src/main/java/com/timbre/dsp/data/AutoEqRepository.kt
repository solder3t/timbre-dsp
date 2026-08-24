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

    val profiles: List<AutoEqProfile> = listOf(
        // ==========================================
        // 1. Nothing & CMF by Nothing Audio
        // ==========================================
        createProfile("Nothing", "Ear (2024)", listOf(0.6f, 0.4f, -0.4f, 0.0f, 0.2f, 0.6f, 1.2f, -1.8f, -2.5f, 0.4f), preamp = -2.2f, category = "TWS Earbuds"),
        createProfile("Nothing", "Ear (a)", listOf(-1.2f, -0.8f, -0.2f, 0.2f, 0.5f, 1.0f, 2.4f, -1.5f, -3.0f, 0.0f), preamp = -3.0f, category = "TWS Earbuds"),
        createProfile("Nothing", "Ear (2)", listOf(1.2f, 0.8f, 0.0f, -0.4f, -0.2f, 0.5f, 1.5f, -2.5f, -4.0f, 0.5f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Nothing", "Ear (1)", listOf(0.8f, 0.5f, -0.2f, -0.2f, 0.2f, 0.8f, 2.0f, -1.8f, -2.8f, 0.2f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Nothing", "Ear (stick)", listOf(6.5f, 5.0f, 2.2f, 0.0f, -0.5f, 0.2f, 1.0f, -1.5f, -2.0f, 0.5f), preamp = -6.5f, category = "TWS Earbuds"),
        createProfile("Nothing", "Head (1)", listOf(1.5f, 1.0f, 0.2f, -0.2f, 0.0f, 0.5f, 1.2f, -1.2f, -2.0f, 0.5f), preamp = -2.5f, category = "Over-Ear"),
        createProfile("CMF", "Buds Pro 2", listOf(-2.5f, -1.8f, -0.6f, 0.2f, 0.6f, 1.2f, 2.2f, -2.0f, -3.5f, 0.2f), preamp = -3.2f, category = "TWS Earbuds"),
        createProfile("CMF", "Buds Pro", listOf(-1.8f, -1.2f, -0.4f, 0.0f, 0.4f, 1.0f, 2.0f, -1.5f, -2.8f, 0.0f), preamp = -2.8f, category = "TWS Earbuds"),
        createProfile("CMF", "Buds", listOf(-1.5f, -1.0f, 0.0f, 0.2f, 0.5f, 1.2f, 1.8f, -1.0f, -2.2f, 0.5f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("CMF", "Neckband Pro", listOf(-2.0f, -1.5f, -0.5f, 0.2f, 0.4f, 1.0f, 2.4f, -1.8f, -3.0f, 0.0f), preamp = -3.0f, category = "TWS Earbuds"),

        // ==========================================
        // 2. Bluetooth Speakers & Soundbars
        // ==========================================
        createProfile("JBL", "Flip 6", listOf(3.5f, 2.2f, -0.5f, -0.8f, 0.2f, 0.8f, 1.5f, 0.4f, -1.2f, 0.5f), preamp = -3.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "Charge 5", listOf(2.5f, 1.5f, -0.8f, -0.6f, 0.0f, 0.6f, 1.2f, -0.5f, -1.5f, 0.2f), preamp = -3.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "Xtreme 3 / 4", listOf(1.5f, 0.8f, -0.5f, 0.0f, 0.2f, 0.5f, 1.0f, -0.8f, -1.2f, 0.0f), preamp = -2.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "Boombox 3", listOf(0.5f, 0.0f, -0.8f, -0.4f, 0.2f, 0.4f, 0.8f, -0.5f, -1.0f, 0.2f), preamp = -1.5f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "Go 3 / Go 4", listOf(6.0f, 4.5f, 1.5f, -0.8f, -0.4f, 0.2f, 1.2f, 0.5f, -1.8f, 0.0f), preamp = -6.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("JBL", "Clip 4", listOf(5.5f, 3.8f, 1.0f, -0.5f, -0.2f, 0.4f, 1.0f, 0.2f, -1.5f, 0.0f), preamp = -5.5f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Bose", "SoundLink Flex", listOf(1.8f, 1.0f, -0.6f, -0.4f, 0.2f, 0.8f, 1.5f, -0.5f, -1.5f, 0.5f), preamp = -2.5f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Bose", "SoundLink Revolve+ II", listOf(2.2f, 1.4f, -0.5f, -0.2f, 0.0f, 0.5f, 1.2f, -0.8f, -1.8f, 0.2f), preamp = -2.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Bose", "SoundLink Mini II", listOf(1.2f, 0.5f, -1.0f, -0.8f, 0.2f, 0.8f, 1.8f, 0.0f, -1.2f, 0.5f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Marshall", "Emberton II", listOf(1.5f, 0.8f, -0.8f, -0.5f, 0.2f, 0.6f, 1.4f, -0.4f, -1.5f, 0.2f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Rock & Acoustic Mastering"),
        createProfile("Marshall", "Stanmore III", listOf(0.8f, 0.2f, -0.5f, 0.0f, 0.2f, 0.5f, 1.0f, -0.5f, -1.2f, 0.4f), preamp = -1.8f, category = "Bluetooth Speaker", source = "Rock & Acoustic Mastering"),
        createProfile("Marshall", "Acton III", listOf(1.2f, 0.5f, -0.6f, -0.2f, 0.2f, 0.6f, 1.2f, -0.6f, -1.5f, 0.2f), preamp = -2.0f, category = "Bluetooth Speaker", source = "Rock & Acoustic Mastering"),
        createProfile("Marshall", "Willen", listOf(5.0f, 3.5f, 0.8f, -0.6f, -0.2f, 0.4f, 1.2f, 0.0f, -1.4f, 0.0f), preamp = -5.0f, category = "Bluetooth Speaker", source = "Rock & Acoustic Mastering"),
        createProfile("Sony", "SRS-XB100 / XB13", listOf(4.5f, 3.2f, 0.5f, -0.5f, 0.0f, 0.6f, 1.5f, 0.2f, -1.8f, 0.0f), preamp = -4.5f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Sony", "SRS-XE200", listOf(2.5f, 1.8f, -0.4f, -0.2f, 0.2f, 0.8f, 1.6f, -0.8f, -2.0f, 0.2f), preamp = -3.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Sony", "ULT FIELD 1", listOf(1.2f, 0.5f, -0.8f, -0.4f, 0.2f, 0.6f, 1.5f, -0.5f, -1.8f, 0.2f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Anker", "Soundcore Motion+", listOf(1.2f, 0.5f, -0.6f, 0.0f, 0.2f, 0.5f, 1.0f, -1.2f, -2.2f, 0.5f), preamp = -2.0f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Anker", "Soundcore Motion Boom", listOf(0.5f, -0.2f, -1.0f, -0.5f, 0.2f, 0.8f, 1.8f, -0.5f, -1.5f, 0.2f), preamp = -2.2f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Anker", "Soundcore Motion 300", listOf(1.8f, 1.0f, -0.4f, -0.2f, 0.2f, 0.6f, 1.2f, -0.8f, -1.8f, 0.4f), preamp = -2.4f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Tribit", "StormBox Micro 2", listOf(3.8f, 2.5f, 0.0f, -0.5f, 0.2f, 0.8f, 1.4f, 0.0f, -1.5f, 0.2f), preamp = -3.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Tribit", "StormBox Blast", listOf(0.4f, 0.0f, -0.6f, -0.2f, 0.2f, 0.5f, 1.0f, -0.6f, -1.2f, 0.2f), preamp = -1.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Ultimate Ears", "Wonderboom 3", listOf(2.5f, 1.8f, -0.5f, -0.4f, 0.0f, 0.5f, 1.2f, -0.2f, -1.4f, 0.0f), preamp = -2.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Ultimate Ears", "Boom 3", listOf(2.2f, 1.5f, -0.6f, -0.2f, 0.2f, 0.6f, 1.4f, -0.4f, -1.6f, 0.2f), preamp = -2.6f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),
        createProfile("Harman Kardon", "Onyx Studio 7 / 8", listOf(-0.8f, -0.5f, -0.8f, 0.0f, 0.2f, 0.5f, 1.0f, -0.4f, -1.0f, 0.4f), preamp = -1.8f, category = "Bluetooth Speaker", source = "Studio Acoustic Flattening"),

        // ==========================================
        // 3. Chi-Fi & Audiophile In-Ear Monitors (IEMs)
        // ==========================================
        createProfile("Moondrop", "Chu II", listOf(0.8f, 0.5f, 0.0f, -0.2f, 0.0f, 0.4f, 1.2f, -2.0f, -1.5f, 0.8f), preamp = -2.0f, category = "IEM"),
        createProfile("Moondrop", "Aria / Aria SE", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.5f, -2.0f, 1.0f), preamp = -2.0f, category = "IEM"),
        createProfile("Moondrop", "Kato", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.2f, -1.2f, -1.8f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Moondrop", "Blessing 2 / Dusk", listOf(2.0f, 1.5f, 0.5f, 0.0f, -0.2f, 0.2f, 0.8f, -1.0f, 1.5f, 0.5f), preamp = -2.5f, category = "IEM"),
        createProfile("Moondrop", "Blessing 3", listOf(1.2f, 0.8f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, 1.0f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Moondrop", "Variations", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.4f, 0.8f, -0.8f, 0.5f, 0.2f), preamp = -1.5f, category = "IEM"),
        createProfile("Moondrop", "Starfield", listOf(0.6f, 0.3f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.4f, -1.8f, 0.8f), preamp = -2.0f, category = "IEM"),
        createProfile("Moondrop", "Stellaris", listOf(0.5f, 0.2f, -0.2f, 0.0f, -0.4f, -0.8f, -1.5f, -4.5f, -6.0f, -1.0f), preamp = -2.5f, category = "IEM"),
        createProfile("Truthear", "Zero:RED", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -0.8f, -1.2f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Truthear", "Hexa", listOf(1.2f, 0.8f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.0f, 1.2f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Truthear", "Nova", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.4f, 0.6f, -0.5f, 0.8f, 0.2f), preamp = -1.5f, category = "IEM"),
        createProfile("Truthear", "Gate", listOf(0.6f, 0.4f, 0.0f, -0.2f, 0.0f, 0.4f, 1.0f, -1.5f, -1.8f, 0.6f), preamp = -1.8f, category = "IEM"),
        createProfile("Truthear", "Hola", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.6f, 1.4f, -0.6f, -1.0f, 0.2f), preamp = -1.8f, category = "IEM"),
        createProfile("Tangzu", "Wan'er S.G", listOf(0.8f, 0.5f, 0.0f, -0.2f, 0.0f, 0.4f, 1.0f, -1.8f, -2.0f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Tangzu", "Fudu Verse 1", listOf(0.4f, 0.2f, -0.4f, 0.0f, 0.2f, 0.5f, 1.2f, -1.2f, -1.5f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Tangzu", "Xuan NV", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.0f, -1.2f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("7Hz", "Salnotes Zero 2", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.2f, -1.5f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("7Hz", "Salnotes Zero (1st Gen)", listOf(1.5f, 1.0f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.5f, -2.0f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("7Hz", "Timeless (Planar)", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.5f, 1.0f, -2.2f, -3.5f, 0.8f), preamp = -2.2f, category = "IEM"),
        createProfile("Kiwi Ears", "Cadenza", listOf(0.6f, 0.4f, 0.0f, -0.2f, 0.0f, 0.5f, 1.2f, -1.5f, -1.8f, 0.4f), preamp = -2.0f, category = "IEM"),
        createProfile("Kiwi Ears", "Quintet", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.0f, -2.8f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Kiwi Ears", "Orchestra Lite", listOf(1.0f, 0.6f, 0.0f, -0.2f, 0.0f, 0.2f, 0.6f, -0.8f, -1.2f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Simgot", "EA500 LM", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.5f, -3.2f, 0.8f), preamp = -2.0f, category = "IEM"),
        createProfile("Simgot", "EW200", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.8f, -3.8f, 0.6f), preamp = -2.2f, category = "IEM"),
        createProfile("Simgot", "SuperMix 4", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.5f, -2.0f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Simgot", "EA1000 Fermat", listOf(0.3f, 0.1f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -2.0f, -2.5f, 0.8f), preamp = -2.0f, category = "IEM"),
        createProfile("KZ", "Castor (Harman Target)", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.8f, -2.2f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("KZ", "Castor (Bass Enhanced)", listOf(-1.8f, -1.2f, -0.4f, 0.2f, 0.4f, 0.8f, 1.5f, -1.5f, -2.5f, 0.4f), preamp = -2.8f, category = "IEM"),
        createProfile("KZ", "ZS10 Pro X", listOf(-0.5f, -0.2f, -0.4f, 0.2f, 0.2f, 0.6f, 1.2f, -3.5f, -5.2f, -1.0f), preamp = -2.8f, category = "IEM"),
        createProfile("KZ", "ZSN Pro X", listOf(-1.0f, -0.5f, -0.8f, 0.2f, 0.2f, 0.5f, 1.0f, -4.2f, -6.5f, -1.5f), preamp = -3.2f, category = "IEM"),
        createProfile("CCA", "CRA / CRA+", listOf(-0.8f, -0.4f, -0.4f, 0.2f, 0.2f, 0.5f, 1.0f, -3.2f, -4.8f, -0.5f), preamp = -2.8f, category = "IEM"),
        createProfile("CCA", "Rhapsody", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.5f, -2.2f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Dunu", "Titan S", listOf(1.5f, 1.0f, 0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, -1.8f, 0.5f), preamp = -2.0f, category = "IEM"),
        createProfile("Dunu", "SA6 MKII", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.4f, 0.8f, -0.8f, -1.0f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Dunu", "Falcon Ultra", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.5f, -2.0f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Tanchjim", "Tanya", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.5f, 1.2f, -1.0f, -1.5f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Tanchjim", "One", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.0f, 0.4f, 0.8f, -1.2f, -1.5f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Tanchjim", "Origin", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.4f, 0.8f, -1.0f, -1.2f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("Letshuoer", "S12 / S12 Pro (Planar)", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.4f, 0.8f, -3.0f, -4.5f, 0.5f), preamp = -2.5f, category = "IEM"),
        createProfile("AFUL", "Performer 5", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 0.8f, -1.2f, -1.5f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("AFUL", "Performer 8", listOf(0.6f, 0.4f, 0.0f, 0.0f, 0.0f, 0.4f, 0.8f, -1.0f, -1.2f, 0.5f), preamp = -1.8f, category = "IEM"),
        createProfile("AFUL", "Explorer", listOf(0.2f, 0.0f, -0.2f, 0.0f, 0.2f, 0.6f, 1.2f, -0.8f, -1.0f, 0.2f), preamp = -1.8f, category = "IEM"),
        createProfile("Kefine", "Delci", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -1.4f, -1.8f, 0.4f), preamp = -1.8f, category = "IEM"),
        createProfile("Artti", "T10 (Planar)", listOf(0.2f, 0.0f, -0.4f, 0.0f, 0.2f, 0.4f, 0.8f, -2.5f, -3.8f, 0.6f), preamp = -2.2f, category = "IEM"),
        createProfile("Shure", "SE215", listOf(-3.5f, -2.8f, -1.2f, 0.5f, 1.2f, 2.4f, 4.5f, 3.0f, -1.5f, -4.0f), preamp = -5.0f, category = "IEM"),

        // ==========================================
        // 4. Mainstream & Audiophile TWS Earbuds
        // ==========================================
        createProfile("Apple", "AirPods Pro 2 (USB-C / Lightning)", listOf(1.5f, 1.2f, 0.2f, -0.4f, -0.2f, 0.4f, 0.8f, -0.5f, 1.8f, 0.5f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Apple", "AirPods Pro (1st Gen)", listOf(2.4f, 1.8f, 0.5f, -0.2f, -0.8f, 0.0f, 1.2f, -1.5f, 2.8f, 1.0f), preamp = -3.2f, category = "TWS Earbuds"),
        createProfile("Apple", "AirPods 3", listOf(4.2f, 3.0f, 1.0f, -0.5f, -0.8f, 0.2f, 1.5f, -2.0f, -1.5f, 1.0f), preamp = -4.5f, category = "TWS Earbuds"),
        createProfile("Apple", "AirPods 2", listOf(6.5f, 4.8f, 2.0f, -0.2f, -0.5f, 0.5f, 1.8f, -1.2f, -2.0f, 0.5f), preamp = -6.5f, category = "TWS Earbuds"),
        createProfile("Samsung", "Galaxy Buds 3 Pro", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.6f, 1.0f, -0.4f, 1.2f, 0.2f), preamp = -1.8f, category = "TWS Earbuds"),
        createProfile("Samsung", "Galaxy Buds 2 Pro", listOf(0.8f, 0.5f, 0.0f, -0.4f, 0.2f, 0.8f, 1.2f, -0.5f, 1.5f, -0.5f), preamp = -2.0f, category = "TWS Earbuds"),
        createProfile("Samsung", "Galaxy Buds FE", listOf(1.5f, 1.0f, 0.2f, -0.2f, 0.0f, 0.5f, 1.8f, -0.8f, 2.0f, 0.0f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Samsung", "Galaxy Buds Pro", listOf(0.5f, 0.2f, 0.0f, -0.2f, 0.4f, 1.0f, 1.8f, -1.0f, 1.2f, 0.0f), preamp = -2.2f, category = "TWS Earbuds"),
        createProfile("Sony", "WF-1000XM5", listOf(-1.8f, -1.2f, 0.0f, 0.2f, 0.5f, 1.8f, 3.5f, -0.8f, -2.5f, 0.5f), preamp = -4.0f, category = "TWS Earbuds"),
        createProfile("Sony", "WF-1000XM4", listOf(-2.4f, -1.8f, 0.2f, 0.5f, 0.0f, 1.5f, 4.2f, -1.2f, -4.0f, -0.5f), preamp = -4.5f, category = "TWS Earbuds"),
        createProfile("Sony", "LinkBuds S", listOf(-1.2f, -0.8f, 0.0f, 0.4f, 0.6f, 1.4f, 2.8f, -1.0f, -2.0f, 0.2f), preamp = -3.0f, category = "TWS Earbuds"),
        createProfile("Google", "Pixel Buds Pro 2", listOf(0.5f, 0.2f, -0.4f, 0.0f, 0.2f, 0.6f, 1.2f, -0.8f, 1.2f, 0.4f), preamp = -2.0f, category = "TWS Earbuds"),
        createProfile("Google", "Pixel Buds Pro", listOf(-1.5f, -1.0f, -0.2f, 0.2f, 0.4f, 1.0f, 2.0f, -1.0f, 1.5f, 0.0f), preamp = -2.8f, category = "TWS Earbuds"),
        createProfile("Google", "Pixel Buds A-Series", listOf(1.8f, 1.2f, 0.2f, -0.2f, 0.0f, 0.5f, 1.2f, -1.2f, -1.5f, 0.2f), preamp = -2.4f, category = "TWS Earbuds"),
        createProfile("OnePlus", "Buds Pro 3", listOf(-1.2f, -0.8f, -0.2f, 0.2f, 0.4f, 0.8f, 1.5f, -1.2f, -2.0f, 0.5f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("OnePlus", "Buds Pro 2", listOf(-2.2f, -1.5f, -0.5f, 0.2f, 0.5f, 1.0f, 2.2f, -1.5f, -2.8f, 0.2f), preamp = -3.0f, category = "TWS Earbuds"),
        createProfile("OnePlus", "Nord Buds 2", listOf(-2.8f, -2.0f, -0.6f, 0.2f, 0.4f, 1.2f, 2.5f, -1.8f, -3.2f, 0.0f), preamp = -3.2f, category = "TWS Earbuds"),
        createProfile("Realme", "Buds Air 6 Pro", listOf(-1.8f, -1.2f, -0.2f, 0.2f, 0.4f, 1.0f, 2.0f, -1.4f, -2.5f, 0.4f), preamp = -2.8f, category = "TWS Earbuds"),
        createProfile("Realme", "Buds Air 5 Pro", listOf(-2.0f, -1.4f, -0.4f, 0.2f, 0.4f, 1.2f, 2.2f, -1.5f, -2.8f, 0.2f), preamp = -3.0f, category = "TWS Earbuds"),
        createProfile("Sennheiser", "Momentum True Wireless 4", listOf(-0.5f, -0.2f, -0.2f, 0.0f, 0.2f, 0.6f, 1.5f, -0.8f, -1.8f, 0.5f), preamp = -2.2f, category = "TWS Earbuds"),
        createProfile("Sennheiser", "Momentum True Wireless 3", listOf(-1.0f, -0.5f, -0.2f, 0.0f, 0.2f, 0.8f, 1.8f, -1.0f, -2.2f, 0.4f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Technics", "EAH-AZ80", listOf(0.4f, 0.2f, -0.2f, 0.0f, 0.2f, 0.5f, 1.0f, -0.8f, -1.2f, 0.5f), preamp = -1.8f, category = "TWS Earbuds"),
        createProfile("Jabra", "Elite 10", listOf(0.5f, 0.2f, -0.2f, 0.0f, 0.2f, 0.6f, 1.2f, -0.5f, -1.2f, 0.5f), preamp = -2.0f, category = "TWS Earbuds"),
        createProfile("Jabra", "Elite 8 Active", listOf(-1.2f, -0.8f, 0.0f, 0.2f, 0.4f, 1.0f, 1.8f, -1.0f, -2.0f, 0.2f), preamp = -2.6f, category = "TWS Earbuds"),
        createProfile("Anker", "Soundcore Liberty 4 NC", listOf(-1.5f, -1.0f, 0.0f, 0.4f, 0.6f, 1.4f, 2.2f, -1.2f, -2.5f, 0.5f), preamp = -2.8f, category = "TWS Earbuds"),
        createProfile("Earfun", "Air Pro 4", listOf(-1.2f, -0.8f, 0.0f, 0.2f, 0.4f, 1.0f, 1.8f, -1.2f, -2.2f, 0.4f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("SoundPEATS", "Engine 4", listOf(-1.8f, -1.2f, -0.2f, 0.2f, 0.4f, 1.0f, 2.0f, -1.5f, -2.8f, 0.2f), preamp = -2.8f, category = "TWS Earbuds"),
        createProfile("Beats", "Fit Pro", listOf(1.0f, 0.8f, 0.0f, -0.4f, -0.2f, 0.5f, 1.5f, -1.0f, 1.5f, 0.2f), preamp = -2.5f, category = "TWS Earbuds"),
        createProfile("Bose", "QuietComfort Ultra (Earbuds)", listOf(0.8f, 0.5f, -0.2f, 0.0f, 0.2f, 0.8f, 1.6f, -1.2f, -2.0f, 0.5f), preamp = -2.5f, category = "TWS Earbuds"),

        // ==========================================
        // 5. Over-Ear & Studio Headphones
        // ==========================================
        createProfile("Sony", "WH-1000XM5", listOf(-3.6f, -3.2f, -1.5f, 0.2f, 0.8f, 1.6f, 2.8f, 0.5f, -2.4f, 0.0f), preamp = -3.5f, category = "Over-Ear"),
        createProfile("Sony", "WH-1000XM4", listOf(-4.8f, -4.2f, -1.8f, -0.6f, 0.4f, 1.2f, 3.4f, 1.8f, -3.2f, -1.0f), preamp = -4.0f, category = "Over-Ear"),
        createProfile("Sony", "WH-1000XM3", listOf(-5.2f, -4.5f, -2.0f, -0.8f, 0.2f, 1.0f, 3.0f, 1.5f, -3.8f, -1.2f), preamp = -4.2f, category = "Over-Ear"),
        createProfile("Sony", "WH-CH720N", listOf(-2.5f, -1.8f, -0.5f, 0.2f, 0.5f, 1.2f, 2.0f, -0.5f, -1.8f, 0.0f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Sony", "MDR-7506 (Studio)", listOf(1.5f, 0.5f, -1.0f, -0.5f, 0.2f, 0.8f, -1.2f, -4.5f, -3.2f, 1.0f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Apple", "AirPods Max", listOf(0.8f, 0.5f, -0.4f, -0.8f, -0.2f, 0.6f, 2.2f, 0.5f, -1.8f, 1.5f), preamp = -2.8f, category = "Over-Ear"),
        createProfile("Sennheiser", "HD 600", listOf(5.8f, 4.5f, 2.2f, 0.4f, -0.5f, -0.8f, 0.2f, 2.4f, -1.5f, -3.0f), preamp = -6.0f, category = "Over-Ear"),
        createProfile("Sennheiser", "HD 650 / HD 6XX", listOf(6.2f, 5.0f, 2.0f, -0.2f, -1.0f, -0.6f, 0.5f, 3.2f, -0.8f, -2.5f), preamp = -6.5f, category = "Over-Ear"),
        createProfile("Sennheiser", "HD 560S", listOf(3.5f, 2.8f, 1.0f, 0.0f, -0.4f, -0.8f, -0.2f, -1.8f, -2.5f, 0.5f), preamp = -4.0f, category = "Over-Ear"),
        createProfile("Sennheiser", "HD 800 S", listOf(5.0f, 3.8f, 1.5f, 0.0f, -0.2f, 0.4f, -0.5f, -4.2f, -6.5f, 1.5f), preamp = -5.5f, category = "Over-Ear"),
        createProfile("Sennheiser", "Momentum 4 Wireless", listOf(-4.5f, -3.8f, -1.2f, 0.5f, 0.8f, 1.2f, 2.5f, -1.0f, -3.5f, 0.0f), preamp = -3.5f, category = "Over-Ear"),
        createProfile("Bose", "QuietComfort Ultra (Headphones)", listOf(0.5f, 0.2f, -0.5f, 0.0f, 0.4f, 1.2f, 2.0f, -0.5f, -1.8f, 0.8f), preamp = -2.5f, category = "Over-Ear"),
        createProfile("Bose", "QuietComfort 45 / SE", listOf(2.0f, 1.5f, 0.2f, -0.2f, 0.0f, 0.8f, 1.5f, -2.8f, -4.5f, -1.0f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Bose", "QuietComfort 35 II", listOf(1.2f, 0.8f, 0.0f, -0.5f, 0.2f, 1.5f, 3.0f, 0.8f, -1.5f, 1.0f), preamp = -3.5f, category = "Over-Ear"),
        createProfile("Beyerdynamic", "DT 770 Pro (80 Ohm)", listOf(-1.5f, -0.8f, 1.2f, 0.5f, -0.2f, 0.8f, 1.5f, -4.5f, -6.8f, -2.5f), preamp = -3.5f, category = "Over-Ear"),
        createProfile("Beyerdynamic", "DT 770 Pro (250 Ohm)", listOf(-1.8f, -1.0f, 1.0f, 0.4f, -0.4f, 0.6f, 1.2f, -5.0f, -7.5f, -3.0f), preamp = -3.8f, category = "Over-Ear"),
        createProfile("Beyerdynamic", "DT 990 Pro (250 Ohm)", listOf(3.2f, 2.5f, 0.8f, -0.5f, -0.2f, 0.5f, 1.2f, -5.2f, -8.0f, -4.0f), preamp = -4.0f, category = "Over-Ear"),
        createProfile("Beyerdynamic", "DT 700 Pro X", listOf(-0.8f, -0.5f, 0.2f, 0.0f, 0.2f, 0.8f, 1.2f, -2.5f, -4.0f, -1.5f), preamp = -2.5f, category = "Over-Ear"),
        createProfile("Beyerdynamic", "DT 900 Pro X", listOf(1.5f, 1.0f, 0.2f, -0.2f, 0.0f, 0.5f, 1.0f, -3.0f, -4.5f, -2.0f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Beyerdynamic", "DT 1990 Pro", listOf(2.0f, 1.2f, 0.4f, -0.2f, 0.0f, 0.6f, 1.2f, -4.2f, -6.5f, -2.5f), preamp = -3.5f, category = "Over-Ear"),
        createProfile("Audio-Technica", "ATH-M50x", listOf(0.5f, -0.8f, -2.5f, -1.8f, 0.2f, 0.8f, -1.2f, -3.5f, -2.0f, 1.5f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Audio-Technica", "ATH-M40x", listOf(1.2f, 0.4f, -1.2f, -0.8f, 0.0f, 0.6f, 0.8f, -2.2f, -1.5f, 1.0f), preamp = -2.5f, category = "Over-Ear"),
        createProfile("Audio-Technica", "ATH-R70x", listOf(4.2f, 3.2f, 1.2f, 0.0f, -0.2f, 0.2f, 0.6f, 1.8f, -1.0f, -2.0f), preamp = -4.5f, category = "Over-Ear"),
        createProfile("AKG", "K371", listOf(0.2f, 0.0f, -0.5f, 0.0f, 0.2f, 0.4f, 0.8f, -0.5f, -1.0f, 0.5f), preamp = -1.5f, category = "Over-Ear"),
        createProfile("AKG", "K240 Studio", listOf(5.5f, 4.0f, 1.5f, -0.2f, -0.8f, -0.4f, 0.5f, 2.8f, -2.0f, -3.5f), preamp = -5.8f, category = "Over-Ear"),
        createProfile("AKG", "K702", listOf(6.0f, 4.5f, 1.8f, 0.0f, -0.4f, 0.2f, 0.8f, -2.0f, -4.0f, -1.5f), preamp = -6.2f, category = "Over-Ear"),
        createProfile("Hifiman", "Sundara (2020)", listOf(3.8f, 2.8f, 1.0f, 0.0f, -0.2f, 0.4f, 1.2f, -1.5f, -2.8f, 0.8f), preamp = -4.2f, category = "Over-Ear"),
        createProfile("Hifiman", "Edition XS", listOf(2.5f, 1.8f, 0.5f, -0.2f, 0.0f, 0.4f, 1.0f, -1.2f, -2.0f, 1.2f), preamp = -3.2f, category = "Over-Ear"),
        createProfile("Hifiman", "Arya (Stealth)", listOf(2.0f, 1.4f, 0.4f, -0.2f, 0.0f, 0.5f, 1.2f, -1.0f, -2.2f, 1.5f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Hifiman", "HE400se", listOf(4.2f, 3.0f, 1.2f, 0.0f, -0.2f, 0.2f, 0.8f, -1.8f, -3.0f, 0.5f), preamp = -4.5f, category = "Over-Ear"),
        createProfile("Focal", "Bathys (Wireless)", listOf(0.4f, 0.0f, -0.6f, 0.0f, 0.2f, 0.8f, 1.5f, -0.8f, -1.5f, 0.5f), preamp = -2.2f, category = "Over-Ear"),
        createProfile("Focal", "Clear", listOf(2.5f, 1.8f, 0.5f, -0.2f, 0.0f, 0.4f, 1.0f, -2.5f, -1.8f, 0.5f), preamp = -3.0f, category = "Over-Ear"),
        createProfile("Anker", "Soundcore Space Q45", listOf(-2.8f, -2.0f, -0.8f, 0.2f, 0.5f, 1.2f, 2.5f, -1.0f, -2.8f, 0.2f), preamp = -3.2f, category = "Over-Ear"),
        createProfile("Beats", "Studio Pro", listOf(-1.8f, -1.2f, -0.5f, 0.0f, 0.4f, 1.2f, 2.5f, -0.8f, -2.2f, 0.5f), preamp = -3.0f, category = "Over-Ear")
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
