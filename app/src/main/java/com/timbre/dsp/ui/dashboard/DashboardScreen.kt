package com.timbre.dsp.ui.dashboard

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
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
import com.timbre.dsp.model.TargetCurve
import com.timbre.dsp.ui.components.AutoEqDialog
import com.timbre.dsp.ui.components.EQCurveVisualizer
import com.timbre.dsp.ui.components.ImportExportDialog
import com.timbre.dsp.ui.components.ParametricBandEditorDialog
import com.timbre.dsp.ui.components.SavePresetDialog
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    onSetTargetCurve: (TargetCurve) -> Unit,
    onNavigateToSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showAutoEqDialog by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var editingParametricBand by remember { mutableStateOf<EQBand?>(null) }

    if (showAutoEqDialog) {
        AutoEqDialog(
            onDismiss = { showAutoEqDialog = false },
            onSelectProfile = { profile ->
                onApplyAutoEq(profile)
                showAutoEqDialog = false
            }
        )
    }

    if (showSavePresetDialog) {
        SavePresetDialog(
            onDismiss = { showSavePresetDialog = false },
            onSave = { name ->
                onSaveCustomPreset(name)
                showSavePresetDialog = false
            }
        )
    }

    if (showImportExportDialog) {
        val currentPreset = presets.find { it.id == settings.currentPresetId } ?: EQPreset(
            id = settings.currentPresetId,
            name = "Active Preset",
            eqMode = settings.eqMode,
            bands = settings.bands,
            preampGain = settings.preampGain
        )
        ImportExportDialog(
            currentPreset = currentPreset,
            onDismiss = { showImportExportDialog = false },
            onImportPreset = { preset ->
                onApplyImportedPreset(preset)
                showImportExportDialog = false
            }
        )
    }

    if (editingParametricBand != null) {
        ParametricBandEditorDialog(
            band = editingParametricBand!!,
            canDelete = settings.bands.size > 1,
            onDismiss = { editingParametricBand = null },
            onSaveBand = { updatedBand ->
                onUpdateParametricBand(updatedBand)
                editingParametricBand = null
            },
            onDeleteBand = {
                onDeleteParametricBand(editingParametricBand!!.index)
                editingParametricBand = null
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Master Switch Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (settings.isEnabled)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PowerSettingsNew,
                            contentDescription = null,
                            tint = if (settings.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (settings.isEnabled) "Timbre DSP Engine Active" else "Timbre DSP Engine Bypassed",
                                style = MaterialTheme.typography.titleMedium
                            )
                            val presetName = presets.find { it.id == settings.currentPresetId }?.name ?: settings.currentPresetId.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                            Text(
                                text = "Preset: $presetName • ${String.format(Locale.ROOT, "%.1f", settings.preampGain)} dB Preamp",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = settings.isEnabled,
                        onCheckedChange = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onToggleMaster(it)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
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
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    else
                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOperational) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isOperational) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column {
                        Text(
                            text = "Routing: ${routingStatus.effectiveMode.name} Mode",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
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
                targetCurve = settings.targetCurve,
                fftMagnitudes = if (settings.isEnabled) fftMagnitudes else null,
                peakLevels = if (settings.isEnabled) peakLevels else null,
                onBandGainChange = { idx, gain ->
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBandGainChange(idx, gain)
                },
                onTargetCurveChange = onSetTargetCurve,
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
                AssistChip(
                    onClick = { showAutoEqDialog = true },
                    label = { Text("AutoEq Profiles") },
                    leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                )

                AssistChip(
                    onClick = { showSavePresetDialog = true },
                    label = { Text("Save Preset") },
                    leadingIcon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null) }
                )

                AssistChip(
                    onClick = { showImportExportDialog = true },
                    label = { Text("Peace / APO Import") },
                    leadingIcon = { Icon(Icons.Default.ImportExport, contentDescription = null) }
                )

                AssistChip(
                    onClick = onResetBands,
                    label = { Text("Reset Flat") },
                    leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 6. EQ Mode Tab Bar
        item {
            val tabs = listOf("10-Band Graphic", "Parametric EQ")
            val selectedIndex = if (settings.eqMode == EQMode.PARAMETRIC) 1 else 0

            PrimaryTabRow(
                selectedTabIndex = selectedIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            if (index == 0) onSetEQMode(EQMode.GRAPHIC_10)
                            else onSetEQMode(EQMode.PARAMETRIC)
                        },
                        text = { Text(title) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 7. Equalizer Controls (Graphic Sliders vs Parametric Band List)
        if (settings.eqMode != EQMode.PARAMETRIC) {
            // Graphic 10-Band EQ Sliders
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    settings.bands.forEach { band ->
                        BandSliderColumn(
                            band = band,
                            isEnabled = settings.isEnabled,
                            onGainChange = { newGain ->
                                onBandGainChange(band.index, newGain)
                            }
                        )
                    }
                }
            }
        } else {
            // Parametric EQ Band Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Parametric Filters (${settings.bands.size})",
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
                ParametricBandCard(
                    band = band,
                    isEnabled = settings.isEnabled,
                    onClick = { editingParametricBand = band }
                )
            }
        }

        // 8. Preset Quick Chips Bar
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Preset Library",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = settings.currentPresetId == preset.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                            onSelectPreset(preset)
                        },
                        label = { Text(preset.name) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                        } else null
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BandSliderColumn(
    band: EQBand,
    isEnabled: Boolean,
    onGainChange: (Float) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val freqLabel = formatFrequency(band.frequency)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(58.dp)
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "${if (band.gain > 0) "+" else ""}${String.format(Locale.ROOT, "%.1f", band.gain)}",
            style = MaterialTheme.typography.labelSmall,
            color = if (band.gain != 0f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (band.gain != 0f) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier.height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Slider(
                value = band.gain,
                onValueChange = {
                    onGainChange((it * 2).toInt() / 2f)
                },
                valueRange = -15f..15f,
                enabled = isEnabled,
                modifier = Modifier
                    .height(160.dp)
                    .width(42.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = freqLabel,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ParametricBandCard(
    band: EQBand,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = "#${band.index + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        text = "${formatFrequency(band.frequency)} • ${band.type.name.replace("_", " ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Q: ${String.format(Locale.ROOT, "%.2f", band.q)} • Gain: ${if (band.gain > 0) "+" else ""}${String.format(Locale.ROOT, "%.1f", band.gain)} dB",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onClick) {
                Icon(Icons.Default.Tune, contentDescription = "Edit Band")
            }
        }
    }
}

private fun formatFrequency(freq: Float): String {
    return if (freq >= 1000f) {
        val kHz = freq / 1000f
        if (kHz % 1.0f == 0f) "${kHz.toInt()}k" else "${String.format(Locale.ROOT, "%.1f", kHz)}k"
    } else {
        "${freq.toInt()}Hz"
    }
}
