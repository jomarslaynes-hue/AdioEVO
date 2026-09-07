package com.adioevo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "audio_clips",
    foreignKeys = [
        ForeignKey(
            entity = AudioTrack::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AudioClip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val trackId: Int,
    val filePath: String,
    val clipName: String,
    val startTime: Long,
    val duration: Long,
    val offset: Long = 0,
    val volume: Float = 1f
)
