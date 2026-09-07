package com.adioevo.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "midi_clips",
    foreignKeys = [
        ForeignKey(
            entity = AudioTrack::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MidiClip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val trackId: Int,
    val clipName: String,
    val startTime: Long,
    val duration: Long,
    val channel: Int = 0
)

@Serializable
data class MidiNote(
    val pitch: Int,
    val velocity: Int,
    val startTime: Long,
    val duration: Long
)
