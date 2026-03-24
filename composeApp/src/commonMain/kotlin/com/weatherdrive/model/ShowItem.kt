package com.weatherdrive.model

import com.weatherdrive.util.decodeHtml
import com.weatherdrive.util.formatFileSize

/**
 * Interstitial presentation model that transforms a [Show] domain model into
 * display-ready data, including formatted title, category enum, typed streams, and a structured date.
 */
data class ShowItem(
    val id: Long,
    val title: String,
    val category: Category?,
    val date: ShowDate?,
    val thumbnail: String?,
    val streams: List<Stream>,
    val totalDuration: Int,
    val tracklisting: String
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
        fun from(show: Show): ShowItem = ShowItem(
            id = show.id,
            title = show.titles.joinToString(", ") { it.decodeHtml() },
            category = Category.fromValue(show.category),
            date = show.date,
            thumbnail = show.thumbnail,
            streams = show.filelist.map { fileItem ->
                Stream.GoogleDrive(
                    title = fileItem.title.decodeHtml(),
                    fileSize = fileItem.fileSizeInMB.formatFileSize(),
                    id = fileItem.googleDriveId,
                    timeInSeconds = fileItem.timeInSeconds
                )
            },
            totalDuration = show.filelist.sumOf { it.timeInSeconds },
            tracklisting = show.tracklisting.decodeHtml()
        )
    }
}

