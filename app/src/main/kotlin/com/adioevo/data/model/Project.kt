package com.adioevo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val bpm: Float = 120f,
    val timeSignatureNumerator: Int = 4,
    val timeSignatureDenominator: Int = 4,
    val sampleRate: Int = 44100,
    val bufferSize: Int = 512,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val projectPath: String = ""
)

data class ProjectWithTracks(
    val project: Project,
    val tracks: List<AudioTrack>
)
