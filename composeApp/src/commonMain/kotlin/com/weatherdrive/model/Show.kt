package com.weatherdrive.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Show(
    val id: String = "",
    @SerialName("title")
    val titles: List<String> = emptyList(),
    val thumbnail: String? = null,
    val year: String = "",
    val category: String = "",
    val filelist: List<FileItem> = emptyList()
) {
    val title: String
        get() = titles.joinToString(", ")
}

@Serializable
data class FileItem(
    val title: String = "",
    val googleDriveId: String = "",
    val fileSizeInMB: Int = 0,
    val timeInSeconds: Int = 0,
    val largerThan100MB: Boolean = false
)
