package com.weatherdrive.navigation

import com.weatherdrive.model.Show

/**
 * Platform-specific coordinator for navigation.
 * 
 * On Android: Uses Compose state-based navigation
 * On iOS: Exposes ComposeUIViewControllers for native UINavigationController
 */
expect class AppCoordinator(
    onShowDetail: (Show) -> Unit,
    onBack: () -> Unit
) {
    fun navigateToShowDetail(show: Show)
    fun navigateBack()
}
