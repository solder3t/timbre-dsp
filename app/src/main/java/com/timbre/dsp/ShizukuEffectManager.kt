package com.timbre.dsp

import android.content.Context
import com.timbre.dsp.audio.AudioEffectManager

/**
 * @deprecated Use [AudioEffectManager] directly.
 */
@Deprecated("Use AudioEffectManager instead", ReplaceWith("AudioEffectManager.getInstance(context)"))
class ShizukuEffectManager {
    fun attachEffectToSession(sessionId: Int) {
        // Delegated to AudioEffectManager
    }

    fun releaseEffectForSession(sessionId: Int) {
        // Delegated to AudioEffectManager
    }
}
