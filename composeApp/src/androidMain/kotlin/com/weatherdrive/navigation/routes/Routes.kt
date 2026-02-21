package com.weatherdrive.navigation.routes

import com.weatherdrive.model.Show
import kotlinx.serialization.Serializable

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
    val category: String
)

/**
 * Extension function to convert a Show to a ShowDetailRoute.
 */
fun Show.toRoute(): ShowDetailRoute = ShowDetailRoute(
    id = id,
    title = title,
    thumbnail = thumbnail,
    year = year,
    category = category
)

/**
 * Extension function to convert a ShowDetailRoute back to a Show.
 */
fun ShowDetailRoute.toShow(): Show = Show(
    id = id,
    title = title,
    thumbnail = thumbnail,
    year = year,
    category = category
)
