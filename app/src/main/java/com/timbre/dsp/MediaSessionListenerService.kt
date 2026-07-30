package com.timbre.dsp

import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class MediaSessionListenerService : NotificationListenerService() {

    private lateinit var effectManager: ShizukuEffectManager

    override fun onCreate() {
        super.onCreate()
        effectManager = ShizukuEffectManager()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        checkActiveSessions()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // Would normally check if a session ended to release its effect
    }

    private fun checkActiveSessions() {
        try {
            val mediaSessionManager = getSystemService(MediaSessionManager::class.java)
            val controllers = mediaSessionManager.getActiveSessions(null)
            
            for (controller in controllers) {
                // In a real scenario, we'd use reflection or Shizuku IPC to extract 
                // the hidden getAudioSessionId() from the MediaController or its PlaybackInfo
                // For demonstration, we assume a mock session ID
                val mockSessionId = 0 
                
                // effectManager.attachEffectToSession(mockSessionId)
            }
        } catch (e: SecurityException) {
            Log.w("MediaSessionListener", "Missing NotificationListener permission")
        }
    }
}
