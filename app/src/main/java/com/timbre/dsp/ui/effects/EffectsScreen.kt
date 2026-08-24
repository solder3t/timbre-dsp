package com.timbre.dsp.ui.effects

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timbre.dsp.R
import com.timbre.dsp.data.ImpulseResponseProfile
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.HearingAudiogram
import com.timbre.dsp.ui.components.GlassmorphicCard
import com.timbre.dsp.ui.hearing.HearingTestWizard
import dev.chrisbanes.haze.HazeState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectsScreen(
    settings: DSPSettings,
    irProfiles: List<ImpulseResponseProfile>,
    onPreampGainChange: (Float) -> Unit,
    onAutoPreampChange: (Boolean) -> Unit,
    onChannelBalanceChange: (Float) -> Unit,
    onMonoChange: (Boolean) -> Unit,
    onLimiterChange: (Boolean) -> Unit,
    onBassBoostChange: (enabled: Boolean, gain: Float, cutoff: Float) -> Unit,
    onCrossfeedChange: (enabled: Boolean, strength: Float) -> Unit,
    onVirtualizerChange: (enabled: Boolean, strength: Float) -> Unit,
    onClarityChange: (enabled: Boolean, gain: Float) -> Unit,
    onConvolutionChange: (enabled: Boolean, profileId: String, wetDry: Float) -> Unit,
    onImportCustomIR: (Uri, String) -> Boolean,
    onApplyHearingAudiogram: (HearingAudiogram, EQPreset) -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showHearingWizard by remember { mutableStateOf(false) }
    var irDropdownExpanded by remember { mutableStateOf(false) }

    val irPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "custom_ir.wav"
            val success = onImportCustomIR(uri, fileName)
            if (success) {
                Toast.makeText(context, context.getString(R.string.toast_loaded_ir, fileName), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, context.getString(R.string.toast_failed_ir), Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showHearingWizard) {
        HearingTestWizard(
            onDismiss = { showHearingWizard = false },
            onCompleteTest = { audiogram, preset ->
                onApplyHearingAudiogram(audiogram, preset)
                showHearingWizard = false
            }
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.effects_master_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.effects_master_active),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Hearing Calibration Wizard Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                containerColor = if (settings.hearingAudiogram.isCalibrated)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Hearing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.effects_section_hearing),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (settings.hearingAudiogram.isCalibrated)
                                    stringResource(R.string.effects_calibrated_profile)
                                else
                                    stringResource(R.string.effects_hearing_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showHearingWizard = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (settings.hearingAudiogram.isCalibrated) stringResource(R.string.effects_retake_hearing_test) else stringResource(R.string.effects_perform_hearing_test))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Convolution / Impulse Response (.wav / .irs) Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                containerColor = if (settings.convolutionEnabled)
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
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.SurroundSound,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.effects_section_convolution),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    stringResource(R.string.effects_convolution_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.convolutionEnabled,
                            onCheckedChange = { onConvolutionChange(it, settings.activeConvolutionId, settings.convolutionWetDry) }
                        )
                    }

                    if (settings.convolutionEnabled) {
                        Spacer(modifier = Modifier.height(12.dp))

                        val currentIRName = irProfiles.find { it.id == settings.activeConvolutionId }?.name ?: "Warm Studio Room"

                        Text(stringResource(R.string.effects_active_ir), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))

                        ExposedDropdownMenuBox(
                            expanded = irDropdownExpanded,
                            onExpandedChange = { irDropdownExpanded = !irDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = currentIRName,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = irDropdownExpanded) },
                                modifier = Modifier.menuAnchor(androidx.compose.material3.ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = irDropdownExpanded,
                                onDismissRequest = { irDropdownExpanded = false }
                            ) {
                                irProfiles.forEach { profile ->
                                    DropdownMenuItem(
                                        text = { Text(profile.name) },
                                        onClick = {
                                            onConvolutionChange(true, profile.id, settings.convolutionWetDry)
                                            irDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.effects_wet_dry_mix), style = MaterialTheme.typography.bodySmall)
                            Text("${(settings.convolutionWetDry * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = settings.convolutionWetDry,
                            onValueChange = { onConvolutionChange(true, settings.activeConvolutionId, it) },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                irPickerLauncher.launch(arrayOf("audio/x-wav", "audio/wav", "application/octet-stream", "*/*"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.effects_import_custom_ir))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Preamp Gain & Auto-Preamp Headroom Protection
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.effects_section_dynamics),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (settings.autoPreampEnabled) stringResource(R.string.effects_auto_preamp_desc) else stringResource(R.string.effects_limiter_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = String.format(Locale.US, "%.1f dB", settings.preampGain),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    if (!settings.autoPreampEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = settings.preampGain,
                            onValueChange = onPreampGainChange,
                            valueRange = -15f..15f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Auto-Preamp Headroom Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.effects_auto_preamp_title),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    stringResource(R.string.effects_auto_preamp_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.autoPreampEnabled,
                            onCheckedChange = onAutoPreampChange
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Anti-Clipping Peak Limiter Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.effects_limiter_title),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    stringResource(R.string.effects_limiter_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.limiterEnabled,
                            onCheckedChange = onLimiterChange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Channel Balance & Mono Summing Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Balance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.effects_balance_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                stringResource(R.string.effects_balance_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${stringResource(R.string.effects_label_left)} ${if (settings.channelBalance < 0) String.format(Locale.US, "%.0f%%", -settings.channelBalance * 100) else ""}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = stringResource(R.string.effects_label_center),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "${stringResource(R.string.effects_label_right)} ${if (settings.channelBalance > 0) String.format(Locale.US, "%.0f%%", settings.channelBalance * 100) else ""}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Slider(
                        value = settings.channelBalance,
                        onValueChange = onChannelBalanceChange,
                        valueRange = -1f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                stringResource(R.string.effects_force_mono_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                stringResource(R.string.effects_force_mono_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.isMono,
                            onCheckedChange = onMonoChange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Bass Boost Card
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                containerColor = if (settings.bassBoostEnabled)
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
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Speaker,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.effects_bass_boost_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    stringResource(R.string.effects_bass_boost_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.bassBoostEnabled,
                            onCheckedChange = { onBassBoostChange(it, settings.bassBoostGain, settings.bassBoostCutoffFreq) }
                        )
                    }

                    if (settings.bassBoostEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.effects_strength), style = MaterialTheme.typography.bodySmall)
                            Text(
                                String.format(Locale.US, "+%.1f dB", settings.bassBoostGain),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = settings.bassBoostGain,
                            onValueChange = { onBassBoostChange(true, it, settings.bassBoostCutoffFreq) },
                            valueRange = 0f..15f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.effects_cutoff_frequency), style = MaterialTheme.typography.bodySmall)
                            Text(
                                String.format(Locale.US, "%.0f Hz", settings.bassBoostCutoffFreq),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = settings.bassBoostCutoffFreq,
                            onValueChange = { onBassBoostChange(true, settings.bassBoostGain, it) },
                            valueRange = 40f..200f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Binaural Crossfeed & Spatializer
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                containerColor = if (settings.crossfeedEnabled || settings.virtualizerEnabled)
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
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.effects_crossfeed_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    stringResource(R.string.effects_crossfeed_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.crossfeedEnabled,
                            onCheckedChange = { onCrossfeedChange(it, settings.crossfeedStrength) }
                        )
                    }

                    if (settings.crossfeedEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.effects_strength), style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${(settings.crossfeedStrength * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = settings.crossfeedStrength,
                            onValueChange = { onCrossfeedChange(true, it) },
                            valueRange = 0f..1f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                stringResource(R.string.effects_virtualizer_title),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                stringResource(R.string.effects_virtualizer_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.virtualizerEnabled,
                            onCheckedChange = { onVirtualizerChange(it, settings.virtualizerStrength) }
                        )
                    }

                    if (settings.virtualizerEnabled) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = settings.virtualizerStrength,
                            onValueChange = { onVirtualizerChange(true, it) },
                            valueRange = 0f..100f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Treble Clarity & Harmonic Exciter
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                containerColor = if (settings.clarityEnabled)
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
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    stringResource(R.string.effects_clarity_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    stringResource(R.string.effects_clarity_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = settings.clarityEnabled,
                            onCheckedChange = { onClarityChange(it, settings.clarityGain) }
                        )
                    }

                    if (settings.clarityEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = settings.clarityGain,
                            onValueChange = { onClarityChange(true, it) },
                            valueRange = 0f..10f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
