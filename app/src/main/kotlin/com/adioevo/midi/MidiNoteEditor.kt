package com.adioevo.midi

import com.adioevo.data.model.MidiNote
import timber.log.Timber

/**
 * MIDI Note Editor - Manages note add, delete, move, resize operations
 */
class MidiNoteEditor {

    private val notes = mutableListOf<MidiNote>()
    private var nextNoteId = 0

    /**
     * Add a new MIDI note
     */
    fun addNote(
        pitch: Int,
        velocity: Int,
        startTime: Long,
        duration: Long
    ): MidiNote {
        if (pitch !in 0..127) {
            Timber.w("Invalid pitch: $pitch")
            return MidiNote(pitch = 60, velocity = 100, startTime = 0, duration = 0)
        }
        if (velocity !in 1..127) {
            Timber.w("Invalid velocity: $velocity")
            return MidiNote(pitch = pitch, velocity = 100, startTime = startTime, duration = duration)
        }
        if (duration <= 0) {
            Timber.w("Invalid duration: $duration")
            return MidiNote(pitch = pitch, velocity = velocity, startTime = startTime, duration = 1)
        }

        val note = MidiNote(
            pitch = pitch,
            velocity = velocity,
            startTime = startTime,
            duration = duration
        )
        notes.add(note)
        Timber.d("Added MIDI note: pitch=$pitch, velocity=$velocity, startTime=$startTime, duration=$duration")
        return note
    }

    /**
     * Delete a MIDI note
     */
    fun deleteNote(note: MidiNote): Boolean {
        val removed = notes.remove(note)
        if (removed) {
            Timber.d("Deleted MIDI note: pitch=${note.pitch}, startTime=${note.startTime}")
        }
        return removed
    }

    /**
     * Move a note to a new start time
     */
    fun moveNote(note: MidiNote, newStartTime: Long): MidiNote {
        val index = notes.indexOf(note)
        if (index < 0) {
            Timber.w("Note not found")
            return note
        }

        val movedNote = note.copy(startTime = newStartTime)
        notes[index] = movedNote
        Timber.d("Moved note from ${note.startTime} to $newStartTime")
        return movedNote
    }

    /**
     * Change note pitch
     */
    fun changePitch(note: MidiNote, newPitch: Int): MidiNote {
        if (newPitch !in 0..127) {
            Timber.w("Invalid pitch: $newPitch")
            return note
        }

        val index = notes.indexOf(note)
        if (index < 0) {
            Timber.w("Note not found")
            return note
        }

        val updatedNote = note.copy(pitch = newPitch)
        notes[index] = updatedNote
        Timber.d("Changed pitch from ${note.pitch} to $newPitch")
        return updatedNote
    }

    /**
     * Resize a note (change duration)
     */
    fun resizeNote(note: MidiNote, newDuration: Long): MidiNote {
        if (newDuration <= 0) {
            Timber.w("Invalid duration: $newDuration")
            return note
        }

        val index = notes.indexOf(note)
        if (index < 0) {
            Timber.w("Note not found")
            return note
        }

        val resizedNote = note.copy(duration = newDuration)
        notes[index] = resizedNote
        Timber.d("Resized note from ${note.duration} to $newDuration samples")
        return resizedNote
    }

    /**
     * Change note velocity
     */
    fun changeVelocity(note: MidiNote, newVelocity: Int): MidiNote {
        if (newVelocity !in 1..127) {
            Timber.w("Invalid velocity: $newVelocity")
            return note
        }

        val index = notes.indexOf(note)
        if (index < 0) {
            Timber.w("Note not found")
            return note
        }

        val updatedNote = note.copy(velocity = newVelocity)
        notes[index] = updatedNote
        Timber.d("Changed velocity from ${note.velocity} to $newVelocity")
        return updatedNote
    }

    /**
     * Get all notes
     */
    fun getNotes(): List<MidiNote> = notes.toList()

    /**
     * Get notes in a time range
     */
    fun getNotesInRange(startTime: Long, endTime: Long): List<MidiNote> {
        return notes.filter { it.startTime >= startTime && it.startTime + it.duration <= endTime }
    }

    /**
     * Clear all notes
     */
    fun clearAll() {
        notes.clear()
        Timber.d("Cleared all MIDI notes")
    }

    /**
     * Quantize notes to grid
     */
    fun quantizeNotes(gridInterval: Long) {
        notes.forEachIndexed { index, note ->
            val quantizedStart = (note.startTime / gridInterval) * gridInterval
            val quantizedDuration = ((note.duration + gridInterval / 2) / gridInterval) * gridInterval
            notes[index] = note.copy(
                startTime = quantizedStart,
                duration = quantizedDuration.coerceAtLeast(gridInterval)
            )
        }
        Timber.d("Quantized ${notes.size} notes to grid interval: $gridInterval")
    }

    /**
     * Get note count
     */
    fun getNoteCount(): Int = notes.size
}
