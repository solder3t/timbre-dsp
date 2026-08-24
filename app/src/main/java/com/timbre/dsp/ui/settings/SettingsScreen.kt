package com.timbre.dsp.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timbre.dsp.R
import com.timbre.dsp.data.api.UpdateChecker
import com.timbre.dsp.data.api.UpdateInfo
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.PermissionStatus
import com.timbre.dsp.model.RoutingMode
import com.timbre.dsp.permission.PermissionManager
import com.timbre.dsp.theme.ThemeMode
import com.timbre.dsp.theme.ThemeSettings
import com.timbre.dsp.ui.components.GlassmorphicCard
import com.timbre.dsp.ui.components.UpdateDialog
import com.timbre.dsp.ui.magisk.MagiskInstallerDialog
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch

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
    onSetFrostedGlass: (Boolean) -> Unit,
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
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showMagiskDialog by remember { mutableStateOf(false) }
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var detectedUpdate by remember { mutableStateOf<UpdateInfo?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            val ok = onExportBackup(uri)
            Toast.makeText(context, if (ok) context.getString(R.string.toast_backup_exported) else context.getString(R.string.toast_backup_export_failed), Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val ok = onImportBackup(uri)
            Toast.makeText(context, if (ok) context.getString(R.string.toast_backup_restored) else context.getString(R.string.toast_backup_invalid), Toast.LENGTH_SHORT).show()
        }
    }

    if (showMagiskDialog) {
        MagiskInstallerDialog(
            isRootAvailable = permissionStatus.hasRootPermission || permissionStatus.isRootAvailable,
            onDismiss = { showMagiskDialog = false }
        )
    }

    if (detectedUpdate != null) {
        UpdateDialog(
            updateInfo = detectedUpdate!!,
            onDismiss = { detectedUpdate = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ═══════════════════════════════════════════════════════════════════════════
        // SECTION 1: APPEARANCE & THEME
        // ═══════════════════════════════════════════════════════════════════════════
        SettingsSectionTitle(title = stringResource(R.string.settings_section_appearance))

        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Theme Mode Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.theme_mode_title), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ThemeMode.values().forEach { mode ->
                        val label = when (mode) {
                            ThemeMode.SYSTEM -> stringResource(R.string.theme_mode_system)
                            ThemeMode.DARK -> stringResource(R.string.theme_mode_dark)
                            ThemeMode.LIGHT -> stringResource(R.string.theme_mode_light)
                            ThemeMode.AMOLED -> stringResource(R.string.theme_mode_amoled)
                        }
                        FilterChip(
                            selected = themeSettings.themeMode == mode,
                            onClick = { onSetThemeMode(mode) },
                            label = { Text(label) },
                            leadingIcon = if (themeSettings.themeMode == mode) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Material You Dynamic Color Switch
                SettingsSwitchRow(
                    icon = Icons.Default.Wallpaper,
                    title = stringResource(R.string.theme_dynamic_title),
                    subtitle = stringResource(R.string.theme_dynamic_desc),
                    checked = themeSettings.useDynamicColor,
                    onCheckedChange = onSetDynamicColor
                )

                // Custom Accent Color Palette
                AnimatedVisibility(visible = !themeSettings.useDynamicColor) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ColorLens, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.theme_accent_title), style = MaterialTheme.typography.titleSmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ThemeSettings.predefinedAccents.forEach { accent ->
                                val isSelected = themeSettings.seedColor == accent.colorValue
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(accent.colorValue))
                                        .clickable { onSetSeedColor(accent.colorValue) }
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = accent.name,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Frosted Glass Blur Switch
                SettingsSwitchRow(
                    icon = Icons.Default.BlurOn,
                    title = stringResource(R.string.theme_blur_title),
                    subtitle = stringResource(R.string.theme_blur_desc),
                    checked = themeSettings.enableFrostedGlass,
                    onCheckedChange = onSetFrostedGlass
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ═══════════════════════════════════════════════════════════════════════════
        // SECTION 2: AUDIO SETUP & PERMISSIONS
        // ═══════════════════════════════════════════════════════════════════════════
        SettingsSectionTitle(title = stringResource(R.string.settings_section_permissions))

        // Shizuku Integration Card
        PermissionCard(
            title = stringResource(R.string.perm_shizuku_title),
            subtitle = if (permissionStatus.hasShizukuPermission) "Shizuku binder connected & permission active"
            else if (permissionStatus.isShizukuRunning) "Shizuku service running, permission required"
            else stringResource(R.string.perm_shizuku_desc),
            isGranted = permissionStatus.hasShizukuPermission,
            icon = Icons.Default.Security,
            hazeState = hazeState
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRequestShizuku,
                    enabled = permissionStatus.isShizukuRunning && !permissionStatus.hasShizukuPermission,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (permissionStatus.hasShizukuPermission) stringResource(R.string.status_granted) else stringResource(R.string.btn_grant_permission))
                }
                if (permissionStatus.hasShizukuPermission && !permissionStatus.hasDumpPermission) {
                    OutlinedButton(
                        onClick = onGrantDumpShizuku,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.btn_grant_dump))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Root Access Card
        PermissionCard(
            title = stringResource(R.string.perm_root_title),
            subtitle = if (permissionStatus.hasRootPermission) "Root access granted (SU active)"
            else if (permissionStatus.isRootAvailable) "Root detected, SU permissions pending"
            else stringResource(R.string.perm_root_desc),
            isGranted = permissionStatus.hasRootPermission,
            icon = Icons.Default.FlashOn,
            hazeState = hazeState
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (permissionStatus.isRootAvailable && !permissionStatus.hasDumpPermission) {
                    Button(
                        onClick = onGrantDumpRoot,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.btn_grant_dump_su))
                    }
                }
                OutlinedButton(
                    onClick = { showMagiskDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.btn_install_module))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Notification Access Card
        PermissionCard(
            title = stringResource(R.string.perm_notif_title),
            subtitle = if (permissionStatus.hasNotificationAccess) "Notification listener active for media session detection"
            else stringResource(R.string.perm_notif_desc),
            isGranted = permissionStatus.hasNotificationAccess,
            icon = Icons.Default.Notifications,
            hazeState = hazeState
        ) {
            Button(
                onClick = onOpenNotificationSettings,
                enabled = !permissionStatus.hasNotificationAccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (permissionStatus.hasNotificationAccess) stringResource(R.string.status_enabled) else stringResource(R.string.btn_open_settings))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Battery Optimization Card
        PermissionCard(
            title = stringResource(R.string.perm_battery_title),
            subtitle = if (permissionStatus.isBatteryOptimizationIgnored) "Battery optimization disabled for seamless background DSP"
            else stringResource(R.string.perm_battery_desc),
            isGranted = permissionStatus.isBatteryOptimizationIgnored,
            icon = Icons.Default.BatteryChargingFull,
            hazeState = hazeState
        ) {
            Button(
                onClick = onRequestBatteryOptimization,
                enabled = !permissionStatus.isBatteryOptimizationIgnored,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (permissionStatus.isBatteryOptimizationIgnored) stringResource(R.string.status_ignored) else stringResource(R.string.btn_disable_optimization))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ADB Dump Fallback Card
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.adb_command_title), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.adb_command_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = PermissionManager.ADB_DUMP_COMMAND,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", PermissionManager.ADB_DUMP_COMMAND))
                            Toast.makeText(context, context.getString(R.string.toast_copied_clipboard), Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // OEM / Samsung Diagnostics Card
        val isSamsung = android.os.Build.MANUFACTURER.contains("samsung", ignoreCase = true)
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSamsung) "Samsung SoundAlive & Dolby Atmos" else "OEM Audio Effects Diagnostics",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isSamsung)
                        "Samsung One UI includes built-in Dolby Atmos and SoundAlive which apply post-processing. For pure, bit-accurate EQ curves, set Dolby Atmos to Auto or Off in Samsung Sound Settings."
                    else
                        "System audio enhancements (e.g. Dirac, Dolby Atmos) may alter output dynamics before Timbre DSP. You can configure them in Sound Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_SOUND_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, context.getString(R.string.toast_could_not_open_sound_settings), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_open_sound_settings))
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ═══════════════════════════════════════════════════════════════════════════
        // SECTION 3: DSP ENGINE & AUDIO ROUTING
        // ═══════════════════════════════════════════════════════════════════════════
        SettingsSectionTitle(title = stringResource(R.string.settings_section_engine))

        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.engine_routing_title), style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.engine_routing_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RoutingMode.values().forEach { mode ->
                        FilterChip(
                            selected = settings.routingMode == mode,
                            onClick = { onSetRoutingMode(mode) },
                            label = { Text(mode.name) },
                            leadingIcon = if (settings.routingMode == mode) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Headroom Peak Limiter
                SettingsSwitchRow(
                    icon = Icons.Default.Speed,
                    title = stringResource(R.string.engine_limiter_title),
                    subtitle = stringResource(R.string.engine_limiter_desc),
                    checked = settings.limiterEnabled,
                    onCheckedChange = onToggleLimiter
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Live Audio Visualizer
                SettingsSwitchRow(
                    icon = Icons.Default.Speed,
                    title = stringResource(R.string.engine_visualizer_title),
                    subtitle = stringResource(R.string.engine_visualizer_desc),
                    checked = settings.isVisualizerEnabled,
                    onCheckedChange = onToggleVisualizer
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ═══════════════════════════════════════════════════════════════════════════
        // SECTION 4: SLEEP TIMER & BACKUP SUITE
        // ═══════════════════════════════════════════════════════════════════════════
        SettingsSectionTitle(title = stringResource(R.string.settings_section_backup))

        // Sleep Timer Card
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bedtime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(stringResource(R.string.sleep_timer_title), style = MaterialTheme.typography.titleMedium)
                        val minutes = sleepTimerSeconds / 60
                        val seconds = sleepTimerSeconds % 60
                        Text(
                            text = if (isSleepTimerRunning) stringResource(R.string.sleep_timer_running, "${minutes}m ${seconds}s") else stringResource(R.string.sleep_timer_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        FilterChip(
                            selected = isSleepTimerRunning && (sleepTimerSeconds in ((mins - 1) * 60)..(mins * 60)),
                            onClick = { onStartSleepTimer(mins) },
                            label = { Text(stringResource(R.string.mins_format, mins)) }
                        )
                    }
                    if (isSleepTimerRunning) {
                        OutlinedButton(onClick = onCancelSleepTimer) {
                            Text(stringResource(R.string.btn_cancel_timer))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Full Suite Backup & Restore Card
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(stringResource(R.string.backup_suite_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.backup_suite_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { exportLauncher.launch("timbre_backup.json") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_export_backup))
                    }

                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.btn_restore_backup))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ═══════════════════════════════════════════════════════════════════════════
        // SECTION 5: APP INFO & UPDATES
        // ═══════════════════════════════════════════════════════════════════════════
        SettingsSectionTitle(title = stringResource(R.string.settings_section_about))

        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(stringResource(R.string.app_version_format, UpdateChecker.CURRENT_VERSION), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = stringResource(R.string.about_app_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Check for Updates Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isCheckingUpdates = true
                            try {
                                val info = UpdateChecker.checkForUpdates()
                                isCheckingUpdates = false
                                if (info.isAvailable) {
                                    detectedUpdate = info
                                } else {
                                    Toast.makeText(context, context.getString(R.string.update_up_to_date, UpdateChecker.CURRENT_VERSION), Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                isCheckingUpdates = false
                                Toast.makeText(context, context.getString(R.string.update_error), Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isCheckingUpdates,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isCheckingUpdates) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.update_checking))
                    } else {
                        Icon(Icons.Default.SystemUpdate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_check_updates))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // GitHub Source Code
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/solder3t/timbre-dsp")).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.about_github_title))
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(
    hazeState: HazeState? = null,
    content: @Composable () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        content()
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PermissionCard(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    icon: ImageVector,
    hazeState: HazeState? = null,
    content: @Composable () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = RoundedCornerShape(16.dp),
        containerColor = if (isGranted)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
