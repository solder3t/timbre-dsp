package com.timbre.dsp.audio

import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max
import kotlin.math.min

data class ParsedWavAudio(
    val sampleRate: Int,
    val numChannels: Int,
    val leftChannel: FloatArray,
    val rightChannel: FloatArray
)

object WavReader {

    fun parseWav(inputStream: InputStream): ParsedWavAudio? {
        return try {
            val bytes = inputStream.readBytes()
            if (bytes.size < 44) return null

            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)

            // Check RIFF header
            val riff = String(bytes, 0, 4)
            val wave = String(bytes, 8, 4)
            if (riff != "RIFF" || wave != "WAVE") return null

            var offset = 12
            var numChannels = 2
            var sampleRate = 48000
            var bitsPerSample = 16
            var audioFormat = 1 // 1 = PCM, 3 = IEEE Float
            var dataOffset = -1
            var dataSize = 0

            while (offset + 8 <= bytes.size) {
                val chunkId = String(bytes, offset, 4)
                val chunkSize = buffer.getInt(offset + 4)
                offset += 8

                if (chunkId == "fmt ") {
                    audioFormat = buffer.getShort(offset).toInt()
                    numChannels = buffer.getShort(offset + 2).toInt()
                    sampleRate = buffer.getInt(offset + 4)
                    bitsPerSample = buffer.getShort(offset + 14).toInt()
                    offset += chunkSize
                } else if (chunkId == "data") {
                    dataOffset = offset
                    dataSize = chunkSize
                    break
                } else {
                    offset += chunkSize
                }
            }

            if (dataOffset == -1 || dataOffset + dataSize > bytes.size) {
                dataOffset = 44
                dataSize = bytes.size - 44
            }

            val bytesPerSample = bitsPerSample / 8
            if (bytesPerSample <= 0 || numChannels <= 0) return null

            val numFrames = min(dataSize / (bytesPerSample * numChannels), 8192) // Limit to 8192 points max
            val left = FloatArray(numFrames)
            val right = FloatArray(numFrames)

            var readPos = dataOffset
            for (i in 0 until numFrames) {
                if (readPos + bytesPerSample * numChannels > bytes.size) break

                // Left sample
                val sampleL = readSampleAsFloat(buffer, readPos, audioFormat, bitsPerSample)
                left[i] = sampleL
                readPos += bytesPerSample

                // Right sample (if stereo, otherwise copy left)
                if (numChannels > 1) {
                    val sampleR = readSampleAsFloat(buffer, readPos, audioFormat, bitsPerSample)
                    right[i] = sampleR
                    readPos += bytesPerSample * (numChannels - 1)
                } else {
                    right[i] = sampleL
                }
            }

            // Normalize peak to -1dB (0.89) to prevent digital clipping
            normalizeAudio(left)
            if (numChannels > 1) normalizeAudio(right)

            ParsedWavAudio(sampleRate, numChannels, left, right)
        } catch (e: Exception) {
            null
        }
    }

    private fun readSampleAsFloat(buffer: ByteBuffer, offset: Int, format: Int, bits: Int): Float {
        return try {
            if (format == 3 && bits == 32) { // 32-bit float
                buffer.getFloat(offset)
            } else if (bits == 16) { // 16-bit PCM
                buffer.getShort(offset) / 32768.0f
            } else if (bits == 24) { // 24-bit PCM
                val b0 = buffer.get(offset).toInt() and 0xFF
                val b1 = buffer.get(offset + 1).toInt() and 0xFF
                val b2 = buffer.get(offset + 2).toInt() // signed
                val sample24 = (b2 shl 16) or (b1 shl 8) or b0
                sample24 / 8388608.0f
            } else if (bits == 32) { // 32-bit PCM
                buffer.getInt(offset) / 2147483648.0f
            } else {
                0f
            }
        } catch (e: Exception) {
            0f
        }
    }

    private fun normalizeAudio(samples: FloatArray) {
        var maxPeak = 0.0001f
        for (s in samples) {
            val absVal = kotlin.math.abs(s)
            if (absVal > maxPeak) maxPeak = absVal
        }
        if (maxPeak > 0.0001f) {
            val scale = 0.89f / maxPeak
            for (i in samples.indices) {
                samples[i] *= scale
            }
        }
    }
}
