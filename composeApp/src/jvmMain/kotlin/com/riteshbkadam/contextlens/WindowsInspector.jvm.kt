package com.riteshbkadam.contextlens

import com.riteshbkadam.contextlens.utils.utils
import com.riteshbkadam.contextlens.utils.utils.WindowMetadata
import com.sun.jna.Platform
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File


actual class JvmWindowIntrospector : utils.WindowIntrospector {

    actual override suspend fun readActiveWindow(): WindowMetadata {
        return if (Platform.isWindows()) readActiveWindowWindows() else emptyMeta()
    }

    actual override suspend fun readAllWindows(limit: Int): List<WindowMetadata> {
        return if (Platform.isWindows()) enumerateWindowsWindows(limit) else emptyList()
    }

    private fun readActiveWindowWindows(): WindowMetadata {
        val hwnd: HWND = User32.INSTANCE.GetForegroundWindow() ?: return emptyMeta()
        val title = getWindowTitle(hwnd)
        val pidRef = IntByReference()
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, pidRef)
        val pid = pidRef.value.toLong()
        val exe = queryProcessImage(pid)
        val appName = exe?.substringAfterLast(File.separatorChar)
        val docPath: String? = null // hook UIA here later
        return buildFrom(title, appName, exe, pid, docPath)
    }

    private fun getWindowTitle(hwnd: HWND): String? {
        val len = User32.INSTANCE.GetWindowTextLength(hwnd)
        if (len <= 0) return null
        val buffer = CharArray(len + 1)
        val copied = User32.INSTANCE.GetWindowText(hwnd, buffer, buffer.size)
        return if (copied > 0) Native.toString(buffer) else null
    }

    private fun queryProcessImage(pid: Long): String? {
        val access = WinNT.PROCESS_QUERY_LIMITED_INFORMATION or WinNT.PROCESS_VM_READ
        val hProcess = Kernel32.INSTANCE.OpenProcess(access, false, pid.toInt()) ?: return null
        return try {
            val cap = 2048
            val buf = CharArray(cap)
            val size = IntByReference(cap)
            val ok = Kernel32.INSTANCE.QueryFullProcessImageName(hProcess, 0, buf, size)
            if (ok) String(buf, 0, size.value) else null
        } finally {
            Kernel32.INSTANCE.CloseHandle(hProcess)
        }
    }

    private fun enumerateWindowsWindows(limit: Int): List<WindowMetadata> {
        // Optionally implement EnumWindows and map top-level windows; omitted for brevity
        return emptyList()
    }

    private fun buildFrom(
        title: String?, appName: String?, exe: String?, pid: Long?, docPath: String?
    ): WindowMetadata {
        val fileName = docPath?.let { File(it).name }
        val dir = docPath?.let { File(it).parent }
        return WindowMetadata(
            appName = appName,
            processName = exe?.let { File(it).nameWithoutExtension },
            pid = pid,
            windowTitle = title,
            documentPath = docPath,
            fileName = fileName,
            dirPath = dir
        )
    }

    private fun emptyMeta() = WindowMetadata(null, null, null, null, null, null, null)

    fun createWindowIntrospector(): utils.WindowIntrospector = this

}
