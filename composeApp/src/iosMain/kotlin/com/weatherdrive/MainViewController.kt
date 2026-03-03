package com.weatherdrive

import androidx.compose.ui.window.ComposeUIViewController
import com.weatherdrive.di.commonModule
import com.weatherdrive.di.iosModule
import org.koin.core.context.startKoin

/**
 * Initialize Koin for iOS. Call this before using any Koin-injected dependencies.
 */
fun initKoin() {
    startKoin {
        modules(commonModule, iosModule)
    }
}

/**
 * Main entry point for iOS. Creates the default App UIViewController.
 * Note: Call initKoin() in your AppDelegate before using this.
 */
fun MainViewController() = ComposeUIViewController {
    App()
}

// For native UINavigationController integration on iOS, use AppCoordinator:
//
// ```swift
// // In AppDelegate or early initialization:
// MainViewControllerKt.initKoin()
//
// let navigationController = UINavigationController()
// let coordinator = AppCoordinator(navigationController: navigationController)
// let rootVC = coordinator.start()
// navigationController.setViewControllers([rootVC], animated: false)
// ```
//
// The coordinator owns the navigation controller and handles all navigation internally.
