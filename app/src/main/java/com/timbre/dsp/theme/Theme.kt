package com.timbre.dsp.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

fun getCustomColorScheme(seedColor: Long, isDark: Boolean, isAmoled: Boolean): ColorScheme {
    val primary = Color(seedColor)
    return if (isDark) {
        val baseDark = darkColorScheme(
            primary = primary,
            secondary = primary.copy(alpha = 0.8f),
            tertiary = Color(0xFFEFB8C8),
            background = if (isAmoled) Color.Black else Color(0xFF121212),
            surface = if (isAmoled) Color.Black else Color(0xFF181818),
            surfaceVariant = if (isAmoled) Color(0xFF141414) else Color(0xFF242424),
            onPrimary = Color.White,
            onBackground = Color(0xFFE6E1E5),
            onSurface = Color(0xFFE6E1E5),
            surfaceContainer = if (isAmoled) Color(0xFF080808) else Color(0xFF1E1E1E),
            surfaceContainerHigh = if (isAmoled) Color(0xFF121212) else Color(0xFF282828)
        )
        baseDark
    } else {
        lightColorScheme(
            primary = primary,
            secondary = primary.copy(alpha = 0.85f),
            tertiary = Color(0xFF7D5260),
            background = Color(0xFFFAF9FD),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFF2F0F7),
            onPrimary = Color.White,
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F)
        )
    }
}

@Composable
fun TimbreTheme(
    themeSettings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val isDark = when (themeSettings.themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.DARK, ThemeMode.AMOLED -> true
        ThemeMode.LIGHT -> false
    }

    val isAmoled = themeSettings.themeMode == ThemeMode.AMOLED

    val colorScheme = when {
        themeSettings.useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val base = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (isAmoled) {
                base.copy(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceVariant = Color(0xFF141414),
                    surfaceContainer = Color(0xFF080808),
                    surfaceContainerHigh = Color(0xFF121212)
                )
            } else {
                base
            }
        }
        else -> getCustomColorScheme(themeSettings.seedColor, isDark, isAmoled)
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

