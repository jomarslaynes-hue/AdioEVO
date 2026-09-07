package com.adioevo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.adioevo.data.db.AppDatabase
import com.adioevo.data.model.Project
import com.adioevo.data.repository.ProjectRepository
import com.adioevo.midi.MidiEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class ProjectViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val projectRepository = ProjectRepository(database)
    private val midiEngine = MidiEngine(application)

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

    val midiInputDevices = midiEngine.midiInputDevices
    val midiActivityStatus = midiEngine.midiActivityStatus
    val sustainPedalStatus = midiEngine.sustainPedalStatus

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
        Timber.d("Play")
    }

    fun pause() {
        _isPlaying.value = false
        Timber.d("Pause")
    }

    fun stop() {
        _isPlaying.value = false
        _isRecording.value = false
        midiEngine.allNotesOff()
        Timber.d("Stop")
    }

    fun startRecording() {
        _isRecording.value = true
        Timber.d("Start recording")
    }

    fun stopRecording() {
        _isRecording.value = false
        Timber.d("Stop recording")
    }

    fun refreshMidiDevices() {
        midiEngine.refreshMidiDevices()
    }

    override fun onCleared() {
        super.onCleared()
        midiEngine.allNotesOff()
        Timber.d("ProjectViewModel cleared")
    }
}
