package com.adioevo.project

import android.content.Context
import com.adioevo.data.db.AppDatabase
import com.adioevo.data.model.Project
import com.adioevo.data.model.AudioTrack
import com.adioevo.data.repository.ProjectRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.util.*

/**
 * Project Manager - High-level project operations with auto-save
 */
class ProjectManager(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val projectRepository = ProjectRepository(database)
    private val projectSerializer = ProjectSerializer(context)

    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject: StateFlow<Project?> = _currentProject

    private val _allProjects = MutableStateFlow<List<Project>>(emptyList())
    val allProjects: StateFlow<List<Project>> = _allProjects

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _lastSaveTime = MutableStateFlow<Long?>(null)
    val lastSaveTime: StateFlow<Long?> = _lastSaveTime

    private val _autoSaveEnabled = MutableStateFlow(true)
    val autoSaveEnabled: StateFlow<Boolean> = _autoSaveEnabled

    private val autoSaveJob = MutableStateFlow<Job?>(null)

    init {
        Timber.d("ProjectManager initialized")
        loadAllProjects()
    }

    /**
     * Create a new project
     */
    suspend fun createNewProject(
        name: String,
        description: String = "",
        bpm: Float = 120f,
        timeSignatureNumerator: Int = 4,
        timeSignatureDenominator: Int = 4
    ): Project? {
        return try {
            val project = Project(
                name = name,
                description = description,
                bpm = bpm,
                timeSignatureNumerator = timeSignatureNumerator,
                timeSignatureDenominator = timeSignatureDenominator,
                createdAt = System.currentTimeMillis(),
                modifiedAt = System.currentTimeMillis()
            )

            val projectId = projectRepository.createProject(project)
            val createdProject = projectRepository.getProject(projectId.toInt())

            if (createdProject != null) {
                _currentProject.value = createdProject
                loadAllProjects()
                Timber.d("Created new project: $name (id=$projectId)")
            }

            createdProject
        } catch (e: Exception) {
            Timber.e(e, "Error creating project")
            null
        }
    }

    /**
     * Open an existing project
     */
    suspend fun openProject(projectId: Int): Project? {
        return try {
            val project = projectRepository.getProject(projectId)
            _currentProject.value = project

            if (project != null) {
                startAutoSave(projectId)
                Timber.d("Opened project: ${project.name}")
            }

            project
        } catch (e: Exception) {
            Timber.e(e, "Error opening project")
            null
        }
    }

    /**
     * Save current project
     */
    suspend fun saveProject(): Boolean {
        val project = _currentProject.value ?: return false

        return try {
            _isSaving.value = true
            val updatedProject = project.copy(modifiedAt = System.currentTimeMillis())
            projectRepository.updateProject(updatedProject)
            _currentProject.value = updatedProject
            _lastSaveTime.value = System.currentTimeMillis()
            Timber.d("Project saved: ${project.name}")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error saving project")
            false
        } finally {
            _isSaving.value = false
        }
    }

    /**
     * Start auto-save timer
     */
    private fun startAutoSave(projectId: Int) {
        autoSaveJob.value?.cancel()

        autoSaveJob.value = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                if (_autoSaveEnabled.value) {
                    delay(30000) // Auto-save every 30 seconds
                    saveProject()
                } else {
                    delay(1000)
                }
            }
        }
    }

    /**
     * Enable/disable auto-save
     */
    fun setAutoSaveEnabled(enabled: Boolean) {
        _autoSaveEnabled.value = enabled
        Timber.d("Auto-save: ${if (enabled) "ON" else "OFF"}")
    }

    /**
     * Rename project
     */
    suspend fun renameProject(projectId: Int, newName: String): Boolean {
        return try {
            val project = projectRepository.getProject(projectId) ?: return false
            val renamed = project.copy(
                name = newName,
                modifiedAt = System.currentTimeMillis()
            )
            projectRepository.updateProject(renamed)
            if (_currentProject.value?.id == projectId) {
                _currentProject.value = renamed
            }
            loadAllProjects()
            Timber.d("Project renamed to: $newName")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error renaming project")
            false
        }
    }

    /**
     * Delete project
     */
    suspend fun deleteProject(projectId: Int): Boolean {
        return try {
            val project = projectRepository.getProject(projectId) ?: return false
            projectRepository.deleteProject(project)
            if (_currentProject.value?.id == projectId) {
                _currentProject.value = null
                autoSaveJob.value?.cancel()
            }
            loadAllProjects()
            Timber.d("Project deleted: ${project.name}")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error deleting project")
            false
        }
    }

    /**
     * Load all projects from database
     */
    private fun loadAllProjects() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val projects = projectRepository.getRecentProjects(limit = 100)
                _allProjects.value = projects
                Timber.d("Loaded ${projects.size} projects")
            } catch (e: Exception) {
                Timber.e(e, "Error loading projects")
            }
        }
    }

    /**
     * Close current project and cleanup
     */
    suspend fun closeProject() {
        try {
            saveProject()
            autoSaveJob.value?.cancel()
            _currentProject.value = null
            Timber.d("Project closed")
        } catch (e: Exception) {
            Timber.e(e, "Error closing project")
        }
    }

    /**
     * Get recently modified projects
     */
    fun getRecentProjects(limit: Int = 10): List<Project> = _allProjects.value.take(limit)
}
