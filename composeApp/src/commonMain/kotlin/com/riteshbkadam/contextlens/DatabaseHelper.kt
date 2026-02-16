package com.riteshbkadam.contextlens

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.SqlDriver
import com.riteshbkadam.contextlens.db.ContextLensDatabase
import com.riteshbkadam.contextlens.db.Files
import com.riteshbkadam.contextlens.db.Projects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
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
    fun getAllProjects(): Flow<Projects>{
        return db.getAllProjects().executeAsList().asFlow()
    }

    // 🔵 Get files by project ID
    fun getFilesByProjectId(projectId: Long): Flow<List<Files>> = callbackFlow {

        val query = db.getFilesByProjectId(projectId)

        // Emit initial value
        trySend(query.executeAsList())

        val listener = Query.Listener {
            trySend(query.executeAsList())
        }

        query.addListener(listener)

        awaitClose {
            query.removeListener(listener)
        }
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
    suspend fun getFileIdByFileName(name: String,projectId: Long) = withContext(Dispatchers.IO) {
        db.getFileIdByFileName(name, projectId).executeAsOneOrNull()
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
