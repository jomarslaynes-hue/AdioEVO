package com.adioevo.timeline

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * Timeline Manager - Manages playback position, grid/snap settings, and loop regions
 */
class TimelineManager(
    val sampleRate: Int = 44100,
    val bpm: Float = 120f,
    val timeSignatureNumerator: Int = 4,
    val timeSignatureDenominator: Int = 4
) {

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration

    private val _playheadPixelPosition = MutableStateFlow(0f)
    val playheadPixelPosition: StateFlow<Float> = _playheadPixelPosition

    private val _isLooping = MutableStateFlow(false)
    val isLooping: StateFlow<Boolean> = _isLooping

    private val _loopStart = MutableStateFlow(0L)
    val loopStart: StateFlow<Long> = _loopStart

    private val _loopEnd = MutableStateFlow(0L)
    val loopEnd: StateFlow<Long> = _loopEnd

    private val _snapEnabled = MutableStateFlow(true)
    val snapEnabled: StateFlow<Boolean> = _snapEnabled

    private val _snapGridType = MutableStateFlow(SnapGridType.SIXTEENTH)
    val snapGridType: StateFlow<SnapGridType> = _snapGridType

    private val _zoomLevel = MutableStateFlow(1f)
    val zoomLevel: StateFlow<Float> = _zoomLevel

    private val _horizontalScroll = MutableStateFlow(0f)
    val horizontalScroll: StateFlow<Float> = _horizontalScroll

    init {
        Timber.d(
            "TimelineManager initialized: BPM=$bpm, " +
                    "TimeSignature=$timeSignatureNumerator/$timeSignatureDenominator, " +
                    "SampleRate=$sampleRate"
        )
    }

    /**
     * Calculate samples per beat based on BPM and sample rate
     */
    fun getSamplesPerBeat(): Long {
        // 60 seconds per minute, BPM beats per minute
        val secondsPerBeat = 60f / bpm
        return (secondsPerBeat * sampleRate).toLong()
    }

    /**
     * Calculate grid interval in samples based on snap grid type
     */
    fun getGridIntervalSamples(): Long {
        val samplesPerBeat = getSamplesPerBeat()
        return when (_snapGridType.value) {
            SnapGridType.WHOLE -> samplesPerBeat * 4
            SnapGridType.HALF -> samplesPerBeat * 2
            SnapGridType.QUARTER -> samplesPerBeat
            SnapGridType.EIGHTH -> samplesPerBeat / 2
            SnapGridType.SIXTEENTH -> samplesPerBeat / 4
            SnapGridType.THIRTYSECOND -> samplesPerBeat / 8
            SnapGridType.TRIPLET -> samplesPerBeat / 3
        }
    }

    /**
     * Snap a position to the nearest grid line
     */
    fun snapPosition(position: Long): Long {
        if (!_snapEnabled.value) return position
        val gridInterval = getGridIntervalSamples()
        return (position / gridInterval) * gridInterval
    }

    /**
     * Update current playback position
     */
    fun setPosition(position: Long) {
        _currentPosition.value = position
        updatePlayheadPosition()

        // Check for loop end and wrap if needed
        if (_isLooping.value && position >= _loopEnd.value) {
            _currentPosition.value = _loopStart.value
            updatePlayheadPosition()
            Timber.d("Loop wrap: position=${_loopStart.value}")
        }
    }

    /**
     * Move playback position forward by delta samples
     */
    fun advancePosition(deltaSamples: Long) {
        val newPosition = _currentPosition.value + deltaSamples
        setPosition(newPosition)
    }

    /**
     * Set total duration in samples
     */
    fun setTotalDuration(duration: Long) {
        _totalDuration.value = duration
        Timber.d("Total duration set to: $duration samples (${samplesToSeconds(duration)}s)")
    }

    /**
     * Set loop region
     */
    fun setLoopRegion(start: Long, end: Long) {
        _loopStart.value = start
        _loopEnd.value = end
        Timber.d(
            "Loop region set: ${samplesToSeconds(start)}s - ${samplesToSeconds(end)}s"
        )
    }

    /**
     * Toggle looping mode
     */
    fun toggleLooping() {
        _isLooping.value = !_isLooping.value
        Timber.d("Looping: ${if (_isLooping.value) "ON" else "OFF"}")
    }

    /**
     * Enable/disable snap to grid
     */
    fun setSnapEnabled(enabled: Boolean) {
        _snapEnabled.value = enabled
        Timber.d("Snap to grid: ${if (enabled) "ON" else "OFF"}")
    }

    /**
     * Set snap grid type
     */
    fun setSnapGridType(gridType: SnapGridType) {
        _snapGridType.value = gridType
        Timber.d("Snap grid type set to: ${gridType.name}")
    }

    /**
     * Set zoom level (1.0 = default, > 1.0 = zoomed in, < 1.0 = zoomed out)
     */
    fun setZoomLevel(zoom: Float) {
        _zoomLevel.value = zoom.coerceIn(0.1f, 4f)
        Timber.d("Zoom level: ${_zoomLevel.value}x")
    }

    /**
     * Zoom in
     */
    fun zoomIn() {
        setZoomLevel(_zoomLevel.value * 1.2f)
    }

    /**
     * Zoom out
     */
    fun zoomOut() {
        setZoomLevel(_zoomLevel.value / 1.2f)
    }

    /**
     * Reset zoom to default
     */
    fun resetZoom() {
        setZoomLevel(1f)
    }

    /**
     * Set horizontal scroll position
     */
    fun setHorizontalScroll(scroll: Float) {
        _horizontalScroll.value = scroll.coerceAtLeast(0f)
    }

    /**
     * Update playhead pixel position based on current position and zoom
     */
    private fun updatePlayheadPosition() {
        // Assuming 100 pixels per second at 1.0x zoom
        val pixelsPerSample = (100f / sampleRate) * _zoomLevel.value
        _playheadPixelPosition.value = _currentPosition.value * pixelsPerSample
    }

    /**
     * Convert samples to seconds
     */
    fun samplesToSeconds(samples: Long): Float {
        return samples.toFloat() / sampleRate
    }

    /**
     * Convert seconds to samples
     */
    fun secondsToSamples(seconds: Float): Long {
        return (seconds * sampleRate).toLong()
    }

    /**
     * Get beat from position
     */
    fun samplesToBeats(samples: Long): Float {
        return samples.toFloat() / getSamplesPerBeat().toFloat()
    }

    /**
     * Get time code string in MM:SS:MS format
     */
    fun getTimeCodeString(samples: Long = _currentPosition.value): String {
        val totalSeconds = samplesToSeconds(samples).toLong()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val milliseconds = (samplesToSeconds(samples) % 1f * 1000).toInt()
        return String.format("%02d:%02d:%03d", minutes, seconds, milliseconds)
    }
}

enum class SnapGridType {
    WHOLE,
    HALF,
    QUARTER,
    EIGHTH,
    SIXTEENTH,
    THIRTYSECOND,
    TRIPLET
}
