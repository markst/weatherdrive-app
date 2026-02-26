package com.weatherdrive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weatherdrive.navigation.routes.DownloadsHomeRoute
import com.weatherdrive.ui.DownloadsListScreen

/**
 * Android implementation of DownloadsCoordinator.
 * Uses Jetpack Compose Navigation with NavHost and NavController.
 */
actual class DownloadsCoordinator actual constructor() {
    private var navController: NavHostController? by mutableStateOf(null)

    /**
     * Composable content that renders the downloads navigation host.
     * Embed this in the tab content.
     */
    @Composable
    actual fun Content() {
        val controller = rememberNavController()
        navController = controller

        NavHost(
            navController = controller,
            startDestination = DownloadsHomeRoute
        ) {
            composable<DownloadsHomeRoute> {
                DownloadsListScreen()
            }
        }
    }

    actual fun navigateBack() {
        navController?.popBackStack()
    }
}
