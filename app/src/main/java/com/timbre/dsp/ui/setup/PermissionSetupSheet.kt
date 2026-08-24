package com.timbre.dsp.ui.setup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.timbre.dsp.model.PermissionStatus
import com.timbre.dsp.permission.PermissionManager
import com.timbre.dsp.ui.components.GlassmorphicCard
import com.timbre.dsp.ui.magisk.MagiskInstallerDialog
import dev.chrisbanes.haze.HazeState

@Composable
fun PermissionSetupSheet(
    permissionStatus: PermissionStatus,
    isSleepTimerRunning: Boolean,
    sleepTimerSeconds: Int,
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
    var showMagiskDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            val ok = onExportBackup(uri)
            Toast.makeText(context, if (ok) "Configuration backup exported!" else "Export failed", Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val ok = onImportBackup(uri)
            Toast.makeText(context, if (ok) "Configuration restored successfully!" else "Invalid backup file", Toast.LENGTH_SHORT).show()
        }
    }

    if (showMagiskDialog) {
        MagiskInstallerDialog(
            isRootAvailable = permissionStatus.hasRootPermission || permissionStatus.isRootAvailable,
            onDismiss = { showMagiskDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "System Permissions & Tools",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Configure routing permissions, sleep timer, and backups",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. DSP Sleep Timer Card (Option B)
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            containerColor = if (isSleepTimerRunning)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bedtime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("DSP Sleep Timer", style = MaterialTheme.typography.titleMedium)
                            val minutes = sleepTimerSeconds / 60
                            val seconds = sleepTimerSeconds % 60
                            Text(
                                text = if (isSleepTimerRunning) "Auto-bypass in ${minutes}m ${seconds}s" else "Automatically pause DSP for bedtime listening",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                            label = { Text("${mins}m") }
                        )
                    }
                    if (isSleepTimerRunning) {
                        OutlinedButton(onClick = onCancelSleepTimer) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Full Suite Backup & Restore Card (Option B)
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Backup & Restore Suite", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Export/import all custom presets, device profiles, & app rules",
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
                        Text("Export Backup")
                    }

                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Shizuku Integration Card
        SetupItemCard(
            title = "Shizuku Integration",
            subtitle = if (permissionStatus.hasShizukuPermission) "Shizuku binder connected & permission granted"
            else if (permissionStatus.isShizukuRunning) "Shizuku is running, permission required"
            else "Shizuku service is not running or not installed",
            isConfigured = permissionStatus.hasShizukuPermission,
            icon = Icons.Default.Security,
            hazeState = hazeState
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRequestShizuku,
                    enabled = permissionStatus.isShizukuRunning && !permissionStatus.hasShizukuPermission,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (permissionStatus.hasShizukuPermission) "Authorized" else "Authorize Shizuku")
                }
                if (permissionStatus.hasShizukuPermission && !permissionStatus.hasDumpPermission) {
                    OutlinedButton(
                        onClick = onGrantDumpShizuku,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Grant DUMP Hook")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Root Access Card
        SetupItemCard(
            title = "Root Access & Magisk Engine",
            subtitle = if (permissionStatus.hasRootPermission) "Root access granted (SU active)"
            else if (permissionStatus.isRootAvailable) "Root detected, permissions pending"
            else "Standard unrooted device",
            isConfigured = permissionStatus.hasRootPermission,
            icon = Icons.Default.FlashOn,
            hazeState = hazeState
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (permissionStatus.isRootAvailable && !permissionStatus.hasDumpPermission) {
                    Button(
                        onClick = onGrantDumpRoot,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Grant DUMP via SU")
                    }
                }
                OutlinedButton(
                    onClick = { showMagiskDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Magisk / KSU Module")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Notification Access Card
        SetupItemCard(
            title = "Notification / Media Access",
            subtitle = if (permissionStatus.hasNotificationAccess) "Notification listener active for media detection"
            else "Required to auto-detect media players like Spotify & Apple Music",
            isConfigured = permissionStatus.hasNotificationAccess,
            icon = Icons.Default.Notifications,
            hazeState = hazeState
        ) {
            Button(
                onClick = onOpenNotificationSettings,
                enabled = !permissionStatus.hasNotificationAccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (permissionStatus.hasNotificationAccess) "Active" else "Grant Media Notification Access")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6. Battery Optimization Card
        SetupItemCard(
            title = "Battery Unrestricted Mode",
            subtitle = if (permissionStatus.isBatteryOptimizationIgnored) "Battery optimization disabled for seamless background DSP"
            else "Disable battery optimizations so Android does not kill the DSP audio daemon",
            isConfigured = permissionStatus.isBatteryOptimizationIgnored,
            icon = Icons.Default.BatteryChargingFull,
            hazeState = hazeState
        ) {
            Button(
                onClick = onRequestBatteryOptimization,
                enabled = !permissionStatus.isBatteryOptimizationIgnored,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (permissionStatus.isBatteryOptimizationIgnored) "Ignored (Optimal)" else "Request Unrestricted Battery")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 7. Manual ADB Fallback Card
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manual ADB Fallback (No Root / No Shizuku)", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "If Shizuku or Root is unavailable, run this single command once via ADB on your PC:",
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
                            Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 8. OEM / Samsung Audio Diagnostics Card
        val isSamsung = android.os.Build.MANUFACTURER.contains("samsung", ignoreCase = true)
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
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
                            val intent = android.content.Intent(android.provider.Settings.ACTION_SOUND_SETTINGS).apply {
                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open sound settings", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open System Sound Settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SetupItemCard(
    title: String,
    subtitle: String,
    isConfigured: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    hazeState: HazeState? = null,
    content: @Composable () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = RoundedCornerShape(16.dp),
        containerColor = if (isConfigured)
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
                    tint = if (isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
                    imageVector = if (isConfigured) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
