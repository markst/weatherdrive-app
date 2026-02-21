package com.weatherdrive.navigation

import com.weatherdrive.model.Show

/**
 * Android implementation of AppCoordinator.
 * Uses Compose state-based navigation, managed externally via callbacks.
 */
actual class AppCoordinator(
    private val onShowDetail: (Show) -> Unit,
    private val onBack: () -> Unit
) {
    actual fun navigateToShowDetail(show: Show) {
        onShowDetail(show)
    }

    actual fun navigateBack() {
        onBack()
    }
}
