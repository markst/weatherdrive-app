package com.weatherdrive.navigation.routes

import com.weatherdrive.model.Show
import kotlinx.serialization.Serializable

/**
 * Navigation routes for the app.
 */

// Tab routes
@Serializable
object BrowseHomeRoute

@Serializable
object DownloadsHomeRoute

// Detail routes
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
