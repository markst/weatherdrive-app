package com.weatherdrive.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Platform-specific factory for creating a SQLDelight SqlDriver.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
