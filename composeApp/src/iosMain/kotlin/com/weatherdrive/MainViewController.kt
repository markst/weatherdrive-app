package com.weatherdrive

import androidx.compose.ui.window.ComposeUIViewController

/**
 * Main entry point for iOS. Creates the default App UIViewController.
 */
fun MainViewController() = ComposeUIViewController { App() }

// For native UINavigationController integration on iOS, use:
// - AppCoordinator.homeViewController(onShowClick:) for the HomeScreen
// - AppCoordinator.showDetailViewController(show:) for the ShowDetailScreen
// These are available via the AppCoordinator companion object in the navigation package.
