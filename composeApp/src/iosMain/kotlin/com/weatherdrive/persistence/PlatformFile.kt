package com.weatherdrive.persistence

import platform.Foundation.NSFileManager

actual fun fileExists(path: String): Boolean {
    return NSFileManager.defaultManager.fileExistsAtPath(path)
}

