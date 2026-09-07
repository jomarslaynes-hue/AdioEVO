package com.adioevo.data.db

import androidx.room.*
import com.adioevo.data.model.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProject(projectId: Int): Project?

    @Query("SELECT * FROM projects ORDER BY modifiedAt DESC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentProjects(limit: Int = 10): List<Project>
}
