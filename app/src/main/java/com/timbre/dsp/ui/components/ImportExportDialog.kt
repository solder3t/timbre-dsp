package com.timbre.dsp.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.timbre.dsp.R
import com.timbre.dsp.data.EqualizerApoParser
import com.timbre.dsp.model.EQMode
import com.timbre.dsp.model.EQPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportDialog(
    currentPreset: EQPreset,
    onDismiss: () -> Unit,
    onImportPreset: (preset: EQPreset, saveToLibrary: Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var importText by remember { mutableStateOf("") }
    var presetName by remember { mutableStateOf("Imported Preset") }

    val exportedText = remember(currentPreset) {
        EqualizerApoParser.exportToPeace(currentPreset)
    }

    val parsePreview = remember(importText) {
        EqualizerApoParser.parsePreview(importText)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val text = inputStream.bufferedReader().use { it.readText() }
                    inputStream.close()
                    importText = text

                    // Extract file name without extension
                    var fileName = "Imported Preset"
                    val cursor = context.contentResolver.query(uri, null, null, null, null)
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            if (nameIdx >= 0) {
                                val fullName = it.getString(nameIdx)
                                if (!fullName.isNullOrBlank()) {
                                    fileName = fullName.substringBeforeLast(".")
                                }
                            }
                        }
                    }
                    presetName = fileName
                    Toast.makeText(context, context.getString(R.string.import_file_loaded), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.import_file_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 640.dp),
        title = { Text(stringResource(R.string.dialog_import_export_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.tab_import_text)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.tab_export_settings)) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // Import Tab
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it },
                        label = { Text(stringResource(R.string.dialog_preset_name_label)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        label = { Text(stringResource(R.string.import_input_label)) },
                        placeholder = { Text(stringResource(R.string.dialog_paste_config_placeholder)) },
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Parse Preview Card
                    if (importText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (parsePreview.isValid)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                     else
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (parsePreview.isValid) Icons.Default.CheckCircle else Icons.Default.Info,
                                    contentDescription = null,
                                    tint = if (parsePreview.isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 6.dp)
                                )
                                Text(
                                    text = if (parsePreview.isValid) {
                                        val modeStr = if (parsePreview.eqMode == EQMode.GRAPHIC_10) "10-Band Graphic" else "Parametric"
                                        stringResource(R.string.import_preview_detected, parsePreview.bandCount, parsePreview.preampGain, modeStr)
                                    } else {
                                        stringResource(R.string.import_preview_invalid)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (parsePreview.isValid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                filePickerLauncher.launch(arrayOf("text/*", "application/json", "*/*"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.padding(start = 4.dp))
                            Text(stringResource(R.string.btn_choose_file), style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    importText = clip.getItemAt(0).text.toString()
                                    Toast.makeText(context, context.getString(R.string.toast_pasted_clipboard), Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentPaste, contentDescription = null)
                            Spacer(modifier = Modifier.padding(start = 4.dp))
                            Text(stringResource(R.string.btn_paste), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else {
                    // Export Tab
                    OutlinedTextField(
                        value = exportedText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.export_output_label)) },
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("PEQ Configuration", exportedText)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, context.getString(R.string.msg_exported_copied), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Text(modifier = Modifier.padding(start = 8.dp), text = stringResource(R.string.btn_copy_clipboard))
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            if (importText.isNotBlank()) {
                                val imported = EqualizerApoParser.parse(importText, presetName)
                                onImportPreset(imported, true)
                                onDismiss()
                            }
                        },
                        enabled = importText.isNotBlank() && parsePreview.isValid
                    ) {
                        Text(stringResource(R.string.btn_import_and_save))
                    }
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_close))
                }
            }
        },
        dismissButton = {
            if (selectedTab == 0) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        }
    )
}
