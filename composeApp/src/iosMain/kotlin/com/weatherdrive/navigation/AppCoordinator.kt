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
 * 
 * NOTE: The constructor parameters are required for expect/actual compatibility
 * with the common module declaration, but are unused on iOS as navigation is
 * handled by UINavigationController. Use the companion object factory methods
 * for native iOS navigation integration.
 */
actual class AppCoordinator(
    @Suppress("UNUSED_PARAMETER") onShowDetail: (Show) -> Unit,
    @Suppress("UNUSED_PARAMETER") onBack: () -> Unit
) {
    actual fun navigateToShowDetail(show: Show) {
        // No-op on iOS: UINavigationController handles navigation
    }

    actual fun navigateBack() {
        // No-op on iOS: UINavigationController handles back navigation
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
         * @param onBack Optional callback for back navigation (typically handled by UINavigationController)
         */
        fun showDetailViewController(show: Show, onBack: () -> Unit = {}): UIViewController {
            return ComposeUIViewController {
                ShowDetailScreen(show = show, onBack = onBack)
            }
        }
    }
}
