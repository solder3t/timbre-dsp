@file:Suppress("DEPRECATION")

package com.timbre.dsp.audio

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.EQBand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class AudioEffectManager private constructor(private val context: Context) {

    private val activeDynamics = ConcurrentHashMap<Int, DynamicsProcessing>()
    private val activeEqualizers = ConcurrentHashMap<Int, Equalizer>()
    private val activeBassBoosts = ConcurrentHashMap<Int, BassBoost>()
    private val activeVirtualizers = ConcurrentHashMap<Int, Virtualizer>()

    @Volatile
    private var currentSettings: DSPSettings = DSPSettings()

    private val scope = CoroutineScope(Dispatchers.Default)

    fun updateSettings(settings: DSPSettings) {
        currentSettings = settings
        scope.launch {
            applySettingsToAllSessions()
        }
    }

    fun attachEffectToSession(sessionId: Int) {
        if (sessionId <= 0) return
        Log.i(TAG, "Attaching audio effect to session $sessionId")

        try {
            if (!activeDynamics.containsKey(sessionId)) {
                val dp = createDynamicsProcessing(sessionId)
                if (dp != null) {
                    activeDynamics[sessionId] = dp
                    applySettingsToDynamics(dp, currentSettings)
                } else {
                    // Fallback to legacy Equalizer + BassBoost + Virtualizer
                    setupLegacyEffects(sessionId)
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to attach DynamicsProcessing to session $sessionId, falling back to legacy AudioFX", e)
            setupLegacyEffects(sessionId)
        }
    }

    private fun setupLegacyEffects(sessionId: Int) {
        try {
            val eq = Equalizer(0, sessionId).apply {
                enabled = currentSettings.isEnabled
            }
            activeEqualizers[sessionId] = eq

            val bb = BassBoost(0, sessionId).apply {
                enabled = currentSettings.isEnabled && currentSettings.bassBoostEnabled
                if (strengthSupported) {
                    setStrength((currentSettings.bassBoostGain * 66.6f).toInt().coerceIn(0, 1000).toShort())
                }
            }
            activeBassBoosts[sessionId] = bb

            val virt = Virtualizer(0, sessionId).apply {
                enabled = currentSettings.isEnabled && currentSettings.virtualizerEnabled
                if (strengthSupported) {
                    setStrength((currentSettings.virtualizerStrength * 10f).toInt().coerceIn(0, 1000).toShort())
                }
            }
            activeVirtualizers[sessionId] = virt
            applyLegacyEQ(eq, currentSettings.bands)
            Log.i(TAG, "Attached legacy AudioEffects to session $sessionId")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to attach legacy AudioEffects to session $sessionId", e)
        }
    }

    fun releaseEffectForSession(sessionId: Int) {
        Log.i(TAG, "Releasing audio effect for session $sessionId")
        try {
            activeDynamics.remove(sessionId)?.apply {
                enabled = false
                release()
            }
            activeEqualizers.remove(sessionId)?.apply {
                enabled = false
                release()
            }
            activeBassBoosts.remove(sessionId)?.apply {
                enabled = false
                release()
            }
            activeVirtualizers.remove(sessionId)?.apply {
                enabled = false
                release()
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error releasing effect for session $sessionId", e)
        }
    }

    fun releaseAll() {
        for (sessionId in activeDynamics.keys) {
            releaseEffectForSession(sessionId)
        }
        for (sessionId in activeEqualizers.keys) {
            releaseEffectForSession(sessionId)
        }
    }

    private fun applySettingsToAllSessions() {
        for ((_, dp) in activeDynamics) {
            try {
                applySettingsToDynamics(dp, currentSettings)
            } catch (e: Throwable) {
                Log.e(TAG, "Error applying settings to DynamicsProcessing", e)
            }
        }

        for ((_, eq) in activeEqualizers) {
            try {
                eq.enabled = currentSettings.isEnabled
                applyLegacyEQ(eq, currentSettings.bands)
            } catch (e: Throwable) {
                Log.e(TAG, "Error applying settings to legacy Equalizer", e)
            }
        }

        for ((_, bb) in activeBassBoosts) {
            try {
                bb.enabled = currentSettings.isEnabled && currentSettings.bassBoostEnabled
                if (bb.strengthSupported) {
                    bb.setStrength((currentSettings.bassBoostGain * 66.6f).toInt().coerceIn(0, 1000).toShort())
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error applying settings to BassBoost", e)
            }
        }

        for ((_, virt) in activeVirtualizers) {
            try {
                virt.enabled = currentSettings.isEnabled && currentSettings.virtualizerEnabled
                if (virt.strengthSupported) {
                    virt.setStrength((currentSettings.virtualizerStrength * 10f).toInt().coerceIn(0, 1000).toShort())
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error applying settings to Virtualizer", e)
            }
        }
    }

    private fun createDynamicsProcessing(sessionId: Int): DynamicsProcessing? {
        return try {
            val bandCount = currentSettings.bands.size.coerceAtLeast(10)
            val builder = DynamicsProcessing.Config.Builder(
                DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                2, // stereo channels
                true, // pre-EQ enabled
                bandCount,
                false, // multi-band compressor
                0,
                true, // post-EQ enabled
                bandCount,
                true // Limiter enabled
            )
            val config = builder.build()
            val dp = DynamicsProcessing(0, sessionId, config)
            dp.enabled = currentSettings.isEnabled
            dp
        } catch (e: Throwable) {
            Log.w(TAG, "DynamicsProcessing constructor failed for session $sessionId", e)
            null
        }
    }

    private fun applySettingsToDynamics(dp: DynamicsProcessing, settings: DSPSettings) {
        dp.enabled = settings.isEnabled
        if (!settings.isEnabled) return

        // 1. Input Gain / Pre-amp
        dp.setInputGainAllChannelsTo(settings.preampGain)

        // 2. Pre-EQ bands (10-band or custom)
        val bandCount = settings.bands.size
        for (i in 0 until bandCount) {
            val band = settings.bands[i]
            val eqBand = DynamicsProcessing.EqBand(
                band.enabled,
                band.frequency,
                band.gain
            )
            // Channel 0 = Left, Channel 1 = Right
            dp.setPreEqBandByChannelIndex(0, i, eqBand)
            dp.setPreEqBandByChannelIndex(1, i, eqBand)
        }

        // 3. Peak Limiter configuration (anti-clipping protection)
        val limiter = DynamicsProcessing.Limiter(
            settings.limiterEnabled,
            true, // inUse
            0, // channel
            1.0f, // attackTime (ms)
            60.0f, // releaseTime (ms)
            10.0f, // ratio
            -0.2f, // threshold (dB)
            0.0f // postGain (dB)
        )
        dp.setLimiterAllChannelsTo(limiter)
    }

    private fun applyLegacyEQ(eq: Equalizer, bands: List<EQBand>) {
        val numBands = eq.numberOfBands.toInt()
        for (i in 0 until numBands) {
            val band = bands.getOrNull(i) ?: continue
            val levelMb = (band.gain * 100).toInt().coerceIn(
                eq.bandLevelRange[0].toInt(),
                eq.bandLevelRange[1].toInt()
            ).toShort()
            eq.setBandLevel(i.toShort(), levelMb)
        }
    }

    companion object {
        private const val TAG = "AudioEffectManager"

        @Volatile
        private var instance: AudioEffectManager? = null

        fun getInstance(context: Context): AudioEffectManager {
            return instance ?: synchronized(this) {
                instance ?: AudioEffectManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
