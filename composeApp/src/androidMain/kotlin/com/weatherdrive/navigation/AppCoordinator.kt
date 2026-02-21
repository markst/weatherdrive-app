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
import com.weatherdrive.navigation.routes.HomeRoute
import com.weatherdrive.navigation.routes.ShowDetailRoute
import com.weatherdrive.navigation.routes.toRoute
import com.weatherdrive.navigation.routes.toShow
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen

/**
 * Android implementation of AppCoordinator.
 * Uses Jetpack Compose Navigation with NavHost and NavController.
 */
actual class AppCoordinator actual constructor() {
    private var navController: NavHostController? by mutableStateOf(null)

    /**
     * Composable content that renders the navigation host.
     * Embed this in your App composable.
     */
    @Composable
    actual fun Content() {
        val controller = rememberNavController()
        navController = controller

        NavHost(
            navController = controller,
            startDestination = HomeRoute
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onShowClick = { show -> navigateToShowDetail(show) }
                )
            }
            composable<ShowDetailRoute> { backStackEntry ->
                val route: ShowDetailRoute = backStackEntry.toRoute()
                ShowDetailScreen(
                    show = route.toShow(),
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
