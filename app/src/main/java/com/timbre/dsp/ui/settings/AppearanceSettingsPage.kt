package com.timbre.dsp.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timbre.dsp.R
import com.timbre.dsp.theme.ThemeMode
import com.timbre.dsp.theme.ThemeSettings
import com.timbre.dsp.ui.utils.WindowWidthClass
import com.timbre.dsp.ui.utils.rememberWindowWidthClass
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsPage(
    themeSettings: ThemeSettings,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetDynamicColor: (Boolean) -> Unit,
    onSetSeedColor: (Long) -> Unit,
    onBack: () -> Unit,
    hazeState: HazeState? = null
) {
    val windowWidthClass = rememberWindowWidthClass()
    val isCompact = windowWidthClass == WindowWidthClass.COMPACT

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings_section_appearance),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.btn_close)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 1. Theme Mode Card
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.theme_mode_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isCompact) {
                    // 2x2 Adaptive Grid on Compact Screens so text never wraps or clips
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeModeChip(
                                mode = ThemeMode.SYSTEM,
                                isSelected = themeSettings.themeMode == ThemeMode.SYSTEM,
                                onSelect = { onSetThemeMode(ThemeMode.SYSTEM) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeModeChip(
                                mode = ThemeMode.DARK,
                                isSelected = themeSettings.themeMode == ThemeMode.DARK,
                                onSelect = { onSetThemeMode(ThemeMode.DARK) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeModeChip(
                                mode = ThemeMode.LIGHT,
                                isSelected = themeSettings.themeMode == ThemeMode.LIGHT,
                                onSelect = { onSetThemeMode(ThemeMode.LIGHT) },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeModeChip(
                                mode = ThemeMode.AMOLED,
                                isSelected = themeSettings.themeMode == ThemeMode.AMOLED,
                                onSelect = { onSetThemeMode(ThemeMode.AMOLED) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    // 1x4 Row on Medium / Expanded Screens (Tablets / Foldables)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.values().forEach { mode ->
                            ThemeModeChip(
                                mode = mode,
                                isSelected = themeSettings.themeMode == mode,
                                onSelect = { onSetThemeMode(mode) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Material You & Custom Colors Card
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Dynamic Color Toggle
                SettingsSwitchRow(
                    icon = Icons.Default.Wallpaper,
                    title = stringResource(R.string.theme_dynamic_title),
                    subtitle = stringResource(R.string.theme_dynamic_desc),
                    checked = themeSettings.useDynamicColor,
                    onCheckedChange = onSetDynamicColor
                )

                if (!themeSettings.useDynamicColor) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )

                    // Accent Palette Picker
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.theme_accent_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeSettings.predefinedAccents.forEach { opt ->
                            val color = Color(opt.colorValue)
                            val isSelected = themeSettings.seedColor == opt.colorValue
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { onSetSeedColor(opt.colorValue) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = opt.name,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
private fun ThemeModeChip(
    mode: ThemeMode,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onSelect,
        label = {
            Text(
                text = when (mode) {
                    ThemeMode.SYSTEM -> stringResource(R.string.theme_mode_system)
                    ThemeMode.DARK -> stringResource(R.string.theme_mode_dark)
                    ThemeMode.LIGHT -> stringResource(R.string.theme_mode_light)
                    ThemeMode.AMOLED -> stringResource(R.string.theme_mode_amoled)
                },
                maxLines = 1,
                softWrap = false
            )
        },
        leadingIcon = {
            when (mode) {
                ThemeMode.SYSTEM -> Icon(Icons.Default.Brightness4, contentDescription = null, modifier = Modifier.size(16.dp))
                ThemeMode.DARK -> Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                ThemeMode.LIGHT -> Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                ThemeMode.AMOLED -> Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
            }
        },
        modifier = modifier
    )
}
