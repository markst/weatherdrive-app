package com.weatherdrive.persistence

/**
 * Writes content to a file at the specified path.
 */
expect fun writeFile(path: String, content: String)

/**
 * Reads and returns the content of a file at the specified path, or null if not found.
 */
expect fun readFile(path: String): String?

/**
 * Returns true if a file exists at the specified path.
 */
expect fun fileExists(path: String): Boolean

/**
 * Returns a list of absolute file paths for all files in the specified directory.
 */
expect fun listFiles(directoryPath: String): List<String>

/**
 * Deletes the file at the specified path. Returns true if successful.
 */
expect fun deleteFile(path: String): Boolean
