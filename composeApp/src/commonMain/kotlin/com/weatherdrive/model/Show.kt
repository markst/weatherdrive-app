package com.weatherdrive.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Show(
    val id: Long,
    @SerialName("title")
    val titles: List<String> = emptyList(),
    @SerialName("image")
    val thumbnail: String? = null,
    val date: ShowDate? = null,
    val category: String = "",
    val filelist: List<FileItem> = emptyList(),
    val tracklisting: String = ""
) {
    val title: String
        get() = titles.joinToString(", ")

    /** Convenience accessor that returns the year portion of the date. */
    val year: String
        get() = date?.year ?: ""
}

@Serializable
data class FileItem(
    val title: String = "",
    val googleDriveId: String = "",
    val fileSizeInMB: Int = 0,
    val timeInSeconds: Int = 0,
    val largerThan100MB: Boolean = false
)

