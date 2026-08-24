package com.timbre.dsp.audio

import android.content.Context
import android.util.Log
import com.timbre.dsp.model.AppProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class AppProfileManager private constructor(private val context: Context) {

    private val appProfiles = ConcurrentHashMap<String, AppProfile>()
    private val _profilesList = MutableStateFlow<List<AppProfile>>(emptyList())
    val profilesList: StateFlow<List<AppProfile>> = _profilesList.asStateFlow()

    private var onAppProfileChangeListener: ((presetId: String) -> Unit)? = null

    init {
        // Built-in defaults for common media apps
        addAppProfile("com.spotify.music", "Spotify", "bass_boost")
        addAppProfile("com.google.android.youtube", "YouTube", "vocal_booster")
        addAppProfile("com.google.android.apps.youtube.music", "YouTube Music", "rock")
        addAppProfile("com.netflix.mediaclient", "Netflix", "electronic")
    }

    fun setOnProfileChangeListener(listener: (presetId: String) -> Unit) {
        this.onAppProfileChangeListener = listener
    }

    fun addAppProfile(packageName: String, appName: String, presetId: String) {
        val profile = AppProfile(packageName, appName, presetId, isEnabled = true)
        appProfiles[packageName] = profile
        _profilesList.value = appProfiles.values.toList()
    }

    fun removeAppProfile(packageName: String) {
        appProfiles.remove(packageName)
        _profilesList.value = appProfiles.values.toList()
    }

    fun checkAndApplyAppProfile(packageName: String) {
        val profile = appProfiles[packageName] ?: return
        if (profile.isEnabled && profile.presetId.isNotBlank()) {
            Log.i(TAG, "Switching profile for ${profile.appName} -> ${profile.presetId}")
            onAppProfileChangeListener?.invoke(profile.presetId)
        }
    }

    companion object {
        private const val TAG = "AppProfileManager"

        @Volatile
        private var instance: AppProfileManager? = null

        fun getInstance(context: Context): AppProfileManager {
            return instance ?: synchronized(this) {
                instance ?: AppProfileManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
