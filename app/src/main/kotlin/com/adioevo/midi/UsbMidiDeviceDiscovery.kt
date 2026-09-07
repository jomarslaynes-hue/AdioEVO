package com.adioevo.midi

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class UsbMidiDeviceDiscovery(private val context: Context) {

    private val midiManager = context.getSystemService(Context.MIDI_SERVICE) as? MidiManager

    private val _discoveredDevices = MutableStateFlow<List<MidiDeviceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<MidiDeviceInfo>> = _discoveredDevices

    private val _connectedDevices = MutableStateFlow<Map<Int, MidiDevice>>(emptyMap())
    val connectedDevices: StateFlow<Map<Int, MidiDevice>> = _connectedDevices

    private val _deviceConnectivityStatus = MutableStateFlow<String>("Searching for MIDI devices...")
    val deviceConnectivityStatus: StateFlow<String> = _deviceConnectivityStatus

    init {
        Timber.d("UsbMidiDeviceDiscovery initialized")
    }

    /**
     * Performs full device discovery and enumeration.
     * Handles both hardware USB devices and virtual synthesizers.
     */
    fun discoverDevices() {
        try {
            if (midiManager == null) {
                _deviceConnectivityStatus.value = "MIDI not supported on this device"
                Timber.w("MIDI Manager not available")
                return
            }

            val allDevices = midiManager.devices
            if (allDevices.isEmpty()) {
                _deviceConnectivityStatus.value = "No MIDI devices found"
                _discoveredDevices.value = emptyList()
                Timber.d("No MIDI devices discovered")
                return
            }

            _discoveredDevices.value = allDevices.toList()
            _deviceConnectivityStatus.value = "Found ${allDevices.size} MIDI device(s)"

            Timber.d("Discovered ${allDevices.size} MIDI devices")
            allDevices.forEachIndexed { index, device ->
                logDeviceInfo(device, index)
            }
        } catch (e: Exception) {
            _deviceConnectivityStatus.value = "Error discovering devices: ${e.message}"
            Timber.e(e, "Error discovering MIDI devices")
        }
    }

    /**
     * Attempts to reconnect to a previously connected device.
     * Handles device reconnection after USB disconnect/reconnect.
     */
    fun reconnectDevice(deviceInfo: MidiDeviceInfo) {
        try {
            if (midiManager == null) {
                Timber.w("Cannot reconnect - MIDI Manager not available")
                return
            }

            midiManager.openDevice(deviceInfo) { device ->
                if (device != null) {
                    val deviceId = deviceInfo.id
                    val currentConnected = _connectedDevices.value.toMutableMap()
                    currentConnected[deviceId] = device
                    _connectedDevices.value = currentConnected
                    _deviceConnectivityStatus.value = "Reconnected: ${getDeviceName(deviceInfo)}"
                    Timber.d("Reconnected to device: ${getDeviceName(deviceInfo)}")
                } else {
                    _deviceConnectivityStatus.value = "Failed to reconnect device"
                    Timber.e("Failed to reconnect to device")
                }
            }
        } catch (e: Exception) {
            _deviceConnectivityStatus.value = "Reconnection error: ${e.message}"
            Timber.e(e, "Error reconnecting MIDI device")
        }
    }

    /**
     * Opens a MIDI device for communication.
     * Handles both input and output ports.
     */
    fun openDevice(deviceInfo: MidiDeviceInfo): Unit = try {
        if (midiManager == null) {
            Timber.w("Cannot open device - MIDI Manager not available")
            return@try
        }

        midiManager.openDevice(deviceInfo) { device ->
            if (device != null) {
                val deviceId = deviceInfo.id
                val currentConnected = _connectedDevices.value.toMutableMap()
                currentConnected[deviceId] = device
                _connectedDevices.value = currentConnected
                _deviceConnectivityStatus.value = "Connected: ${getDeviceName(deviceInfo)}"
                Timber.d("Opened MIDI device: ${getDeviceName(deviceInfo)}")
            } else {
                _deviceConnectivityStatus.value = "Failed to open device"
                Timber.e("Failed to open MIDI device")
            }
        }
    } catch (e: Exception) {
        _deviceConnectivityStatus.value = "Open device error: ${e.message}"
        Timber.e(e, "Error opening MIDI device")
    }

    /**
     * Closes a previously opened MIDI device.
     * Safely releases resources and cleans up.
     */
    fun closeDevice(deviceId: Int) {
        try {
            val device = _connectedDevices.value[deviceId]
            if (device != null) {
                device.close()
                val currentConnected = _connectedDevices.value.toMutableMap()
                currentConnected.remove(deviceId)
                _connectedDevices.value = currentConnected
                Timber.d("Closed MIDI device with ID: $deviceId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error closing MIDI device")
        }
    }

    /**
     * Closes all opened MIDI devices.
     */
    fun closeAllDevices() {
        try {
            _connectedDevices.value.forEach { (_, device) ->
                device.close()
            }
            _connectedDevices.value = emptyMap()
            _deviceConnectivityStatus.value = "All devices closed"
            Timber.d("Closed all MIDI devices")
        } catch (e: Exception) {
            Timber.e(e, "Error closing all MIDI devices")
        }
    }

    /**
     * Gets a human-readable name for a MIDI device.
     */
    private fun getDeviceName(deviceInfo: MidiDeviceInfo): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            deviceInfo.properties.getString(MidiDeviceInfo.PROPERTY_NAME) ?: "Unknown Device"
        } else {
            "MIDI Device ${deviceInfo.id}"
        }
    }

    /**
     * Logs detailed information about a discovered MIDI device.
     */
    private fun logDeviceInfo(deviceInfo: MidiDeviceInfo, index: Int) {
        val name = getDeviceName(deviceInfo)
        val inputPorts = deviceInfo.inputPortCount
        val outputPorts = deviceInfo.outputPortCount
        Timber.d(
            "Device [$index]: $name | "
                    + "ID=${deviceInfo.id} | "
                    + "Inputs=$inputPorts | "
                    + "Outputs=$outputPorts"
        )
    }
}
