package com.weatherdrive

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.weatherdrive.model.Show
import com.weatherdrive.navigation.AppCoordinator
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen

sealed class Screen {
    data object Home : Screen()
    data class ShowDetail(val show: Show) : Screen()
}

@Composable
fun App() {
    var currentScreen: Screen by remember { mutableStateOf(Screen.Home) }

    val coordinator = remember {
        AppCoordinator(
            onShowDetail = { show -> currentScreen = Screen.ShowDetail(show) },
            onBack = { currentScreen = Screen.Home }
        )
    }

    MaterialTheme {
        when (val screen = currentScreen) {
            is Screen.Home -> HomeScreen(
                onShowClick = { show -> coordinator.navigateToShowDetail(show) }
            )
            is Screen.ShowDetail -> ShowDetailScreen(
                show = screen.show,
                onBack = { coordinator.navigateBack() }
            )
        }
    }
}
