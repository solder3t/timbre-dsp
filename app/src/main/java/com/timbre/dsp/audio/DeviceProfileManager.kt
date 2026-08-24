package com.timbre.dsp.audio

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.util.Log
import com.timbre.dsp.model.DeviceProfile
import com.timbre.dsp.model.OutputDeviceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

class DeviceProfileManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("timbre_device_profiles_prefs", Context.MODE_PRIVATE)
    private val audioManager = context.getSystemService(AudioManager::class.java)

    private val deviceProfiles = ConcurrentHashMap<String, DeviceProfile>()
    private val _currentDevice = MutableStateFlow<DeviceProfile?>(null)
    val currentDevice: StateFlow<DeviceProfile?> = _currentDevice.asStateFlow()

    private var lastHandledDeviceId: String? = null
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
        loadProfiles()
        try {
            audioManager?.registerAudioDeviceCallback(audioDeviceCallback, null)
            detectCurrentOutputDevice()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed registering audio device callback", e)
        }
    }

    private fun loadProfiles() {
        val savedJson = prefs.getString(KEY_DEVICE_PROFILES_JSON, null) ?: return
        try {
            val array = JSONArray(savedJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("deviceId")
                val name = obj.getString("deviceName")
                val typeStr = obj.getString("deviceType")
                val presetId = obj.getString("presetId")
                val isEnabled = obj.optBoolean("isEnabled", true)
                val type = try { OutputDeviceType.valueOf(typeStr) } catch (e: Exception) { OutputDeviceType.OTHER }
                deviceProfiles[id] = DeviceProfile(id, name, type, presetId, isEnabled)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed loading device profiles", e)
        }
    }

    private fun saveProfiles() {
        try {
            val array = JSONArray()
            for (profile in deviceProfiles.values) {
                val obj = JSONObject().apply {
                    put("deviceId", profile.deviceId)
                    put("deviceName", profile.deviceName)
                    put("deviceType", profile.deviceType.name)
                    put("presetId", profile.presetId)
                    put("isEnabled", profile.isEnabled)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_DEVICE_PROFILES_JSON, array.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed saving device profiles", e)
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
        saveProfiles()
        Log.i(TAG, "Bound preset '$presetId' to device '$deviceName' ($deviceId)")
    }

    fun detectCurrentOutputDevice() {
        val devices = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS) ?: return
        var selectedInfo: AudioDeviceInfo? = null

        // Priority: Bluetooth > USB > Wired > Speaker
        val priorityOrder = listOf(
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
            AudioDeviceInfo.TYPE_USB_DEVICE,
            AudioDeviceInfo.TYPE_USB_HEADSET,
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        )

        for (targetType in priorityOrder) {
            val match = devices.firstOrNull { it.type == targetType || it.type == 26 || it.type == 27 }
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
            // Stable device key by type and name
            val devId = "${devType.name}_${name.trim().replace(" ", "_")}"
            val existing = deviceProfiles[devId] ?: DeviceProfile(
                deviceId = devId,
                deviceName = name,
                deviceType = devType,
                presetId = "" // Empty preset means no auto-override unless user configured one
            )
            _currentDevice.value = existing

            val deviceChanged = (lastHandledDeviceId != devId)
            lastHandledDeviceId = devId

            Log.i(TAG, "Active audio output device: $name (${devType.name}) [changed=$deviceChanged]")

            // Only trigger auto-preset if the device has actually changed AND user explicitly bound a preset to it
            if (deviceChanged && existing.isEnabled && existing.presetId.isNotBlank()) {
                Log.i(TAG, "Auto-switching to bound preset: ${existing.presetId} for device $name")
                onDeviceProfileChangeListener?.invoke(existing.presetId)
            }
        }
    }

    private fun resolveDeviceInfo(info: AudioDeviceInfo): Pair<OutputDeviceType, String> {
        val rawName = if (info.productName.isNotBlank()) info.productName.toString().trim() else ""
        val type = when (info.type) {
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP, AudioDeviceInfo.TYPE_BLUETOOTH_SCO, 26, 27 -> OutputDeviceType.BLUETOOTH
            AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET -> OutputDeviceType.USB
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES, AudioDeviceInfo.TYPE_WIRED_HEADSET -> OutputDeviceType.WIRED
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> OutputDeviceType.SPEAKER
            else -> OutputDeviceType.OTHER
        }

        val name = when (type) {
            OutputDeviceType.SPEAKER -> {
                if (rawName.isBlank() || rawName.equals(android.os.Build.MODEL, ignoreCase = true) || rawName.equals(android.os.Build.PRODUCT, ignoreCase = true) || rawName.startsWith("SM-", ignoreCase = true)) {
                    "Built-in Speaker (${android.os.Build.MODEL})"
                } else {
                    rawName
                }
            }
            OutputDeviceType.WIRED -> {
                if (rawName.isBlank() || rawName.equals(android.os.Build.MODEL, ignoreCase = true) || rawName.startsWith("SM-", ignoreCase = true)) {
                    "Wired Headphones"
                } else {
                    rawName
                }
            }
            OutputDeviceType.USB -> {
                if (rawName.isBlank() || rawName.equals(android.os.Build.MODEL, ignoreCase = true)) {
                    "USB Audio DAC"
                } else {
                    rawName
                }
            }
            OutputDeviceType.BLUETOOTH -> {
                if (rawName.isBlank()) "Bluetooth Audio" else rawName
            }
            OutputDeviceType.OTHER -> if (rawName.isBlank()) "External Output" else rawName
        }

        return Pair(type, name)
    }

    companion object {
        private const val TAG = "DeviceProfileManager"
        private const val KEY_DEVICE_PROFILES_JSON = "key_device_profiles_json"

        @Volatile
        private var instance: DeviceProfileManager? = null

        fun getInstance(context: Context): DeviceProfileManager {
            return instance ?: synchronized(this) {
                instance ?: DeviceProfileManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
