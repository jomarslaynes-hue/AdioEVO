package com.adioevo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontSize
import androidx.compose.ui.unit.dp
import com.adioevo.midi.CompleteMidiSystem
import com.adioevo.viewmodel.ProjectViewModel

@Composable
fun MidiSettingsScreen(
    viewModel: ProjectViewModel,
    midiSystem: CompleteMidiSystem,
    onClose: () -> Unit
) {
    val discoveredDevices = midiSystem.discoveredDevices.collectAsState().value
    val selectedDevice = midiSystem.selectedInputDevice.collectAsState().value
    val selectedChannel = midiSystem.selectedMidiChannel.collectAsState().value
    val connectionStatus = midiSystem.connectionStatus.collectAsState().value
    val sustainStatus = midiSystem.sustainPedalStatus.collectAsState().value
    val midiActivity = midiSystem.midiActivityStatus.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MIDI Settings",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
        }

        Divider(color = Color(0xFF333333), thickness = 1.dp)

        // MIDI Device Connection Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Connection Status",
                    color = Color(0xFFBB86FC),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = connectionStatus,
                    color = if (connectionStatus == "Connected") Color(0xFF03DAC5) else Color.Gray,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (selectedDevice != null) {
                    Text(
                        text = "Device: ${selectedDevice.id}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // MIDI Channel Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MIDI Channel",
                        color = Color(0xFFBB86FC),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "${selectedChannel + 1}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Slider(
                    value = selectedChannel.toFloat(),
                    onValueChange = { midiSystem.setMidiChannel(it.toInt()) },
                    valueRange = 0f..15f,
                    steps = 14,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF6200EE),
                        activeTrackColor = Color(0xFF6200EE)
                    )
                )
            }
        }

        // Sustain Pedal Status
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sustain Pedal",
                    color = Color(0xFFBB86FC),
                    style = MaterialTheme.typography.labelLarge
                )
                Chip(
                    selected = sustainStatus,
                    onClick = { },
                    label = {
                        Text(
                            text = if (sustainStatus) "ON" else "OFF",
                            color = if (sustainStatus) Color(0xFF03DAC5) else Color.Gray
                        )
                    },
                    colors = ChipDefaults.chipColors(
                        containerColor = if (sustainStatus) Color(0xFF2A2A2A) else Color(0xFF0A0A0A)
                    )
                )
            }
        }

        // MIDI Activity Indicator
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "MIDI Activity",
                    color = Color(0xFFBB86FC),
                    style = MaterialTheme.typography.labelLarge
                )
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (midiActivity) Color(0xFF03DAC5) else Color.Gray,
                            shape = MaterialTheme.shapes.small
                        )
                )
            }
        }

        // Available MIDI Devices
        Text(
            text = "Available MIDI Devices",
            color = Color(0xFFBB86FC),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        Button(
            onClick = { midiSystem.refreshDevices() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
        ) {
            Icon(
                Icons.Filled.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh Devices")
        }

        if (discoveredDevices.isEmpty()) {
            Text(
                text = "No MIDI devices found. Connect a USB MIDI keyboard.",
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn {
                items(discoveredDevices) { device ->
                    MidiDeviceItem(
                        device = device,
                        isSelected = device == selectedDevice,
                        onSelect = { midiSystem.connectToDevice(device) }
                    )
                }
            }
        }
    }
}

@Composable
fun MidiDeviceItem(
    device: android.media.midi.MidiDeviceInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2A2A2A) else Color(0xFF1E1E1E)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MIDI Device ${device.id}",
                    color = if (isSelected) Color(0xFF03DAC5) else Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Inputs: ${device.inputPortCount} | Outputs: ${device.outputPortCount}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (isSelected) {
                Chip(
                    selected = true,
                    onClick = { },
                    label = { Text("Connected", color = Color.White) },
                    colors = ChipDefaults.chipColors(containerColor = Color(0xFF03DAC5))
                )
            }
        }
    }
}
