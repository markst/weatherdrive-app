package com.weatherdrive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.interop.UIKitViewController
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UITabBarController

/**
 * iOS implementation of AppCoordinator.
 * Owns a UITabBarController and hosts the Browse and Downloads coordinators as tabs.
 */
actual class AppCoordinator(
    private val tabBarController: UITabBarController
) {
    private val browseCoordinator = BrowseCoordinator()
    private val downloadsCoordinator = DownloadsCoordinator()

    /**
     * No-arg constructor for expect/actual compatibility.
     * Creates coordinator with a new UITabBarController and sets up the tabs.
     */
    actual constructor() : this(UITabBarController()) {
        setupTabs()
    }

    init {
        // Only setup if tab bar controller is empty (not already set up by secondary constructor)
        if (tabBarController.viewControllers.isNullOrEmpty()) {
            setupTabs()
        }
    }

    private fun setupTabs() {
        val browseNav = browseCoordinator.getNavigationController()
        val downloadsNav = downloadsCoordinator.getNavigationController()
        
        tabBarController.setViewControllers(listOf(browseNav, downloadsNav), animated = false)
    }

    /**
     * Returns the tab bar controller for direct UIKit integration.
     */
    fun getTabBarController(): UITabBarController = tabBarController

    /**
     * Composable content that wraps the UITabBarController.
     */
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun Content() {
        UIKitViewController(
            factory = { tabBarController },
            modifier = androidx.compose.ui.Modifier
        )
    }
}
