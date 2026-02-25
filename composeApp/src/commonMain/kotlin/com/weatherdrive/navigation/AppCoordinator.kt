package com.weatherdrive.navigation

import androidx.compose.runtime.Composable
import com.weatherdrive.model.Show

/**
 * Platform-specific coordinator for the Browse tab navigation.
 * 
 * On Android: Owns navigation state and provides Content() composable
 * On iOS: Owns UINavigationController and handles push/pop navigation
 */
expect class BrowseCoordinator() {
    fun navigateToShowDetail(show: Show)
    fun navigateBack()

    /**
     * Composable content that renders the current screen.
     * Used on Android to embed navigation in the App composable.
     */
    @Composable
    fun Content()
}

/**
 * Platform-specific coordinator for the Downloads tab navigation.
 * 
 * On Android: Owns navigation state and provides Content() composable
 * On iOS: Owns UINavigationController and handles push/pop navigation
 */
expect class DownloadsCoordinator() {
    fun navigateBack()

    /**
     * Composable content that renders the current screen.
     * Used on Android to embed navigation in the App composable.
     */
    @Composable
    fun Content()
}

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
