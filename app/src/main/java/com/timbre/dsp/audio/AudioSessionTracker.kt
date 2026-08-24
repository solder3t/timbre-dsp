package com.timbre.dsp.audio

import android.content.Context
import android.content.pm.PackageManager
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
    private val scope = CoroutineScope(Dispatchers.IO)

    fun onSessionOpened(sessionId: Int, packageName: String) {
        if (sessionId <= 0) return
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
        // Try Shizuku first if available
        if (tryShizukuDump()) {
            return@withContext runShizukuDump()
        }

        // Try direct dumpsys if DUMP permission is granted
        try {
            val process = Runtime.getRuntime().exec(arrayOf("dumpsys", "media.audio_flinger"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val text = reader.readText()
            process.waitFor()
            if (text.isNotBlank()) return@withContext text
        } catch (e: Exception) {
            // Ignored
        }

        // Try root
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "dumpsys media.audio_flinger"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val text = reader.readText()
            process.waitFor()
            if (text.isNotBlank()) return@withContext text
        } catch (e: Exception) {
            // Ignored
        }

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
            val process = method.invoke(null, arrayOf("dumpsys", "media.audio_flinger"), null, null) as? Process
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
        // Patterns for AudioFlinger track sessions
        val sessionPattern = Pattern.compile("Session\\s*(?:id)?\\s*[:=]\\s*(\\d+)", Pattern.CASE_INSENSITIVE)
        val matcher = sessionPattern.matcher(dumpText)
        val discoveredSessions = mutableSetOf<Int>()

        while (matcher.find()) {
            val idStr = matcher.group(1) ?: continue
            val sid = idStr.toIntOrNull() ?: continue
            if (sid > 0) {
                discoveredSessions.add(sid)
            }
        }

        // Register newly discovered sessions
        for (sid in discoveredSessions) {
            if (!sessionsMap.containsKey(sid)) {
                onSessionOpened(sid, "Active Playback Stream")
            }
        }
    }

    private fun getAppName(packageName: String): String {
        if (packageName == "Unknown" || packageName == "Active Playback Stream") return packageName
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
