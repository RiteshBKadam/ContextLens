package com.riteshbkadam.contextlens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.TabRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riteshbkadam.contextlens.db.Projects
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlin.collections.mutableListOf
import kotlin.math.exp

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
            var projectTitle by remember { mutableStateOf("") }
            var allProjects by remember { mutableStateOf(mutableListOf<Projects>())}

            LaunchedEffect(Unit){
                allProjects= dbHelper.getAllProjects() as MutableList<Projects>

                windowIntrospector.readActiveWindow().fileName.contains()
            }

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
            var exp by remember { mutableStateOf(false) }
            MaterialTheme {
                Scaffold(){
                    Row(modifier = Modifier.fillMaxWidth()
                        .height(100.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly){
                       Column(verticalArrangement = Arrangement.SpaceEvenly,
                           modifier = Modifier.fillMaxHeight()){
                           Text(
                               text = projectTitle.ifEmpty { "Select Project"},
                               modifier = Modifier
                                   .clickable { exp = true },
                               fontSize = 17.sp

                           )
                           DropdownMenu(expanded = exp,
                               onDismissRequest = {exp=false},
                               modifier = Modifier.wrapContentHeight()
                                   .wrapContentWidth()
                               ) {
                               allProjects.forEach {project ->
                                   DropdownMenuItem(
                                       text = { Text(text =project.name) },
                                       onClick = {
                                           projectTitle= project.name
                                                 exp=false
                                                 },
                                   )
                               }
                           }
                           Text("Files", fontSize = 17.sp)
                       }
                        Text("Snippets", fontSize = 17.sp)
                    }
                }
            }
        }
    }
}


