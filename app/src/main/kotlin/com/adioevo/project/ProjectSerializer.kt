package com.adioevo.project

import android.content.Context
import com.adioevo.data.db.AppDatabase
import com.adioevo.data.model.Project
import com.adioevo.data.model.AudioTrack
import com.adioevo.data.model.AudioClip
import com.adioevo.data.model.MidiClip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File

/**
 * Project Serializer - Handles saving and loading projects to/from disk
 */
class ProjectSerializer(private val context: Context) {

    private val projectsDir = File(context.filesDir, "projects")
    private val json = Json { prettyPrint = true }

    init {
        if (!projectsDir.exists()) {
            projectsDir.mkdirs()
            Timber.d("Created projects directory: ${projectsDir.absolutePath}")
        }
    }

    /**
     * Save a complete project to disk
     */
    suspend fun saveProject(
        project: Project,
        tracks: List<AudioTrack> = emptyList(),
        audioClips: Map<Int, List<AudioClip>> = emptyMap(),
        midiClips: Map<Int, List<MidiClip>> = emptyMap()
    ): Boolean {
        return try {
            val projectData = SerializedProject(
                project = project,
                audioTracks = tracks,
                audioClips = audioClips,
                midiClips = midiClips
            )

            val projectFile = File(projectsDir, "${project.id}_${project.name}.json")
            val jsonString = json.encodeToString(projectData)
            projectFile.writeText(jsonString)

            Timber.d("Project saved: ${project.name} (${projectFile.absolutePath})")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error saving project")
            false
        }
    }

    /**
     * Load a project from disk
     */
    suspend fun loadProject(fileName: String): SerializedProject? {
        return try {
            val projectFile = File(projectsDir, fileName)
            if (!projectFile.exists()) {
                Timber.w("Project file not found: $fileName")
                return null
            }

            val jsonString = projectFile.readText()
            val projectData = json.decodeFromString<SerializedProject>(jsonString)
            Timber.d("Project loaded: ${projectData.project.name}")
            projectData
        } catch (e: Exception) {
            Timber.e(e, "Error loading project")
            null
        }
    }

    /**
     * Get list of all saved projects
     */
    fun listProjects(): List<String> {
        return try {
            projectsDir.listFiles()?.map { it.name }?.toList() ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error listing projects")
            emptyList()
        }
    }

    /**
     * Delete a project file
     */
    fun deleteProject(fileName: String): Boolean {
        return try {
            val projectFile = File(projectsDir, fileName)
            val deleted = projectFile.delete()
            if (deleted) {
                Timber.d("Project deleted: $fileName")
            } else {
                Timber.w("Failed to delete project: $fileName")
            }
            deleted
        } catch (e: Exception) {
            Timber.e(e, "Error deleting project")
            false
        }
    }

    /**
     * Export project as WAV (future implementation)
     */
    fun exportProjectAsWav(project: Project, outputFile: File): Boolean {
        return try {
            // TODO: Implement WAV export
            Timber.d("WAV export not yet implemented")
            false
        } catch (e: Exception) {
            Timber.e(e, "Error exporting project")
            false
        }
    }

    /**
     * Export project as MP3 (future implementation)
     */
    fun exportProjectAsMp3(project: Project, outputFile: File): Boolean {
        return try {
            // TODO: Implement MP3 export
            Timber.d("MP3 export not yet implemented")
            false
        } catch (e: Exception) {
            Timber.e(e, "Error exporting project")
            false
        }
    }
}

@Serializable
data class SerializedProject(
    val project: Project,
    val audioTracks: List<AudioTrack> = emptyList(),
    val audioClips: Map<Int, List<AudioClip>> = emptyMap(),
    val midiClips: Map<Int, List<MidiClip>> = emptyMap()
)
