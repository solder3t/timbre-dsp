package com.timbre.dsp.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeRepository {

    private const val PREFS_NAME = "timbre_theme_prefs"
    private const val KEY_THEME_MODE = "key_theme_mode"
    private const val KEY_DYNAMIC_COLOR = "key_dynamic_color"
    private const val KEY_SEED_COLOR = "key_seed_color"

    private val _themeSettings = MutableStateFlow(ThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val modeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)
        val themeMode = try { ThemeMode.valueOf(modeStr ?: ThemeMode.SYSTEM.name) } catch (e: Exception) { ThemeMode.SYSTEM }
        val useDynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        val seedColor = prefs.getLong(KEY_SEED_COLOR, 0xFF7B61FF)

        _themeSettings.value = ThemeSettings(
            themeMode = themeMode,
            useDynamicColor = useDynamicColor,
            seedColor = seedColor
        )
    }

    fun setThemeMode(mode: ThemeMode, context: Context? = null) {
        _themeSettings.value = _themeSettings.value.copy(themeMode = mode)
        persist(context)
    }

    fun setUseDynamicColor(useDynamic: Boolean, context: Context? = null) {
        _themeSettings.value = _themeSettings.value.copy(useDynamicColor = useDynamic)
        persist(context)
    }

    fun setSeedColor(seed: Long, context: Context? = null) {
        _themeSettings.value = _themeSettings.value.copy(seedColor = seed)
        persist(context)
    }

    private fun persist(context: Context?) {
        if (context == null) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = _themeSettings.value
        prefs.edit()
            .putString(KEY_THEME_MODE, current.themeMode.name)
            .putBoolean(KEY_DYNAMIC_COLOR, current.useDynamicColor)
            .putLong(KEY_SEED_COLOR, current.seedColor)
            .apply()
    }
}
