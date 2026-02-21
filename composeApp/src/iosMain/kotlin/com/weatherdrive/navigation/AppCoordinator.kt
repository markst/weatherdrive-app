package com.weatherdrive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.interop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.weatherdrive.model.Show
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen
import kotlinx.cinterop.ExperimentalForeignApi
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
     * No-arg constructor for expect/actual compatibility.
     * Creates coordinator with a new UINavigationController.
     */
    actual constructor() : this(UINavigationController())

    init {
        // Set the root view controller when coordinator is created
        val homeVC = ComposeUIViewController {
            HomeScreen(onShowClick = { show -> navigateToShowDetail(show) })
        }
        navigationController.setViewControllers(listOf(homeVC), animated = false)
    }

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

    /**
     * Composable content that wraps the UINavigationController.
     * This allows the coordinator to be used in a Compose hierarchy on iOS.
     */
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun Content() {
        UIKitViewController(
            factory = { navigationController },
            modifier = androidx.compose.ui.Modifier
        )
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
