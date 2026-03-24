package com.weatherdrive.database

import app.cash.sqldelight.db.SqlDriver
import com.weatherdrive.model.FileItem

/**
 * Wraps the SQLDelight-generated [Downloads] database, providing typed CRUD
 * operations for persisting downloaded [FileItem] metadata.
 */
class DownloadDatabase(driver: SqlDriver) {
    private val db = Downloads(driver)

    fun insert(fileItem: FileItem) {
        db.downloadedFilesQueries.insertDownloadedFile(
            googleDriveId = fileItem.googleDriveId,
            title = fileItem.title,
            fileSizeInMB = fileItem.fileSizeInMB.toLong(),
            timeInSeconds = fileItem.timeInSeconds.toLong(),
            largerThan100MB = if (fileItem.largerThan100MB) 1L else 0L
        )
    }

    fun delete(googleDriveId: String) {
        db.downloadedFilesQueries.deleteDownloadedFile(googleDriveId)
    }

    fun getAll(): List<FileItem> {
        return db.downloadedFilesQueries.getAllDownloadedFiles().executeAsList().map { row ->
            FileItem(
                googleDriveId = row.googleDriveId,
                title = row.title,
                fileSizeInMB = row.fileSizeInMB.toInt(),
                timeInSeconds = row.timeInSeconds.toInt(),
                largerThan100MB = row.largerThan100MB != 0L
            )
        }
    }

    fun saveProgress(googleDriveId: String, positionSeconds: Double) {
        db.playbackProgressQueries.upsertPlaybackProgress(googleDriveId, positionSeconds)
    }

    fun getProgress(googleDriveId: String): Double? {
        return db.playbackProgressQueries.getPlaybackProgress(googleDriveId)
            .executeAsOneOrNull()
    }

    fun clearProgress(googleDriveId: String) {
        db.playbackProgressQueries.deletePlaybackProgress(googleDriveId)
    }
}
