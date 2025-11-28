package com.riteshbkadam.contextlens

import androidx.compose.foundation.background
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ContextLens",
    ) {
        val windowIntrospector = JvmWindowIntrospector()
        val dbHelper = DatabaseHelper(createDriver())
        val client = JavaGeminiClient("API")

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            var history by remember { mutableStateOf(listOf<String>()) }
            var alertMessege by remember { mutableStateOf("") }
            LaunchedEffect(Unit) {

                launch {
                    ClipboardPoller.observeClipboard(1000).collectLatest { text ->
                        history = listOf(text) + history
                    }
                }

                launch(Dispatchers.Default) {
                    agentWork(client,windowIntrospector,dbHelper){it->
                        alertMessege=it
                    }
                }
            }

            MaterialTheme {
                Column(Modifier.padding(16.dp)) {
                    Spacer(Modifier.height(16.dp))
                    Spacer(Modifier.height(16.dp))

                    LazyColumn {
                        items(history) { it ->
                            Text(it + alertMessege)
                        }
                    }
                }
            }
        }
    }
}

