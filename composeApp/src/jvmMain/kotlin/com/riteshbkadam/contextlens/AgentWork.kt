package com.riteshbkadam.contextlens

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

suspend fun CoroutineScope.agentWork(
    client: JavaGeminiClient,
    windowIntrospector: JvmWindowIntrospector,
    dbHelper: DatabaseHelper,
    onAlert: (String) -> Unit
) {
//    dbHelper.clearFiles()
//    dbHelper.clearProjects()
//    dbHelper.clearSnippets()
    ClipboardPoller.observeClipboard().collect { text ->

        val metadata = windowIntrospector.readActiveWindow()
        println(metadata)

        launch(Dispatchers.IO) {

            val result = client.classifyCode(
                metadata.windowTitle,
                metadata.appName,
                metadata.pid.toString(),
                text
            )

            val meta = parseMetadata(result)
            if(meta.projectName.contentEquals("KCTAFVW")){
                onAlert("Kindly copy the text again with active focused window")
            }


            val projectId = dbHelper.getProjectId(meta.projectName)
            val fileIds = projectId?.let { dbHelper.getFileId(it) }
            if (fileIds != null) {
                for(fileID in fileIds){
                    val existingProjectName=dbHelper.getProjectNameByFileId(fileID)
                    val existingProjectId=dbHelper.getProjectId(existingProjectName)
                    if (meta.fileTitle.contentEquals(dbHelper.getFileNameByFileId(fileID)) && meta.projectName!=existingProjectName && existingProjectId!=null){
                        dbHelper.changeProjectName(meta.projectName,existingProjectId)
                    }
                }
            }
            val savedName = projectId?.let { dbHelper.getProjectNameByProjectId(it) }

            if (savedName == meta.projectName) {
                var bestFileId: Long? = null
                var bestScore = 0.0

                if (fileIds != null) {
                    println(fileIds)
                    for (fid in fileIds) {
                        println(fid)
                        val snippets = dbHelper.getSnippetsByFileId(fid)
                        var score=0.0
                        try{
                            score= client.getSimilarityScore(text, snippets.toString())
                        }catch (e:Exception){
                            println(e)
                        }
                        if (score > bestScore) {
                            bestScore = score
                            bestFileId = fid
                        }
                    }
                }

                if (bestScore >= 0.55 && meta.fileTitle==dbHelper.getFileNameByFileId(bestFileId!!)) {
                    dbHelper.insertSnippet(bestFileId, text, meta.language)
                    println("inserted in project ${dbHelper.getProjectNameByProjectId(projectId)} and in file ${dbHelper.getFileNameByFileId(bestFileId)}")
                    onAlert("Success")

                } else {
                    dbHelper.insertFile(projectId, meta.fileTitle)
                    val newFileId = dbHelper.getFileIdByFileName(meta.fileTitle,projectId)
                    if (newFileId != null) {
                        dbHelper.insertSnippet(newFileId, text, meta.language)
                        println("inserted in project ${dbHelper.getProjectNameByProjectId(projectId)} and in file ${dbHelper.getFileNameByFileId(newFileId)}")

                    }
                    onAlert("Success")
                }

            } else {
                dbHelper.insertProject(meta.projectName)

                val newProjectId = dbHelper.getProjectId(meta.projectName)
                if (newProjectId != null) {
                    dbHelper.insertFile(newProjectId, meta.fileTitle)
                    val fileId = dbHelper.getFileIdByFileName(meta.fileTitle,newProjectId)
                    if (fileId != null) {
                        dbHelper.insertSnippet(fileId, text, meta.language)
                        println("inserted in project ${dbHelper.getProjectNameByProjectId(newProjectId)} and in file ${dbHelper.getFileNameByFileId(fileId)}")

                    }
                }
                onAlert("Success")

            }
        }
    }
}
//}