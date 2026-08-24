package com.timbre.dsp.ui.hearing

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.timbre.dsp.model.EQBand
import com.timbre.dsp.model.EQPreset
import com.timbre.dsp.model.FilterType
import com.timbre.dsp.model.HearingAudiogram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.sin

@Composable
fun HearingTestWizard(
    onDismiss: () -> Unit,
    onCompleteTest: (HearingAudiogram, EQPreset) -> Unit
) {
    val scope = rememberCoroutineScope()
    val frequencies = listOf(250, 500, 1000, 2000, 4000, 8000)
    
    // Step index: 0..5 = Left Ear, 6..11 = Right Ear
    var stepIndex by remember { mutableIntStateOf(0) }
    var currentVolumeGain by remember { mutableFloatStateOf(-30f) } // -50dB to 0dB

    val leftOffsets = remember { mutableMapOf<Int, Float>() }
    val rightOffsets = remember { mutableMapOf<Int, Float>() }

    var isPlayingTone by remember { mutableStateOf(false) }
    var audioTrack by remember { mutableStateOf<AudioTrack?>(null) }
    var playbackJob by remember { mutableStateOf<Job?>(null) }

    val isLeftEar = stepIndex < 6
    val currentFreq = frequencies[stepIndex % 6]
    val totalSteps = 12

    fun stopTone() {
        playbackJob?.cancel()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {}
        audioTrack = null
        isPlayingTone = false
    }

    fun playTone() {
        stopTone()
        isPlayingTone = true
        playbackJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 48000
            val bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_FLOAT
            ).coerceAtLeast(2048)

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 4)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack = track
            track.play()

            val numFrames = 512
            val buffer = FloatArray(numFrames * 2)
            var phase = 0.0
            val linearAmp = 10.0.pow(currentVolumeGain / 20.0).toFloat()

            try {
                while (isActive && isPlayingTone) {
                    val amp = 10.0.pow(currentVolumeGain / 20.0).toFloat()
                    for (i in 0 until numFrames) {
                        val s = (sin(phase) * amp).toFloat()
                        if (isLeftEar) {
                            buffer[i * 2] = s
                            buffer[i * 2 + 1] = 0f
                        } else {
                            buffer[i * 2] = 0f
                            buffer[i * 2 + 1] = s
                        }
                        phase += 2.0 * Math.PI * currentFreq / sampleRate
                    }
                    track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
                }
            } catch (e: Exception) {}
        }
    }

    DisposableEffect(Unit) {
        onDispose { stopTone() }
    }

    AlertDialog(
        onDismissRequest = {
            stopTone()
            onDismiss()
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Hearing, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Hearing Threshold Test")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = { (stepIndex + 1) / totalSteps.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Testing: ${if (isLeftEar) "Left Ear" else "Right Ear"}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Frequency: $currentFreq Hz (Step ${stepIndex + 1} of $totalSteps)",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Wear your headphones in a quiet environment. Adjust the volume until the tone is just barely audible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tone Level", style = MaterialTheme.typography.bodySmall)
                    Text(String.format("%.1f dB", currentVolumeGain), style = MaterialTheme.typography.bodySmall)
                }

                Slider(
                    value = currentVolumeGain,
                    onValueChange = {
                        currentVolumeGain = it
                        if (!isPlayingTone) playTone()
                    },
                    valueRange = -50f..0f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        if (isPlayingTone) stopTone() else playTone()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(if (isPlayingTone) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPlayingTone) "Pause Tone" else "Play Test Tone")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                stopTone()
                val offset = (currentVolumeGain - (-30f)).coerceIn(-12f, 12f)
                if (isLeftEar) {
                    leftOffsets[currentFreq] = -offset
                } else {
                    rightOffsets[currentFreq] = -offset
                }

                if (stepIndex < totalSteps - 1) {
                    stepIndex++
                    currentVolumeGain = -30f
                } else {
                    // Completed!
                    val audiogram = HearingAudiogram(leftOffsets.toMap(), rightOffsets.toMap(), isCalibrated = true)
                    val defaultFreqs = listOf(31.25f, 62.5f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
                    val bands = defaultFreqs.mapIndexed { idx, f ->
                        val l = leftOffsets[f.toInt()] ?: 0f
                        val r = rightOffsets[f.toInt()] ?: 0f
                        val avgOffset = (l + r) / 2f
                        EQBand(
                            index = idx,
                            frequency = f,
                            gain = avgOffset.coerceIn(-12f, 12f),
                            q = 1.414f,
                            type = FilterType.PEAK
                        )
                    }
                    val hearingPreset = EQPreset(
                        id = "hearing_calibrated",
                        name = "My Hearing Calibration",
                        isCustom = true,
                        bands = bands
                    )
                    onCompleteTest(audiogram, hearingPreset)
                    onDismiss()
                }
            }) {
                Text(if (stepIndex < totalSteps - 1) "Next Frequency" else "Finish & Calibrate")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                stopTone()
                onDismiss()
            }) {
                Text("Cancel")
            }
        }
    )
}
