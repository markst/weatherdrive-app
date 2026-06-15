package com.weatherdrive.download

import android.content.Context
import java.io.File

private lateinit var downloadDir: File

/**
 * Must be called from [android.app.Application.onCreate] before downloads start.
 */
fun initDownloadStorage(context: Context) {
    val base = context.getExternalFilesDir(null) ?: context.filesDir
    downloadDir = File(base, "Downloads").apply { mkdirs() }
}

/**
 * Android implementation: app-scoped external storage.
 *
 * Public directories like [android.os.Environment.DIRECTORY_DOWNLOADS] require
 * MediaStore or legacy storage permissions on API 29+ and will fail with
 * EACCES when written via a direct file path.
 */
actual fun getDownloadDirectory(): String {
    check(::downloadDir.isInitialized) {
        "initDownloadStorage() must be called before getDownloadDirectory()"
    }
    return downloadDir.absolutePath
}
