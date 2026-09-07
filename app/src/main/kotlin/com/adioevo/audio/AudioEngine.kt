package com.adioevo.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import kotlin.concurrent.thread

/**
 * Core Audio Engine for AdioEVO
 * Handles audio recording, playback, mixing, and low-latency processing
 */
class AudioEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    private val _sampleRate = MutableStateFlow(44100)
    val sampleRate: StateFlow<Int> = _sampleRate

    private val _bufferSize = MutableStateFlow(512)
    val bufferSize: StateFlow<Int> = _bufferSize

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _masterVolume = MutableStateFlow(1.0f)
    val masterVolume: StateFlow<Float> = _masterVolume

    private val _inputLevel = MutableStateFlow(0f)
    val inputLevel: StateFlow<Float> = _inputLevel

    private val audioBuffer = AudioBuffer(_sampleRate.value, _bufferSize.value)

    private var recordingThread: Thread? = null
    private var playbackThread: Thread? = null
    private var isEngineRunning = false

    init {
        Timber.d("AudioEngine initialized")
        setupAudioAttributes()
    }

    private fun setupAudioAttributes() {
        try {
            // Request low-latency audio mode
            audioManager?.setProperty(
                AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER,
                _bufferSize.value.toString()
            )
            Timber.d("Audio attributes configured for low-latency")
        } catch (e: Exception) {
            Timber.e(e, "Error setting audio attributes")
        }
    }

    /**
     * Initialize recording with specified parameters
     */
    fun initializeRecording(
        sampleRate: Int = 44100,
        bufferSize: Int = 512,
        channelCount: Int = 1
    ): Boolean {
        return try {
            _sampleRate.value = sampleRate
            _bufferSize.value = bufferSize

            val minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT
            )

            val recordBufferSize = (minBufferSize * 2).coerceAtLeast(bufferSize * 2)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                android.media.AudioFormat.CHANNEL_IN_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT,
                recordBufferSize
            )

            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED) {
                Timber.d(
                    "Recording initialized: " +
                            "sampleRate=$sampleRate, bufferSize=$recordBufferSize"
                )
                true
            } else {
                Timber.e("Failed to initialize AudioRecord")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing recording")
            false
        }
    }

    /**
     * Initialize playback with specified parameters
     */
    fun initializePlayback(
        sampleRate: Int = 44100,
        bufferSize: Int = 512,
        channelCount: Int = 1
    ): Boolean {
        return try {
            _sampleRate.value = sampleRate
            _bufferSize.value = bufferSize

            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                android.media.AudioFormat.CHANNEL_OUT_MONO,
                android.media.AudioFormat.ENCODING_PCM_16BIT
            )

            val playBufferSize = (minBufferSize * 2).coerceAtLeast(bufferSize * 2)

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            audioTrack = AudioTrack(
                audioAttributes,
                audioFormat,
                playBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            if (audioTrack?.state == AudioTrack.STATE_INITIALIZED) {
                Timber.d(
                    "Playback initialized: " +
                            "sampleRate=$sampleRate, bufferSize=$playBufferSize"
                )
                true
            } else {
                Timber.e("Failed to initialize AudioTrack")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing playback")
            false
        }
    }

    /**
     * Start recording audio
     */
    fun startRecording(): Boolean {
        return try {
            if (audioRecord == null) {
                if (!initializeRecording()) return false
            }

            audioRecord?.startRecording()
            _isRecording.value = true
            isEngineRunning = true
            audioBuffer.startRecording()

            recordingThread = thread(start = true) {
                val buffer = ShortArray(_bufferSize.value)
                while (_isRecording.value && isEngineRunning) {
                    val samplesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (samplesRead > 0) {
                        // Convert to float and update input level
                        val floatBuffer = FloatArray(samplesRead) { i ->
                            (buffer[i] / 32768f)
                        }
                        audioBuffer.write(floatBuffer)

                        // Calculate RMS for level meter
                        val rms = calculateRMS(floatBuffer)
                        _inputLevel.value = rms
                    }
                }
            }

            Timber.d("Recording started")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error starting recording")
            false
        }
    }

    /**
     * Stop recording audio
     */
    fun stopRecording() {
        try {
            _isRecording.value = false
            audioRecord?.stop()
            audioBuffer.stop()
            recordingThread?.join(1000)
            Timber.d("Recording stopped")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping recording")
        }
    }

    /**
     * Start audio playback
     */
    fun startPlayback(): Boolean {
        return try {
            if (audioTrack == null) {
                if (!initializePlayback()) return false
            }

            audioTrack?.play()
            _isPlaying.value = true
            isEngineRunning = true
            audioBuffer.startPlayback()

            playbackThread = thread(start = true) {
                val buffer = ShortArray(_bufferSize.value)
                while (_isPlaying.value && isEngineRunning) {
                    val floatBuffer = FloatArray(_bufferSize.value)
                    val samplesRead = audioBuffer.read(floatBuffer, 0, _bufferSize.value)

                    if (samplesRead > 0) {
                        // Apply master volume and convert back to short
                        for (i in 0 until samplesRead) {
                            val sample = (floatBuffer[i] * _masterVolume.value * 32767f).toInt()
                            buffer[i] = sample.coerceIn(-32768, 32767).toShort()
                        }
                        audioTrack?.write(buffer, 0, samplesRead)
                    }
                }
            }

            Timber.d("Playback started")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error starting playback")
            false
        }
    }

    /**
     * Stop audio playback
     */
    fun stopPlayback() {
        try {
            _isPlaying.value = false
            audioTrack?.stop()
            audioBuffer.stop()
            playbackThread?.join(1000)
            Timber.d("Playback stopped")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping playback")
        }
    }

    /**
     * Set master volume (0.0 - 1.0)
     */
    fun setMasterVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0f, 1f)
        _masterVolume.value = clampedVolume
        Timber.d("Master volume set to: $clampedVolume")
    }

    /**
     * Release all audio resources
     */
    fun release() {
        try {
            isEngineRunning = false
            stopRecording()
            stopPlayback()
            audioRecord?.release()
            audioTrack?.release()
            audioRecord = null
            audioTrack = null
            Timber.d("AudioEngine released")
        } catch (e: Exception) {
            Timber.e(e, "Error releasing AudioEngine")
        }
    }

    /**
     * Calculate RMS (Root Mean Square) for level metering
     */
    private fun calculateRMS(samples: FloatArray): Float {
        if (samples.isEmpty()) return 0f
        val sum = samples.sumOf { (it * it).toDouble() }
        return kotlin.math.sqrt(sum / samples.size).toFloat()
    }
}
