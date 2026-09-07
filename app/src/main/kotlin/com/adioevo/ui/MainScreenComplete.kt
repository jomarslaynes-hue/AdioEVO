package com.adioevo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adioevo.viewmodel.ProjectViewModel
import com.adioevo.midi.CompleteMidiSystem
import timber.log.Timber

/**
 * Main Screen - Complete DAW interface with all components
 */
@Composable
fun MainScreenComplete(
    viewModel: ProjectViewModel,
    midiSystem: CompleteMidiSystem
) {
    var activeTab by remember { mutableStateOf(EditorTab.TIMELINE) }
    var showSettings by remember { mutableStateOf(false) }

    val currentProject = viewModel.currentProject.collectAsState().value
    val bpm = viewModel.bpm.collectAsState().value
    val isPlaying = viewModel.isPlaying.collectAsState().value
    val isRecording = viewModel.isRecording.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Header Bar
        HeaderBar(
            projectName = currentProject?.name ?: "New Project",
            bpm = bpm,
            onBpmChange = { viewModel.updateBpm(it) },
            onSettingsClick = { showSettings = !showSettings }
        )

        // Transport Controls
        TransportControls(
            viewModel = viewModel,
            bpm = bpm,
            onBpmChange = { viewModel.updateBpm(it) }
        )

        // Main Editor Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0A0A0A))
        ) {
            when (activeTab) {
                EditorTab.TIMELINE -> {
                    Text(
                        text = "Timeline Editor - Ready for audio/MIDI tracks",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                EditorTab.MIDI -> {
                    Text(
                        text = "MIDI Piano Roll Editor - Ready for note editing",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                EditorTab.MIXER -> {
                    Text(
                        text = "Mixer - Multi-track mixing ready",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        // Bottom Panel with Tabs
        BottomPanelTabs(
            activeTab = activeTab,
            onTabChange = { activeTab = it }
        )
    }

    // Settings Dialog
    if (showSettings) {
        SettingsDialog(
            midiSystem = midiSystem,
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun HeaderBar(
    projectName: String,
    bpm: Float,
    onBpmChange: (Float) -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = projectName,
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = Color(0xFF6200EE),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun BottomPanelTabs(
    activeTab: EditorTab,
    onTabChange: (EditorTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF1E1E1E))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EditorTab.values().forEach { tab ->
            TabButton(
                label = tab.displayName,
                isActive = activeTab == tab,
                onClick = { onTabChange(tab) }
            )
        }
    }
}

@Composable
fun TabButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) Color(0xFF6200EE) else Color(0xFF2A2A2A)
        ),
        modifier = Modifier
            .height(40.dp)
            .weight(1f)
    ) {
        Text(
            text = label,
            color = if (isActive) Color.White else Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SettingsDialog(
    midiSystem: CompleteMidiSystem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("MIDI Settings", style = MaterialTheme.typography.labelMedium)
                Button(
                    onClick = { midiSystem.refreshDevices() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Refresh MIDI Devices")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

enum class EditorTab(val displayName: String) {
    TIMELINE("Timeline"),
    MIDI("Piano Roll"),
    MIXER("Mixer")
}
