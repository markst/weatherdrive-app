package com.weatherdrive.download

import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation: returns the app's Caches directory for downloads.
 */
actual fun getDownloadDirectory(): String {
    val fileManager = NSFileManager.defaultManager
    val urls = fileManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask)
    val cacheURL = urls.firstOrNull() as? NSURL ?: error("Could not find Caches directory")
    val cachePath = cacheURL.path ?: error("Could not get path")
    return "$cachePath/Downloads"
}
