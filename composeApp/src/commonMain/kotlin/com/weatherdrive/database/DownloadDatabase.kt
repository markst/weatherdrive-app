package com.weatherdrive.database

import app.cash.sqldelight.db.SqlDriver
import com.weatherdrive.model.FileItem

data class PersistedDownload(
    val fileItem: FileItem,
    val showTitle: String = "",
    val artworkUrl: String? = null
)

/**
 * Wraps the SQLDelight-generated [Downloads] database, providing typed CRUD
 * operations for persisting downloaded [FileItem] metadata.
 */
class DownloadDatabase(driver: SqlDriver) {
    private val db = Downloads(driver)

    fun insert(fileItem: FileItem, showTitle: String = "", artworkUrl: String? = null) {
        db.downloadedFilesQueries.insertDownloadedFile(
            googleDriveId = fileItem.googleDriveId,
            title = fileItem.title,
            fileSizeInMB = fileItem.fileSizeInMB.toLong(),
            timeInSeconds = fileItem.timeInSeconds.toLong(),
            largerThan100MB = if (fileItem.largerThan100MB) 1L else 0L,
            showTitle = showTitle,
            artworkUrl = artworkUrl
        )
    }

    fun delete(googleDriveId: String) {
        db.downloadedFilesQueries.deleteDownloadedFile(googleDriveId)
    }

    fun getAll(): List<PersistedDownload> {
        return db.downloadedFilesQueries.getAllDownloadedFiles().executeAsList().map { row ->
            PersistedDownload(
                fileItem = FileItem(
                    googleDriveId = row.googleDriveId,
                    title = row.title,
                    fileSizeInMB = row.fileSizeInMB.toInt(),
                    timeInSeconds = row.timeInSeconds.toInt(),
                    largerThan100MB = row.largerThan100MB != 0L
                ),
                showTitle = row.showTitle,
                artworkUrl = row.artworkUrl
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
