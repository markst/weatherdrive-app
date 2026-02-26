package com.weatherdrive.model

import com.weatherdrive.util.formatFileSize

/**
 * Interstitial presentation model that transforms a [Show] domain model into
 * display-ready data, including formatted title, category enum, and typed streams.
 */
data class ShowDescriptor(
    val id: Long,
    val title: String,
    val category: Category?,
    val year: String,
    val thumbnail: String?,
    val streams: List<Stream>,
    val totalDuration: Int
) {
    sealed class Stream {
        abstract val title: String
        abstract val fileSize: String?
        abstract val id: String
        abstract val timeInSeconds: Int

        data class GoogleDrive(
            override val title: String,
            override val fileSize: String?,
            override val id: String,
            override val timeInSeconds: Int
        ) : Stream()

        data class MixCloud(
            override val title: String,
            override val fileSize: String?,
            override val id: String,
            override val timeInSeconds: Int = 0
        ) : Stream()

        data class SoundCloud(
            override val title: String,
            override val fileSize: String?,
            override val id: String,
            override val timeInSeconds: Int = 0
        ) : Stream()
    }

    companion object {
        fun from(show: Show): ShowDescriptor = ShowDescriptor(
            id = show.id,
            title = show.titles.joinToString(", "),
            category = Category.fromValue(show.category),
            year = show.year,
            thumbnail = show.thumbnail,
            streams = show.filelist.map { fileItem ->
                Stream.GoogleDrive(
                    title = fileItem.title,
                    fileSize = fileItem.fileSizeInMB.formatFileSize(),
                    id = fileItem.googleDriveId,
                    timeInSeconds = fileItem.timeInSeconds
                )
            },
            totalDuration = show.filelist.sumOf { it.timeInSeconds }
        )
    }
}
