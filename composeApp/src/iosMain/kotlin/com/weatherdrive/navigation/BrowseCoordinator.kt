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
 * iOS implementation of BrowseCoordinator.
 * Owns a UINavigationController and handles navigation by pushing/popping view controllers.
 */
actual class BrowseCoordinator(
    private val navigationController: UINavigationController
) {
    /**
     * No-arg constructor for expect/actual compatibility.
     * Creates coordinator with a new UINavigationController and sets up the root view controller.
     */
    actual constructor() : this(UINavigationController()) {
        setupNavigationController()
    }

    init {
        // Only setup if navigation controller is empty (not already set up by secondary constructor)
        if (navigationController.viewControllers.isEmpty()) {
            setupNavigationController()
        }
    }

    private fun setupNavigationController() {
        val homeVC = ComposeUIViewController {
            HomeScreen(
                onShowClick = { show -> navigateToShowDetail(show) },
                showTopBar = false
            )
        }
        navigationController.setViewControllers(listOf(homeVC), animated = false)
    }

    /**
     * Creates and returns the root HomeScreen view controller.
     * Call this to get the initial view controller for the navigation stack.
     * Use this for direct UIKit integration.
     */
    fun start(): UIViewController {
        return ComposeUIViewController {
            HomeScreen(
                onShowClick = { show -> navigateToShowDetail(show) },
                showTopBar = false
            )
        }
    }

    /**
     * Returns the navigation controller for embedding in a tab bar controller.
     */
    fun getNavigationController(): UINavigationController = navigationController

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
            ShowDetailScreen(
                showId = show.id,
                onBack = { navigateBack() },
                showTopBar = false
            )
        }
        navigationController.pushViewController(detailVC, animated = true)
    }

    actual fun navigateBack() {
        navigationController.popViewControllerAnimated(animated = true)
    }
}
