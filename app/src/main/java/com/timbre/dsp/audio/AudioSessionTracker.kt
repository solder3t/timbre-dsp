package com.timbre.dsp.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.timbre.dsp.model.AudioSessionInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

class AudioSessionTracker private constructor(private val context: Context) {

    private val sessionsMap = ConcurrentHashMap<Int, AudioSessionInfo>()
    private val _activeSessions = MutableStateFlow<List<AudioSessionInfo>>(emptyList())
    val activeSessions: StateFlow<List<AudioSessionInfo>> = _activeSessions.asStateFlow()

    private val effectManager = AudioEffectManager.getInstance(context)
    private val appProfileManager = AppProfileManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var playbackCallback: AudioManager.AudioPlaybackCallback? = null

    private val ignoredPackages = setOf(
        "com.samsung.android.app.clockpack",
        "com.samsung.android.honeyboard",
        "com.sec.android.app.soundalive",
        "com.android.systemui",
        "android",
        "com.google.android.inputmethod.latin",
        "com.mediatek",
        "com.mediatek.engineermode",
        "com.android.server.telecom",
        "com.google.android.dialer",
        "com.samsung.android.dialer",
        context.packageName
    )

    init {
        registerPlaybackCallback()
    }

    private fun registerPlaybackCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioManager != null) {
            playbackCallback = object : AudioManager.AudioPlaybackCallback() {
                override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>?) {
                    super.onPlaybackConfigChanged(configs)
                    if (configs == null) return

                    val currentSids = mutableSetOf<Int>()

                    for (config in configs) {
                        // Check if actively playing (API 29+) or valid audio configuration
                        val isPlaying = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            try {
                                val stateMethod = AudioPlaybackConfiguration::class.java.getDeclaredMethod("getPlayerState")
                                stateMethod.isAccessible = true
                                val state = stateMethod.invoke(config) as? Int
                                state == null || state == 2 // 2 = AudioPlaybackConfiguration.PLAYER_STATE_STARTED
                            } catch (e: Throwable) {
                                true
                            }
                        } else {
                            true
                        }

                        val sid = extractSessionId(config)
                        val pkgName = resolvePackageNameForConfig(config)

                        if (sid > 0 && isPlaying && !isPackageIgnored(pkgName)) {
                            currentSids.add(sid)
                            if (!sessionsMap.containsKey(sid)) {
                                onSessionOpened(sid, pkgName)
                            }
                        }
                    }

                    // Remove sessions that ended or stopped
                    val toRemove = sessionsMap.keys.filter { it !in currentSids && it > 0 }
                    for (sid in toRemove) {
                        onSessionClosed(sid)
                    }
                }
            }

            try {
                audioManager.registerAudioPlaybackCallback(playbackCallback!!, Handler(Looper.getMainLooper()))
                Log.i(TAG, "Registered AudioPlaybackCallback")
            } catch (e: Throwable) {
                Log.w(TAG, "Could not register AudioPlaybackCallback", e)
            }
        }
    }

    private fun isPackageIgnored(packageName: String): Boolean {
        if (packageName.isBlank() || packageName in ignoredPackages) return true
        val lower = packageName.lowercase()
        return lower.startsWith("com.samsung.android.app.clockpack") ||
               lower.startsWith("com.android.systemui") ||
               lower == "android" ||
               lower.contains("soundalive") ||
               lower == context.packageName.lowercase()
    }

    private fun extractSessionId(config: AudioPlaybackConfiguration): Int {
        return try {
            val method = AudioPlaybackConfiguration::class.java.getDeclaredMethod("getAudioSessionId")
            method.isAccessible = true
            (method.invoke(config) as? Int) ?: 0
        } catch (e: Throwable) {
            0
        }
    }

    private fun resolvePackageNameForConfig(config: AudioPlaybackConfiguration): String {
        return try {
            val uidMethod = AudioPlaybackConfiguration::class.java.getDeclaredMethod("getClientUid")
            uidMethod.isAccessible = true
            val uid = (uidMethod.invoke(config) as? Int) ?: 0
            if (uid > 0) {
                context.packageManager.getPackagesForUid(uid)?.firstOrNull() ?: "Active Player"
            } else {
                "Active Player"
            }
        } catch (e: Throwable) {
            "Active Player"
        }
    }

    fun onSessionOpened(sessionId: Int, packageName: String) {
        if (sessionId <= 0 || isPackageIgnored(packageName)) return
        val appName = getAppName(packageName)
        val session = AudioSessionInfo(
            sessionId = sessionId,
            packageName = packageName,
            appName = appName,
            isPlaying = true
        )
        sessionsMap[sessionId] = session
        updateState()
        effectManager.attachEffectToSession(sessionId)
        appProfileManager.checkAndApplyAppProfile(packageName)
        Log.i(TAG, "Audio session registered: $sessionId ($appName)")
    }

    fun onSessionClosed(sessionId: Int) {
        if (sessionsMap.remove(sessionId) != null) {
            updateState()
            effectManager.releaseEffectForSession(sessionId)
            Log.i(TAG, "Audio session closed: $sessionId")
        }
    }

    fun scanAudioFlinger() {
        scope.launch {
            try {
                val output = executeAudioFlingerDump()
                if (output.isNotBlank()) {
                    parseAndRegisterSessions(output)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed scanning audio flinger", e)
            }
        }
    }

    private suspend fun executeAudioFlingerDump(): String = withContext(Dispatchers.IO) {
        if (tryShizukuDump()) {
            return@withContext runShizukuDump()
        }

        try {
            val process = Runtime.getRuntime().exec(arrayOf("dumpsys", "media.audio_flinger"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val text = reader.readText()
            process.waitFor()
            if (text.isNotBlank()) return@withContext text
        } catch (e: Exception) {}

        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "dumpsys media.audio_flinger"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val text = reader.readText()
            process.waitFor()
            if (text.isNotBlank()) return@withContext text
        } catch (e: Exception) {}

        ""
    }

    private fun tryShizukuDump(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            false
        }
    }

    private fun runShizukuDump(): String {
        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, arrayOf("dumpsys", "media.audio_flinger"), null, null) as? java.lang.Process
            if (process != null) {
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val text = reader.readText()
                process.waitFor()
                text
            } else {
                ""
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Shizuku dumpsys failed", e)
            ""
        }
    }

    private fun parseAndRegisterSessions(dumpText: String) {
        val discoveredSessions = mutableMapOf<Int, String>()

        // 1. Table format: pid/uid session port -> e.g. "2498/ 10053 4849 3496"
        val tablePattern = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)\\s+(\\d+)\\s+(\\d+)")
        val tableMatcher = tablePattern.matcher(dumpText)
        while (tableMatcher.find()) {
            val uid = tableMatcher.group(2)?.toIntOrNull() ?: 0
            val sid = tableMatcher.group(3)?.toIntOrNull() ?: 0
            if (sid > 0 && uid >= 10000) { // Only user application UIDs (ignore system daemon UIDs < 10000)
                val pkgs = context.packageManager.getPackagesForUid(uid)
                val pkg = pkgs?.firstOrNull() ?: ""
                if (pkg.isNotBlank() && !isPackageIgnored(pkg)) {
                    discoveredSessions[sid] = pkg
                }
            }
        }

        // Register newly discovered sessions
        for ((sid, pkg) in discoveredSessions) {
            if (!sessionsMap.containsKey(sid)) {
                onSessionOpened(sid, pkg)
            }
        }
    }

    private fun getAppName(packageName: String): String {
        if (packageName == "Unknown" || packageName == "Active Playback Stream" || packageName == "Active Player" || packageName == "Player") return packageName
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    private fun updateState() {
        _activeSessions.value = sessionsMap.values.toList()
    }

    companion object {
        private const val TAG = "AudioSessionTracker"

        @Volatile
        private var instance: AudioSessionTracker? = null

        fun getInstance(context: Context): AudioSessionTracker {
            return instance ?: synchronized(this) {
                instance ?: AudioSessionTracker(context.applicationContext).also { instance = it }
            }
        }
    }
}
