package com.weatherdrive.persistence

/**
 * Returns true if a file exists at the specified path.
 */
expect fun fileExists(path: String): Boolean

/**
 * Deletes the file at the specified path.
 * Returns true if the file was successfully deleted, false otherwise.
 */
expect fun deleteFile(path: String): Boolean
