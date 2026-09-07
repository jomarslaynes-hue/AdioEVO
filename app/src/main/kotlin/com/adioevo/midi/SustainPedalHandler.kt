package com.adioevo.midi

import timber.log.Timber

/**
 * Sustain Pedal Handler - Implements MIDI CC64 sustain pedal functionality.
 *
 * CC64 >= 64: Sustain ON - NOTE_OFF events keep notes active
 * CC64 < 64: Sustain OFF - Release sustained notes
 *
 * This ensures proper sustain behavior for MIDI keyboards with pedals.
 */
class SustainPedalHandler {

    private var isSustainActive = false
    private val sustainedNotes = mutableSetOf<Int>()
    private val noteOnTimes = mutableMapOf<Int, Long>()

    /**
     * Called when sustain pedal CC64 message is received.
     * Returns list of notes to release if sustain was just turned off.
     */
    fun handleSustainPedal(value: Int): List<Int> {
        val wasActive = isSustainActive
        isSustainActive = value >= 64

        Timber.d("Sustain Pedal: ${if (isSustainActive) "ON" else "OFF"} (CC64=$value)")

        // If sustain just turned OFF, release sustained notes
        if (wasActive && !isSustainActive) {
            val notesToRelease = sustainedNotes.toList()
            sustainedNotes.clear()
            Timber.d("Sustain OFF - Releasing ${notesToRelease.size} sustained notes: $notesToRelease")
            return notesToRelease
        }

        return emptyList()
    }

    /**
     * Called when NOTE_OFF is received.
     * If sustain is active, sustains the note instead of releasing it.
     */
    fun handleNoteOff(pitch: Int): Boolean {
        if (isSustainActive) {
            sustainedNotes.add(pitch)
            Timber.d("Note $pitch sustained (sustain active)")
            return true  // Note was sustained, not released
        }
        sustainedNotes.remove(pitch)
        return false  // Note was released normally
    }

    /**
     * Called when NOTE_ON is received.
     * Removes the note from sustained set (new note starts).
     */
    fun handleNoteOn(pitch: Int) {
        sustainedNotes.remove(pitch)
        noteOnTimes[pitch] = System.currentTimeMillis()
    }

    /**
     * Force release all sustained notes.
     * Called when playback stops, MIDI device disconnects, etc.
     */
    fun releaseAllSustainedNotes(): List<Int> {
        val notesToRelease = sustainedNotes.toList()
        sustainedNotes.clear()
        isSustainActive = false
        Timber.d("Force-released ${notesToRelease.size} sustained notes")
        return notesToRelease
    }

    fun getSustainStatus(): Boolean = isSustainActive

    fun getSustainedNotes(): Set<Int> = sustainedNotes.toSet()

    fun reset() {
        sustainedNotes.clear()
        noteOnTimes.clear()
        isSustainActive = false
        Timber.d("Sustain pedal handler reset")
    }
}
