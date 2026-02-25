package com.weatherdrive.persistence

import java.io.File

actual fun writeFile(path: String, content: String) {
    File(path).writeText(content)
}

actual fun readFile(path: String): String? {
    return try {
        File(path).readText()
    } catch (e: Exception) {
        null
    }
}

actual fun fileExists(path: String): Boolean = File(path).exists()

actual fun listFiles(directoryPath: String): List<String> {
    return File(directoryPath).listFiles()?.map { it.absolutePath } ?: emptyList()
}

actual fun deleteFile(path: String): Boolean = File(path).delete()
