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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

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
