package com.weatherdrive.navigation.routes

import com.weatherdrive.model.FileItem
import com.weatherdrive.model.Show
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Navigation routes for the app.
 */
@Serializable
object HomeRoute

@Serializable
data class ShowDetailRoute(
    val id: String,
    val title: String,
    val thumbnail: String?,
    val year: String,
    val category: String,
    val filelistJson: String = "[]"
)

private val json = Json { ignoreUnknownKeys = true }

/**
 * Extension function to convert a Show to a ShowDetailRoute.
 */
fun Show.toRoute(): ShowDetailRoute = ShowDetailRoute(
    id = id,
    title = title,
    thumbnail = thumbnail,
    year = year,
    category = category,
    filelistJson = json.encodeToString(filelist)
)

/**
 * Extension function to convert a ShowDetailRoute back to a Show.
 */
fun ShowDetailRoute.toShow(): Show = Show(
    id = id,
    title = title,
    thumbnail = thumbnail,
    year = year,
    category = category,
    filelist = json.decodeFromString(filelistJson)
)
