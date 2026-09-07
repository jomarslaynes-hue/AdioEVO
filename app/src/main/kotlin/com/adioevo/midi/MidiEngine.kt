package com.adioevo.midi

import android.content.Context
import android.media.midi.MidiManager
import android.media.midi.MidiDeviceInfo
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class MidiEngine(private val context: Context) {

    private val midiManager = context.getSystemService(Context.MIDI_SERVICE) as? MidiManager

    private val _midiInputDevices = MutableStateFlow<List<MidiDeviceInfo>>(emptyList())
    val midiInputDevices: StateFlow<List<MidiDeviceInfo>> = _midiInputDevices

    private val _midiOutputDevices = MutableStateFlow<List<MidiDeviceInfo>>(emptyList())
    val midiOutputDevices: StateFlow<List<MidiDeviceInfo>> = _midiOutputDevices

    private val _midiActivityStatus = MutableStateFlow<Boolean>(false)
    val midiActivityStatus: StateFlow<Boolean> = _midiActivityStatus

    private val _sustainPedalStatus = MutableStateFlow<Boolean>(false)
    val sustainPedalStatus: StateFlow<Boolean> = _sustainPedalStatus

    private val activeNotes = mutableSetOf<Byte>()
    private val sustainedNotes = mutableSetOf<Byte>()

    init {
        Timber.d("MidiEngine initialized")
        refreshMidiDevices()
    }

    fun refreshMidiDevices() {
        try {
            if (midiManager == null) {
                Timber.w("MIDI Manager not available")
                return
            }

            val allDevices = midiManager.devices
            val inputDevices = mutableListOf<MidiDeviceInfo>()
            val outputDevices = mutableListOf<MidiDeviceInfo>()

            for (device in allDevices) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (device.inputPortCount > 0) {
                        inputDevices.add(device)
                    }
                    if (device.outputPortCount > 0) {
                        outputDevices.add(device)
                    }
                } else {
                    if (device.inputPortCount > 0) {
                        inputDevices.add(device)
                    }
                    if (device.outputPortCount > 0) {
                        outputDevices.add(device)
                    }
                }
            }

            _midiInputDevices.value = inputDevices
            _midiOutputDevices.value = outputDevices

            Timber.d("Found ${inputDevices.size} MIDI inputs, ${outputDevices.size} MIDI outputs")
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing MIDI devices")
        }
    }

    fun handleNoteOn(pitch: Byte, velocity: Byte, channel: Int) {
        Timber.d("MIDI NOTE_ON: pitch=$pitch, velocity=$velocity, channel=$channel")
        activeNotes.add(pitch)
        _midiActivityStatus.value = true
    }

    fun handleNoteOff(pitch: Byte, velocity: Byte, channel: Int) {
        Timber.d("MIDI NOTE_OFF: pitch=$pitch, velocity=$velocity, channel=$channel")

        if (_sustainPedalStatus.value) {
            // Sustain is active, keep note alive
            sustainedNotes.add(pitch)
            Timber.d("Note sustained: $pitch")
        } else {
            activeNotes.remove(pitch)
            sustainedNotes.remove(pitch)
        }
    }

    fun handleControlChange(controller: Int, value: Int, channel: Int) {
        // CC64 is sustain pedal
        if (controller == 64) {
            val sustainActive = value >= 64
            _sustainPedalStatus.value = sustainActive
            Timber.d("Sustain pedal: ${if (sustainActive) "ON" else "OFF"} (value=$value)")

            if (!sustainActive) {
                // Release all sustained notes
                sustainedNotes.forEach { activeNotes.remove(it) }
                sustainedNotes.clear()
                Timber.d("Sustain released, cleared ${sustainedNotes.size} notes")
            }
        }
    }

    fun handlePitchBend(bend: Int, channel: Int) {
        Timber.d("MIDI PITCH_BEND: bend=$bend, channel=$channel")
        _midiActivityStatus.value = true
    }

    fun handleProgramChange(program: Int, channel: Int) {
        Timber.d("MIDI PROGRAM_CHANGE: program=$program, channel=$channel")
        _midiActivityStatus.value = true
    }

    fun allNotesOff() {
        Timber.d("All Notes Off called")
        activeNotes.clear()
        sustainedNotes.clear()
        _sustainPedalStatus.value = false
    }

    fun clearActiveNotes() {
        Timber.d("Clearing active notes")
        activeNotes.clear()
        sustainedNotes.clear()
    }

    fun getActiveNotes(): Set<Byte> = activeNotes.toSet()

    fun getSustainedNotes(): Set<Byte> = sustainedNotes.toSet()
}
