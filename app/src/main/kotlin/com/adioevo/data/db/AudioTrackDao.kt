package com.adioevo.data.db

import androidx.room.*
import com.adioevo.data.model.AudioTrack
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioTrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: AudioTrack): Long

    @Update
    suspend fun updateTrack(track: AudioTrack)

    @Delete
    suspend fun deleteTrack(track: AudioTrack)

    @Query("SELECT * FROM audio_tracks WHERE id = :trackId")
    suspend fun getTrack(trackId: Int): AudioTrack?

    @Query("SELECT * FROM audio_tracks WHERE projectId = :projectId ORDER BY trackIndex ASC")
    fun getTracksForProject(projectId: Int): Flow<List<AudioTrack>>

    @Query("SELECT * FROM audio_tracks WHERE projectId = :projectId ORDER BY trackIndex ASC")
    suspend fun getTracksForProjectSync(projectId: Int): List<AudioTrack>
}
