package com.timbre.dsp.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.timbre.dsp.audio.ParsedWavAudio
import com.timbre.dsp.audio.WavReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import kotlin.math.exp
import kotlin.math.sin

data class ImpulseResponseProfile(
    val id: String,
    val name: String,
    val category: String,
    val leftChannel: FloatArray,
    val rightChannel: FloatArray,
    val isCustom: Boolean = false
)

object ConvolutionRepository {

    private const val TAG = "ConvolutionRepo"
    private val _profiles = MutableStateFlow<List<ImpulseResponseProfile>>(emptyList())
    val profiles: StateFlow<List<ImpulseResponseProfile>> = _profiles.asStateFlow()

    fun initialize(context: Context) {
        val list = mutableListOf<ImpulseResponseProfile>()

        // 1. Built-in Studio Room
        list.add(generateSyntheticIR("studio_room", "Warm Studio Room", "Acoustics", decayMs = 120f, stereoSpread = 0.3f))
        // 2. Built-in Vintage Tube Amp
        list.add(generateSyntheticIR("tube_amp", "Vintage Tube Amp", "Harmonics", decayMs = 45f, stereoSpread = 0.1f, isTube = true))
        // 3. Built-in Binaural Spatializer
        list.add(generateSyntheticIR("binaural_spatial", "Binaural Spatializer", "Spatial", decayMs = 80f, stereoSpread = 0.8f))
        // 4. Built-in Acoustic Live Hall
        list.add(generateSyntheticIR("live_hall", "Acoustic Live Hall", "Reverb", decayMs = 250f, stereoSpread = 0.5f))

        // Load custom imported IRs from disk
        val irDir = File(context.filesDir, "irs")
        if (irDir.exists()) {
            irDir.listFiles { file -> file.extension.lowercase() in listOf("wav", "irs") }?.forEach { file ->
                try {
                    file.inputStream().use { stream ->
                        val parsed = WavReader.parseWav(stream)
                        if (parsed != null) {
                            list.add(
                                ImpulseResponseProfile(
                                    id = "custom_${file.name}",
                                    name = file.nameWithoutExtension.replace("_", " "),
                                    category = "Custom",
                                    leftChannel = parsed.leftChannel,
                                    rightChannel = parsed.rightChannel,
                                    isCustom = true
                                )
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed reading custom IR: ${file.name}", e)
                }
            }
        }

        _profiles.value = list
    }

    fun importCustomIR(context: Context, uri: Uri, fileName: String): ImpulseResponseProfile? {
        return try {
            val irDir = File(context.filesDir, "irs").apply { mkdirs() }
            val cleanName = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(irDir, cleanName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            targetFile.inputStream().use { stream ->
                val parsed = WavReader.parseWav(stream) ?: return null
                val profile = ImpulseResponseProfile(
                    id = "custom_${targetFile.name}",
                    name = targetFile.nameWithoutExtension.replace("_", " "),
                    category = "Custom",
                    leftChannel = parsed.leftChannel,
                    rightChannel = parsed.rightChannel,
                    isCustom = true
                )
                val current = _profiles.value.toMutableList()
                current.removeAll { it.id == profile.id }
                current.add(profile)
                _profiles.value = current
                profile
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed importing custom IR from uri $uri", e)
            null
        }
    }

    fun getProfileById(id: String): ImpulseResponseProfile? {
        return _profiles.value.find { it.id == id }
    }

    private fun generateSyntheticIR(
        id: String,
        name: String,
        category: String,
        decayMs: Float,
        stereoSpread: Float,
        isTube: Boolean = false
    ): ImpulseResponseProfile {
        val sampleRate = 48000
        val lengthSamples = (sampleRate * (decayMs / 1000f)).toInt().coerceIn(512, 2048)
        val left = FloatArray(lengthSamples)
        val right = FloatArray(lengthSamples)

        val decayFactor = 5.0f / lengthSamples

        for (i in 0 until lengthSamples) {
            val env = exp(-decayFactor * i)
            val noiseL = (sin(i * 0.12) * 0.5 + sin(i * 0.37) * 0.3 + sin(i * 1.1) * 0.2).toFloat()
            val noiseR = (sin(i * 0.15 + stereoSpread) * 0.5 + sin(i * 0.41 - stereoSpread) * 0.3 + sin(i * 1.05) * 0.2).toFloat()

            if (i == 0) {
                left[0] = 1.0f
                right[0] = 1.0f
            } else {
                val scale = if (isTube) 0.15f else 0.4f
                left[i] = (noiseL * env * scale)
                right[i] = (noiseR * env * scale)
            }
        }

        return ImpulseResponseProfile(
            id = id,
            name = name,
            category = category,
            leftChannel = left,
            rightChannel = right,
            isCustom = false
        )
    }
}
