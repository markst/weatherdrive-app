package com.weatherdrive.persistence

import java.io.File

actual fun fileExists(path: String): Boolean = File(path).exists()

actual fun deleteFile(path: String): Boolean {
    return try {
        File(path).delete()
    } catch (e: Exception) {
        false
    }
}
