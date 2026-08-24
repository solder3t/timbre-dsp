package com.timbre.dsp.ui.main

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.timbre.dsp.DSPEngine
import com.timbre.dsp.DSPForegroundService
import com.timbre.dsp.RoutingManager
import com.timbre.dsp.RoutingStatus
import com.timbre.dsp.audio.AppProfileManager
import com.timbre.dsp.audio.AudioEffectManager
import com.timbre.dsp.audio.AudioSessionTracker
import com.timbre.dsp.audio.DeviceProfileManager
import com.timbre.dsp.audio.InstalledAppItem
import com.timbre.dsp.audio.LiveAudioVisualizer
import com.timbre.dsp.audio.SleepTimerManager
import com.timbre.dsp.data.AutoEqRepository
import com.timbre.dsp.data.BackupManager
import com.timbre.dsp.data.ConvolutionRepository
import com.timbre.dsp.data.ImpulseResponseProfile
import com.timbre.dsp.data.PresetRepository
import com.timbre.dsp.data.SettingsRepository
import com.timbre.dsp.model.AppProfile
import com.timbre.dsp.model.AudioSessionInfo
import com.timbre.dsp.model.AutoEqProfile
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.DeviceProfile
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.EQMode
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.FilterType
import com.timbre.dsp.model.HearingAudiogram
import com.timbre.dsp.model.OutputDeviceType
import com.timbre.dsp.model.PermissionStatus
import com.timbre.dsp.model.RoutingMode
import com.timbre.dsp.model.TargetCurve
import com.timbre.dsp.permission.PermissionManager
import com.timbre.dsp.service.DSPTileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val permissionManager = PermissionManager(application)
    private val routingManager = RoutingManager(application).apply { initialize() }
    private val sessionTracker = AudioSessionTracker.getInstance(application)
    private val effectManager = AudioEffectManager.getInstance(application)
    private val visualizer = LiveAudioVisualizer.getInstance(application)
    private val deviceManager = DeviceProfileManager.getInstance(application)
    private val appProfileManager = AppProfileManager.getInstance(application)

    val permissionStatus: StateFlow<PermissionStatus> = permissionManager.status
    val routingStatus: StateFlow<RoutingStatus> = routingManager.status
    val activeSessions: StateFlow<List<AudioSessionInfo>> = sessionTracker.activeSessions
    val presets: StateFlow<List<EQPreset>> = PresetRepository.presets
    val fftMagnitudes: StateFlow<FloatArray> = visualizer.fftMagnitudes
    val peakLevels: StateFlow<Pair<Float, Float>> = visualizer.peakLevels
    val currentDevice: StateFlow<DeviceProfile?> = deviceManager.currentDevice
    val appProfiles: StateFlow<List<AppProfile>> = appProfileManager.profilesList
    val irProfiles: StateFlow<List<ImpulseResponseProfile>> = ConvolutionRepository.profiles
    val sleepTimerSeconds: StateFlow<Int> = SleepTimerManager.remainingSeconds
    val isSleepTimerRunning: StateFlow<Boolean> = SleepTimerManager.isTimerRunning

    private val _settings = MutableStateFlow(SettingsRepository.loadSettings(application))
    val settings: StateFlow<DSPSettings> = _settings.asStateFlow()

    init {
        DSPEngine.start()
        ConvolutionRepository.initialize(application)

        val initialSettings = _settings.value
        applyInitialIR(initialSettings)

        DSPForegroundService.currentSettings = initialSettings
        DSPTileService.currentSettings = initialSettings
        effectManager.updateSettings(initialSettings)
        DSPEngine.applySettings(initialSettings)

        refreshPermissions()
        sessionTracker.scanAudioFlinger()

        // Device auto-switch listener (only triggered on device changes with bound presets)
        deviceManager.setOnProfileChangeListener { presetId ->
            val preset = PresetRepository.getPresetById(presetId)
            if (preset != null) {
                selectPreset(preset)
            }
        }

        // App profile auto-switch listener
        appProfileManager.setOnProfileChangeListener { presetId ->
            val preset = PresetRepository.getPresetById(presetId)
            if (preset != null) {
                selectPreset(preset)
            }
        }
    }

    private fun applyInitialIR(settings: DSPSettings) {
        val profile = ConvolutionRepository.getProfileById(settings.activeConvolutionId)
            ?: ConvolutionRepository.profiles.value.firstOrNull()
        if (profile != null) {
            DSPEngine.setImpulseResponse(profile.leftChannel, profile.rightChannel, profile.leftChannel.size)
        }
    }

    private fun computeEffectivePreamp(settings: DSPSettings): Float {
        if (!settings.autoPreampEnabled) return settings.preampGain
        val maxBandGain = settings.bands.filter { it.enabled }.maxOfOrNull { it.gain } ?: 0f
        val bassExtra = if (settings.bassBoostEnabled) settings.bassBoostGain else 0f
        val clarityExtra = if (settings.clarityEnabled) settings.clarityGain else 0f
        val totalBoost = maxOf(0f, maxBandGain + bassExtra * 0.5f + clarityExtra * 0.5f)
        return -totalBoost
    }

    private fun pushSettings(newSettings: DSPSettings) {
        val effectivePreamp = computeEffectivePreamp(newSettings)
        val finalSettings = if (newSettings.autoPreampEnabled) newSettings.copy(preampGain = effectivePreamp) else newSettings

        _settings.value = finalSettings
        SettingsRepository.saveSettings(getApplication(), finalSettings)
        DSPTileService.isDspEnabled = finalSettings.isEnabled
        DSPTileService.currentSettings = finalSettings
        DSPForegroundService.currentSettings = finalSettings
        effectManager.updateSettings(finalSettings)
        DSPEngine.applySettings(finalSettings)
    }

    fun toggleMaster(enabled: Boolean) {
        pushSettings(_settings.value.copy(isEnabled = enabled))
        if (enabled && permissionStatus.value.hasRecordAudioPermission) {
            visualizer.start()
        } else {
            visualizer.stop()
        }
    }

    fun setEQMode(mode: EQMode) {
        pushSettings(_settings.value.copy(eqMode = mode))
    }

    fun setBandGain(index: Int, gain: Float) {
        val currentBands = _settings.value.bands.toMutableList()
        if (index in currentBands.indices) {
            currentBands[index] = currentBands[index].copy(gain = gain)
            pushSettings(_settings.value.copy(bands = currentBands, currentPresetId = "custom"))
        }
    }

    fun updateParametricBand(band: EQBand) {
        val currentBands = _settings.value.bands.toMutableList()
        if (band.index in currentBands.indices) {
            currentBands[band.index] = band
        } else {
            currentBands.add(band.copy(index = currentBands.size))
        }
        pushSettings(_settings.value.copy(bands = currentBands, eqMode = EQMode.PARAMETRIC, currentPresetId = "custom"))
    }

    fun addParametricBand(freq: Float = 1000f, gain: Float = 0f, q: Float = 1.414f, type: FilterType = FilterType.PEAK) {
        val currentBands = _settings.value.bands.toMutableList()
        val newBand = EQBand(
            index = currentBands.size,
            frequency = freq,
            gain = gain,
            q = q,
            type = type
        )
        currentBands.add(newBand)
        pushSettings(_settings.value.copy(bands = currentBands, eqMode = EQMode.PARAMETRIC, currentPresetId = "custom"))
    }

    fun deleteParametricBand(index: Int) {
        val currentBands = _settings.value.bands.toMutableList()
        if (index in currentBands.indices && currentBands.size > 1) {
            currentBands.removeAt(index)
            val reindexed = currentBands.mapIndexed { idx, b -> b.copy(index = idx) }
            pushSettings(_settings.value.copy(bands = reindexed, currentPresetId = "custom"))
        }
    }

    fun resetBands() {
        val flatBands = DSPSettings.default10Bands()
        pushSettings(_settings.value.copy(bands = flatBands, preampGain = 0f, eqMode = EQMode.GRAPHIC_10, currentPresetId = "flat"))
    }

    fun setPreampGain(gain: Float) {
        pushSettings(_settings.value.copy(preampGain = gain, autoPreampEnabled = false))
    }

    fun setAutoPreamp(enabled: Boolean) {
        pushSettings(_settings.value.copy(autoPreampEnabled = enabled))
    }

    fun setTargetCurve(curve: TargetCurve) {
        pushSettings(_settings.value.copy(targetCurve = curve))
    }

    fun applyAiSettings(newSettings: DSPSettings) {
        pushSettings(newSettings)
    }

    fun setChannelBalance(balance: Float) {
        pushSettings(_settings.value.copy(channelBalance = balance))
    }

    fun setMono(isMono: Boolean) {
        pushSettings(_settings.value.copy(isMono = isMono))
    }

    fun setLimiter(enabled: Boolean) {
        pushSettings(_settings.value.copy(limiterEnabled = enabled))
    }

    fun setBassBoost(enabled: Boolean, gain: Float, cutoff: Float) {
        pushSettings(_settings.value.copy(bassBoostEnabled = enabled, bassBoostGain = gain, bassBoostCutoffFreq = cutoff))
    }

    fun setCrossfeed(enabled: Boolean, strength: Float) {
        pushSettings(_settings.value.copy(crossfeedEnabled = enabled, crossfeedStrength = strength))
    }

    fun setVirtualizer(enabled: Boolean, strength: Float) {
        pushSettings(_settings.value.copy(virtualizerEnabled = enabled, virtualizerStrength = strength))
    }

    fun setClarity(enabled: Boolean, gain: Float) {
        pushSettings(_settings.value.copy(clarityEnabled = enabled, clarityGain = gain))
    }

    fun setConvolution(enabled: Boolean, profileId: String, wetDry: Float) {
        val profile = ConvolutionRepository.getProfileById(profileId)
        if (profile != null) {
            DSPEngine.setImpulseResponse(profile.leftChannel, profile.rightChannel, profile.leftChannel.size)
        }
        pushSettings(_settings.value.copy(
            convolutionEnabled = enabled,
            activeConvolutionId = profileId,
            convolutionWetDry = wetDry
        ))
    }

    fun importCustomIR(uri: Uri, fileName: String): Boolean {
        val profile = ConvolutionRepository.importCustomIR(getApplication(), uri, fileName)
        if (profile != null) {
            setConvolution(enabled = true, profileId = profile.id, wetDry = _settings.value.convolutionWetDry)
            return true
        }
        return false
    }

    fun startSleepTimer(minutes: Int) {
        SleepTimerManager.startTimer(minutes) {
            toggleMaster(false)
        }
    }

    fun cancelSleepTimer() {
        SleepTimerManager.cancelTimer()
    }

    fun exportFullBackup(uri: Uri): Boolean {
        return BackupManager.exportBackup(getApplication(), uri)
    }

    fun importFullBackup(uri: Uri): Boolean {
        val success = BackupManager.importBackup(getApplication(), uri)
        if (success) {
            val restored = SettingsRepository.loadSettings(getApplication())
            pushSettings(restored)
            PresetRepository.refreshPresets(getApplication())
        }
        return success
    }

    fun selectPreset(preset: EQPreset) {
        val updatedBands = preset.bands.map { it.copy() }
        pushSettings(
            _settings.value.copy(
                currentPresetId = preset.id,
                eqMode = preset.eqMode,
                bands = updatedBands,
                preampGain = preset.preampGain,
                bassBoostGain = preset.bassBoostGain,
                bassBoostEnabled = preset.bassBoostGain > 0,
                clarityGain = preset.clarityGain,
                clarityEnabled = preset.clarityGain > 0,
                virtualizerStrength = preset.virtualizerStrength,
                virtualizerEnabled = preset.virtualizerStrength > 0,
                crossfeedStrength = preset.crossfeedStrength,
                crossfeedEnabled = preset.crossfeedStrength > 0
            )
        )
    }

    fun applyAutoEq(profile: AutoEqProfile) {
        val updatedBands = profile.bands.map { it.copy() }
        pushSettings(
            _settings.value.copy(
                currentPresetId = "autoeq_${profile.model}",
                bands = updatedBands,
                preampGain = profile.preampGain
            )
        )
    }

    fun applyImportedPreset(preset: EQPreset) {
        selectPreset(preset)
    }

    fun saveCustomPreset(name: String) {
        val newPreset = PresetRepository.saveCustomPreset(name, _settings.value)
        pushSettings(_settings.value.copy(currentPresetId = newPreset.id))
    }

    fun deleteCustomPreset(id: String) {
        PresetRepository.deleteCustomPreset(id)
        if (_settings.value.currentPresetId == id) {
            resetBands()
        }
    }

    fun bindPresetToCurrentDevice(presetId: String) {
        val dev = currentDevice.value ?: return
        deviceManager.bindPresetToDevice(dev.deviceId, dev.deviceName, dev.deviceType, presetId)
    }

    fun getInstalledMediaApps(): List<InstalledAppItem> {
        return appProfileManager.getInstalledMediaApps()
    }

    fun bindAppToPreset(packageName: String, appName: String, presetId: String) {
        appProfileManager.addAppProfile(packageName, appName, presetId)
        val preset = PresetRepository.getPresetById(presetId)
        if (preset != null) {
            selectPreset(preset)
        }
    }

    fun updateAppProfile(packageName: String, presetId: String, isEnabled: Boolean) {
        appProfileManager.updateAppProfile(packageName, presetId, isEnabled)
        if (isEnabled) {
            val preset = PresetRepository.getPresetById(presetId)
            if (preset != null) {
                selectPreset(preset)
            }
        }
    }

    fun toggleAppProfile(packageName: String, isEnabled: Boolean) {
        appProfileManager.toggleAppProfile(packageName, isEnabled)
        if (isEnabled) {
            val profile = appProfileManager.profilesList.value.find { it.packageName == packageName }
            if (profile != null && profile.presetId.isNotBlank()) {
                val preset = PresetRepository.getPresetById(profile.presetId)
                if (preset != null) {
                    selectPreset(preset)
                }
            }
        }
    }

    fun removeAppProfile(packageName: String) {
        appProfileManager.removeAppProfile(packageName)
    }

    fun applyHearingAudiogram(audiogram: HearingAudiogram, preset: EQPreset) {
        pushSettings(_settings.value.copy(hearingAudiogram = audiogram))
        selectPreset(preset)
    }

    fun startVisualizerIfPermitted() {
        if (permissionStatus.value.hasRecordAudioPermission && _settings.value.isEnabled) {
            visualizer.start()
        }
    }

    fun refreshPermissions() {
        permissionManager.refreshStatus()
        routingManager.initialize()
        sessionTracker.scanAudioFlinger()
        startVisualizerIfPermitted()
    }

    fun requestShizukuPermission() {
        viewModelScope.launch {
            permissionManager.requestShizukuPermission()
        }
    }

    fun grantDumpViaShizuku() {
        viewModelScope.launch {
            permissionManager.grantDumpPermissionViaShizuku()
            refreshPermissions()
        }
    }

    fun grantDumpViaRoot() {
        viewModelScope.launch {
            permissionManager.grantDumpPermissionViaRoot()
            refreshPermissions()
        }
    }

    fun openNotificationSettings() {
        permissionManager.openNotificationAccessSettings(getApplication())
    }

    fun requestBatteryOptimization() {
        permissionManager.requestIgnoreBatteryOptimization(getApplication())
    }

    fun rescanSessions() {
        sessionTracker.scanAudioFlinger()
    }
}

