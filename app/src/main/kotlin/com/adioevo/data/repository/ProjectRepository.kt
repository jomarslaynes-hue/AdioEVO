package com.adioevo.data.repository

import com.adioevo.data.db.AppDatabase
import com.adioevo.data.model.Project
import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val database: AppDatabase) {

    private val projectDao = database.projectDao()

    suspend fun createProject(project: Project): Long {
        return projectDao.insertProject(project)
    }

    suspend fun updateProject(project: Project) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: Project) {
        projectDao.deleteProject(project)
    }

    suspend fun getProject(projectId: Int): Project? {
        return projectDao.getProject(projectId)
    }

    fun getAllProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects()
    }

    suspend fun getRecentProjects(limit: Int = 10): List<Project> {
        return projectDao.getRecentProjects(limit)
    }
}
