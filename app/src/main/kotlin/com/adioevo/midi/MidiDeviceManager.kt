package com.adioevo.midi

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import timber.log.Timber

class MidiDeviceManager(
    private val context: Context,
    private val midiEngine: MidiEngine
) {

    private val midiManager = context.getSystemService(Context.MIDI_SERVICE) as? MidiManager
    private var currentInputDevice: MidiDevice? = null
    private var currentInputPort: MidiInputPort? = null
    private var midiInputManager: MidiInputManager? = null

    fun openMidiInputDevice(deviceInfo: MidiDeviceInfo, portIndex: Int = 0) {
        try {
            closeMidiInputDevice()

            midiManager?.openDevice(deviceInfo) { device ->
                if (device != null) {
                    currentInputDevice = device
                    val port = device.openInputPort(portIndex)
                    if (port != null) {
                        currentInputPort = port
                        midiInputManager = MidiInputManager(midiEngine)
                        port.connect(midiInputManager)
                        Timber.d("Connected MIDI input device: ${deviceInfo.properties.getString(MidiDeviceInfo.PROPERTY_NAME)}")
                    }
                } else {
                    Timber.e("Failed to open MIDI input device")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error opening MIDI input device")
        }
    }

    fun closeMidiInputDevice() {
        try {
            currentInputPort?.disconnect(midiInputManager)
            currentInputPort?.close()
            currentInputDevice?.close()
            currentInputPort = null
            currentInputDevice = null
            midiInputManager = null
            Timber.d("Closed MIDI input device")
            midiEngine.clearActiveNotes()
        } catch (e: Exception) {
            Timber.e(e, "Error closing MIDI input device")
        }
    }

    fun getCurrentInputDevice(): MidiDevice? = currentInputDevice
}
