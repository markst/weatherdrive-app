package com.weatherdrive.navigation.routes

import com.weatherdrive.model.Show
import kotlinx.serialization.Serializable

/**
 * Navigation routes for the app.
 */

// Browse tab routes
@Serializable
object BrowseHomeRoute

@Serializable
data class ShowDetailRoute(
    val id: Long
)

// Downloads tab routes
@Serializable
object DownloadsHomeRoute

/**
 * Extension function to convert a Show to a ShowDetailRoute.
 */
fun Show.toRoute(): ShowDetailRoute = ShowDetailRoute(
    id = id
)
