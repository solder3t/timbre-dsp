package com.timbre.dsp.audio

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.timbre.dsp.model.AppProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isMediaApp: Boolean = false
)

class AppProfileManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("timbre_app_profiles_prefs", Context.MODE_PRIVATE)
    private val appProfiles = ConcurrentHashMap<String, AppProfile>()
    private val _profilesList = MutableStateFlow<List<AppProfile>>(emptyList())
    val profilesList: StateFlow<List<AppProfile>> = _profilesList.asStateFlow()

    private var onAppProfileChangeListener: ((presetId: String) -> Unit)? = null

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        val savedJson = prefs.getString(KEY_PROFILES_JSON, null)
        if (savedJson != null) {
            try {
                val array = JSONArray(savedJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val pkg = obj.getString("packageName")
                    val name = obj.getString("appName")
                    val preset = obj.getString("presetId")
                    val enabled = obj.optBoolean("isEnabled", true)
                    appProfiles[pkg] = AppProfile(pkg, name, preset, enabled)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed loading saved app profiles", e)
            }
        }

        // Add defaults if empty
        if (appProfiles.isEmpty()) {
            addAppProfile("com.spotify.music", "Spotify", "bass_boost")
            addAppProfile("com.google.android.youtube", "YouTube", "vocal_booster")
            addAppProfile("com.google.android.apps.youtube.music", "YouTube Music", "rock")
            addAppProfile("com.netflix.mediaclient", "Netflix", "electronic")
        } else {
            _profilesList.value = appProfiles.values.toList()
        }
    }

    private fun saveProfiles() {
        try {
            val array = JSONArray()
            for (profile in appProfiles.values) {
                val obj = JSONObject().apply {
                    put("packageName", profile.packageName)
                    put("appName", profile.appName)
                    put("presetId", profile.presetId)
                    put("isEnabled", profile.isEnabled)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_PROFILES_JSON, array.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving app profiles", e)
        }
    }

    fun setOnProfileChangeListener(listener: (presetId: String) -> Unit) {
        this.onAppProfileChangeListener = listener
    }

    fun addAppProfile(packageName: String, appName: String, presetId: String) {
        val profile = AppProfile(packageName, appName, presetId, isEnabled = true)
        appProfiles[packageName] = profile
        _profilesList.value = appProfiles.values.toList()
        saveProfiles()
    }

    fun updateAppProfile(packageName: String, presetId: String, isEnabled: Boolean) {
        val existing = appProfiles[packageName] ?: return
        val updated = existing.copy(presetId = presetId, isEnabled = isEnabled)
        appProfiles[packageName] = updated
        _profilesList.value = appProfiles.values.toList()
        saveProfiles()
    }

    fun toggleAppProfile(packageName: String, isEnabled: Boolean) {
        val existing = appProfiles[packageName] ?: return
        val updated = existing.copy(isEnabled = isEnabled)
        appProfiles[packageName] = updated
        _profilesList.value = appProfiles.values.toList()
        saveProfiles()
    }

    fun removeAppProfile(packageName: String) {
        appProfiles.remove(packageName)
        _profilesList.value = appProfiles.values.toList()
        saveProfiles()
    }

    fun checkAndApplyAppProfile(packageName: String) {
        val profile = appProfiles[packageName] ?: return
        if (profile.isEnabled && profile.presetId.isNotBlank()) {
            Log.i(TAG, "Switching profile for ${profile.appName} -> ${profile.presetId}")
            onAppProfileChangeListener?.invoke(profile.presetId)
        }
    }

    fun getInstalledMediaApps(): List<InstalledAppItem> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolvedApps = pm.queryIntentActivities(mainIntent, 0)
        val list = mutableListOf<InstalledAppItem>()

        val knownMediaKeywords = listOf("music", "audio", "sound", "player", "podcast", "radio", "video", "media", "spotify", "youtube", "netflix", "tidal", "deezer", "amazon", "vlc", "poweramp")

        for (resolveInfo in resolvedApps) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName) continue
            val label = resolveInfo.loadLabel(pm).toString()
            val isMedia = knownMediaKeywords.any { pkg.contains(it, ignoreCase = true) || label.contains(it, ignoreCase = true) }
            list.add(InstalledAppItem(packageName = pkg, appName = label, isMediaApp = isMedia))
        }

        return list.sortedWith(compareByDescending<InstalledAppItem> { it.isMediaApp }.thenBy { it.appName.lowercase() })
    }

    companion object {
        private const val TAG = "AppProfileManager"
        private const val KEY_PROFILES_JSON = "key_app_profiles_json"

        @Volatile
        private var instance: AppProfileManager? = null

        fun getInstance(context: Context): AppProfileManager {
            return instance ?: synchronized(this) {
                instance ?: AppProfileManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
