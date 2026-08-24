package com.timbre.dsp.data.api

import android.content.Context
import android.util.Log
import com.google.gson.JsonParser
import com.timbre.dsp.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class UpdateInfo(
    val isAvailable: Boolean,
    val latestVersion: String,
    val changelog: String,
    val apkUrl: String?,
    val releaseUrl: String
)

object UpdateChecker {

    private const val TAG = "UpdateChecker"
    private const val GITHUB_RELEASES_URL = "https://api.github.com/repos/solder3t/timbre-dsp/releases/latest"
    
    val CURRENT_VERSION: String
        get() = BuildConfig.VERSION_NAME

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdates(currentVersion: String = CURRENT_VERSION): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(GITHUB_RELEASES_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "Timbre-DSP-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Update check HTTP error: ${response.code}")
                    return@withContext UpdateInfo(
                        isAvailable = false,
                        latestVersion = currentVersion,
                        changelog = "",
                        apkUrl = null,
                        releaseUrl = "https://github.com/solder3t/timbre-dsp/releases"
                    )
                }

                val body = response.body?.string() ?: return@withContext UpdateInfo(
                    isAvailable = false,
                    latestVersion = currentVersion,
                    changelog = "",
                    apkUrl = null,
                    releaseUrl = "https://github.com/solder3t/timbre-dsp/releases"
                )

                val json = JsonParser.parseString(body).asJsonObject
                val rawTag = json.get("tag_name")?.asString ?: ""
                val releaseName = json.get("name")?.asString ?: rawTag
                val changelog = json.get("body")?.asString ?: "Bug fixes and DSP engine improvements."
                val htmlUrl = json.get("html_url")?.asString ?: "https://github.com/solder3t/timbre-dsp/releases"

                // Extract clean semver from tag like "v1.2.0" or "v1.2.0-3"
                val latestCleanVersion = rawTag
                    .removePrefix("v")
                    .substringBefore("-")
                    .trim()

                // Find APK asset download URL if available
                var apkDownloadUrl: String? = null
                val assetsArray = json.getAsJsonArray("assets")
                if (assetsArray != null) {
                    for (assetElem in assetsArray) {
                        val assetObj = assetElem.asJsonObject
                        val name = assetObj.get("name")?.asString ?: ""
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = assetObj.get("browser_download_url")?.asString
                            break
                        }
                    }
                }

                val isNewer = isVersionNewer(latestCleanVersion, currentVersion)

                Log.d(TAG, "Current: $currentVersion, Latest: $latestCleanVersion, isNewer: $isNewer, apkUrl: $apkDownloadUrl")

                return@withContext UpdateInfo(
                    isAvailable = isNewer,
                    latestVersion = latestCleanVersion.ifBlank { releaseName },
                    changelog = changelog,
                    apkUrl = apkDownloadUrl,
                    releaseUrl = htmlUrl
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            return@withContext UpdateInfo(
                isAvailable = false,
                latestVersion = currentVersion,
                changelog = "",
                apkUrl = null,
                releaseUrl = "https://github.com/solder3t/timbre-dsp/releases"
            )
        }
    }

    fun isVersionNewer(latest: String, current: String): Boolean {
        if (latest.isBlank() || current.isBlank()) return false
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
