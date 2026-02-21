package com.weatherdrive.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.weatherdrive.model.Show
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen
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
 * Android implementation of AppCoordinator.
 * Uses Jetpack Compose Navigation with NavHost and NavController.
 */
actual class AppCoordinator actual constructor() {
    private lateinit var navController: NavHostController

    /**
     * Composable content that renders the navigation host.
     * Embed this in your App composable.
     */
    @Composable
    actual fun Content() {
        navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = HomeRoute
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onShowClick = { show -> navigateToShowDetail(show) }
                )
            }
            composable<ShowDetailRoute> { backStackEntry ->
                val route: ShowDetailRoute = backStackEntry.toRoute()
                val show = Show(
                    id = route.id,
                    title = route.title,
                    thumbnail = route.thumbnail,
                    year = route.year,
                    category = route.category
                )
                ShowDetailScreen(
                    show = show,
                    onBack = { navigateBack() }
                )
            }
        }
    }

    actual fun navigateToShowDetail(show: Show) {
        navController.navigate(
            ShowDetailRoute(
                id = show.id,
                title = show.title,
                thumbnail = show.thumbnail,
                year = show.year,
                category = show.category
            )
        )
    }

    actual fun navigateBack() {
        navController.popBackStack()
    }
}
