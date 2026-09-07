package com.adioevo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.adioevo.midi.CompleteMidiSystem
import com.adioevo.ui.MainScreenComplete
import com.adioevo.viewmodel.ProjectViewModel
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private lateinit var projectViewModel: ProjectViewModel
    private lateinit var midiSystem: CompleteMidiSystem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Timber logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("MainActivity created")

        projectViewModel = ViewModelProvider(this).get(ProjectViewModel::class.java)
        midiSystem = CompleteMidiSystem(this)

        // Auto-create a new project for testing
        projectViewModel.createNewProject("New Project")

        setContent {
            AdioEVOTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212)
                ) {
                    MainScreenComplete(projectViewModel, midiSystem)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        midiSystem.shutdown()
        Timber.d("MainActivity destroyed")
    }
}

@Composable
fun AdioEVOTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        primary = Color(0xFF6200EE),
        secondary = Color(0xFF03DAC5),
        tertiary = Color(0xFF03DAC5),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        error = Color(0xFFCF6679)
    )

    MaterialTheme(
        colorScheme = darkColorScheme,
        content = content
    )
}
