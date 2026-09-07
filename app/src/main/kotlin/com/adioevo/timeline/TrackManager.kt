package com.adioevo.timeline

import com.adioevo.data.model.AudioTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * Track Manager - Manages audio tracks, their properties, and state
 */
class TrackManager {

    private val _tracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    val tracks: StateFlow<List<AudioTrack>> = _tracks

    private val _selectedTrackId = MutableStateFlow<Int?>(null)
    val selectedTrackId: StateFlow<Int?> = _selectedTrackId

    private val _masterVolume = MutableStateFlow(1f)
    val masterVolume: StateFlow<Float> = _masterVolume

    private val _masterPan = MutableStateFlow(0f)
    val masterPan: StateFlow<Float> = _masterPan

    init {
        Timber.d("TrackManager initialized")
    }

    /**
     * Create a new audio track
     */
    fun createTrack(name: String, projectId: Int): AudioTrack {
        val trackIndex = _tracks.value.size
        val newTrack = AudioTrack(
            projectId = projectId,
            name = name,
            trackIndex = trackIndex
        )
        _tracks.value = _tracks.value + newTrack
        Timber.d("Created track: $name (index=$trackIndex)")
        return newTrack
    }

    /**
     * Add a track to the list
     */
    fun addTrack(track: AudioTrack) {
        _tracks.value = _tracks.value + track
        Timber.d("Added track: ${track.name}")
    }

    /**
     * Add multiple tracks
     */
    fun addTracks(tracksToAdd: List<AudioTrack>) {
        _tracks.value = _tracks.value + tracksToAdd
        Timber.d("Added ${tracksToAdd.size} tracks")
    }

    /**
     * Remove a track
     */
    fun removeTrack(trackId: Int) {
        _tracks.value = _tracks.value.filterNot { it.id == trackId }
        if (_selectedTrackId.value == trackId) {
            _selectedTrackId.value = null
        }
        Timber.d("Removed track with ID: $trackId")
    }

    /**
     * Select a track
     */
    fun selectTrack(trackId: Int) {
        _selectedTrackId.value = trackId
        Timber.d("Selected track: $trackId")
    }

    /**
     * Get track by ID
     */
    fun getTrack(trackId: Int): AudioTrack? {
        return _tracks.value.find { it.id == trackId }
    }

    /**
     * Update track volume
     */
    fun setTrackVolume(trackId: Int, volume: Float) {
        val updatedTracks = _tracks.value.map { track ->
            if (track.id == trackId) {
                track.copy(volume = volume.coerceIn(0f, 1f))
            } else {
                track
            }
        }
        _tracks.value = updatedTracks
        Timber.d("Track $trackId volume set to: $volume")
    }

    /**
     * Update track pan
     */
    fun setTrackPan(trackId: Int, pan: Float) {
        val updatedTracks = _tracks.value.map { track ->
            if (track.id == trackId) {
                track.copy(pan = pan.coerceIn(-1f, 1f))
            } else {
                track
            }
        }
        _tracks.value = updatedTracks
        Timber.d("Track $trackId pan set to: $pan")
    }

    /**
     * Toggle mute on track
     */
    fun toggleMute(trackId: Int) {
        val updatedTracks = _tracks.value.map { track ->
            if (track.id == trackId) {
                track.copy(isMuted = !track.isMuted)
            } else {
                track
            }
        }
        _tracks.value = updatedTracks
        val track = getTrack(trackId)
        Timber.d("Track $trackId mute: ${track?.isMuted}")
    }

    /**
     * Toggle solo on track
     */
    fun toggleSolo(trackId: Int) {
        val updatedTracks = _tracks.value.map { track ->
            if (track.id == trackId) {
                track.copy(isSolo = !track.isSolo)
            } else {
                track.copy(isSolo = false) // Only one solo at a time
            }
        }
        _tracks.value = updatedTracks
        val track = getTrack(trackId)
        Timber.d("Track $trackId solo: ${track?.isSolo}")
    }

    /**
     * Toggle record arm on track
     */
    fun toggleArm(trackId: Int) {
        val updatedTracks = _tracks.value.map { track ->
            if (track.id == trackId) {
                track.copy(isArmed = !track.isArmed)
            } else {
                track
            }
        }
        _tracks.value = updatedTracks
        val track = getTrack(trackId)
        Timber.d("Track $trackId armed: ${track?.isArmed}")
    }

    /**
     * Set master volume
     */
    fun setMasterVolume(volume: Float) {
        _masterVolume.value = volume.coerceIn(0f, 1f)
        Timber.d("Master volume set to: ${_masterVolume.value}")
    }

    /**
     * Set master pan
     */
    fun setMasterPan(pan: Float) {
        _masterPan.value = pan.coerceIn(-1f, 1f)
        Timber.d("Master pan set to: ${_masterPan.value}")
    }

    /**
     * Get armed tracks (ready for recording)
     */
    fun getArmedTracks(): List<AudioTrack> {
        return _tracks.value.filter { it.isArmed }
    }

    /**
     * Get solo tracks
     */
    fun getSoloTracks(): List<AudioTrack> {
        return _tracks.value.filter { it.isSolo }
    }

    /**
     * Clear all tracks
     */
    fun clearTracks() {
        _tracks.value = emptyList()
        _selectedTrackId.value = null
        Timber.d("Cleared all tracks")
    }
}
