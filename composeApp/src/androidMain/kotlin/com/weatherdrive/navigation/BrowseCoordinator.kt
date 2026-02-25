package com.weatherdrive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.weatherdrive.model.Show
import com.weatherdrive.navigation.routes.BrowseHomeRoute
import com.weatherdrive.navigation.routes.ShowDetailRoute
import com.weatherdrive.navigation.routes.toRoute
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen

/**
 * Android implementation of BrowseCoordinator.
 * Uses Jetpack Compose Navigation with NavHost and NavController.
 */
actual class BrowseCoordinator actual constructor() {
    private var navController: NavHostController? by mutableStateOf(null)

    /**
     * Composable content that renders the browse navigation host.
     * Embed this in the tab content.
     */
    @Composable
    actual fun Content() {
        val controller = rememberNavController()
        navController = controller

        NavHost(
            navController = controller,
            startDestination = BrowseHomeRoute
        ) {
            composable<BrowseHomeRoute> {
                HomeScreen(
                    onShowClick = { show -> navigateToShowDetail(show) }
                )
            }
            composable<ShowDetailRoute> { backStackEntry ->
                val route: ShowDetailRoute = backStackEntry.toRoute()

                ShowDetailScreen(
                    showId = route.id,
                    onBack = { navigateBack() }
                )
            }
        }
    }

    actual fun navigateToShowDetail(show: Show) {
        navController?.navigate(show.toRoute())
    }

    actual fun navigateBack() {
        navController?.popBackStack()
    }
}
