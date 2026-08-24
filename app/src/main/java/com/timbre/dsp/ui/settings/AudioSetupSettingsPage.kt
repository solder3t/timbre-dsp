package com.timbre.dsp.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timbre.dsp.R
import com.timbre.dsp.model.PermissionStatus
import com.timbre.dsp.permission.PermissionManager
import com.timbre.dsp.ui.magisk.MagiskInstallerDialog
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioSetupSettingsPage(
    permissionStatus: PermissionStatus,
    onRequestShizuku: () -> Unit,
    onGrantDumpShizuku: () -> Unit,
    onGrantDumpRoot: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onBack: () -> Unit,
    hazeState: HazeState? = null
) {
    val context = LocalContext.current
    var showMagiskDialog by remember { mutableStateOf(false) }

    if (showMagiskDialog) {
        MagiskInstallerDialog(
            isRootAvailable = permissionStatus.hasRootPermission || permissionStatus.isRootAvailable,
            onDismiss = { showMagiskDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings_section_permissions),
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

        // 1. Shizuku Service Card
        PermissionCard(
            title = stringResource(R.string.perm_shizuku_title),
            subtitle = if (permissionStatus.hasShizukuPermission) stringResource(R.string.status_granted)
            else stringResource(R.string.perm_shizuku_desc),
            isGranted = permissionStatus.hasShizukuPermission,
            icon = Icons.Default.Security,
            hazeState = hazeState
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!permissionStatus.hasShizukuPermission) {
                    Button(
                        onClick = onRequestShizuku,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.btn_grant_permission))
                    }
                } else if (!permissionStatus.hasDumpPermission) {
                    Button(
                        onClick = onGrantDumpShizuku,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.btn_grant_dump))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Magisk / Root Hook Card
        PermissionCard(
            title = stringResource(R.string.perm_root_title),
            subtitle = if (permissionStatus.hasRootPermission) stringResource(R.string.status_active)
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

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Notification Access Card
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

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Battery Optimization Whitelist Card
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

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Manual ADB Fallback Card
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.adb_command_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(6.dp))
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

        Spacer(modifier = Modifier.height(12.dp))

        // 6. OEM / Samsung Diagnostics Card
        val isSamsung = android.os.Build.MANUFACTURER.contains("samsung", ignoreCase = true)
        SettingsCard(hazeState = hazeState) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSamsung) "Samsung SoundAlive & Dolby Atmos" else "OEM Audio Effects Diagnostics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
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

        Spacer(modifier = Modifier.height(100.dp))
    }
}
