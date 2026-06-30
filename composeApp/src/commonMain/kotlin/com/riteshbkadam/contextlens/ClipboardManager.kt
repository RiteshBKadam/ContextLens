package com.riteshbkadam.contextlens
//Testing Conflict
import app.cash.sqldelight.db.SqlDriver
import com.riteshbkadam.contextlens.utils.utils


expect object ClipboardManager {
    fun getClipboardText(): String?

}
// commonMain
expect class JvmWindowIntrospector() : utils.WindowIntrospector {
    override suspend fun readActiveWindow(): utils.WindowMetadata
    override suspend fun readAllWindows(limit: Int): List<utils.WindowMetadata>
}

expect fun createDriver(): SqlDriver

