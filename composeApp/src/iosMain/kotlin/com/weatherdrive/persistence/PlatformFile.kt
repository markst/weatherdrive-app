package com.weatherdrive.persistence

import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

actual fun writeFile(path: String, content: String) {
    (content as NSString).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
}

actual fun readFile(path: String): String? {
    return NSString.stringWithContentsOfFile(path, encoding = NSUTF8StringEncoding, error = null) as? String
}

actual fun fileExists(path: String): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(path)
}

actual fun listFiles(directoryPath: String): List<String> {
    val contents = NSFileManager.defaultManager.contentsOfDirectoryAtPath(directoryPath, error = null)
    return contents?.mapNotNull { item ->
        (item as? String)?.let { "$directoryPath/$it" }
    } ?: emptyList()
}

actual fun deleteFile(path: String): Boolean {
    return NSFileManager.defaultManager.removeItemAtPath(path, error = null)
}

