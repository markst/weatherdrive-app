package com.weatherdrive.util

import com.weatherdrive.model.FileItem
import com.weatherdrive.model.ShowItem
import kotlin.math.roundToInt

/**
 * Formats file information combining size and duration.
 */
fun FileItem.formatInfo(): String {
    val duration = timeInSeconds.formatDuration()
    return "$fileSizeInMB MB • $duration"
}

/**
 * Formats seconds into a human-readable duration string (e.g., "1:23:45" or "23:45").
 */
fun Int.formatDuration(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val secs = this % 60
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
    } else {
        "$minutes:${secs.toString().padStart(2, '0')}"
    }
}

/**
 * Formats seconds (as Double) into a human-readable duration string (e.g., "1:23:45" or "23:45").
 */
fun Double.formatDuration(): String = this.toInt().formatDuration()

/**
 * Formats bytes per second into a human-readable speed string (e.g., "1.5 MB/s", "500 KB/s").
 */
fun Long.formatSpeed(): String {
    return when {
        this >= 1_000_000 -> "${(this / 1_000_000.0).toOneDecimalString()} MB/s"
        this >= 1_000 -> "${(this / 1_000.0).toOneDecimalString()} KB/s"
        else -> "$this B/s"
    }
}

/**
 * Format seconds to MM:SS display format.
 */
fun Double.formatTime(): String {
    val totalSeconds = this.toInt()
    val minutes = totalSeconds / 60
    val secs = totalSeconds % 60
    return "$minutes:${secs.toString().padStart(2, '0')}"
}

/**
 * Sanitizes a string to be used as part of a filename by removing special characters.
 */
fun String.sanitizeForFilename(): String {
    return this.replace(Regex("[^a-zA-Z0-9\\s-]"), "").trim()
}

/**
 * Formats an Int representing megabytes into a human-readable file size string
 * (e.g., "120.5 MB", "500.0 KB").
 */
fun Int.formatFileSize(): String {
    val bytes = this * 1_000_000L
    return when {
        bytes >= 1_000_000 -> "${(bytes / 1_000_000.0).toOneDecimalString()} MB"
        bytes >= 1_000 -> "${(bytes / 1_000.0).toOneDecimalString()} KB"
        else -> "$bytes B"
    }
}

/**
 * Formats stream information combining file size and duration.
 */
fun ShowItem.Stream.formatInfo(): String {
    val duration = timeInSeconds.formatDuration()
    return fileSize?.let { "$it • $duration" } ?: duration
}

private fun Double.toOneDecimalString(): String {
    val rounded = (this * 10).roundToInt() / 10.0
    val whole = rounded.toInt()
    return if (rounded == whole.toDouble()) "$whole.0" else rounded.toString()
}
