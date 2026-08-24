package com.timbre.dsp.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.util.Log
import com.timbre.dsp.model.DeviceProfile
import com.timbre.dsp.model.OutputDeviceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

class DeviceProfileManager private constructor(private val context: Context) {

    private val audioManager = context.getSystemService(AudioManager::class.java)

    private val deviceProfiles = ConcurrentHashMap<String, DeviceProfile>()
    private val _currentDevice = MutableStateFlow<DeviceProfile?>(null)
    val currentDevice: StateFlow<DeviceProfile?> = _currentDevice.asStateFlow()

    private var onDeviceProfileChangeListener: ((presetId: String) -> Unit)? = null

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            detectCurrentOutputDevice()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            detectCurrentOutputDevice()
        }
    }

    init {
        try {
            audioManager?.registerAudioDeviceCallback(audioDeviceCallback, null)
            detectCurrentOutputDevice()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed registering audio device callback", e)
        }
    }

    fun setOnProfileChangeListener(listener: (presetId: String) -> Unit) {
        this.onDeviceProfileChangeListener = listener
    }

    fun bindPresetToDevice(deviceId: String, deviceName: String, type: OutputDeviceType, presetId: String) {
        val profile = DeviceProfile(deviceId, deviceName, type, presetId, isEnabled = true)
        deviceProfiles[deviceId] = profile
        if (_currentDevice.value?.deviceId == deviceId) {
            _currentDevice.value = profile
        }
    }

    fun detectCurrentOutputDevice() {
        val devices = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS) ?: return
        var selectedInfo: AudioDeviceInfo? = null

        // Priority: Bluetooth > USB > Wired > Speaker
        val priorityOrder = listOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLUETOOTH_LE_HEADSET,
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        )

        for (targetType in priorityOrder) {
            val match = devices.firstOrNull { it.type == targetType }
            if (match != null) {
                selectedInfo = match
                break
            }
        }

        if (selectedInfo == null) {
            selectedInfo = devices.firstOrNull()
        }

        if (selectedInfo != null) {
            val (devType, name) = resolveDeviceInfo(selectedInfo)
            val devId = "${devType.name}_${selectedInfo.id}"
            val existing = deviceProfiles[devId] ?: DeviceProfile(
                deviceId = devId,
                deviceName = name,
                deviceType = devType,
                presetId = "flat"
            )
            _currentDevice.value = existing
            Log.i(TAG, "Active audio output device: $name (${devType.name})")

            if (existing.isEnabled && existing.presetId.isNotBlank()) {
                onDeviceProfileChangeListener?.invoke(existing.presetId)
            }
        }
    }

    private fun resolveDeviceInfo(info: AudioDeviceInfo): Pair<OutputDeviceType, String> {
        val name = if (info.productName.isNotBlank()) info.productName.toString() else "Audio Output"
        val type = when (info.type) {
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_LE_HEADSET -> OutputDeviceType.BLUETOOTH
            AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> OutputDeviceType.USB
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET -> OutputDeviceType.WIRED
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> OutputDeviceType.SPEAKER
            else -> OutputDeviceType.OTHER
        }
        return Pair(type, name)
    }

    companion object {
        private const val TAG = "DeviceProfileManager"

        @Volatile
        private var instance: DeviceProfileManager? = null

        fun getInstance(context: Context): DeviceProfileManager {
            return instance ?: synchronized(this) {
                instance ?: DeviceProfileManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
