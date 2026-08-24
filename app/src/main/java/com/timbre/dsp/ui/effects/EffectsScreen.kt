package com.timbre.dsp.ui.effects

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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.HearingAudiogram
import com.timbre.dsp.ui.hearing.HearingTestWizard
import java.util.Locale

@Composable
fun EffectsScreen(
    settings: DSPSettings,
    onPreampGainChange: (Float) -> Unit,
    onChannelBalanceChange: (Float) -> Unit,
    onMonoChange: (Boolean) -> Unit,
    onLimiterChange: (Boolean) -> Unit,
    onBassBoostChange: (enabled: Boolean, gain: Float, cutoff: Float) -> Unit,
    onCrossfeedChange: (enabled: Boolean, strength: Float) -> Unit,
    onVirtualizerChange: (enabled: Boolean, strength: Float) -> Unit,
    onClarityChange: (enabled: Boolean, gain: Float) -> Unit,
    onApplyHearingAudiogram: (HearingAudiogram, EQPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var showHearingWizard by remember { mutableStateOf(false) }

    if (showHearingWizard) {
        HearingTestWizard(
            onDismiss = { showHearingWizard = false },
            onCompleteTest = { audiogram, preset ->
                onApplyHearingAudiogram(audiogram, preset)
                showHearingWizard = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Dynamics & Audio Effects",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Fine-tune acoustics, staging, and dynamics processing",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Hearing Calibration Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (settings.hearingAudiogram.isCalibrated)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Hearing, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Personalized Hearing Calibration", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (settings.hearingAudiogram.isCalibrated) "Audiogram calibrated and active" else "Run the audiogram test to compensate for hearing asymmetry",
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
                    Text(if (settings.hearingAudiogram.isCalibrated) "Re-Calibrate Hearing Test" else "Start Hearing Calibration Test")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Channel Balance & Mono Summing Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Balance, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Stereo Channel Balance", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Adjust Left / Right listening distribution",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Left ${if (settings.channelBalance < 0) String.format(Locale.US, "%.0f%%", -settings.channelBalance * 100) else ""}", style = MaterialTheme.typography.bodySmall)
                    Text("Center", style = MaterialTheme.typography.bodySmall)
                    Text("Right ${if (settings.channelBalance > 0) String.format(Locale.US, "%.0f%%", settings.channelBalance * 100) else ""}", style = MaterialTheme.typography.bodySmall)
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
                    Column {
                        Text("Mono Audio Sum", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Combine Left and Right into mono stream",
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

        // 3. Preamp Gain & Limiter Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Pre-Amp Gain", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Master input attenuation / boost",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = String.format(Locale.US, "%.1f dB", settings.preampGain),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = settings.preampGain,
                    onValueChange = onPreampGainChange,
                    valueRange = -15f..15f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Anti-Clipping Peak Limiter", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Prevents digital distortion & speaker damage",
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

        // 4. Bass Boost Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speaker, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Sub-Bass Enhancer", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Low-end resonance synthesizer",
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
                        Text("Boost Level", style = MaterialTheme.typography.bodySmall)
                        Text(String.format(Locale.US, "%.1f dB", settings.bassBoostGain), style = MaterialTheme.typography.bodySmall)
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
                        Text("Cutoff Frequency", style = MaterialTheme.typography.bodySmall)
                        Text("${settings.bassBoostCutoffFreq.toInt()} Hz", style = MaterialTheme.typography.bodySmall)
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

        // 5. Binaural Crossfeed & Spatializer
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Headphones, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Binaural Crossfeed (Chu Moy)", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Reduces headphone listening fatigue",
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
                        Text("Crossfeed Strength", style = MaterialTheme.typography.bodySmall)
                        Text("${(settings.crossfeedStrength * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = settings.crossfeedStrength,
                        onValueChange = { onCrossfeedChange(true, it) },
                        valueRange = 0f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("3D Spatial Widener", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Expands soundstage geometry",
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

        // 6. Treble Clarity & Harmonic Exciter
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("High-Frequency Clarity", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Restores brilliance and air in vocals",
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

        Spacer(modifier = Modifier.height(32.dp))
    }
}
