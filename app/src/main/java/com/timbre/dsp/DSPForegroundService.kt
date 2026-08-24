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
        if (intent?.action == ACTION_TOGGLE_DSP) {
            val isEnabled = intent.getBooleanExtra(EXTRA_DSP_STATE, true)
            val effectMgr = AudioEffectManager.getInstance(this)
            val newSettings = currentSettings.copy(isEnabled = isEnabled)
            currentSettings = newSettings
            effectMgr.updateSettings(newSettings)
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

        val toggleIntent = Intent(this, DSPForegroundService::class.java).apply {
            action = ACTION_TOGGLE_DSP
            putExtra(EXTRA_DSP_STATE, !currentSettings.isEnabled)
        }
        val pendingToggleIntent = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleTitle = if (currentSettings.isEnabled) "Bypass" else "Enable"
        val statusText = if (currentSettings.isEnabled) "DSP Active • Limiter Enabled" else "DSP Bypassed"

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Timbre DSP")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingMainIntent)
            .addAction(android.R.drawable.ic_media_pause, toggleTitle, pendingToggleIntent)
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
        const val EXTRA_DSP_STATE = "extra_dsp_state"
        var currentSettings: DSPSettings = DSPSettings()
    }
}
