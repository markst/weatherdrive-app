package com.weatherdrive.download

import android.os.Environment

/**
 * Android implementation: returns the public Downloads directory.
 */
actual fun getDownloadDirectory(): String {
    return Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS
    ).absolutePath
}
