package com.timbre.dsp.data.api

import android.content.Context
import android.util.Log
import com.google.gson.JsonParser
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
    const val CURRENT_VERSION = "1.0"

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
                    val code = response.code
                    Log.w(TAG, "GitHub API returned status $code")
                    return@withContext UpdateInfo(
                        isAvailable = false,
                        latestVersion = currentVersion,
                        changelog = "",
                        apkUrl = null,
                        releaseUrl = "https://github.com/solder3t/timbre-dsp/releases"
                    )
                }

                val body = response.body.string()
                val json = JsonParser.parseString(body).asJsonObject

                val tagName = json.get("tag_name")?.asString ?: "1.0"
                val cleanTag = tagName.removePrefix("v").removePrefix("V")
                val releaseName = json.get("name")?.asString ?: "Release $cleanTag"
                val changelog = json.get("body")?.asString ?: "No changelog provided."
                val htmlUrl = json.get("html_url")?.asString ?: "https://github.com/solder3t/timbre-dsp/releases"

                var apkUrl: String? = null
                val assets = json.getAsJsonArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.size()) {
                        val assetObj = assets.get(i).asJsonObject
                        val name = assetObj.get("name")?.asString ?: ""
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = assetObj.get("browser_download_url")?.asString
                            break
                        }
                    }
                }

                val isNewer = isVersionNewer(cleanTag, currentVersion)

                UpdateInfo(
                    isAvailable = isNewer,
                    latestVersion = cleanTag,
                    changelog = changelog,
                    apkUrl = apkUrl,
                    releaseUrl = htmlUrl
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates", e)
            UpdateInfo(
                isAvailable = false,
                latestVersion = currentVersion,
                changelog = "",
                apkUrl = null,
                releaseUrl = "https://github.com/solder3t/timbre-dsp/releases"
            )
        }
    }

    fun isVersionNewer(latest: String, current: String): Boolean {
        try {
            val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
        } catch (e: Exception) {
            return latest.trim() != current.trim()
        }
        return false
    }
}
