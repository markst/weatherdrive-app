package com.weatherdrive.model

import com.weatherdrive.util.decodeHtml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Persisted as JSON by [com.weatherdrive.database.DownloadDatabase]; keep field names and defaults
 * backwards compatible so previously downloaded shows still decode.
 */
@Serializable
data class Show(
    val id: Long,
    /** Lookup key for `/recording/{slug}`. Blank for shows persisted before the API migration. */
    val slug: String = "",
    @SerialName("title")
    val titles: List<String> = emptyList(),
    @SerialName("image")
    val thumbnail: String? = null,
    val date: ShowDate? = null,
    val category: String = "",
    val content: String = "",
    val filelist: List<FileItem> = emptyList(),
    val tracklisting: String = "",
    val webpage: Webpage? = null
) {
    val title: String
        get() = titles.joinToString(", ") { it.decodeHtml() }

    /** Convenience accessor that returns the year portion of the date. */
    val year: String
        get() = date?.year ?: ""
}

/**
 * [fileSizeInMB] and [largerThan100MB] are no longer provided by the API (0/false); they are retained
 * for persisted downloads.
 */
@Serializable
data class FileItem(
    val title: String = "",
    val googleDriveId: String = "",
    val fileSizeInMB: Int = 0,
    val timeInSeconds: Int = 0,
    val largerThan100MB: Boolean = false
)

@Serializable
data class Webpage(
    val url: String,
    val title: String = ""
)
