package com.weatherdrive.download

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation: returns the app's Caches directory for downloads.
 */
actual fun getDownloadDirectory(): String {
    val fileManager = NSFileManager.defaultManager
    val paths = fileManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask)
    val cacheURL = paths.firstOrNull() ?: error("Could not find Caches directory")
    @Suppress("CAST_NEVER_SUCCEEDS")
    val downloadPath = (cacheURL as platform.Foundation.NSURL).path ?: error("Could not get path")
    return "$downloadPath/Downloads"
}
