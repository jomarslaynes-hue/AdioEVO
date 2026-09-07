package com.adioevo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontSize
import androidx.compose.ui.unit.dp
import com.adioevo.viewmodel.ProjectViewModel

@Composable
fun MainScreen(viewModel: ProjectViewModel) {
    val currentProject = viewModel.currentProject.collectAsState().value
    val bpm = viewModel.bpm.collectAsState().value
    val isPlaying = viewModel.isPlaying.collectAsState().value
    val isRecording = viewModel.isRecording.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF1E1E1E))
                .padding(16.dp)
        ) {
            Text(
                text = currentProject?.name ?: "AdioEVO",
                color = Color.White,
                fontSize = androidx.compose.material3.MaterialTheme.typography.headlineSmall.fontSize
            )
        }

        // Transport Controls
        TransportControlsSection(viewModel, isPlaying, isRecording)

        // Timeline/Editor Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF0A0A0A))
        ) {
            Text(
                text = "Timeline & Editor Area",
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Bottom Panel
        BottomPanel(viewModel)
    }
}

@Composable
fun TransportControlsSection(
    viewModel: ProjectViewModel,
    isPlaying: Boolean,
    isRecording: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF1E1E1E))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text("Transport Controls", color = Color.Gray)
    }
}

@Composable
fun BottomPanel(viewModel: ProjectViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFF1E1E1E))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text("MIDI Keyboard | Mixer | Editor | Settings", color = Color.Gray)
    }
}
