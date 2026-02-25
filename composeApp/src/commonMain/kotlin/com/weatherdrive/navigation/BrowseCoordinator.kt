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
