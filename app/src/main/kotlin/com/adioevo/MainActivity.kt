package com.adioevo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.adioevo.ui.MainScreen
import com.adioevo.viewmodel.ProjectViewModel
import timber.log.Timber

class MainActivity : ComponentActivity() {

    private lateinit var projectViewModel: ProjectViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Timber logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("MainActivity created")

        projectViewModel = ViewModelProvider(this).get(ProjectViewModel::class.java)

        setContent {
            AdioEVOTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF121212)
                ) {
                    MainScreen(projectViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
