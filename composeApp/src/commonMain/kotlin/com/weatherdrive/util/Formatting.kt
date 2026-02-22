package com.weatherdrive.util

import com.weatherdrive.model.FileItem

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
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}

/**
 * Formats bytes per second into a human-readable speed string (e.g., "1.5 MB/s", "500 KB/s").
 */
fun Long.formatSpeed(): String {
    return when {
        this >= 1_000_000 -> String.format("%.1f MB/s", this / 1_000_000.0)
        this >= 1_000 -> String.format("%.1f KB/s", this / 1_000.0)
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
    return "%d:%02d".format(minutes, secs)
}

/**
 * Sanitizes a string to be used as part of a filename by removing special characters.
 */
fun String.sanitizeForFilename(): String {
    return this.replace(Regex("[^a-zA-Z0-9\\s-]"), "").trim()
}
