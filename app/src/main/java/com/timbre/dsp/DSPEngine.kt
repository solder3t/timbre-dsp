package com.timbre.dsp

import android.util.Log
import com.timbre.dsp.model.DSPSettings
import com.timbre.dsp.model.FilterType

object DSPEngine {
    private var engineHandle: Long = 0

    init {
        try {
            System.loadLibrary("timbre_dsp")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("DSPEngine", "Failed to load timbre_dsp library", e)
        }
    }

    fun start() {
        if (engineHandle == 0L) {
            engineHandle = createEngine()
        }
    }

    fun stop() {
        if (engineHandle != 0L) {
            destroyEngine(engineHandle)
            engineHandle = 0
        }
    }

    fun process(buffer: FloatArray) {
        if (engineHandle != 0L) {
            processBuffer(engineHandle, buffer, buffer.size)
        }
    }

    fun processStereo(left: FloatArray, right: FloatArray) {
        if (engineHandle != 0L) {
            val numSamples = minOf(left.size, right.size)
            processStereoBuffer(engineHandle, left, right, numSamples)
        }
    }

    fun applySettings(settings: DSPSettings) {
        if (engineHandle == 0L) return
        setPreampGain(engineHandle, settings.preampGain)
        setLimiterEnabled(engineHandle, settings.limiterEnabled)
        setBassBoost(engineHandle, settings.bassBoostEnabled, settings.bassBoostGain, settings.bassBoostCutoffFreq)
        setCrossfeed(engineHandle, settings.crossfeedEnabled, settings.crossfeedStrength)

        settings.bands.forEachIndexed { index, band ->
            val typeInt = when (band.type) {
                FilterType.LOW_PASS -> 0
                FilterType.HIGH_PASS -> 1
                FilterType.BAND_PASS -> 2
                FilterType.NOTCH -> 3
                FilterType.PEAK -> 4
                FilterType.LOW_SHELF -> 5
                FilterType.HIGH_SHELF -> 6
            }
            setBandParameters(engineHandle, index, typeInt, band.frequency, band.q, if (band.enabled) band.gain else 0f)
        }
    }

    fun setBandGain(index: Int, gain: Float) {
        if (engineHandle != 0L) {
            setBandGain(engineHandle, index, gain)
        }
    }

    // Native methods mapping to jni_bridge.cpp
    private external fun createEngine(): Long
    private external fun destroyEngine(handle: Long)
    private external fun processBuffer(handle: Long, buffer: FloatArray, numSamples: Int)
    private external fun processStereoBuffer(handle: Long, left: FloatArray, right: FloatArray, numSamples: Int)
    private external fun setBandGain(handle: Long, index: Int, gain: Float)
    private external fun setBandParameters(handle: Long, index: Int, type: Int, fc: Float, q: Float, gain: Float)
    private external fun setPreampGain(handle: Long, gainDb: Float)
    private external fun setLimiterEnabled(handle: Long, enabled: Boolean)
    private external fun setBassBoost(handle: Long, enabled: Boolean, gainDb: Float, cutoffFreq: Float)
    private external fun setCrossfeed(handle: Long, enabled: Boolean, strength: Float)
}
