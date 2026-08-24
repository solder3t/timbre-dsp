package com.timbre.dsp.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.audiofx.Visualizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

class LiveAudioVisualizer private constructor(private val context: Context) {

    private var visualizer: Visualizer? = null

    private val _fftMagnitudes = MutableStateFlow(FloatArray(NUM_BARS) { 0f })
    val fftMagnitudes: StateFlow<FloatArray> = _fftMagnitudes.asStateFlow()

    private val _peakLevels = MutableStateFlow(Pair(0f, 0f)) // Left, Right (0.0 to 1.0)
    val peakLevels: StateFlow<Pair<Float, Float>> = _peakLevels.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    fun start(audioSessionId: Int = 0) {
        if (visualizer != null) return

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Cannot start Visualizer: RECORD_AUDIO permission not granted")
            return
        }

        try {
            val captureRate = Visualizer.getMaxCaptureRate().coerceAtMost(30000)
            val captureSize = Visualizer.getCaptureSizeRange()[1] // 1024 typically

            val viz = Visualizer(audioSessionId).apply {
                this.captureSize = captureSize
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                            if (waveform != null) {
                                processWaveform(waveform)
                            }
                        }

                        override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                            if (fft != null) {
                                processFft(fft)
                            }
                        }
                    },
                    captureRate,
                    true, // waveform
                    true  // FFT
                )
                enabled = true
            }

            visualizer = viz
            _isCapturing.value = true
            Log.i(TAG, "Visualizer started on session $audioSessionId")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed initializing Visualizer", e)
            release()
        }
    }

    fun stop() {
        try {
            visualizer?.enabled = false
            _isCapturing.value = false
        } catch (e: Throwable) {
            Log.e(TAG, "Error stopping Visualizer", e)
        }
    }

    fun release() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
            visualizer = null
            _isCapturing.value = false
            _fftMagnitudes.value = FloatArray(NUM_BARS) { 0f }
            _peakLevels.value = Pair(0f, 0f)
        } catch (e: Throwable) {
            Log.e(TAG, "Error releasing Visualizer", e)
        }
    }

    private fun processWaveform(waveform: ByteArray) {
        var sumSquaresL = 0.0
        var sumSquaresR = 0.0
        val n = waveform.size

        for (i in 0 until n) {
            // Unsigned 8-bit PCM (128 is zero)
            val sample = ((waveform[i].toInt() and 0xFF) - 128) / 128.0
            if (i % 2 == 0) {
                sumSquaresL += sample * sample
            } else {
                sumSquaresR += sample * sample
            }
        }

        val rmsL = sqrt(sumSquaresL / (n / 2).coerceAtLeast(1)).toFloat().coerceIn(0f, 1f)
        val rmsR = sqrt(sumSquaresR / (n / 2).coerceAtLeast(1)).toFloat().coerceIn(0f, 1f)

        _peakLevels.value = Pair(rmsL * 2.2f, rmsR * 2.2f)
    }

    private fun processFft(fft: ByteArray) {
        val n = fft.size / 2
        val rawMags = FloatArray(n)

        for (i in 0 until n) {
            val r = fft[2 * i].toFloat()
            val im = if (2 * i + 1 < fft.size) fft[2 * i + 1].toFloat() else 0f
            val mag = hypot(r, im) / 128f
            rawMags[i] = mag.coerceIn(0f, 1f)
        }

        // Map to NUM_BARS logarithmically
        val bars = FloatArray(NUM_BARS)
        val currentBars = _fftMagnitudes.value

        for (b in 0 until NUM_BARS) {
            val startRatio = (b.toDouble() / NUM_BARS).let { it * it }
            val endRatio = ((b + 1).toDouble() / NUM_BARS).let { it * it }

            val startIdx = (startRatio * n).toInt().coerceIn(0, n - 1)
            val endIdx = (endRatio * n).toInt().coerceIn(startIdx + 1, n)

            var avg = 0f
            for (idx in startIdx until endIdx) {
                avg += rawMags[idx]
            }
            avg /= (endIdx - startIdx).coerceAtLeast(1)

            // Smooth decay filter for buttery 60fps animations
            val prev = currentBars.getOrElse(b) { 0f }
            bars[b] = if (avg > prev) avg else prev * 0.82f
        }

        _fftMagnitudes.value = bars
    }

    companion object {
        private const val TAG = "LiveAudioVisualizer"
        const val NUM_BARS = 32

        @Volatile
        private var instance: LiveAudioVisualizer? = null

        fun getInstance(context: Context): LiveAudioVisualizer {
            return instance ?: synchronized(this) {
                instance ?: LiveAudioVisualizer(context.applicationContext).also { instance = it }
            }
        }
    }
}
