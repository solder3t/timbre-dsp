package com.timbre.dsp.ui.settings

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timbre.dsp.R
import com.timbre.dsp.data.api.UpdateChecker
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.PermissionStatus
import com.timbre.dsp.model.RoutingMode
import com.timbre.dsp.theme.ThemeMode
import com.timbre.dsp.theme.ThemeSettings
import dev.chrisbanes.haze.HazeState

enum class SettingsSubScreen {
    MAIN,
    APPEARANCE,
    PERMISSIONS,
    DSP_ENGINE,
    BACKUP,
    ABOUT
}

@Composable
fun SettingsScreen(
    settings: DSPSettings,
    themeSettings: ThemeSettings,
    permissionStatus: PermissionStatus,
    isSleepTimerRunning: Boolean,
    sleepTimerSeconds: Int,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetDynamicColor: (Boolean) -> Unit,
    onSetSeedColor: (Long) -> Unit,
    onSetRoutingMode: (RoutingMode) -> Unit,
    onToggleLimiter: (Boolean) -> Unit,
    onToggleVisualizer: (Boolean) -> Unit,
    onRequestShizuku: () -> Unit,
    onGrantDumpShizuku: () -> Unit,
    onGrantDumpRoot: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onStartSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onExportBackup: (Uri) -> Boolean,
    onImportBackup: (Uri) -> Boolean,
    onRefresh: () -> Unit,
    hazeState: HazeState? = null
) {
    var currentSubScreen by rememberSaveable { mutableStateOf(SettingsSubScreen.MAIN) }

    BackHandler(enabled = currentSubScreen != SettingsSubScreen.MAIN) {
        currentSubScreen = SettingsSubScreen.MAIN
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(modifier = Modifier.widthIn(max = 840.dp).fillMaxSize()) {
            AnimatedContent(
                targetState = currentSubScreen,
                transitionSpec = {
                    if (targetState == SettingsSubScreen.MAIN) {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                    } else {
                        (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                    }
                },
                label = "settings_subscreen_nav"
            ) { screen ->
                when (screen) {
                    SettingsSubScreen.MAIN -> {
                        SettingsOverviewScreen(
                            themeSettings = themeSettings,
                            permissionStatus = permissionStatus,
                            onNavigate = { currentSubScreen = it },
                            hazeState = hazeState
                        )
                    }
                    SettingsSubScreen.APPEARANCE -> {
                        AppearanceSettingsPage(
                            themeSettings = themeSettings,
                            onSetThemeMode = onSetThemeMode,
                            onSetDynamicColor = onSetDynamicColor,
                            onSetSeedColor = onSetSeedColor,
                            onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                            hazeState = hazeState
                        )
                    }
                    SettingsSubScreen.PERMISSIONS -> {
                        AudioSetupSettingsPage(
                            permissionStatus = permissionStatus,
                            onRequestShizuku = onRequestShizuku,
                            onGrantDumpShizuku = onGrantDumpShizuku,
                            onGrantDumpRoot = onGrantDumpRoot,
                            onOpenNotificationSettings = onOpenNotificationSettings,
                            onRequestBatteryOptimization = onRequestBatteryOptimization,
                            onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                            hazeState = hazeState
                        )
                    }
                    SettingsSubScreen.DSP_ENGINE -> {
                        DspEngineSettingsPage(
                            settings = settings,
                            onSetRoutingMode = onSetRoutingMode,
                            onToggleLimiter = onToggleLimiter,
                            onToggleVisualizer = onToggleVisualizer,
                            onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                            hazeState = hazeState
                        )
                    }
                    SettingsSubScreen.BACKUP -> {
                        BackupSettingsPage(
                            isSleepTimerRunning = isSleepTimerRunning,
                            sleepTimerSeconds = sleepTimerSeconds,
                            onStartSleepTimer = onStartSleepTimer,
                            onCancelSleepTimer = onCancelSleepTimer,
                            onExportBackup = onExportBackup,
                            onImportBackup = onImportBackup,
                            onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                            hazeState = hazeState
                        )
                    }
                    SettingsSubScreen.ABOUT -> {
                        AboutSettingsPage(
                            onBack = { currentSubScreen = SettingsSubScreen.MAIN },
                            hazeState = hazeState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsOverviewScreen(
    themeSettings: ThemeSettings,
    permissionStatus: PermissionStatus,
    onNavigate: (SettingsSubScreen) -> Unit,
    hazeState: HazeState? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.settings_header_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.settings_header_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Group 1: Appearance & Theme
        SettingsGroup(hazeState = hazeState) {
            SettingsNavigationTile(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.settings_section_appearance),
                subtitle = stringResource(R.string.settings_section_appearance_subtitle),
                onClick = { onNavigate(SettingsSubScreen.APPEARANCE) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Group 2: Audio & DSP Engine
        SettingsGroup(hazeState = hazeState) {
            SettingsNavigationTile(
                icon = Icons.Default.Security,
                title = stringResource(R.string.settings_section_permissions),
                subtitle = stringResource(R.string.settings_section_permissions_subtitle),
                onClick = { onNavigate(SettingsSubScreen.PERMISSIONS) }
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )
            SettingsNavigationTile(
                icon = Icons.Default.Tune,
                title = stringResource(R.string.settings_section_engine),
                subtitle = stringResource(R.string.settings_section_engine_subtitle),
                onClick = { onNavigate(SettingsSubScreen.DSP_ENGINE) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Group 3: Sleep Timer & Backup Suite
        SettingsGroup(hazeState = hazeState) {
            SettingsNavigationTile(
                icon = Icons.Default.Backup,
                title = stringResource(R.string.settings_section_backup),
                subtitle = stringResource(R.string.settings_section_backup_subtitle),
                onClick = { onNavigate(SettingsSubScreen.BACKUP) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Group 4: App Info & Updates
        SettingsGroup(hazeState = hazeState) {
            SettingsNavigationTile(
                icon = Icons.Default.Info,
                title = stringResource(R.string.settings_section_about),
                subtitle = stringResource(R.string.settings_section_about_subtitle, UpdateChecker.CURRENT_VERSION),
                onClick = { onNavigate(SettingsSubScreen.ABOUT) }
            )
        }

        Spacer(modifier = Modifier.height(96.dp))
    }
}

@Composable
fun SettingsGroup(
    hazeState: HazeState? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    SettingsCard(hazeState = hazeState) {
        Column(content = content)
    }
}

@Composable
fun SettingsNavigationTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsCard(
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun PermissionCard(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    icon: ImageVector,
    hazeState: HazeState? = null,
    actionContent: @Composable () -> Unit
) {
    SettingsCard(hazeState = hazeState) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (isGranted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = stringResource(R.string.status_granted),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            actionContent()
        }
    }
}
