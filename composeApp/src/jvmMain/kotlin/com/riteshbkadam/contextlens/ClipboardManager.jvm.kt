package com.riteshbkadam.contextlens

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.riteshbkadam.contextlens.db.ContextLensDatabase
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor

actual object ClipboardManager {
    private var lastText: String? = null

    actual fun getClipboardText(): String? {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        return try {
            clipboard.getData(DataFlavor.stringFlavor) as String
        } catch (e: Exception) {
            null
        }
    }

}

actual fun createDriver(): SqlDriver {
    val driver = JdbcSqliteDriver("jdbc:sqlite:contextlens.db")
    ContextLensDatabase.Schema.create(driver)
    return driver
}