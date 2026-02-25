package com.weatherdrive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.interop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.weatherdrive.model.Show
import com.weatherdrive.ui.DownloadsListScreen
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UITabBarItem
import platform.UIKit.UIViewController

/**
 * iOS implementation of BrowseCoordinator.
 * Owns a UINavigationController and handles navigation by pushing/popping view controllers.
 */
actual class BrowseCoordinator(
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
            HomeScreen(
                onShowClick = { show -> navigateToShowDetail(show) }
            )
        }
    }

    /**
     * Returns the navigation controller for embedding in a tab bar controller.
     */
    fun getNavigationController(): UINavigationController {
        if (!isInitialized) {
            val homeVC = ComposeUIViewController {
                HomeScreen(
                    onShowClick = { show -> navigateToShowDetail(show) }
                )
            }
            navigationController.setViewControllers(listOf(homeVC), animated = false)
            navigationController.tabBarItem = UITabBarItem(
                title = "Browse",
                image = null,
                tag = 0
            )
            isInitialized = true
        }
        return navigationController
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
                HomeScreen(
                    onShowClick = { show -> navigateToShowDetail(show) }
                )
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

/**
 * iOS implementation of DownloadsCoordinator.
 * Owns a UINavigationController and handles navigation by pushing/popping view controllers.
 */
actual class DownloadsCoordinator(
    private val navigationController: UINavigationController
) {
    private var isInitialized = false

    /**
     * No-arg constructor for expect/actual compatibility.
     * Creates coordinator with a new UINavigationController.
     */
    actual constructor() : this(UINavigationController())

    /**
     * Returns the navigation controller for embedding in a tab bar controller.
     */
    fun getNavigationController(): UINavigationController {
        if (!isInitialized) {
            val downloadsVC = ComposeUIViewController {
                DownloadsListScreen()
            }
            navigationController.setViewControllers(listOf(downloadsVC), animated = false)
            navigationController.tabBarItem = UITabBarItem(
                title = "Downloads",
                image = null,
                tag = 1
            )
            isInitialized = true
        }
        return navigationController
    }

    /**
     * Composable content that wraps the UINavigationController.
     * Sets up the root view controller on first composition.
     */
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun Content() {
        if (!isInitialized) {
            val downloadsVC = ComposeUIViewController {
                DownloadsListScreen()
            }
            navigationController.setViewControllers(listOf(downloadsVC), animated = false)
            isInitialized = true
        }
        UIKitViewController(
            factory = { navigationController },
            modifier = androidx.compose.ui.Modifier
        )
    }

    actual fun navigateBack() {
        navigationController.popViewControllerAnimated(animated = true)
    }
}

/**
 * iOS implementation of AppCoordinator.
 * Owns a UITabBarController and hosts the Browse and Downloads coordinators as tabs.
 */
actual class AppCoordinator(
    private val tabBarController: UITabBarController
) {
    private val browseCoordinator = BrowseCoordinator()
    private val downloadsCoordinator = DownloadsCoordinator()
    private var isInitialized = false

    /**
     * No-arg constructor for expect/actual compatibility.
     * Creates coordinator with a new UITabBarController.
     */
    actual constructor() : this(UITabBarController())

    /**
     * Returns the tab bar controller for direct UIKit integration.
     */
    fun getTabBarController(): UITabBarController {
        if (!isInitialized) {
            setupTabs()
        }
        return tabBarController
    }

    private fun setupTabs() {
        val browseNav = browseCoordinator.getNavigationController()
        val downloadsNav = downloadsCoordinator.getNavigationController()
        
        tabBarController.setViewControllers(listOf(browseNav, downloadsNav), animated = false)
        isInitialized = true
    }

    /**
     * Composable content that wraps the UITabBarController.
     * Sets up the tab view controllers on first composition.
     */
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun Content() {
        if (!isInitialized) {
            setupTabs()
        }
        UIKitViewController(
            factory = { tabBarController },
            modifier = androidx.compose.ui.Modifier
        )
    }
}
