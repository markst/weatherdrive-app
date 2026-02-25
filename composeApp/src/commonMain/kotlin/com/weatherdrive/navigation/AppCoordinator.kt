package com.weatherdrive.navigation

import androidx.compose.runtime.Composable
import com.weatherdrive.model.Show

/**
 * Platform-specific coordinator for navigation.
 * 
 * On Android: Owns navigation state and provides Content() composable
 * On iOS: Owns UINavigationController and handles push/pop navigation
 */
expect class AppCoordinator() {
    fun navigateToShowDetail(show: Show)
    fun navigateToDownloads()
    fun navigateBack()

    /**
     * Composable content that renders the current screen.
     * Used on Android to embed navigation in the App composable.
     */
    @Composable
    fun Content()
}
