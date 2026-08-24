package com.timbre.dsp.data.api

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

data class DownloadProgress(
    val bytesDownloaded: Long = 0L,
    val totalBytes: Long = 0L,
    val progress: Float = 0f,
    val isComplete: Boolean = false,
    val error: String? = null
)

object ApkDownloader {
    private const val TAG = "ApkDownloader"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun downloadApk(
        context: Context,
        url: String,
        version: String,
        onProgress: (DownloadProgress) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Timbre-DSP-App")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e(TAG, "Download failed with status: ${response.code}")
                onProgress(DownloadProgress(error = "HTTP Error: ${response.code}"))
                return@withContext null
            }

            val body = response.body
            val totalBytes = body.contentLength()
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val cleanVersion = version.removePrefix("v").removePrefix("V")
            val apkFile = File(updatesDir, "Timbre-DSP-v$cleanVersion.apk")

            if (apkFile.exists()) {
                apkFile.delete()
            }

            body.byteStream().use { inputStream ->
                FileOutputStream(apkFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Long = 0L
                    var read: Int

                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        bytesRead += read
                        val progress = if (totalBytes > 0) bytesRead.toFloat() / totalBytes else 0f
                        onProgress(
                            DownloadProgress(
                                bytesDownloaded = bytesRead,
                                totalBytes = totalBytes,
                                progress = progress
                            )
                        )
                    }
                    outputStream.flush()
                }
            }

            Log.i(TAG, "APK successfully downloaded to: ${apkFile.absolutePath}")
            onProgress(
                DownloadProgress(
                    bytesDownloaded = totalBytes,
                    totalBytes = totalBytes,
                    progress = 1.0f,
                    isComplete = true
                )
            )
            return@withContext apkFile
        } catch (e: Exception) {
            Log.e(TAG, "Exception during APK download", e)
            onProgress(DownloadProgress(error = e.localizedMessage ?: "Download failed"))
            return@withContext null
        }
    }

    fun installApk(context: Context, apkFile: File): Boolean {
        return try {
            if (!apkFile.exists()) {
                Log.e(TAG, "APK file does not exist: ${apkFile.absolutePath}")
                return false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    return false
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(installIntent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch package installer", e)
            false
        }
    }
}
