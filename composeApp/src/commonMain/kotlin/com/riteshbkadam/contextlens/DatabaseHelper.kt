package com.riteshbkadam.contextlens

import app.cash.sqldelight.db.SqlDriver
import com.riteshbkadam.contextlens.db.ContextLensDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseHelper(
    driver: SqlDriver
) {
    private val db = ContextLensDatabase(driver).contextLensDatabasesQueries


    // 🟢 Insert new project
    suspend fun insertProject(name: String) = withContext(Dispatchers.IO) {
        db.insertProject(name)
    }
    suspend fun getLastInsertedFileIdInProject(projectId: Long) = withContext(Dispatchers.IO) {
        db.getLastInsertedFileId(projectId).executeAsOneOrNull()
    }

    // 🟢 Insert file under a project
    suspend fun insertFile(projectId: Long, name: String) = withContext(Dispatchers.IO) {
        db.insertFile(projectId, name)
    }

    // 🟢 Insert snippet under a file
    suspend fun insertSnippet(fileId: Long, content: String, language: String? = null) =
        withContext(Dispatchers.IO) {
            db.insertSnippet(fileId, content, language)
        }

    // 🔵 Get all projects
    suspend fun getAllProjects() = withContext(Dispatchers.IO) {
        db.getAllProjects().executeAsList()
    }

    // 🔵 Get files by project ID
    suspend fun getFilesByProjectId(projectId: Long) = withContext(Dispatchers.IO) {
        db.getFilesByProjectId(projectId).executeAsList()
    }

    // 🔵 Get snippets by file ID
    suspend fun getSnippetsByFileId(fileId: Long) = withContext(Dispatchers.IO) {
        db.getSnippetsByFileId(fileId).executeAsList()
    }

    suspend fun getProjectId(name: String) = withContext(Dispatchers.IO) {
        db.getProjectId(name).executeAsOneOrNull()
    }

    suspend fun getFileId(projectId: Long) = withContext(Dispatchers.IO) {
        db.getFileId(projectId).executeAsList()
    }
    suspend fun getFileNameByFileId(fileID : Long) = withContext(Dispatchers.IO) {
        db.getFileNameByFileId(fileID).executeAsOne()
    }
    suspend fun getProjectNameByProjectId(projectID : Long) = withContext(Dispatchers.IO) {
        db.getProjectNameByProjectId(projectID).executeAsOne()
    }
    suspend fun getProjectNameByFileId(fileId: Long)= withContext(Dispatchers.IO) {
        val projectId=db.getProjectIdFromFileId(fileId).executeAsOne()
        db.getProjectNameByProjectId(projectId).executeAsOne()
    }

    suspend fun changeProjectName(projectName: String,  existingProjectId: Long)= withContext(Dispatchers.IO){
        val projId= db.getProjectId(projectName).executeAsOne()
        db.updateProjectId(projId, existingProjectId)
        db.deleteProjectById(projId)
    }

    suspend fun clearProjects() = withContext(Dispatchers.IO) {
        db.deleteAllProjects()
    }

    suspend fun clearFiles() = withContext(Dispatchers.IO) {
        db.deleteAllFiles()
    }

    suspend fun clearSnippets() = withContext(Dispatchers.IO) {
        db.deleteAllSnippets()
    }
}
