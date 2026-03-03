package com.weatherdrive.persistence

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager

actual fun fileExists(path: String): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(path)
}

@OptIn(ExperimentalForeignApi::class)
actual fun deleteFile(path: String): Boolean {
    return try {
        NSFileManager.defaultManager.removeItemAtPath(path, error = null)
    } catch (e: Exception) {
        false
    }
}
