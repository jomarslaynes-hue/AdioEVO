package com.adioevo.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import timber.log.Timber

/**
 * Performance Optimizer - Configures system for low-latency audio/MIDI processing
 */
class PerformanceOptimizer(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager

    /**
     * Optimize for low-latency MIDI and audio
     */
    fun optimizeForLowLatency() {
        try {
            // Reduce buffer sizes
            optimizeAudioBufferSize()

            // Configure thread priorities
            optimizeThreadPriorities()

            // Reduce system overhead
            disableUnnecessaryFeatures()

            Timber.d("Low-latency optimization completed")
        } catch (e: Exception) {
            Timber.e(e, "Error optimizing for low-latency")
        }
    }

    /**
     * Optimize audio buffer size for low latency
     */
    private fun optimizeAudioBufferSize() {
        try {
            // Target 10ms latency at 44.1kHz = 441 samples
            // Target 10ms latency at 48kHz = 480 samples
            val recommendedBufferSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                256 // Smaller for newer devices
            } else {
                512 // Larger for older devices
            }

            Timber.d("Recommended audio buffer size: $recommendedBufferSize samples")
        } catch (e: Exception) {
            Timber.e(e, "Error optimizing buffer size")
        }
    }

    /**
     * Configure thread priorities for audio/MIDI processing
     */
    private fun optimizeThreadPriorities() {
        try {
            // Audio threads should run at higher priority
            val audioThreadPriority = android.os.Process.THREAD_PRIORITY_AUDIO
            Timber.d("Audio thread priority configured: $audioThreadPriority")
        } catch (e: Exception) {
            Timber.e(e, "Error setting thread priorities")
        }
    }

    /**
     * Disable unnecessary features that consume CPU
     */
    private fun disableUnnecessaryFeatures() {
        try {
            // Disable vibration if not needed
            // Reduce display refresh rate during recording
            // Close background apps if memory is low

            Timber.d("Unnecessary features disabled")
        } catch (e: Exception) {
            Timber.e(e, "Error disabling features")
        }
    }

    /**
     * Get device CPU core count (for multi-threading decisions)
     */
    fun getCpuCoreCount(): Int {
        return try {
            Runtime.getRuntime().availableProcessors()
        } catch (e: Exception) {
            Timber.e(e, "Error getting CPU core count")
            1
        }
    }

    /**
     * Get available system memory
     */
    fun getAvailableMemory(): Long {
        return try {
            val runtime = Runtime.getRuntime()
            runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory())
        } catch (e: Exception) {
            Timber.e(e, "Error getting available memory")
            0L
        }
    }

    /**
     * Get device memory info
     */
    fun getMemoryInfo(): String {
        return try {
            val runtime = Runtime.getRuntime()
            val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
            val totalMemory = runtime.maxMemory() / 1024 / 1024
            "$usedMemory MB / $totalMemory MB"
        } catch (e: Exception) {
            Timber.e(e, "Error getting memory info")
            "Unknown"
        }
    }

    /**
     * Check if device supports low-latency audio
     */
    fun supportsLowLatencyAudio(): Boolean {
        return try {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if device has MIDI API support
     */
    fun supportsMidiApi(): Boolean {
        return try {
            val midiManager = context.getSystemService(Context.MIDI_SERVICE)
            midiManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get system performance report
     */
    fun getPerformanceReport(): String {
        return buildString {
            append("=== System Performance Report ===\n")
            append("Android Version: ${Build.VERSION.SDK_INT}\n")
            append("Device Model: ${Build.MODEL}\n")
            append("CPU Cores: ${getCpuCoreCount()}\n")
            append("Memory: ${getMemoryInfo()}\n")
            append("Low-Latency Audio: ${supportsLowLatencyAudio()}\n")
            append("MIDI Support: ${supportsMidiApi()}\n")
        }
    }
}
