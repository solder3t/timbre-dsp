package com.timbre.dsp.ui.setup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.timbre.dsp.ui.magisk.MagiskInstallerDialog

@Composable
fun PermissionSetupSheet(
    permissionStatus: PermissionStatus,
    onRequestShizuku: () -> Unit,
    onGrantDumpShizuku: () -> Unit,
    onGrantDumpRoot: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onRequestBatteryOptimization: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
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
                    text = "System Permissions & Setup",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Configure routing permissions for system-wide DSP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Shizuku Integration Card
        SetupItemCard(
            title = "Shizuku Integration",
            subtitle = if (permissionStatus.hasShizukuPermission) "Shizuku binder connected & permission granted"
            else if (permissionStatus.isShizukuRunning) "Shizuku is running, permission required"
            else "Shizuku service is not running or not installed",
            isConfigured = permissionStatus.hasShizukuPermission,
            icon = Icons.Default.Security
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

        // 2. Root Access Card
        SetupItemCard(
            title = "Root Access & Magisk Engine",
            subtitle = if (permissionStatus.hasRootPermission) "Root access granted (SU active)"
            else if (permissionStatus.isRootAvailable) "Root detected, permissions pending"
            else "Standard unrooted device",
            isConfigured = permissionStatus.hasRootPermission,
            icon = Icons.Default.FlashOn
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

        // 3. Notification Access Card
        SetupItemCard(
            title = "Notification / Media Access",
            subtitle = if (permissionStatus.hasNotificationAccess) "Media session tracking active"
            else "Required to detect playback from Spotify, Apple Music, etc.",
            isConfigured = permissionStatus.hasNotificationAccess,
            icon = Icons.Default.Notifications
        ) {
            Button(
                onClick = onOpenNotificationSettings,
                enabled = !permissionStatus.hasNotificationAccess,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (permissionStatus.hasNotificationAccess) "Access Enabled" else "Enable Notification Access")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Battery Optimization Card
        SetupItemCard(
            title = "Background Battery Optimization",
            subtitle = if (permissionStatus.isBatteryOptimizationIgnored) "Unrestricted background DSP execution"
            else "Prevent Android from killing audio service during screen-off",
            isConfigured = permissionStatus.isBatteryOptimizationIgnored,
            icon = Icons.Default.BatteryChargingFull
        ) {
            Button(
                onClick = onRequestBatteryOptimization,
                enabled = !permissionStatus.isBatteryOptimizationIgnored,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (permissionStatus.isBatteryOptimizationIgnored) "Unrestricted" else "Disable Optimization")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 5. Manual ADB Fallback Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Manual ADB Command (No Root/Shizuku)",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If you do not have Root or Shizuku, run this command from your computer:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = PermissionManager.ADB_DUMP_COMMAND,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("ADB Command", PermissionManager.ADB_DUMP_COMMAND)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Command copied to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
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
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConfigured)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
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
