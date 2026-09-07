package com.adioevo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adioevo.audio.AudioEngine
import com.adioevo.data.db.AppDatabase
import com.adioevo.data.model.Project
import com.adioevo.data.repository.ProjectRepository
import com.adioevo.midi.MidiEngine
import com.adioevo.performance.PerformanceOptimizer
import com.adioevo.project.ProjectManager
import com.adioevo.timeline.TimelineManager
import com.adioevo.timeline.TrackManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val projectRepository = ProjectRepository(database)
    private val projectManager = ProjectManager(application)
    private val performanceOptimizer = PerformanceOptimizer(application)

    private val midiEngine = MidiEngine(application)
    private val audioEngine = AudioEngine(application)
    private val timelineManager = TimelineManager()
    private val trackManager = TrackManager()

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> = _currentProject.asStateFlow()

    private val _allProjects = projectRepository.getAllProjects()
    val allProjects: StateFlow<List<Project>> = _allProjects.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _bpm = MutableStateFlow(120f)
    val bpm: StateFlow<Float> = _bpm.asStateFlow()

    private val _systemInfo = MutableStateFlow("")
    val systemInfo: StateFlow<String> = _systemInfo.asStateFlow()

    val midiInputDevices = midiEngine.midiInputDevices
    val midiActivityStatus = midiEngine.midiActivityStatus
    val sustainPedalStatus = midiEngine.sustainPedalStatus

    init {
        Timber.d("ProjectViewModel initialized")
        performanceOptimizer.optimizeForLowLatency()
        _systemInfo.value = performanceOptimizer.getPerformanceReport()
        Timber.d(_systemInfo.value)
    }

    fun createNewProject(name: String) {
        viewModelScope.launch {
            try {
                val project = Project(
                    name = name,
                    bpm = _bpm.value
                )
                val projectId = projectRepository.createProject(project)
                val createdProject = projectRepository.getProject(projectId.toInt())
                if (createdProject != null) {
                    _currentProject.value = createdProject
                    Timber.d("Created new project: $name (id=$projectId)")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error creating new project")
            }
        }
    }

    fun openProject(projectId: Int) {
        viewModelScope.launch {
            try {
                val project = projectRepository.getProject(projectId)
                _currentProject.value = project
                if (project != null) {
                    _bpm.value = project.bpm
                    Timber.d("Opened project: ${project.name}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error opening project")
            }
        }
    }

    fun updateBpm(newBpm: Float) {
        _bpm.value = newBpm
        viewModelScope.launch {
            try {
                val project = _currentProject.value?.copy(bpm = newBpm)
                if (project != null) {
                    projectRepository.updateProject(project)
                    _currentProject.value = project
                }
            } catch (e: Exception) {
                Timber.e(e, "Error updating BPM")
            }
        }
    }

    fun play() {
        _isPlaying.value = true
        audioEngine.startPlayback()
        Timber.d("Play")
    }

    fun pause() {
        _isPlaying.value = false
        audioEngine.stopPlayback()
        Timber.d("Pause")
    }

    fun stop() {
        _isPlaying.value = false
        _isRecording.value = false
        audioEngine.stopPlayback()
        audioEngine.stopRecording()
        midiEngine.allNotesOff()
        Timber.d("Stop")
    }

    fun startRecording() {
        _isRecording.value = true
        audioEngine.startRecording()
        Timber.d("Start recording")
    }

    fun stopRecording() {
        _isRecording.value = false
        audioEngine.stopRecording()
        Timber.d("Stop recording")
    }

    fun refreshMidiDevices() {
        midiEngine.refreshMidiDevices()
    }

    override fun onCleared() {
        super.onCleared()
        stop()
        audioEngine.release()
        midiEngine.allNotesOff()
        Timber.d("ProjectViewModel cleared")
    }
}
