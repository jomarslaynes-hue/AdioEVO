package com.adioevo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.adioevo.data.model.AudioClip
import com.adioevo.data.model.AudioTrack
import com.adioevo.data.model.MidiClip
import com.adioevo.data.model.Project

@Database(
    entities = [
        Project::class,
        AudioTrack::class,
        AudioClip::class,
        MidiClip::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun audioTrackDao(): AudioTrackDao
    abstract fun audioClipDao(): AudioClipDao
    abstract fun midiClipDao(): MidiClipDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "adioevo_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
