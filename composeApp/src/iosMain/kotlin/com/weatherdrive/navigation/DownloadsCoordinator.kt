package com.weatherdrive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.interop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.weatherdrive.ui.DownloadsListScreen
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UINavigationController

/**
 * iOS implementation of DownloadsCoordinator.
 * Owns a UINavigationController and handles navigation by pushing/popping view controllers.
 */
actual class DownloadsCoordinator(
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
        val downloadsVC = ComposeUIViewController {
            DownloadsListScreen(showTopBar = false)
        }
        navigationController.setViewControllers(listOf(downloadsVC), animated = false)
    }

    /**
     * Returns the navigation controller for embedding in a tab bar controller.
     */
    fun getNavigationController(): UINavigationController = navigationController

    /**
     * Composable content that wraps the UINavigationController.
     */
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun Content() {
        UIKitViewController(
            factory = { navigationController },
            modifier = androidx.compose.ui.Modifier
        )
    }

    actual fun navigateBack() {
        navigationController.popViewControllerAnimated(animated = true)
    }
}
