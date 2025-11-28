package com.riteshbkadam.contextlens

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
object ClipboardPoller {
    fun observeClipboard(intervalMs: Long = 1000): Flow<String> = flow {
        var lastValue = ""

        while (true) {
            val current = ClipboardManager.getClipboardText()

            if (current != null && current != lastValue) {
                lastValue = current
                if (current.length >= 5) {      // filter small strings
                    emit(current)              // send clipboard text
                }
            }

            delay(intervalMs)
        }
    }
}

data class ProjectMetadata(
    val projectName: String,
    val pid: String?,
    val fileTitle: String,
    val language: String,
    val codeOrigin: String,
    val classificationType: String,
    val confidenceScore: Double
)
fun parseMetadata(jsonString: String): ProjectMetadata {
    val cleanJson = jsonString.trim()
        .trim('`')
        .trim('j')
        .trim('s')
        .trim('o')
        .trim('n')
    val mapper = jacksonObjectMapper()
    return mapper.readValue(cleanJson)
}


