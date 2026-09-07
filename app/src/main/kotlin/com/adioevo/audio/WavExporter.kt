package com.adioevo.audio

import android.content.Context
import timber.log.Timber
import java.io.File
import java.io.RandomAccessFile

/**
 * WAV Exporter - Exports audio to WAV format with proper headers
 * Supports mono and stereo, 16-bit PCM
 */
class WavExporter(private val context: Context) {

    /**
     * Export audio buffer to WAV file
     */
    fun exportToWav(
        audioData: FloatArray,
        sampleRate: Int = 44100,
        channelCount: Int = 1,
        outputFile: File
    ): Boolean {
        return try {
            if (audioData.isEmpty()) {
                Timber.w("Audio data is empty")
                return false
            }

            val pcmData = convertFloatToPcm16(audioData)
            writeWavFile(outputFile, pcmData, sampleRate, channelCount)

            Timber.d(
                "WAV file exported: ${outputFile.absolutePath} " +
                        "(${outputFile.length() / 1024}KB, ${audioData.size} samples)"
            )
            true
        } catch (e: Exception) {
            Timber.e(e, "Error exporting WAV file")
            false
        }
    }

    /**
     * Export multiple tracks as a mixed WAV file
     */
    fun exportMixedWav(
        trackData: Map<Int, FloatArray>,
        trackVolumes: Map<Int, Float> = emptyMap(),
        sampleRate: Int = 44100,
        outputFile: File
    ): Boolean {
        return try {
            if (trackData.isEmpty()) {
                Timber.w("No track data to export")
                return false
            }

            // Find the maximum length
            val maxLength = trackData.values.maxOfOrNull { it.size } ?: 0
            if (maxLength == 0) return false

            // Mix all tracks
            val mixedData = FloatArray(maxLength)
            trackData.forEach { (trackId, data) ->
                val volume = trackVolumes[trackId] ?: 1f
                for (i in data.indices) {
                    mixedData[i] += data[i] * volume
                }
            }

            // Normalize to prevent clipping
            val maxAmplitude = mixedData.maxOrNull()?.let { kotlin.math.abs(it) } ?: 1f
            if (maxAmplitude > 1f) {
                for (i in mixedData.indices) {
                    mixedData[i] /= maxAmplitude
                }
            }

            val pcmData = convertFloatToPcm16(mixedData)
            writeWavFile(outputFile, pcmData, sampleRate, 1) // Mono output

            Timber.d(
                "Mixed WAV file exported: ${outputFile.absolutePath} " +
                        "(${outputFile.length() / 1024}KB, ${trackData.size} tracks)"
            )
            true
        } catch (e: Exception) {
            Timber.e(e, "Error exporting mixed WAV file")
            false
        }
    }

    /**
     * Convert 32-bit float audio to 16-bit PCM
     */
    private fun convertFloatToPcm16(floatData: FloatArray): ByteArray {
        val pcmData = ByteArray(floatData.size * 2)
        for (i in floatData.indices) {
            val sample = (floatData[i] * 32767f).toInt().coerceIn(-32768, 32767).toShort()
            pcmData[i * 2] = (sample.toInt() and 0xFF).toByte()
            pcmData[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return pcmData
    }

    /**
     * Write WAV file with proper headers
     */
    private fun writeWavFile(
        file: File,
        pcmData: ByteArray,
        sampleRate: Int,
        channelCount: Int
    ) {
        RandomAccessFile(file, "rw").use { raf ->
            // RIFF header
            raf.write("RIFF".toByteArray())
            val fileSize = 36 + pcmData.size
            writeInt(raf, fileSize)
            raf.write("WAVE".toByteArray())

            // fmt subchunk
            raf.write("fmt ".toByteArray())
            writeInt(raf, 16) // Subchunk1Size
            writeShort(raf, 1) // AudioFormat (1 = PCM)
            writeShort(raf, channelCount)
            writeInt(raf, sampleRate)
            writeInt(raf, sampleRate * channelCount * 2) // ByteRate
            writeShort(raf, (channelCount * 2).toShort()) // BlockAlign
            writeShort(raf, 16) // BitsPerSample

            // data subchunk
            raf.write("data".toByteArray())
            writeInt(raf, pcmData.size)
            raf.write(pcmData)
        }
    }

    private fun writeInt(raf: RandomAccessFile, value: Int) {
        raf.writeByte(value and 0xFF)
        raf.writeByte((value shr 8) and 0xFF)
        raf.writeByte((value shr 16) and 0xFF)
        raf.writeByte((value shr 24) and 0xFF)
    }

    private fun writeShort(raf: RandomAccessFile, value: Short) {
        raf.writeByte(value.toInt() and 0xFF)
        raf.writeByte((value.toInt() shr 8) and 0xFF)
    }

    private fun writeShort(raf: RandomAccessFile, value: Int) {
        raf.writeByte(value and 0xFF)
        raf.writeByte((value shr 8) and 0xFF)
    }
}
