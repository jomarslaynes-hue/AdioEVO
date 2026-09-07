package com.adioevo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "audio_tracks",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AudioTrack(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val projectId: Int,
    val name: String,
    val trackIndex: Int,
    val volume: Float = 1f,
    val pan: Float = 0f,
    val isMuted: Boolean = false,
    val isSolo: Boolean = false,
    val isArmed: Boolean = false,
    val isRecording: Boolean = false
)
