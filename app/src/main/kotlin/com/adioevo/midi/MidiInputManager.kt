package com.adioevo.midi

import android.media.midi.MidiDevice
import android.media.midi.MidiInputPort
import android.media.midi.MidiReceiver
import timber.log.Timber

class MidiInputManager(private val midiEngine: MidiEngine) : MidiReceiver() {

    override fun onSend(
        msg: ByteArray?,
        offset: Int,
        count: Int,
        timestamp: Long
    ) {
        if (msg == null || count < 1) return

        val status = msg[offset].toInt() and 0xF0
        val channel = msg[offset].toInt() and 0x0F

        when (status) {
            0x90 -> {
                // NOTE_ON
                if (count >= 3) {
                    val pitch = msg[offset + 1]
                    val velocity = msg[offset + 2]
                    midiEngine.handleNoteOn(pitch, velocity, channel)
                }
            }
            0x80 -> {
                // NOTE_OFF
                if (count >= 3) {
                    val pitch = msg[offset + 1]
                    val velocity = msg[offset + 2]
                    midiEngine.handleNoteOff(pitch, velocity, channel)
                }
            }
            0xB0 -> {
                // CONTROL_CHANGE
                if (count >= 3) {
                    val controller = msg[offset + 1].toInt()
                    val value = msg[offset + 2].toInt()
                    midiEngine.handleControlChange(controller, value, channel)
                }
            }
            0xE0 -> {
                // PITCH_BEND
                if (count >= 3) {
                    val lsb = msg[offset + 1].toInt() and 0x7F
                    val msb = msg[offset + 2].toInt() and 0x7F
                    val bend = (msb shl 7) or lsb
                    midiEngine.handlePitchBend(bend, channel)
                }
            }
            0xC0 -> {
                // PROGRAM_CHANGE
                if (count >= 2) {
                    val program = msg[offset + 1].toInt()
                    midiEngine.handleProgramChange(program, channel)
                }
            }
            else -> {
                Timber.d("Unhandled MIDI status: 0x${status.toString(16).padStart(2, '0')}")
            }
        }
    }
}
