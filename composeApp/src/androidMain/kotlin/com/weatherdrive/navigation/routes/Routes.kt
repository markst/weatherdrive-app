package com.weatherdrive.navigation.routes

import com.weatherdrive.model.Show
import kotlinx.serialization.Serializable

/**
 * Navigation routes for the app.
 */
@Serializable
object HomeRoute

@Serializable
object DownloadsRoute

@Serializable
data class ShowDetailRoute(
    val id: Long
)

/**
 * Extension function to convert a Show to a ShowDetailRoute.
 */
fun Show.toRoute(): ShowDetailRoute = ShowDetailRoute(
    id = id
)
