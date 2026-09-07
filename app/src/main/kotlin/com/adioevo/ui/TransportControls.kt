package com.adioevo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adioevo.viewmodel.ProjectViewModel
import timber.log.Timber

/**
 * Transport Controls UI Component
 * Contains Play, Stop, Record, Loop, Metronome controls
 */
@Composable
fun TransportControls(
    viewModel: ProjectViewModel,
    bpm: Float = 120f,
    onBpmChange: (Float) -> Unit = {},
    onMetronomeToggle: () -> Unit = {}
) {
    val isPlaying = viewModel.isPlaying.collectAsState().value
    val isRecording = viewModel.isRecording.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E1E1E))
            .padding(8.dp)
    ) {
        // Primary Transport Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Record Button
            TransportButton(
                icon = Icons.Filled.RadioButtonChecked,
                label = "REC",
                isActive = isRecording,
                isError = isRecording,
                onClick = {
                    if (!isRecording) {
                        viewModel.startRecording()
                        Timber.d("Recording started")
                    } else {
                        viewModel.stopRecording()
                        Timber.d("Recording stopped")
                    }
                }
            )

            // Stop Button
            TransportButton(
                icon = Icons.Filled.Stop,
                label = "STOP",
                isActive = false,
                onClick = {
                    viewModel.stop()
                    Timber.d("Stopped")
                }
            )

            // Play Button
            TransportButton(
                icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                label = if (isPlaying) "PAUSE" else "PLAY",
                isActive = isPlaying,
                onClick = {
                    if (!isPlaying) {
                        viewModel.play()
                        Timber.d("Playback started")
                    } else {
                        viewModel.pause()
                        Timber.d("Paused")
                    }
                }
            )

            // Loop Button
            TransportButton(
                icon = Icons.Filled.Repeat,
                label = "LOOP",
                isActive = false,
                onClick = { Timber.d("Loop toggled") }
            )

            // Metronome Button
            TransportButton(
                icon = Icons.Filled.MoreVert,
                label = "METRO",
                isActive = false,
                onClick = {
                    onMetronomeToggle()
                    Timber.d("Metronome toggled")
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // BPM Control
            BpmControl(bpm, onBpmChange)
        }
    }
}

/**
 * Individual transport control button
 */
@Composable
fun TransportButton(
    icon: androidx.compose.material.icons.Icons,
    label: String,
    isActive: Boolean = false,
    isError: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isError -> Color(0xFFCF6679) // Error red
        isActive -> Color(0xFF03DAC5) // Active cyan
        else -> Color(0xFF2A2A2A) // Default dark
    }

    val contentColor = when {
        isError -> Color.White
        isActive -> Color.Black
        else -> Color.White
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .size(width = 60.dp, height = 48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 8.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * BPM Control Component
 */
@Composable
fun BpmControl(
    bpm: Float,
    onBpmChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .background(Color(0xFF0A0A0A), MaterialTheme.shapes.small)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onBpmChange((bpm - 1f).coerceAtLeast(1f)) },
            modifier = Modifier.size(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                Icons.Filled.Remove,
                contentDescription = "Decrease BPM",
                modifier = Modifier.size(14.dp),
                tint = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(50.dp)
        ) {
            Text(
                text = "BPM",
                color = Color.Gray,
                fontSize = 8.sp
            )
            Text(
                text = String.format("%.0f", bpm),
                color = Color.White,
                fontSize = 14.sp,
                style = MaterialTheme.typography.labelMedium
            )
        }

        Button(
            onClick = { onBpmChange((bpm + 1f).coerceAtMost(300f)) },
            modifier = Modifier.size(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Increase BPM",
                modifier = Modifier.size(14.dp),
                tint = Color.White
            )
        }
    }
}
