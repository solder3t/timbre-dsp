package com.timbre.dsp.model

enum class RoutingMode {
    AUTO,
    STANDALONE,
    BROADCAST,
    SHIZUKU,
    ROOT
}

enum class EQMode {
    GRAPHIC_10,
    GRAPHIC_15,
    GRAPHIC_31,
    PARAMETRIC
}

enum class FilterType {
    LOW_PASS,
    HIGH_PASS,
    BAND_PASS,
    NOTCH,
    PEAK,
    LOW_SHELF,
    HIGH_SHELF
}

enum class OutputDeviceType(val displayName: String) {
    SPEAKER("Built-in Speaker"),
    WIRED("Wired Headphones"),
    BLUETOOTH("Bluetooth Audio"),
    USB("USB Audio DAC"),
    OTHER("External Audio")
}

enum class TargetCurve(val displayName: String) {
    NONE("None"),
    HARMAN_OVER_EAR("Harman Over-Ear (2019)"),
    HARMAN_IN_EAR("Harman In-Ear (2019)"),
    IEF_NEUTRAL("IEF Neutral Target"),
    DIFFUSE_FIELD("Diffuse Field"),
    FREE_FIELD("Free Field")
}

data class EQBand(
    val index: Int,
    val frequency: Float,
    val gain: Float = 0f,
    val q: Float = 1.414f,
    val type: FilterType = FilterType.PEAK,
    val enabled: Boolean = true
)

data class EQPreset(
    val id: String,
    val name: String,
    val eqMode: EQMode = EQMode.GRAPHIC_10,
    val bands: List<EQBand>,
    val preampGain: Float = 0f,
    val bassBoostGain: Float = 0f,
    val clarityGain: Float = 0f,
    val virtualizerStrength: Float = 0f,
    val crossfeedStrength: Float = 0f,
    val isCustom: Boolean = false
)

data class AutoEqProfile(
    val model: String,
    val brand: String,
    val source: String,
    val bands: List<EQBand>,
    val preampGain: Float = -4.5f
)

data class AudioSessionInfo(
    val sessionId: Int,
    val packageName: String,
    val appName: String = "Active Player",
    val isPlaying: Boolean = true
)

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
    val autoPreampEnabled: Boolean = false,
    val bands: List<EQBand> = default10Bands(),
    val targetCurve: TargetCurve = TargetCurve.NONE,
    val bassBoostEnabled: Boolean = false,
    val bassBoostGain: Float = 0f,
    val bassBoostCutoffFreq: Float = 80f,
    val virtualizerEnabled: Boolean = false,
    val virtualizerStrength: Float = 0f,
    val crossfeedEnabled: Boolean = false,
    val crossfeedStrength: Float = 0.5f,
    val clarityEnabled: Boolean = false,
    val clarityGain: Float = 0f,
    val convolutionEnabled: Boolean = false,
    val convolutionWetDry: Float = 0.5f,
    val activeConvolutionId: String = "studio_room",
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
