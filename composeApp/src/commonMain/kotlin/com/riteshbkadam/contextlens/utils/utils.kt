package com.riteshbkadam.contextlens.utils


class utils {
    data class WindowMetadata(
        val appName: String?,
        val processName: String?,
        val pid: Long?,
        val windowTitle: String?,
        val documentPath: String?,  // absolute path or URI if available
        val fileName: String?,      // derived from documentPath when present
        val dirPath: String?        // derived from documentPath when present
    )

    // A platform-neutral inspector interface
    interface WindowIntrospector {
        suspend fun readActiveWindow(): WindowMetadata
        suspend fun readAllWindows(limit: Int = 50): List<WindowMetadata>
    }

    interface Paste{
        fun install()
        fun uninstall()
    }
}