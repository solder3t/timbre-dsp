package com.timbre.dsp.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log

class AudioSessionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        val action = intent.action ?: return
        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR)
        val packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME) ?: "Unknown"

        Log.d(TAG, "Received audio session broadcast: action=$action, session=$sessionId, package=$packageName")

        if (sessionId == AudioEffect.ERROR) return

        when (action) {
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                AudioSessionTracker.getInstance(context).onSessionOpened(sessionId, packageName)
            }
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                AudioSessionTracker.getInstance(context).onSessionClosed(sessionId)
            }
        }
    }

    companion object {
        private const val TAG = "AudioSessionReceiver"
    }
}
