package com.adioevo.data.db

import androidx.room.*
import com.adioevo.data.model.MidiClip
import kotlinx.coroutines.flow.Flow

@Dao
interface MidiClipDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMidiClip(clip: MidiClip): Long

    @Update
    suspend fun updateMidiClip(clip: MidiClip)

    @Delete
    suspend fun deleteMidiClip(clip: MidiClip)

    @Query("SELECT * FROM midi_clips WHERE id = :clipId")
    suspend fun getMidiClip(clipId: Int): MidiClip?

    @Query("SELECT * FROM midi_clips WHERE trackId = :trackId ORDER BY startTime ASC")
    fun getMidiClipsForTrack(trackId: Int): Flow<List<MidiClip>>

    @Query("SELECT * FROM midi_clips WHERE trackId = :trackId ORDER BY startTime ASC")
    suspend fun getMidiClipsForTrackSync(trackId: Int): List<MidiClip>
}
