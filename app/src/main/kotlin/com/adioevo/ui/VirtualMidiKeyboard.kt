package com.adioevo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import timber.log.Timber

/**
 * Virtual MIDI Piano Keyboard
 * Displays 88 piano keys (standard piano range: A0 to C8)
 * Supports touch input and MIDI note generation
 */
@Composable
fun VirtualMidiKeyboard(
    onNoteOn: (pitch: Int, velocity: Int) -> Unit,
    onNoteOff: (pitch: Int) -> Unit,
    currentOctave: Int = 4,
    onOctaveChange: (Int) -> Unit = {},
    velocity: Int = 100,
    sustainActive: Boolean = false
) {
    var activeKeys by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0A))
            .padding(8.dp)
    ) {
        // Octave and Velocity Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Octave Down Button
            Button(
                onClick = { onOctaveChange(currentOctave - 1) },
                enabled = currentOctave > 0,
                modifier = Modifier.size(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    disabledContainerColor = Color(0xFF333333)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    Icons.Filled.Remove,
                    contentDescription = "Octave Down",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Octave Display
            Text(
                text = "Octave: $currentOctave",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )

            // Octave Up Button
            Button(
                onClick = { onOctaveChange(currentOctave + 1) },
                enabled = currentOctave < 8,
                modifier = Modifier.size(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6200EE),
                    disabledContainerColor = Color(0xFF333333)
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Octave Up",
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Velocity Display
            Text(
                text = "Velocity: $velocity",
                color = Color(0xFF03DAC5),
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )

            // Sustain Indicator
            if (sustainActive) {
                Chip(
                    selected = true,
                    onClick = { },
                    label = {
                        Text(
                            "Sustain ON",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    },
                    colors = ChipDefaults.chipColors(
                        containerColor = Color(0xFF03DAC5),
                        selectedContainerColor = Color(0xFF03DAC5)
                    ),
                    modifier = Modifier
                        .height(28.dp)
                        .padding(start = 8.dp)
                )
            }
        }

        // Piano Keyboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color(0xFF1A1A1A))
                .padding(4.dp)
        ) {
            PianoKeys(
                startOctave = currentOctave,
                activeKeys = activeKeys,
                onNoteOn = { pitch ->
                    activeKeys = activeKeys + pitch
                    onNoteOn(pitch, velocity)
                    Timber.d("Virtual keyboard: Note ON - pitch=$pitch")
                },
                onNoteOff = { pitch ->
                    activeKeys = activeKeys - pitch
                    onNoteOff(pitch)
                    Timber.d("Virtual keyboard: Note OFF - pitch=$pitch")
                }
            )
        }
    }
}

/**
 * Individual piano keys component
 */
@Composable
fun PianoKeys(
    startOctave: Int,
    activeKeys: Set<Int>,
    onNoteOn: (pitch: Int) -> Unit,
    onNoteOff: (pitch: Int) -> Unit
) {
    val whiteKeyNotes = listOf(0, 2, 4, 5, 7, 9, 11) // C, D, E, F, G, A, B
    val whiteKeyCount = 14 // 2 octaves displayed
    val whiteKeyWidth = (0.95f) / whiteKeyCount

    Box(modifier = Modifier.fillMaxSize()) {
        // White Keys
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(whiteKeyCount) { index ->
                val noteInOctave = whiteKeyNotes[index % 7]
                val octave = startOctave + (index / 7)
                val pitch = octave * 12 + noteInOctave
                val isActive = activeKeys.contains(pitch)

                PianoKey(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    isWhiteKey = true,
                    isActive = isActive,
                    onPress = { onNoteOn(pitch) },
                    onRelease = { onNoteOff(pitch) },
                    noteName = getNoteDisplayName(noteInOctave, octave)
                )
            }
        }

        // Black Keys
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = (whiteKeyWidth * 50).dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(whiteKeyCount - 1) { index ->
                val isBlackKey = (index % 7) !in listOf(2, 6) // No black key between E-F and B-C

                if (isBlackKey) {
                    val noteInOctave = whiteKeyNotes[index % 7] + 1
                    val octave = startOctave + (index / 7)
                    val pitch = octave * 12 + noteInOctave
                    val isActive = activeKeys.contains(pitch)

                    PianoKey(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(0.6f)
                            .align(Alignment.Top),
                        isWhiteKey = false,
                        isActive = isActive,
                        onPress = { onNoteOn(pitch) },
                        onRelease = { onNoteOff(pitch) },
                        noteName = getNoteDisplayName(noteInOctave, octave)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Individual piano key component
 */
@Composable
fun PianoKey(
    modifier: Modifier = Modifier,
    isWhiteKey: Boolean,
    isActive: Boolean,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    noteName: String
) {
    val backgroundColor = when {
        isActive && isWhiteKey -> Color(0xFFDDDDDD)
        isActive && !isWhiteKey -> Color(0xFF333333)
        isWhiteKey -> Color(0xFFFFFFF0)
        else -> Color(0xFF1A1A1A)
    }

    val borderColor = when {
        isActive -> Color(0xFF03DAC5)
        isWhiteKey -> Color(0xFF333333)
        else -> Color(0xFF0A0A0A)
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = if (isWhiteKey) MaterialTheme.shapes.small else MaterialTheme.shapes.extraSmall
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPress()
                        awaitRelease()
                        onRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Note label (optional, only on white keys)
        if (isWhiteKey) {
            Text(
                text = noteName,
                color = if (isActive) Color(0xFF6200EE) else Color.Gray,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Get human-readable note name from MIDI pitch
 */
fun getNoteDisplayName(noteInOctave: Int, octave: Int): String {
    val noteNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    return "${noteNames[noteInOctave % 12]}${octave}"
}
