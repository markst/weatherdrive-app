package com.weatherdrive.model

import kotlinx.serialization.Serializable

@Serializable
data class Show(
    val id: String = "",
    val title: String = "",
    val thumbnail: String? = null,
    val year: String = "",
    val category: String = "",
    val filelist: List<FileItem> = emptyList()
)

@Serializable
data class FileItem(
    val title: String = "",
    val googleDriveId: String = "",
    val fileSizeInMB: Int = 0,
    val timeInSeconds: Int = 0,
    val largerThan100MB: Boolean = false
)
