package com.timbre.dsp.theme

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT,
    AMOLED
}

data class AccentColorOption(
    val name: String,
    val hexColor: String,
    val colorValue: Long
)

data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val seedColor: Long = 0xFF7B61FF
) {
    companion object {
        val predefinedAccents = listOf(
            AccentColorOption("Indigo", "#7B61FF", 0xFF7B61FF),
            AccentColorOption("Cyan", "#00C4FF", 0xFF00C4FF),
            AccentColorOption("Emerald", "#00D26A", 0xFF00D26A),
            AccentColorOption("Sunset", "#FF7A00", 0xFFFF7A00),
            AccentColorOption("Rose", "#FF3366", 0xFFFF3366),
            AccentColorOption("Amber", "#FFB800", 0xFFFFB800)
        )
    }
}
