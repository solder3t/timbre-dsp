package com.timbre.dsp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.FilterType
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParametricBandEditorDialog(
    band: EQBand,
    canDelete: Boolean = true,
    onDismiss: () -> Unit,
    onSaveBand: (EQBand) -> Unit,
    onDeleteBand: () -> Unit
) {
    var filterType by remember { mutableStateOf(band.type) }
    var frequency by remember { mutableFloatStateOf(band.frequency) }
    var gain by remember { mutableFloatStateOf(band.gain) }
    var q by remember { mutableFloatStateOf(band.q) }
    var isEnabled by remember { mutableStateOf(band.enabled) }

    var typeDropdownExpanded by remember { mutableStateOf(false) }

    val filterTypes = FilterType.values()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Band #${band.index + 1}")
                if (canDelete) {
                    IconButton(onClick = {
                        onDeleteBand()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Band", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 1. Filter Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = filterType.name.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filter Geometry") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        filterTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.replace("_", " ")) },
                                onClick = {
                                    filterType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Frequency Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Center Frequency", style = MaterialTheme.typography.bodySmall)
                    Text("${frequency.toInt()} Hz", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = frequency,
                    onValueChange = { frequency = it },
                    valueRange = 20f..20000f,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Gain Slider (if not lowpass/highpass/notch)
                val supportsGain = filterType == FilterType.PEAK || filterType == FilterType.LOW_SHELF || filterType == FilterType.HIGH_SHELF
                if (supportsGain) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Band Gain", style = MaterialTheme.typography.bodySmall)
                        Text(String.format(Locale.US, "%.1f dB", gain), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = gain,
                        onValueChange = { gain = (it * 2).toInt() / 2f },
                        valueRange = -24f..24f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 4. Quality Factor Q Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bandwidth (Q Factor)", style = MaterialTheme.typography.bodySmall)
                    Text(String.format(Locale.US, "Q = %.2f", q), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = q,
                    onValueChange = { q = it },
                    valueRange = 0.1f..10.0f,
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Enable Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Band Active", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedBand = band.copy(
                    type = filterType,
                    frequency = frequency,
                    gain = if (supportsGain) gain else 0f,
                    q = q,
                    enabled = isEnabled
                )
                onSaveBand(updatedBand)
                onDismiss()
            }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
