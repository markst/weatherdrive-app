package com.weatherdrive.model

import kotlinx.serialization.Serializable

@Serializable
data class Show(
    val id: String = "",
    val title: String = "",
    val thumbnail: String? = null,
    val year: String = "",
    val category: String = ""
)
