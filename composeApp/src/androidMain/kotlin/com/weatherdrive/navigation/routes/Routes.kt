package com.weatherdrive.navigation.routes

import com.weatherdrive.model.ShowSummary
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
    val id: Long,
    /** Blank for downloads persisted before the API migration; the detail screen then falls back to the stored copy. */
    val slug: String
)

/**
 * Extension function to convert a ShowSummary to a ShowDetailRoute.
 */
fun ShowSummary.toRoute(): ShowDetailRoute = ShowDetailRoute(
    id = id,
    slug = slug
)
