package com.timbre.dsp

import android.util.Log

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

    fun setBandGain(index: Int, gain: Float) {
        if (engineHandle != 0L) {
            setBandGain(engineHandle, index, gain)
        }
    }

    // Native methods mapping to jni_bridge.cpp
    private external fun createEngine(): Long
    private external fun destroyEngine(handle: Long)
    private external fun processBuffer(handle: Long, buffer: FloatArray, numSamples: Int)
    private external fun setBandGain(handle: Long, index: Int, gain: Float)
}
