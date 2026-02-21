package com.weatherdrive

import androidx.compose.ui.window.ComposeUIViewController

/**
 * Main entry point for iOS. Creates the default App UIViewController.
 */
fun MainViewController() = ComposeUIViewController { App() }

// For native UINavigationController integration on iOS, use AppCoordinator:
//
// ```swift
// let navigationController = UINavigationController()
// let coordinator = AppCoordinator(navigationController: navigationController)
// let rootVC = coordinator.start()
// navigationController.setViewControllers([rootVC], animated: false)
// ```
//
// The coordinator owns the navigation controller and handles all navigation internally.
