package com.adioevo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.adioevo.data.model.MidiNote
import com.adioevo.timeline.TimelineManager
import timber.log.Timber
import kotlin.math.abs

/**
 * MIDI Piano Roll Editor
 * Full note editing interface for MIDI composition
 */
@Composable
fun MidiPianoRollEditor(
    timelineManager: TimelineManager,
    notes: List<MidiNote> = emptyList(),
    onNoteAdd: (MidiNote) -> Unit = {},
    onNoteDelete: (MidiNote) -> Unit = {},
    onNoteMoved: (oldNote: MidiNote, newNote: MidiNote) -> Unit = { _, _ -> },
    onNoteResized: (MidiNote) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedNote by remember { mutableStateOf<MidiNote?>(null) }
    var draggedNote by remember { mutableStateOf<MidiNote?>(null) }
    var isAddingNote by remember { mutableStateOf(false) }
    var newNoteStartTime by remember { mutableStateOf(0L) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
    ) {
        // Header with controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color(0xFF1E1E1E))
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Piano Roll - ${notes.size} notes",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { isAddingNote = !isAddingNote },
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAddingNote) Color(0xFF03DAC5) else Color(0xFF6200EE)
                )
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add Note",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Note", fontSize = 10.sp)
            }

            Button(
                onClick = {
                    if (selectedNote != null) {
                        onNoteDelete(selectedNote!!)
                        selectedNote = null
                    }
                },
                enabled = selectedNote != null,
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFCF6679),
                    disabledContainerColor = Color(0xFF333333)
                )
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete Note",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete", fontSize = 10.sp)
            }
        }

        // Piano roll editor area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF1A1A1A))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            if (isAddingNote) {
                                newNoteStartTime = (change.position.x / 2).toLong()
                            }
                        }
                    )
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (isAddingNote) {
                            Timber.d("Add note mode - click to place")
                        }
                    }
            ) {
                // Draw piano keys on left
                drawPianoKeyLabels()

                // Draw grid
                drawPianoRollGrid(timelineManager, size.width, size.height)

                // Draw notes
                notes.forEach { note ->
                    drawPianoNote(
                        note = note,
                        isSelected = note == selectedNote,
                        canvasWidth = size.width,
                        canvasHeight = size.height,
                        timelineManager = timelineManager
                    )
                }
            }
        }

        // Note properties panel
        if (selectedNote != null) {
            NotePropertiesPanel(
                note = selectedNote!!,
                onVelocityChange = { newVelocity ->
                    val updated = selectedNote!!.copy(velocity = newVelocity)
                    onNoteResized(updated)
                    selectedNote = updated
                },
                onPitchChange = { newPitch ->
                    val updated = selectedNote!!.copy(pitch = newPitch)
                    onNoteMoved(selectedNote!!, updated)
                    selectedNote = updated
                },
                onDurationChange = { newDuration ->
                    val updated = selectedNote!!.copy(duration = newDuration)
                    onNoteResized(updated)
                    selectedNote = updated
                }
            )
        }
    }
}

/**
 * Draw piano key labels on left side
 */
fun DrawScope.drawPianoKeyLabels() {
    val keyHeight = size.height / 128 // 128 MIDI pitches
    val noteNames = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    for (pitch in 0..127) {
        val yPos = size.height - (pitch * keyHeight)
        val noteName = noteNames[pitch % 12]
        val octave = pitch / 12

        // Draw note name at every 12th pitch
        if (pitch % 12 == 0) {
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#AAAAAA")
                    textSize = 8f
                }
                drawText("$noteName$octave", 2f, yPos, paint)
            }
        }
    }
}

/**
 * Draw piano roll grid
 */
fun DrawScope.drawPianoRollGrid(
    timelineManager: TimelineManager,
    width: Float,
    height: Float
) {
    val gridInterval = timelineManager.getGridIntervalSamples()
    val pixelsPerSample = (width / 10000) // Adjust scale as needed
    val gridPixels = (gridInterval * pixelsPerSample).coerceAtLeast(1f)

    val keyHeight = height / 128

    // Vertical grid lines (time)
    var xPos = 0f
    while (xPos < width) {
        drawLine(
            color = Color(0xFF2A2A2A),
            start = androidx.compose.ui.geometry.Offset(xPos, 0f),
            end = androidx.compose.ui.geometry.Offset(xPos, height),
            strokeWidth = 0.5f
        )
        xPos += gridPixels
    }

    // Horizontal grid lines (pitch)
    for (pitch in 0..127) {
        val yPos = height - (pitch * keyHeight)
        val color = if (pitch % 12 == 0) Color(0xFF444444) else Color(0xFF222222)
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(0f, yPos),
            end = androidx.compose.ui.geometry.Offset(width, yPos),
            strokeWidth = 0.5f
        )
    }
}

/**
 * Draw a single MIDI note as a rectangle
 */
fun DrawScope.drawPianoNote(
    note: MidiNote,
    isSelected: Boolean,
    canvasWidth: Float,
    canvasHeight: Float,
    timelineManager: TimelineManager
) {
    val keyHeight = canvasHeight / 128
    val pixelsPerSample = (canvasWidth / 10000)

    val xStart = note.startTime * pixelsPerSample
    val xEnd = (note.startTime + note.duration) * pixelsPerSample
    val yStart = canvasHeight - ((note.pitch + 1) * keyHeight)
    val yEnd = yStart + keyHeight

    val noteColor = if (isSelected) Color(0xFF03DAC5) else Color(0xFF6200EE)
    val velocityAlpha = note.velocity / 127f

    drawRect(
        color = noteColor.copy(alpha = 0.7f * velocityAlpha),
        topLeft = androidx.compose.ui.geometry.Offset(xStart, yStart),
        size = androidx.compose.ui.geometry.Size(xEnd - xStart, keyHeight - 1)
    )

    // Border
    drawRect(
        color = noteColor,
        topLeft = androidx.compose.ui.geometry.Offset(xStart, yStart),
        size = androidx.compose.ui.geometry.Size(xEnd - xStart, keyHeight - 1),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = if (isSelected) 2f else 1f)
    )
}

/**
 * Note Properties Panel
 */
@Composable
fun NotePropertiesPanel(
    note: MidiNote,
    onVelocityChange: (Int) -> Unit,
    onPitchChange: (Int) -> Unit,
    onDurationChange: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Note Properties",
                color = Color(0xFFBB86FC),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Pitch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pitch", color = Color.White, fontSize = 12.sp)
                Text(note.pitch.toString(), color = Color(0xFF03DAC5), fontSize = 12.sp)
            }

            // Velocity
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Velocity", color = Color.White, fontSize = 12.sp)
                Slider(
                    value = note.velocity.toFloat(),
                    onValueChange = { onVelocityChange(it.toInt()) },
                    valueRange = 1f..127f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF6200EE))
                )
                Text(note.velocity.toString(), color = Color(0xFF03DAC5), fontSize = 12.sp)
            }

            // Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Duration (samples)", color = Color.White, fontSize = 12.sp)
                Text(note.duration.toString(), color = Color(0xFF03DAC5), fontSize = 12.sp)
            }
        }
    }
}
