package com.adioevo.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import timber.log.Timber
import kotlin.math.min

/**
 * Low-latency audio buffer for recording and playback.
 * Uses circular buffer design for efficient memory management.
 */
class AudioBuffer(val sampleRate: Int = 44100, val bufferSize: Int = 512) {

    private val buffer: FloatArray = FloatArray(bufferSize * 8) // 8x buffer for headroom
    private var writeIndex = 0
    private var readIndex = 0
    private var isRecording = false
    private var isPlaying = false

    private val lock = Any()

    init {
        Timber.d("AudioBuffer initialized: sampleRate=$sampleRate, bufferSize=$bufferSize")
    }

    /**
     * Write audio samples to buffer (for recording)
     */
    fun write(samples: FloatArray, offset: Int = 0, count: Int = samples.size): Int {
        return synchronized(lock) {
            if (!isRecording) return 0

            val available = getAvailableWriteSpace()
            val toWrite = min(count, available)

            if (toWrite <= 0) {
                Timber.w("Audio buffer: no write space available")
                return 0
            }

            // Handle wrap-around
            val firstPart = min(toWrite, buffer.size - writeIndex)
            val secondPart = toWrite - firstPart

            samples.copyInto(buffer, writeIndex, offset, offset + firstPart)
            if (secondPart > 0) {
                samples.copyInto(buffer, 0, offset + firstPart, offset + firstPart + secondPart)
            }

            writeIndex = (writeIndex + toWrite) % buffer.size
            return toWrite
        }
    }

    /**
     * Read audio samples from buffer (for playback)
     */
    fun read(samples: FloatArray, offset: Int = 0, count: Int = samples.size): Int {
        return synchronized(lock) {
            if (!isPlaying) return 0

            val available = getAvailableReadSpace()
            val toRead = min(count, available)

            if (toRead <= 0) {
                Timber.w("Audio buffer: no data available to read")
                return 0
            }

            // Handle wrap-around
            val firstPart = min(toRead, buffer.size - readIndex)
            val secondPart = toRead - firstPart

            buffer.copyInto(samples, offset, readIndex, readIndex + firstPart)
            if (secondPart > 0) {
                buffer.copyInto(samples, offset + firstPart, 0, secondPart)
            }

            readIndex = (readIndex + toRead) % buffer.size
            return toRead
        }
    }

    /**
     * Get available space for writing (in samples)
     */
    fun getAvailableWriteSpace(): Int {
        return synchronized(lock) {
            if (writeIndex >= readIndex) {
                buffer.size - (writeIndex - readIndex)
            } else {
                readIndex - writeIndex
            }
        }
    }

    /**
     * Get available data for reading (in samples)
     */
    fun getAvailableReadSpace(): Int {
        return synchronized(lock) {
            if (readIndex <= writeIndex) {
                writeIndex - readIndex
            } else {
                buffer.size - (readIndex - writeIndex)
            }
        }
    }

    /**
     * Clear all buffer contents
     */
    fun clear() {
        synchronized(lock) {
            buffer.fill(0f)
            writeIndex = 0
            readIndex = 0
            Timber.d("Audio buffer cleared")
        }
    }

    /**
     * Start recording mode
     */
    fun startRecording() {
        synchronized(lock) {
            isRecording = true
            isPlaying = false
            clear()
            Timber.d("Audio buffer: recording started")
        }
    }

    /**
     * Start playback mode
     */
    fun startPlayback() {
        synchronized(lock) {
            isRecording = false
            isPlaying = true
            readIndex = 0
            Timber.d("Audio buffer: playback started")
        }
    }

    /**
     * Stop all operations
     */
    fun stop() {
        synchronized(lock) {
            isRecording = false
            isPlaying = false
            Timber.d("Audio buffer: stopped")
        }
    }

    fun isRecordingNow(): Boolean = isRecording
    fun isPlayingNow(): Boolean = isPlaying
}
