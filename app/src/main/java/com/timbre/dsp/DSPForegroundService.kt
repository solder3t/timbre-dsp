package com.timbre.dsp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.timbre.dsp.audio.AudioEffectManager
import com.timbre.dsp.audio.AudioSessionTracker
import com.timbre.dsp.data.PresetRepository
import com.timbre.dsp.model.DSPSettings

class DSPForegroundService : Service() {

    private val CHANNEL_ID = "timbre_dsp_service_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        DSPEngine.start()
        AudioSessionTracker.getInstance(this).scanAudioFlinger()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val effectMgr = AudioEffectManager.getInstance(this)

        when (intent?.action) {
            ACTION_TOGGLE_DSP -> {
                val isEnabled = intent.getBooleanExtra(EXTRA_DSP_STATE, true)
                val newSettings = currentSettings.copy(isEnabled = isEnabled)
                currentSettings = newSettings
                effectMgr.updateSettings(newSettings)
                DSPEngine.applySettings(newSettings)
            }
            ACTION_NEXT_PRESET -> {
                val presets = PresetRepository.presets.value
                if (presets.isNotEmpty()) {
                    val currentIndex = presets.indexOfFirst { it.id == currentSettings.currentPresetId }
                    val nextIndex = (currentIndex + 1) % presets.size
                    val nextPreset = presets[nextIndex]
                    val newSettings = currentSettings.copy(
                        currentPresetId = nextPreset.id,
                        bands = nextPreset.bands.map { it.copy() },
                        preampGain = nextPreset.preampGain
                    )
                    currentSettings = newSettings
                    effectMgr.updateSettings(newSettings)
                    DSPEngine.applySettings(newSettings)
                }
            }
            ACTION_PREV_PRESET -> {
                val presets = PresetRepository.presets.value
                if (presets.isNotEmpty()) {
                    val currentIndex = presets.indexOfFirst { it.id == currentSettings.currentPresetId }
                    val prevIndex = if (currentIndex <= 0) presets.size - 1 else currentIndex - 1
                    val prevPreset = presets[prevIndex]
                    val newSettings = currentSettings.copy(
                        currentPresetId = prevPreset.id,
                        bands = prevPreset.bands.map { it.copy() },
                        preampGain = prevPreset.preampGain
                    )
                    currentSettings = newSettings
                    effectMgr.updateSettings(newSettings)
                    DSPEngine.applySettings(newSettings)
                }
            }
        }

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingMainIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Prev Action
        val prevIntent = Intent(this, DSPForegroundService::class.java).apply {
            action = ACTION_PREV_PRESET
        }
        val pendingPrevIntent = PendingIntent.getService(
            this,
            1,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Toggle Action
        val toggleIntent = Intent(this, DSPForegroundService::class.java).apply {
            action = ACTION_TOGGLE_DSP
            putExtra(EXTRA_DSP_STATE, !currentSettings.isEnabled)
        }
        val pendingToggleIntent = PendingIntent.getService(
            this,
            2,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Next Action
        val nextIntent = Intent(this, DSPForegroundService::class.java).apply {
            action = ACTION_NEXT_PRESET
        }
        val pendingNextIntent = PendingIntent.getService(
            this,
            3,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activePreset = PresetRepository.getPresetById(currentSettings.currentPresetId)?.name ?: "Flat"
        val toggleTitle = if (currentSettings.isEnabled) "Bypass" else "Enable"
        val statusText = if (currentSettings.isEnabled) "Active • $activePreset" else "DSP Bypassed"

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Timbre DSP")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingMainIntent)
            .addAction(android.R.drawable.ic_media_previous, "Prev", pendingPrevIntent)
            .addAction(if (currentSettings.isEnabled) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play, toggleTitle, pendingToggleIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", pendingNextIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        DSPEngine.stop()
        AudioEffectManager.getInstance(this).releaseAll()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Timbre DSP Service Channel",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Maintains persistent audio processing engine"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

    companion object {
        const val NOTIFICATION_ID = 101
        const val ACTION_TOGGLE_DSP = "com.timbre.dsp.ACTION_TOGGLE_DSP"
        const val ACTION_NEXT_PRESET = "com.timbre.dsp.ACTION_NEXT_PRESET"
        const val ACTION_PREV_PRESET = "com.timbre.dsp.ACTION_PREV_PRESET"
        const val EXTRA_DSP_STATE = "extra_dsp_state"
        var currentSettings: DSPSettings = DSPSettings()
    }
}
