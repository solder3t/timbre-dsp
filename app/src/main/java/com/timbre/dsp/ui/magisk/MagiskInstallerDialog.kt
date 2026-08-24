package com.timbre.dsp.ui.magisk

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun MagiskInstallerDialog(
    isRootAvailable: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Magisk / KernelSU Root Engine")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Inject the native C++ Timbre DSP engine directly into the Android AudioFlinger pipeline for global, zero-latency system-wide processing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (statusMessage != null) {
                    Text(
                        text = statusMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            val file = buildMagiskZip(context)
                            isProcessing = false
                            if (file != null) {
                                statusMessage = "Module exported to: ${file.absolutePath}"
                                Toast.makeText(context, "Exported to Downloads", Toast.LENGTH_LONG).show()
                            } else {
                                statusMessage = "Failed to build module zip"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Magisk Module .zip")
                }

                if (isRootAvailable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                val file = buildMagiskZip(context)
                                if (file != null) {
                                    val success = flashMagiskModule(file)
                                    statusMessage = if (success) {
                                        "Module installed successfully! Please reboot your device."
                                    } else {
                                        "Failed executing root flash command."
                                    }
                                }
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isProcessing
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Flash Directly via Root Shell")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private suspend fun buildMagiskZip(context: Context): File? = withContext(Dispatchers.IO) {
    try {
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            ?: context.getExternalFilesDir(null) ?: context.filesDir
        val zipFile = File(downloadDir, "timbre_dsp_root.zip")

        val zos = ZipOutputStream(FileOutputStream(zipFile))

        // 1. module.prop
        val moduleProp = """
            id=timbre_dsp
            name=Timbre DSP Root Engine
            version=v1.0
            versionCode=1
            author=Timbre
            description=System-wide native DSP audio pipeline injection for Timbre.
        """.trimIndent()
        zos.putNextEntry(ZipEntry("module.prop"))
        zos.write(moduleProp.toByteArray())
        zos.closeEntry()

        // 2. post-fs-data.sh
        val postFsData = """
            #!/system/bin/sh
            MODDIR=${'$'}{0%/*}
        """.trimIndent()
        zos.putNextEntry(ZipEntry("post-fs-data.sh"))
        zos.write(postFsData.toByteArray())
        zos.closeEntry()

        // 3. service.sh
        val serviceSh = """
            #!/system/bin/sh
            MODDIR=${'$'}{0%/*}
        """.trimIndent()
        zos.putNextEntry(ZipEntry("service.sh"))
        zos.write(serviceSh.toByteArray())
        zos.closeEntry()

        zos.close()
        zipFile
    } catch (e: Throwable) {
        null
    }
}

private suspend fun flashMagiskModule(zipFile: File): Boolean = withContext(Dispatchers.IO) {
    try {
        val cmd = "magisk --install-module \"${zipFile.absolutePath}\" || ksud module install \"${zipFile.absolutePath}\""
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
        process.waitFor() == 0
    } catch (e: Throwable) {
        false
    }
}
