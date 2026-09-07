package com.adioevo.data.db

import androidx.room.*
import com.adioevo.data.model.AudioClip
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioClipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: AudioClip): Long

    @Update
    suspend fun updateClip(clip: AudioClip)

    @Delete
    suspend fun deleteClip(clip: AudioClip)

    @Query("SELECT * FROM audio_clips WHERE id = :clipId")
    suspend fun getClip(clipId: Int): AudioClip?

    @Query("SELECT * FROM audio_clips WHERE trackId = :trackId ORDER BY startTime ASC")
    fun getClipsForTrack(trackId: Int): Flow<List<AudioClip>>

    @Query("SELECT * FROM audio_clips WHERE trackId = :trackId ORDER BY startTime ASC")
    suspend fun getClipsForTrackSync(trackId: Int): List<AudioClip>
}
