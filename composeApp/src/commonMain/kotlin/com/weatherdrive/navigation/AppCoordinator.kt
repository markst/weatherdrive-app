package com.weatherdrive.navigation

import androidx.compose.runtime.Composable

/**
 * Platform-specific app coordinator for the tab bar.
 * 
 * On Android: Uses bottom navigation with NavHost
 * On iOS: Uses UITabBarController to host tab coordinators
 */
expect class AppCoordinator() {
    /**
     * Composable content that renders the tab bar interface.
     */
    @Composable
    fun Content()
}
