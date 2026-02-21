package com.weatherdrive.navigation

import androidx.compose.ui.window.ComposeUIViewController
import com.weatherdrive.model.Show
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen
import platform.UIKit.UIViewController

/**
 * iOS implementation of AppCoordinator.
 * Exposes ComposeUIViewControllers for use with native UINavigationController.
 * 
 * Each screen is exposed as a separate UIViewController that can be pushed
 * onto a UINavigationController stack from Swift code.
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

    companion object {
        /**
         * Creates a UIViewController for the HomeScreen.
         * @param onShowClick Callback invoked when a show is tapped
         */
        fun homeViewController(onShowClick: (Show) -> Unit): UIViewController {
            return ComposeUIViewController {
                HomeScreen(onShowClick = onShowClick)
            }
        }

        /**
         * Creates a UIViewController for the ShowDetailScreen.
         * @param show The show to display details for
         */
        fun showDetailViewController(show: Show): UIViewController {
            return ComposeUIViewController {
                ShowDetailScreen(show = show)
            }
        }
    }
}
