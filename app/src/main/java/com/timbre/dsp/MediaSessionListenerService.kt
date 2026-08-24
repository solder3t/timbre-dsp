package com.timbre.dsp

import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.timbre.dsp.audio.AudioSessionTracker

class MediaSessionListenerService : NotificationListenerService() {

    private lateinit var tracker: AudioSessionTracker

    override fun onCreate() {
        super.onCreate()
        tracker = AudioSessionTracker.getInstance(this)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "NotificationListenerService connected")
        checkActiveSessions()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        checkActiveSessions()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        checkActiveSessions()
    }

    private fun checkActiveSessions() {
        try {
            val mediaSessionManager = getSystemService(MediaSessionManager::class.java)
            val controllers = mediaSessionManager?.getActiveSessions(null) ?: emptyList()
            for (controller in controllers) {
                val pkg = controller.packageName ?: continue
                Log.d(TAG, "Active media controller detected: $pkg")
            }
            // Trigger AudioFlinger scanning to bind newly started sessions
            tracker.scanAudioFlinger()
        } catch (e: SecurityException) {
            Log.w(TAG, "Missing NotificationListener permission", e)
        } catch (e: Throwable) {
            Log.e(TAG, "Error checking active media sessions", e)
        }
    }

    companion object {
        private const val TAG = "MediaSessionListener"
    }
}
