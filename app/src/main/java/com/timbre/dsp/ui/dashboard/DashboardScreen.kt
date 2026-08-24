package com.timbre.dsp.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timbre.dsp.RoutingStatus
import com.timbre.dsp.model.AutoEqProfile
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.DeviceProfile
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.EQMode
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.PermissionStatus
import com.timbre.dsp.ui.components.AutoEqDialog
import com.timbre.dsp.ui.components.EQCurveVisualizer
import com.timbre.dsp.ui.components.ImportExportDialog
import com.timbre.dsp.ui.components.ParametricBandEditorDialog
import com.timbre.dsp.ui.components.SavePresetDialog
import java.util.Locale

@Composable
fun DashboardScreen(
    settings: DSPSettings,
    permissionStatus: PermissionStatus,
    routingStatus: RoutingStatus,
    presets: List<EQPreset>,
    currentDevice: DeviceProfile?,
    fftMagnitudes: FloatArray?,
    peakLevels: Pair<Float, Float>?,
    onToggleMaster: (Boolean) -> Unit,
    onSetEQMode: (EQMode) -> Unit,
    onBandGainChange: (Int, Float) -> Unit,
    onUpdateParametricBand: (EQBand) -> Unit,
    onAddParametricBand: () -> Unit,
    onDeleteParametricBand: (Int) -> Unit,
    onResetBands: () -> Unit,
    onSelectPreset: (EQPreset) -> Unit,
    onApplyAutoEq: (AutoEqProfile) -> Unit,
    onApplyImportedPreset: (EQPreset) -> Unit,
    onSaveCustomPreset: (String) -> Unit,
    onBindCurrentDevice: () -> Unit,
    onNavigateToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showAutoEqDialog by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var editingParametricBand by remember { mutableStateOf<EQBand?>(null) }

    val bandLabels = listOf("31 Hz", "62 Hz", "125 Hz", "250 Hz", "500 Hz", "1 kHz", "2 kHz", "4 kHz", "8 kHz", "16 kHz")

    val currentPreset = remember(settings.currentPresetId, presets, settings.bands) {
        presets.find { it.id == settings.currentPresetId } ?: EQPreset(
            id = "custom",
            name = "Custom",
            isCustom = true,
            bands = settings.bands,
            preampGain = settings.preampGain
        )
    }

    if (showAutoEqDialog) {
        AutoEqDialog(
            onDismiss = { showAutoEqDialog = false },
            onSelectProfile = {
                onApplyAutoEq(it)
                showAutoEqDialog = false
            }
        )
    }

    if (showSavePresetDialog) {
        SavePresetDialog(
            onDismiss = { showSavePresetDialog = false },
            onSave = {
                onSaveCustomPreset(it)
                showSavePresetDialog = false
            }
        )
    }

    if (showImportExportDialog) {
        ImportExportDialog(
            currentPreset = currentPreset,
            onDismiss = { showImportExportDialog = false },
            onImportPreset = {
                onApplyImportedPreset(it)
                showImportExportDialog = false
            }
        )
    }

    if (editingParametricBand != null) {
        ParametricBandEditorDialog(
            band = editingParametricBand!!,
            canDelete = settings.bands.size > 1,
            onDismiss = { editingParametricBand = null },
            onSaveBand = { onUpdateParametricBand(it) },
            onDeleteBand = { onDeleteParametricBand(editingParametricBand!!.index) }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Header & Master Power Toggle
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Timbre DSP",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (settings.isEnabled) "DSP Engine Active" else "DSP Bypassed",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (settings.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.isEnabled,
                    onCheckedChange = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleMaster(it)
                    },
                    thumbContent = {
                        Icon(
                            Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 2. Active Device Banner
        if (currentDevice != null) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (currentDevice.deviceType.name.contains("BLUETOOTH")) Icons.Default.Headphones else Icons.Default.Speaker,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = currentDevice.deviceName,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    text = "Auto-Profile: ${currentDevice.deviceType.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        TextButton(onClick = onBindCurrentDevice) {
                            Text("Remember", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // 3. Routing Status Card
        item {
            val isOperational = permissionStatus.hasShizukuPermission || permissionStatus.hasRootPermission || permissionStatus.hasDumpPermission
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSetup() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOperational)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOperational) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isOperational) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOperational) routingStatus.statusDescription else "Setup Permissions Needed",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (isOperational) "Tap to manage Shizuku / Root routing" else "Tap to configure Shizuku, Root, or Notification access",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 4. Interactive Frequency Response Curve & Live FFT Visualizer
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Acoustic Response Curve",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (settings.eqMode == EQMode.GRAPHIC_10) "10-Band Graphic" else "${settings.bands.size}-Band Parametric",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            EQCurveVisualizer(
                bands = settings.bands,
                preampGain = settings.preampGain,
                fftMagnitudes = if (settings.isEnabled) fftMagnitudes else null,
                peakLevels = if (settings.isEnabled) peakLevels else null,
                onBandGainChange = { idx, gain ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBandGainChange(idx, gain)
                },
                isInteractive = settings.isEnabled
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 5. Presets, AutoEq, and Peace EQ Action Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // AutoEq Button
                AssistChip(
                    onClick = { showAutoEqDialog = true },
                    label = { Text("AutoEq (4000+)") },
                    leadingIcon = {
                        Icon(Icons.Default.Headphones, contentDescription = null)
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                )

                // Peace EQ Import/Export Button
                AssistChip(
                    onClick = { showImportExportDialog = true },
                    label = { Text("Peace EQ / Text") },
                    leadingIcon = {
                        Icon(Icons.Default.ImportExport, contentDescription = null)
                    }
                )

                // Save Preset Button
                AssistChip(
                    onClick = { showSavePresetDialog = true },
                    label = { Text("Save Preset") },
                    leadingIcon = {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                    }
                )

                // Reset Flat Button
                IconButton(onClick = onResetBands) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset to Flat")
                }

                // Preset Chips
                for (preset in presets) {
                    val isSelected = settings.currentPresetId == preset.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectPreset(preset) },
                        label = { Text(preset.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 6. EQ Mode Selector Tab
        item {
            TabRow(
                selectedTabIndex = if (settings.eqMode == EQMode.GRAPHIC_10) 0 else 1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = settings.eqMode == EQMode.GRAPHIC_10,
                    onClick = { onSetEQMode(EQMode.GRAPHIC_10) },
                    text = { Text("Graphic EQ") }
                )
                Tab(
                    selected = settings.eqMode == EQMode.PARAMETRIC,
                    onClick = { onSetEQMode(EQMode.PARAMETRIC) },
                    text = { Text("Parametric EQ") }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 7. Band Controls depending on Mode
        if (settings.eqMode == EQMode.GRAPHIC_10) {
            // Graphic EQ Sliders
            items(settings.bands.size) { index ->
                val band = settings.bands[index]
                val label = bandLabels.getOrElse(index) { "${band.frequency.toInt()} Hz" }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.width(68.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )

                    Slider(
                        value = band.gain,
                        onValueChange = {
                            if (it == 0f) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBandGainChange(index, (it * 2).toInt() / 2f)
                        },
                        valueRange = -15f..15f,
                        enabled = settings.isEnabled,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    )

                    Text(
                        text = String.format(Locale.US, "%+.1f dB", band.gain),
                        modifier = Modifier.width(64.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        color = if (band.gain > 0) MaterialTheme.colorScheme.primary else if (band.gain < 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Parametric EQ Bands List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configured Parametric Filters (${settings.bands.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedButton(
                        onClick = onAddParametricBand,
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Band")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(settings.bands) { band ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { editingParametricBand = band },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "B${band.index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${band.frequency.toInt()} Hz • ${band.type.name.replace("_", " ")}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = String.format(Locale.US, "Gain: %+.1f dB • Q: %.2f", band.gain, band.q),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Default.Tune, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
