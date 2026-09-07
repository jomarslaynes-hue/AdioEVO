package com.adioevo.midi

import android.content.Context
import timber.log.Timber

/**
 * Updated MidiEngine - Integrated with ViewModel and system lifecycle
 */
class MidiEngine(private val context: Context) {

    private val midiManager = context.getSystemService(Context.MIDI_SERVICE) as? android.media.midi.MidiManager
    private val deviceDiscovery = UsbMidiDeviceDiscovery(context)
    private val sustainPedalHandler = SustainPedalHandler()

    val midiInputDevices = deviceDiscovery.discoveredDevices
    val midiActivityStatus = deviceDiscovery.deviceConnectivityStatus
    val sustainPedalStatus = object : kotlinx.coroutines.flow.StateFlow<Boolean> {
        override val replayCache: List<Boolean> = listOf(sustainPedalHandler.getSustainStatus())
        override suspend fun collect(collector: kotlinx.coroutines.flow.FlowCollector<Boolean>) {
            collector.emit(sustainPedalHandler.getSustainStatus())
        }
    }

    private val activeNotes = mutableSetOf<Byte>()

    init {
        Timber.d("MidiEngine initialized")
        refreshMidiDevices()
    }

    fun refreshMidiDevices() {
        deviceDiscovery.discoverDevices()
    }

    fun handleNoteOn(pitch: Byte, velocity: Byte, channel: Int) {
        activeNotes.add(pitch)
        Timber.d("MIDI NOTE_ON: pitch=$pitch, velocity=$velocity, channel=$channel")
    }

    fun handleNoteOff(pitch: Byte, velocity: Byte, channel: Int) {
        if (sustainPedalHandler.handleNoteOff(pitch.toInt())) {
            Timber.d("Note sustained: $pitch")
        } else {
            activeNotes.remove(pitch)
        }
    }

    fun handleControlChange(controller: Int, value: Int, channel: Int) {
        if (controller == 64) {
            val sustainActive = value >= 64
            sustainPedalHandler.handleSustainPedal(value)
            Timber.d("Sustain pedal: ${if (sustainActive) "ON" else "OFF"}")
        }
    }

    fun handlePitchBend(bend: Int, channel: Int) {
        Timber.d("MIDI PITCH_BEND: bend=$bend, channel=$channel")
    }

    fun handleProgramChange(program: Int, channel: Int) {
        Timber.d("MIDI PROGRAM_CHANGE: program=$program, channel=$channel")
    }

    fun allNotesOff() {
        activeNotes.clear()
        sustainPedalHandler.releaseAllSustainedNotes()
        Timber.d("All notes off")
    }

    fun clearActiveNotes() {
        activeNotes.clear()
        sustainPedalHandler.reset()
    }

    fun getActiveNotes(): Set<Byte> = activeNotes.toSet()
}
