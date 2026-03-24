package com.weatherdrive.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private data class Tab(
    val label: String,
    val icon: @Composable () -> Unit,
    val content: @Composable () -> Unit
)

/**
 * Android implementation of AppCoordinator.
 * Uses bottom navigation with Scaffold to manage tabs.
 */
actual class AppCoordinator actual constructor() {

    private val browseCoordinator = BrowseCoordinator()
    private val downloadsCoordinator = DownloadsCoordinator()

    private val tabs = listOf(
        Tab(
            label = "Browse",
            icon = { Icon(Icons.Filled.Home, contentDescription = "Browse") },
            content = { browseCoordinator.Content() }
        ),
        Tab(
            label = "Downloads",
            icon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Downloads") },
            content = { downloadsCoordinator.Content() }
        )
    )

    /**
     * Composable content that renders the tab bar interface with bottom navigation.
     */
    @Composable
    actual fun Content() {
        var selectedTab by remember { mutableIntStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            icon = tab.icon,
                            label = { Text(tab.label) },
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                // All tabs are always kept in the composition so each tab's navigation state
                // (NavController back-stack) is preserved when switching between tabs.
                // Visibility is toggled via size: the selected tab fills all available space
                // while unselected tabs are constrained to 0×0 (invisible and non-interactive).
                tabs.forEachIndexed { index, tab ->
                    Box(
                        modifier = if (selectedTab == index) {
                            Modifier.fillMaxSize()
                        } else {
                            Modifier.size(0.dp)
                        }
                    ) {
                        tab.content()
                    }
                }
            }
        }
    }
}
