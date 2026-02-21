package com.weatherdrive.navigation

import androidx.compose.ui.window.ComposeUIViewController
import com.weatherdrive.model.Show
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController

/**
 * iOS implementation of AppCoordinator.
 * Owns a UINavigationController and handles navigation by pushing/popping view controllers.
 * 
 * Usage from Swift:
 * ```swift
 * let coordinator = AppCoordinator(navigationController: navigationController)
 * let rootVC = coordinator.start()
 * ```
 */
actual class AppCoordinator(
    private val navigationController: UINavigationController
) {
    /**
     * Secondary constructor for expect/actual compatibility.
     * Creates coordinator without navigation controller - use primary constructor instead.
     */
    @Suppress("UNUSED_PARAMETER")
    actual constructor(
        onShowDetail: (Show) -> Unit,
        onBack: () -> Unit
    ) : this(UINavigationController())

    /**
     * Creates and returns the root HomeScreen view controller.
     * Call this to get the initial view controller for the navigation stack.
     */
    fun start(): UIViewController {
        val homeVC = ComposeUIViewController {
            HomeScreen(onShowClick = { show -> navigateToShowDetail(show) })
        }
        return homeVC
    }

    actual fun navigateToShowDetail(show: Show) {
        val detailVC = ComposeUIViewController {
            ShowDetailScreen(show = show, onBack = { navigateBack() })
        }
        navigationController.pushViewController(detailVC, animated = true)
    }

    actual fun navigateBack() {
        navigationController.popViewControllerAnimated(animated = true)
    }
}
