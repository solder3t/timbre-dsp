package com.timbre.dsp.model

enum class FilterType {
    PEAK,
    LOW_SHELF,
    HIGH_SHELF,
    LOW_PASS,
    HIGH_PASS,
    NOTCH,
    BAND_PASS
}

enum class EQMode {
    GRAPHIC_10,
    PARAMETRIC
}

data class EQBand(
    val index: Int,
    val frequency: Float,
    val gain: Float = 0f, // in dB (-15dB to +15dB, or -24 to +24 in parametric)
    val q: Float = 1.414f,
    val type: FilterType = FilterType.PEAK,
    val enabled: Boolean = true
)

data class EQPreset(
    val id: String,
    val name: String,
    val isCustom: Boolean = false,
    val eqMode: EQMode = EQMode.GRAPHIC_10,
    val preampGain: Float = 0f,
    val bands: List<EQBand>,
    val bassBoostGain: Float = 0f,
    val bassBoostFreq: Float = 80f,
    val virtualizerStrength: Float = 0f,
    val clarityGain: Float = 0f,
    val crossfeedStrength: Float = 0f,
    val limiterEnabled: Boolean = true
)

data class AutoEqProfile(
    val model: String,
    val brand: String,
    val source: String,
    val bands: List<EQBand>,
    val preampGain: Float = 0f
)

data class AudioSessionInfo(
    val sessionId: Int,
    val packageName: String,
    val appName: String,
    val isPlaying: Boolean = true,
    val attachedTime: Long = System.currentTimeMillis()
)

enum class RoutingMode {
    AUTO,
    SHIZUKU,
    ROOT,
    BROADCAST,
    STANDALONE
}

enum class OutputDeviceType {
    BLUETOOTH,
    USB,
    WIRED,
    SPEAKER,
    OTHER
}

data class DeviceProfile(
    val deviceId: String,
    val deviceName: String,
    val deviceType: OutputDeviceType,
    val presetId: String,
    val isEnabled: Boolean = true
)

data class AppProfile(
    val packageName: String,
    val appName: String,
    val presetId: String,
    val isEnabled: Boolean = true
)

data class HearingAudiogram(
    val leftOffsets: Map<Int, Float> = emptyMap(), // Frequency (Hz) -> Gain Offset (dB)
    val rightOffsets: Map<Int, Float> = emptyMap(),
    val isCalibrated: Boolean = false
)

data class PermissionStatus(
    val isShizukuRunning: Boolean = false,
    val hasShizukuPermission: Boolean = false,
    val isRootAvailable: Boolean = false,
    val hasRootPermission: Boolean = false,
    val hasDumpPermission: Boolean = false,
    val hasNotificationAccess: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val hasPostNotificationPermission: Boolean = false,
    val hasRecordAudioPermission: Boolean = false
) {
    val isFullyConfigured: Boolean
        get() = (hasShizukuPermission || hasRootPermission || hasDumpPermission) && hasNotificationAccess
}

data class DSPSettings(
    val isEnabled: Boolean = true,
    val routingMode: RoutingMode = RoutingMode.AUTO,
    val eqMode: EQMode = EQMode.GRAPHIC_10,
    val currentPresetId: String = "flat",
    val preampGain: Float = 0f,
    val bands: List<EQBand> = default10Bands(),
    val bassBoostEnabled: Boolean = false,
    val bassBoostGain: Float = 0f,
    val bassBoostCutoffFreq: Float = 80f,
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Float = 0f,
    val crossfeedEnabled: Boolean = false,
    val crossfeedStrength: Float = 0.5f,
    val clarityEnabled: Boolean = false,
    val clarityGain: Float = 0f,
    val limiterEnabled: Boolean = true,
    val channelBalance: Float = 0f, // -1.0 (Left) to +1.0 (Right)
    val isMono: Boolean = false,
    val isVisualizerEnabled: Boolean = true,
    val hearingAudiogram: HearingAudiogram = HearingAudiogram()
) {
    companion object {
        fun default10Bands(): List<EQBand> {
            val freqs = listOf(31.25f, 62.5f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
            return freqs.mapIndexed { index, freq ->
                EQBand(
                    index = index,
                    frequency = freq,
                    gain = 0f,
                    q = 1.414f,
                    type = FilterType.PEAK
                )
            }
        }
    }
}
