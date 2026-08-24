package com.timbre.dsp.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timbre.dsp.R
import com.timbre.dsp.model.OutputDeviceType
import com.timbre.dsp.RoutingStatus
import com.timbre.dsp.model.AutoEqProfile
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.DeviceProfile
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.EQMode
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.PermissionStatus
import com.timbre.dsp.model.TargetCurve
import com.timbre.dsp.ui.components.AiEqDialog
import com.timbre.dsp.ui.components.AutoEqDialog
import com.timbre.dsp.ui.components.EQCurveVisualizer
import com.timbre.dsp.ui.components.GlassmorphicCard
import com.timbre.dsp.ui.components.ImportExportDialog
import com.timbre.dsp.ui.components.ParametricBandEditorDialog
import com.timbre.dsp.ui.components.SavePresetDialog
import dev.chrisbanes.haze.HazeState
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
    onApplyAiSettings: (DSPSettings) -> Unit = {},
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showAiEqDialog by remember { mutableStateOf(false) }
    var showAutoEqDialog by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var editingParametricBand by remember { mutableStateOf<EQBand?>(null) }

    if (showAiEqDialog) {
        AiEqDialog(
            currentSettings = settings,
            onDismiss = { showAiEqDialog = false },
            onApplySettings = { newSettings ->
                onApplyAiSettings(newSettings)
            }
        )
    }

    if (showAutoEqDialog) {
        AutoEqDialog(
            onDismiss = { showAutoEqDialog = false },
            onSelectProfile = { profile ->
                onApplyAutoEq(profile)
                showAutoEqDialog = false
            },
            onAiGenerate = { query ->
                showAiEqDialog = true
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
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 100.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Master Switch Card with Frosted Glass
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                containerColor = if (settings.isEnabled)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                                text = if (settings.isEnabled) stringResource(R.string.dsp_engine_active) else stringResource(R.string.dsp_engine_bypassed),
                                style = MaterialTheme.typography.titleMedium
                            )
                            val presetName = presets.find { it.id == settings.currentPresetId }?.name ?: settings.currentPresetId.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                            Text(
                                text = stringResource(R.string.dsp_preset_summary, presetName, String.format(Locale.ROOT, "%.1f", settings.preampGain)),
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
                        },
                        thumbContent = {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // 2. Active Device Banner
        if (currentDevice != null) {
            item {
                var showDeviceMenu by remember { mutableStateOf(false) }
                val isBoundToCurrent = currentDevice.presetId == settings.currentPresetId && currentDevice.presetId.isNotBlank()
                val boundPreset = presets.find { it.id == currentDevice.presetId }

                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = if (boundPreset != null)
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (currentDevice.deviceType) {
                                    OutputDeviceType.BLUETOOTH -> Icons.Default.Headphones
                                    OutputDeviceType.USB -> Icons.Default.Usb
                                    OutputDeviceType.WIRED -> Icons.Default.Headset
                                    OutputDeviceType.SPEAKER -> Icons.Default.Speaker
                                    else -> Icons.AutoMirrored.Filled.VolumeUp
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = currentDevice.deviceName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                if (boundPreset != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.device_auto_preset_bound, boundPreset.name),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Text(
                                        text = stringResource(R.string.device_auto_routing_active, currentDevice.deviceType.displayName),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box {
                            FilledTonalButton(
                                onClick = { showDeviceMenu = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBoundToCurrent) Icons.Default.Check else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isBoundToCurrent) stringResource(R.string.action_linked) else stringResource(R.string.action_link_preset),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            DropdownMenu(
                                expanded = showDeviceMenu,
                                onDismissRequest = { showDeviceMenu = false }
                            ) {
                                val currentPresetName = presets.find { it.id == settings.currentPresetId }?.name ?: "Current"
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.action_link_to_preset, currentPresetName), fontWeight = FontWeight.SemiBold) },
                                    leadingIcon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    onClick = {
                                        onBindCurrentDevice()
                                        showDeviceMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // 3. Routing Status Card
        item {
            val isOperational = permissionStatus.hasShizukuPermission || permissionStatus.hasRootPermission || permissionStatus.hasDumpPermission
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSetup() },
                hazeState = hazeState,
                shape = RoundedCornerShape(12.dp),
                containerColor = if (isOperational)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                else
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
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
                            text = stringResource(R.string.routing_mode_banner, routingStatus.effectiveMode.name),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isOperational) stringResource(R.string.routing_manage_operational) else stringResource(R.string.routing_configure_required),
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
                    text = stringResource(R.string.acoustic_response_curve),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (settings.eqMode == EQMode.GRAPHIC_10) stringResource(R.string.mode_graphic_10) else stringResource(R.string.mode_parametric_n, settings.bands.size),
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

        // 5. Presets, AI Assistant, AutoEq, and Action Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { showAiEqDialog = true },
                    label = { Text(stringResource(R.string.action_ai_assistant)) },
                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                )

                AssistChip(
                    onClick = { showAutoEqDialog = true },
                    label = { Text(stringResource(R.string.action_autoeq_profiles)) },
                    leadingIcon = { Icon(Icons.Default.Headphones, contentDescription = null) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f))
                )

                AssistChip(
                    onClick = { showSavePresetDialog = true },
                    label = { Text(stringResource(R.string.action_save_preset)) },
                    leadingIcon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null) }
                )

                AssistChip(
                    onClick = { showImportExportDialog = true },
                    label = { Text(stringResource(R.string.action_peace_import)) },
                    leadingIcon = { Icon(Icons.Default.ImportExport, contentDescription = null) }
                )

                AssistChip(
                    onClick = onResetBands,
                    label = { Text(stringResource(R.string.action_reset_flat)) },
                    leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 6. Preset Quick Chips Bar (Preset Library right above the EQ)
        item {
            Text(
                text = stringResource(R.string.preset_library_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))

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
            Spacer(modifier = Modifier.height(12.dp))
        }

        // 7. EQ Mode Tab Bar
        item {
            val tabs = listOf(stringResource(R.string.mode_graphic_10), "Parametric EQ")
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

        // 8. Equalizer Controls (Graphic Sliders vs Parametric Band List)
        if (settings.eqMode != EQMode.PARAMETRIC) {
            // Classic Full-Width Horizontal Band Rows
            items(settings.bands.size) { index ->
                val band = settings.bands[index]
                val label = formatFrequency(band.frequency)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.width(64.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Slider(
                        value = band.gain,
                        onValueChange = {
                            if (it == 0f) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onBandGainChange(index, (it * 2).toInt() / 2f)
                        },
                        valueRange = -15f..15f,
                        enabled = settings.isEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )

                    Text(
                        text = "${if (band.gain > 0) "+" else ""}${String.format(Locale.ROOT, "%.1f", band.gain)} dB",
                        modifier = Modifier.width(68.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        color = if (band.gain > 0) MaterialTheme.colorScheme.primary else if (band.gain < 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (band.gain != 0f) FontWeight.SemiBold else FontWeight.Normal
                    )
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
                        text = stringResource(R.string.parametric_filters_header, settings.bands.size),
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedButton(
                        onClick = onAddParametricBand,
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.action_add_band))
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
    }
}

@Composable
private fun ParametricBandCard(
    band: EQBand,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isEnabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
