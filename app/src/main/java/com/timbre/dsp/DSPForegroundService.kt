package com.timbre.dsp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat

class DSPForegroundService : Service() {

    private val CHANNEL_ID = "timbre_dsp_service_channel"
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        DSPEngine.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Timbre DSP Active")
            .setContentText("Audio processing is running.")
            .setSmallIcon(android.R.drawable.ic_media_play) // default icon
            .build()

        startForeground(1, notification)

        // Process audio stream here (or setup the audio hook)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        DSPEngine.stop()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Timbre DSP Service Channel",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }
}
