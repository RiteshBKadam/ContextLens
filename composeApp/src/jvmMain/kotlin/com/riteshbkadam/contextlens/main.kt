package com.riteshbkadam.contextlens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.riteshbkadam.contextlens.db.Files
import com.riteshbkadam.contextlens.db.Snippets
import com.riteshbkadam.contextlens.ui.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ContextLens - Developer Clipboard",
    ) {
        val dbHelper = DatabaseHelper(createDriver())
        val windowIntrospector = JvmWindowIntrospector()
        val client =
            JavaGeminiClient("API_KEY") // Note: Consider moving API key to config

        ContextLensTheme {
            var allProjects by remember { mutableStateOf(listOf<String>()) }
            var selectedProject by remember { mutableStateOf("") }
            var selectedFileId by remember { mutableStateOf<Long?>(null) }
            var allSnippets by remember { mutableStateOf(listOf<String>()) }
            val coroutineScope=rememberCoroutineScope()
            var isLoadingSnippets by remember { mutableStateOf(false) }
            var projectId by remember { mutableStateOf<Long?>(null) }

            LaunchedEffect(Unit) {
                dbHelper.getAllProjects().collect { project ->
                    if (!allProjects.contains(project.name)) {
                        allProjects = (allProjects + project.name).distinct()
                    }
                    if (selectedProject.isEmpty() && allProjects.isNotEmpty()) {
                        selectedProject = allProjects.first()
                    }
                }
            }


            // 2. Cascade 1: When Project Changes -> Fetch Files

            LaunchedEffect(selectedProject) {
                projectId = if (selectedProject.isNotEmpty()) {
                    dbHelper.getProjectId(selectedProject)
                } else {
                    null
                }
            }


            val allFiles by projectId?.let {
                dbHelper.getFilesByProjectId(projectId!!)
                    .collectAsState(initial = emptyList())
            } ?: remember { mutableStateOf(emptyList()) }



            LaunchedEffect(selectedFileId) {
                selectedFileId?.let { fileId ->
                    isLoadingSnippets = true
                    allSnippets = fetchingSnippets(dbHelper,fileId)
                    isLoadingSnippets = false
                }
            }
            // Background Workers (Clipboard and Agent Logic)
            LaunchedEffect(Unit) {

                launch {
                    ClipboardPoller.observeClipboard(1000).collectLatest { text ->
                        // Optional: Handle clipboard updates if needed for UI feedback
                    }
                }

                launch(Dispatchers.Default) {
                    agentWork(client, windowIntrospector, dbHelper) { message ->
                    }
                }
            }

            Scaffold(
            ) { paddingValues ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Column(modifier = Modifier.weight(0.3f)) {
                        ProjectSelector(
                            selectedProject = selectedProject,
                            allProjects = allProjects,
                            onProjectSelected = { selectedProject = it },
                            modifier = Modifier
                        )

                        FileSidebar(
                            files = allFiles,
                            selectedFileId = selectedFileId,
                            onFileSelected = { selectedFileId = it.id },
                        )

                    }
                    VerticalDivider(thickness = 0.5.dp,)
                    Column(modifier = Modifier.weight(0.7f)) {
                        SnippetPanel(
                            snippets = allSnippets,
                            onCopySnippet = {
                            },
                            onDeleteSnippet = { /* Handle delete */ },
                            onEditSnippet = { /* Handle edit */ },
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            }
        }
    }
}

private suspend fun fetchingSnippets(dbHelper: DatabaseHelper, fileId: Long): List<String> {
    return dbHelper.getSnippetsByFileId(fileId)
}



