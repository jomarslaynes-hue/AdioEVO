package com.adioevo.audio

import timber.log.Timber
import kotlin.math.pow

/**
 * Audio Mixer - Handles multi-track mixing with volume, pan, and mute/solo
 */
class AudioMixer(val trackCount: Int = 8) {

    private data class TrackState(
        val id: Int,
        var volume: Float = 1f,
        var pan: Float = 0f, // -1 = left, 0 = center, 1 = right
        var isMuted: Boolean = false,
        var isSolo: Boolean = false
    )

    private val trackStates = mutableMapOf<Int, TrackState>()
    private var masterVolume = 1f
    private var masterPan = 0f

    init {
        Timber.d("AudioMixer initialized with $trackCount tracks")
    }

    /**
     * Register a track in the mixer
     */
    fun registerTrack(trackId: Int) {
        trackStates[trackId] = TrackState(trackId)
        Timber.d("Track registered: $trackId")
    }

    /**
     * Unregister a track from the mixer
     */
    fun unregisterTrack(trackId: Int) {
        trackStates.remove(trackId)
        Timber.d("Track unregistered: $trackId")
    }

    /**
     * Set track volume (0.0 - 1.0)
     */
    fun setTrackVolume(trackId: Int, volume: Float) {
        trackStates[trackId]?.volume = volume.coerceIn(0f, 1f)
        Timber.d("Track $trackId volume: ${volume.coerceIn(0f, 1f)}")
    }

    /**
     * Set track pan (-1.0 = left, 0.0 = center, 1.0 = right)
     */
    fun setTrackPan(trackId: Int, pan: Float) {
        trackStates[trackId]?.pan = pan.coerceIn(-1f, 1f)
        Timber.d("Track $trackId pan: ${pan.coerceIn(-1f, 1f)}")
    }

    /**
     * Set track mute state
     */
    fun setTrackMute(trackId: Int, muted: Boolean) {
        trackStates[trackId]?.isMuted = muted
        Timber.d("Track $trackId mute: $muted")
    }

    /**
     * Set track solo state (only one track can be solo at a time)
     */
    fun setTrackSolo(trackId: Int, solo: Boolean) {
        if (solo) {
            trackStates.values.forEach { it.isSolo = false }
            trackStates[trackId]?.isSolo = true
        } else {
            trackStates[trackId]?.isSolo = false
        }
        Timber.d("Track $trackId solo: $solo")
    }

    /**
     * Set master volume (0.0 - 1.0)
     */
    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0f, 1f)
        Timber.d("Master volume: $masterVolume")
    }

    /**
     * Set master pan
     */
    fun setMasterPan(pan: Float) {
        masterPan = pan.coerceIn(-1f, 1f)
        Timber.d("Master pan: $masterPan")
    }

    /**
     * Mix audio from multiple tracks
     * Returns stereo output (left, right channels)
     */
    fun mixTracks(
        trackAudio: Map<Int, FloatArray>,
        sampleCount: Int
    ): Pair<FloatArray, FloatArray> {
        val leftChannel = FloatArray(sampleCount)
        val rightChannel = FloatArray(sampleCount)

        // Check if any track is solo
        val hasSolo = trackStates.values.any { it.isSolo }

        trackAudio.forEach { (trackId, audioData) ->
            val state = trackStates[trackId] ?: return@forEach

            // Skip if muted or if another track is solo
            if (state.isMuted) return@forEach
            if (hasSolo && !state.isSolo) return@forEach

            // Process audio samples
            for (i in 0 until minOf(sampleCount, audioData.size) - 1) {
                val sample = audioData[i] * state.volume

                // Apply pan
                val leftGain = if (state.pan <= 0f) {
                    1f
                } else {
                    1f - state.pan
                }
                val rightGain = if (state.pan >= 0f) {
                    1f
                } else {
                    1f + state.pan
                }

                leftChannel[i] += sample * leftGain
                rightChannel[i] += sample * rightGain
            }
        }

        // Apply master volume and pan
        for (i in 0 until sampleCount) {
            val masterLeftGain = if (masterPan <= 0f) {
                1f
            } else {
                1f - masterPan
            }
            val masterRightGain = if (masterPan >= 0f) {
                1f
            } else {
                1f + masterPan
            }

            leftChannel[i] = (leftChannel[i] * masterVolume * masterLeftGain).coerceIn(-1f, 1f)
            rightChannel[i] = (rightChannel[i] * masterVolume * masterRightGain).coerceIn(-1f, 1f)
        }

        return Pair(leftChannel, rightChannel)
    }

    /**
     * Get current mix level (for metering)
     */
    fun getMixLevel(): Float {
        var maxLevel = 0f
        trackStates.values.forEach { state ->
            if (!state.isMuted) {
                maxLevel = maxOf(maxLevel, state.volume)
            }
        }
        return maxLevel * masterVolume
    }

    /**
     * Get all track states (for debugging)
     */
    fun getTrackStates(): Map<Int, TrackState> = trackStates.toMap()
}
