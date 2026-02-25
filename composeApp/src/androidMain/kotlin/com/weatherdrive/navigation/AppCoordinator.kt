package com.weatherdrive.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.weatherdrive.model.Show
import com.weatherdrive.navigation.routes.BrowseHomeRoute
import com.weatherdrive.navigation.routes.DownloadsHomeRoute
import com.weatherdrive.navigation.routes.ShowDetailRoute
import com.weatherdrive.navigation.routes.toRoute
import com.weatherdrive.ui.DownloadsListScreen
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

/**
 * Android implementation of AppCoordinator.
 * Uses bottom navigation with Scaffold to manage tabs.
 */
actual class AppCoordinator actual constructor() {
    private val browseCoordinator = BrowseCoordinator()
    private val downloadsCoordinator = DownloadsCoordinator()

    /**
     * Composable content that renders the tab bar interface with bottom navigation.
     */
    @Composable
    actual fun Content() {
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Browse") },
                        label = { Text("Browse") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Downloads") },
                        label = { Text("Downloads") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 }
                    )
                }
            }
        ) { paddingValues ->
            when (selectedTab) {
                0 -> {
                    Box(
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        browseCoordinator.Content()
                    }
                }
                1 -> {
                    Box(
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        downloadsCoordinator.Content()
                    }
                }
            }
        }
    }
}
