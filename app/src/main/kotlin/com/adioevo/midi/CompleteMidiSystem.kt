package com.adioevo.midi

import android.content.Context
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

/**
 * Complete MIDI system combining device discovery, input handling, and sustain pedal logic.
 */
class CompleteMidiSystem(private val context: Context) {

    private val deviceDiscovery = UsbMidiDeviceDiscovery(context)
    private val midiEngine = MidiEngine(context)
    private val sustainPedalHandler = SustainPedalHandler()
    private var midiInputHandler: EnhancedMidiInputHandler? = null
    private var midiInputPort: MidiInputPort? = null

    private val _selectedInputDevice = MutableStateFlow<MidiDeviceInfo?>(null)
    val selectedInputDevice: StateFlow<MidiDeviceInfo?> = _selectedInputDevice

    private val _selectedMidiChannel = MutableStateFlow(0)  // 0-15 (displayed as 1-16)
    val selectedMidiChannel: StateFlow<Int> = _selectedMidiChannel

    private val _connectionStatus = MutableStateFlow("Disconnected")
    val connectionStatus: StateFlow<String> = _connectionStatus

    val discoveredDevices = deviceDiscovery.discoveredDevices
    val deviceConnectivityStatus = deviceDiscovery.deviceConnectivityStatus
    val midiActivityStatus = midiEngine.midiActivityStatus
    val sustainPedalStatus = midiEngine.sustainPedalStatus

    init {
        Timber.d("CompleteMidiSystem initialized")
    }

    /**
     * Discover all available MIDI devices.
     */
    fun discoverMidiDevices() {
        Timber.d("Starting MIDI device discovery")
        deviceDiscovery.discoverDevices()
    }

    /**
     * Refresh MIDI device list (useful after hot-plugging devices).
     */
    fun refreshDevices() {
        Timber.d("Refreshing MIDI devices")
        discoverMidiDevices()
    }

    /**
     * Connect to a specific MIDI input device.
     */
    fun connectToDevice(deviceInfo: MidiDeviceInfo, portIndex: Int = 0) {
        try {
            Timber.d("Attempting to connect to MIDI device: ${deviceInfo.id}")

            // Close any existing connection
            disconnectDevice()

            deviceDiscovery.openDevice(deviceInfo)
            _selectedInputDevice.value = deviceInfo
            _connectionStatus.value = "Connecting..."

            // Open input port and connect handler
            val midiManager = context.getSystemService(Context.MIDI_SERVICE) as? android.media.midi.MidiManager
            if (midiManager != null) {
                midiManager.openDevice(deviceInfo) { device ->
                    if (device != null) {
                        try {
                            val port = device.openInputPort(portIndex)
                            if (port != null) {
                                midiInputPort = port
                                midiInputHandler = EnhancedMidiInputHandler(midiEngine)
                                port.connect(midiInputHandler)
                                _connectionStatus.value = "Connected"
                                Timber.d("Successfully connected to MIDI device")
                            } else {
                                _connectionStatus.value = "Failed to open input port"
                                Timber.e("Failed to open input port")
                            }
                        } catch (e: Exception) {
                            _connectionStatus.value = "Connection error: ${e.message}"
                            Timber.e(e, "Error opening input port")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            _connectionStatus.value = "Connection failed: ${e.message}"
            Timber.e(e, "Error connecting to MIDI device")
        }
    }

    /**
     * Disconnect from current MIDI device.
     */
    fun disconnectDevice() {
        try {
            midiInputPort?.disconnect(midiInputHandler)
            midiInputPort?.close()
            midiInputPort = null
            midiInputHandler = null
            midiEngine.clearActiveNotes()
            sustainPedalHandler.reset()
            _selectedInputDevice.value = null
            _connectionStatus.value = "Disconnected"
            Timber.d("Disconnected from MIDI device")
        } catch (e: Exception) {
            Timber.e(e, "Error disconnecting from MIDI device")
        }
    }

    /**
     * Set the MIDI channel to listen on (0-15, displayed as 1-16).
     */
    fun setMidiChannel(channel: Int) {
        if (channel in 0..15) {
            _selectedMidiChannel.value = channel
            Timber.d("MIDI channel set to: ${channel + 1}")
        } else {
            Timber.w("Invalid MIDI channel: $channel")
        }
    }

    /**
     * Get all active MIDI notes currently playing.
     */
    fun getActiveNotes(): Set<Byte> = midiEngine.getActiveNotes()

    /**
     * Get all sustained MIDI notes.
     */
    fun getSustainedNotes(): Set<Int> = sustainPedalHandler.getSustainedNotes()

    /**
     * Send all notes off message to prevent stuck notes.
     */
    fun allNotesOff() {
        Timber.d("All Notes Off called")
        midiEngine.allNotesOff()
        sustainPedalHandler.releaseAllSustainedNotes()
    }

    /**
     * Stop playback and cleanup.
     */
    fun stop() {
        allNotesOff()
        Timber.d("MIDI system stopped")
    }

    /**
     * Shutdown the entire MIDI system.
     */
    fun shutdown() {
        disconnectDevice()
        deviceDiscovery.closeAllDevices()
        Timber.d("MIDI system shutdown complete")
    }

    /**
     * Get MIDI activity statistics.
     */
    fun getMidiActivityStats(): String {
        return midiInputHandler?.getActivityStats() ?: "No device connected"
    }
}
