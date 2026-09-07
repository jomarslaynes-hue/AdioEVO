package com.adioevo.midi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * Virtual MIDI Keyboard Controller
 * Manages virtual keyboard note generation, velocity scaling, and octave control
 */
class MidiKeyboardController {

    private val _currentOctave = MutableStateFlow(4)
    val currentOctave: StateFlow<Int> = _currentOctave

    private val _velocity = MutableStateFlow(100)
    val velocity: StateFlow<Int> = _velocity

    private val _activeVirtualNotes = MutableStateFlow<Set<Int>>(emptySet())
    val activeVirtualNotes: StateFlow<Set<Int>> = _activeVirtualNotes

    private val _keyboardActivity = MutableStateFlow(false)
    val keyboardActivity: StateFlow<Boolean> = _keyboardActivity

    fun setOctave(octave: Int) {
        if (octave in 0..8) {
            _currentOctave.value = octave
            Timber.d("Virtual keyboard octave set to: $octave")
        } else {
            Timber.w("Invalid octave: $octave")
        }
    }

    fun setVelocity(velocity: Int) {
        if (velocity in 0..127) {
            _velocity.value = velocity
            Timber.d("Virtual keyboard velocity set to: $velocity")
        } else {
            Timber.w("Invalid velocity: $velocity")
        }
    }

    /**
     * Increase octave (if not at max)
     */
    fun increaseOctave() {
        if (_currentOctave.value < 8) {
            _currentOctave.value += 1
            Timber.d("Octave increased to: ${_currentOctave.value}")
        }
    }

    /**
     * Decrease octave (if not at min)
     */
    fun decreaseOctave() {
        if (_currentOctave.value > 0) {
            _currentOctave.value -= 1
            Timber.d("Octave decreased to: ${_currentOctave.value}")
        }
    }

    /**
     * Increase velocity (if not at max)
     */
    fun increaseVelocity() {
        if (_velocity.value < 127) {
            _velocity.value = (_velocity.value + 5).coerceAtMost(127)
            Timber.d("Velocity increased to: ${_velocity.value}")
        }
    }

    /**
     * Decrease velocity (if not at min)
     */
    fun decreaseVelocity() {
        if (_velocity.value > 1) {
            _velocity.value = (_velocity.value - 5).coerceAtLeast(1)
            Timber.d("Velocity decreased to: ${_velocity.value}")
        }
    }

    /**
     * Generate MIDI note from virtual keyboard key press
     */
    fun generateNoteOn(noteInOctave: Int): Int {
        val pitch = _currentOctave.value * 12 + noteInOctave
        _activeVirtualNotes.value = _activeVirtualNotes.value + pitch
        _keyboardActivity.value = true
        Timber.d("Virtual keyboard NOTE_ON: pitch=$pitch, octave=${_currentOctave.value}, velocity=${_velocity.value}")
        return pitch
    }

    /**
     * Generate MIDI note off from virtual keyboard key release
     */
    fun generateNoteOff(noteInOctave: Int): Int {
        val pitch = _currentOctave.value * 12 + noteInOctave
        _activeVirtualNotes.value = _activeVirtualNotes.value - pitch
        Timber.d("Virtual keyboard NOTE_OFF: pitch=$pitch")
        return pitch
    }

    /**
     * Clear all active virtual notes
     */
    fun clearAllActiveNotes() {
        _activeVirtualNotes.value = emptySet()
        _keyboardActivity.value = false
        Timber.d("Virtual keyboard: cleared all active notes")
    }
}
