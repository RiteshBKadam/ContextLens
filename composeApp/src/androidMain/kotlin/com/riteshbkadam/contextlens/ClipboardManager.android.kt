package com.riteshbkadam.contextlens

import app.cash.sqldelight.db.SqlDriver
import com.riteshbkadam.contextlens.utils.utils

actual class JvmWindowIntrospector actual constructor() : utils.WindowIntrospector {
    actual override suspend fun readActiveWindow(): utils.WindowMetadata {
        TODO("Not yet implemented")
    }

    actual override suspend fun readAllWindows(limit: Int): List<utils.WindowMetadata> {
        TODO("Not yet implemented")
    }
}

actual fun createDriver(): SqlDriver {
    TODO("Not yet implemented")
}