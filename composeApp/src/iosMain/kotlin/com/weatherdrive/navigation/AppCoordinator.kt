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
 * For Compose usage:
 * ```kotlin
 * val coordinator = remember { AppCoordinator() }
 * coordinator.Content()  // Embeds UINavigationController in Compose
 * ```
 * 
 * For Swift/UIKit usage:
 * ```swift
 * let coordinator = AppCoordinator(navigationController: navigationController)
 * let rootVC = coordinator.start()
 * navigationController.setViewControllers([rootVC], animated: false)
 * ```
 */
actual class AppCoordinator(
    private val navigationController: UINavigationController
) {
    private var isInitialized = false

    /**
     * No-arg constructor for expect/actual compatibility.
     * Creates coordinator with a new UINavigationController.
     */
    actual constructor() : this(UINavigationController())

    /**
     * Creates and returns the root HomeScreen view controller.
     * Call this to get the initial view controller for the navigation stack.
     * Use this for direct UIKit integration.
     */
    fun start(): UIViewController {
        return ComposeUIViewController {
            HomeScreen(onShowClick = { show -> navigateToShowDetail(show) })
        }
    }

    /**
     * Composable content that wraps the UINavigationController.
     * Sets up the root view controller on first composition.
     * This allows the coordinator to be used in a Compose hierarchy on iOS.
     */
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun Content() {
        if (!isInitialized) {
            val homeVC = ComposeUIViewController {
                HomeScreen(onShowClick = { show -> navigateToShowDetail(show) })
            }
            navigationController.setViewControllers(listOf(homeVC), animated = false)
            isInitialized = true
        }
        UIKitViewController(
            factory = { navigationController },
            modifier = androidx.compose.ui.Modifier
        )
    }

    actual fun navigateToShowDetail(show: Show) {
        val detailVC = ComposeUIViewController {
            ShowDetailScreen(
                showId = show.id,
                onBack = { navigateBack() }
            )
        }
        navigationController.pushViewController(detailVC, animated = true)
    }

    actual fun navigateBack() {
        navigationController.popViewControllerAnimated(animated = true)
    }
}
