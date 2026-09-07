package com.adioevo.midi

import android.media.midi.MidiReceiver
import timber.log.Timber

/**
 * Enhanced MIDI message processor with detailed logging and state tracking.
 * Handles all MIDI message types and maintains activity status.
 */
class EnhancedMidiInputHandler(private val midiEngine: MidiEngine) : MidiReceiver() {

    private var lastActivityTime = System.currentTimeMillis()
    private var messageCount = 0

    override fun onSend(
        msg: ByteArray?,
        offset: Int,
        count: Int,
        timestamp: Long
    ) {
        if (msg == null || count < 1) return

        lastActivityTime = System.currentTimeMillis()
        messageCount++

        val statusByte = msg[offset].toInt()
        val status = statusByte and 0xF0
        val channel = (statusByte and 0x0F) + 1  // MIDI channels are 1-16 in UI

        try {
            when (status) {
                0x90 -> handleNoteOn(msg, offset, count, channel)
                0x80 -> handleNoteOff(msg, offset, count, channel)
                0xB0 -> handleControlChange(msg, offset, count, channel)
                0xE0 -> handlePitchBend(msg, offset, count, channel)
                0xC0 -> handleProgramChange(msg, offset, count, channel)
                0xD0 -> handleChannelPressure(msg, offset, count, channel)
                0xA0 -> handlePolyphonicKeyPressure(msg, offset, count, channel)
                0xF0 -> handleSystemMessage(msg, offset, count)
                else -> Timber.w("Unknown MIDI status: 0x${status.toString(16).padStart(2, '0')}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error processing MIDI message")
        }
    }

    private fun handleNoteOn(
        msg: ByteArray,
        offset: Int,
        count: Int,
        channel: Int
    ) {
        if (count < 3) return
        val pitch = msg[offset + 1].toInt() and 0x7F
        val velocity = msg[offset + 2].toInt() and 0x7F

        if (velocity > 0) {
            midiEngine.handleNoteOn(pitch.toByte(), velocity.toByte(), channel)
            Timber.d(
                "MIDI NOTE_ON: Pitch=$pitch, Velocity=$velocity, Channel=$channel [#$messageCount]"
            )
        } else {
            // Velocity 0 is treated as NOTE_OFF
            midiEngine.handleNoteOff(pitch.toByte(), 0, channel)
            Timber.d("MIDI NOTE_ON with velocity=0 (NOTE_OFF): Pitch=$pitch, Channel=$channel")
        }
    }

    private fun handleNoteOff(
        msg: ByteArray,
        offset: Int,
        count: Int,
        channel: Int
    ) {
        if (count < 3) return
        val pitch = msg[offset + 1].toInt() and 0x7F
        val velocity = msg[offset + 2].toInt() and 0x7F

        midiEngine.handleNoteOff(pitch.toByte(), velocity.toByte(), channel)
        Timber.d("MIDI NOTE_OFF: Pitch=$pitch, Velocity=$velocity, Channel=$channel [#$messageCount]")
    }

    private fun handleControlChange(
        msg: ByteArray,
        offset: Int,
        count: Int,
        channel: Int
    ) {
        if (count < 3) return
        val controller = msg[offset + 1].toInt() and 0x7F
        val value = msg[offset + 2].toInt() and 0x7F

        midiEngine.handleControlChange(controller, value, channel)

        // Special logging for sustain pedal
        if (controller == 64) {
            Timber.d(
                "MIDI SUSTAIN PEDAL: ${if (value >= 64) "ON" else "OFF"} (CC64=$value), Channel=$channel"
            )
        } else {
            Timber.d("MIDI CONTROL_CHANGE: Controller=$controller, Value=$value, Channel=$channel")
        }
    }

    private fun handlePitchBend(
        msg: ByteArray,
        offset: Int,
        count: Int,
        channel: Int
    ) {
        if (count < 3) return
        val lsb = msg[offset + 1].toInt() and 0x7F
        val msb = msg[offset + 2].toInt() and 0x7F
        val bend = (msb shl 7) or lsb
        val centerValue = 8192
        val semitones = (bend - centerValue) / 8192f

        midiEngine.handlePitchBend(bend, channel)
        Timber.d(
            "MIDI PITCH_BEND: Value=$bend (${String.format("%.2f", semitones)} semitones), Channel=$channel"
        )
    }

    private fun handleProgramChange(
        msg: ByteArray,
        offset: Int,
        count: Int,
        channel: Int
    ) {
        if (count < 2) return
        val program = msg[offset + 1].toInt() and 0x7F

        midiEngine.handleProgramChange(program, channel)
        Timber.d("MIDI PROGRAM_CHANGE: Program=$program, Channel=$channel")
    }

    private fun handleChannelPressure(
        msg: ByteArray,
        offset: Int,
        count: Int,
        channel: Int
    ) {
        if (count < 2) return
        val pressure = msg[offset + 1].toInt() and 0x7F
        Timber.d("MIDI CHANNEL_PRESSURE: Pressure=$pressure, Channel=$channel")
    }

    private fun handlePolyphonicKeyPressure(
        msg: ByteArray,
        offset: Int,
        count: Int,
        channel: Int
    ) {
        if (count < 3) return
        val pitch = msg[offset + 1].toInt() and 0x7F
        val pressure = msg[offset + 2].toInt() and 0x7F
        Timber.d("MIDI POLYPHONIC_KEY_PRESSURE: Pitch=$pitch, Pressure=$pressure, Channel=$channel")
    }

    private fun handleSystemMessage(
        msg: ByteArray,
        offset: Int,
        count: Int
    ) {
        val statusByte = msg[offset].toInt()
        when (statusByte) {
            0xF0 -> Timber.d("MIDI SYSEX_START")
            0xF8 -> Timber.d("MIDI TIMING_CLOCK")
            0xFA -> Timber.d("MIDI START")
            0xFB -> Timber.d("MIDI CONTINUE")
            0xFC -> Timber.d("MIDI STOP")
            0xFE -> Timber.d("MIDI ACTIVE_SENSING")
            0xFF -> Timber.d("MIDI SYSTEM_RESET")
            else -> Timber.d("MIDI SYSTEM_MESSAGE: 0x${statusByte.toString(16).padStart(2, '0')}")
        }
    }

    fun getActivityStats(): String {
        val uptime = System.currentTimeMillis() - lastActivityTime
        return "Messages: $messageCount | Last Activity: ${uptime}ms ago"
    }
}
