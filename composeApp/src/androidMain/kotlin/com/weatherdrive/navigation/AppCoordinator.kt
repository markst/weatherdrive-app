package com.weatherdrive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.weatherdrive.model.Show
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen

/**
 * Navigation state representing the current screen.
 */
private sealed class Screen {
    data object Home : Screen()
    data class ShowDetail(val show: Show) : Screen()
}

/**
 * Android implementation of AppCoordinator.
 * Owns the navigation state internally and provides a Content composable for embedding.
 */
actual class AppCoordinator actual constructor() {
    private var currentScreen: Screen by mutableStateOf(Screen.Home)

    /**
     * Composable content that renders the current screen.
     * Embed this in your App composable.
     */
    @Composable
    actual fun Content() {
        when (val screen = currentScreen) {
            is Screen.Home -> HomeScreen(
                onShowClick = { show -> navigateToShowDetail(show) }
            )
            is Screen.ShowDetail -> ShowDetailScreen(
                show = screen.show,
                onBack = { navigateBack() }
            )
        }
    }

    actual fun navigateToShowDetail(show: Show) {
        currentScreen = Screen.ShowDetail(show)
    }

    actual fun navigateBack() {
        currentScreen = Screen.Home
    }
}
