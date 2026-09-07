package com.adioevo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import com.adioevo.timeline.TimelineManager
import kotlin.math.abs

/**
 * Waveform Display Component
 * Renders audio waveform visualization with playhead
 */
@Composable
fun WaveformDisplay(
    timelineManager: TimelineManager,
    waveformData: List<Float> = emptyList(),
    modifier: Modifier = Modifier,
    height: Float = 100f
) {
    val playheadPosition = timelineManager.playheadPixelPosition.collectAsState().value
    val horizontalScroll = timelineManager.horizontalScroll.collectAsState().value
    val currentPosition = timelineManager.currentPosition.collectAsState().value
    val timeCode = timelineManager.getTimeCodeString(currentPosition)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0A))
    ) {
        // Time ruler
        TimeRuler(timelineManager)

        // Waveform canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .background(Color(0xFF1A1A1A))
        ) {
            drawWaveform(waveformData, Color(0xFF03DAC5))
            drawPlayhead(playheadPosition - horizontalScroll, size.height)
            drawGrid(timelineManager, size.width, size.height)
        }

        // Current time display
        Text(
            text = timeCode,
            color = Color(0xFF03DAC5),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            modifier = Modifier.padding(4.dp)
        )
    }
}

/**
 * Time ruler showing beat/measure markers
 */
@Composable
fun TimeRuler(
    timelineManager: TimelineManager,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color(0xFF0F0F0F))
    ) {
        val samplesPerBeat = timelineManager.getSamplesPerBeat()
        val pixelsPerSample = (100f / timelineManager.sampleRate)
        val beatPixels = samplesPerBeat * pixelsPerSample

        // Draw beat markers
        var beatNumber = 0
        var xPos = 0f
        while (xPos < size.width) {
            drawLine(
                color = if (beatNumber % 4 == 0) Color(0xFFBB86FC) else Color(0xFF444444),
                start = androidx.compose.ui.geometry.Offset(xPos, size.height * 0.5f),
                end = androidx.compose.ui.geometry.Offset(xPos, size.height),
                strokeWidth = if (beatNumber % 4 == 0) 2f else 1f
            )

            // Draw beat number on measure markers
            if (beatNumber % 4 == 0) {
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#AAAAAA")
                        textSize = 10f
                    }
                    drawText("${beatNumber / 4 + 1}", xPos + 2, 12f, paint)
                }
            }

            xPos += beatPixels
            beatNumber++
        }
    }
}

/**
 * Draw waveform path
 */
fun DrawScope.drawWaveform(waveformData: List<Float>, color: Color) {
    if (waveformData.isEmpty()) return

    val centerY = size.height / 2
    val pixelWidth = size.width / waveformData.size.coerceAtLeast(1)
    val maxAmplitude = size.height / 2 * 0.9f

    val path = Path()
    path.moveTo(0f, centerY)

    waveformData.forEachIndexed { index, sample ->
        val x = index * pixelWidth
        val y = centerY - (sample * maxAmplitude)
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 1f)
    )
}

/**
 * Draw playhead line
 */
fun DrawScope.drawPlayhead(xPosition: Float, height: Float) {
    drawLine(
        color = Color(0xFF03DAC5),
        start = androidx.compose.ui.geometry.Offset(xPosition, 0f),
        end = androidx.compose.ui.geometry.Offset(xPosition, height),
        strokeWidth = 2f
    )
}

/**
 * Draw grid lines
 */
fun DrawScope.drawGrid(
    timelineManager: TimelineManager,
    width: Float,
    height: Float
) {
    val gridInterval = timelineManager.getGridIntervalSamples()
    val pixelsPerSample = (100f / timelineManager.sampleRate)
    val gridPixels = gridInterval * pixelsPerSample

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
}
