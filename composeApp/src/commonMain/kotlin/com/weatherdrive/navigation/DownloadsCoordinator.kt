package com.weatherdrive.navigation

import androidx.compose.runtime.Composable

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
