package com.weatherdrive

import androidx.compose.ui.window.ComposeUIViewController
import com.weatherdrive.di.commonModule
import com.weatherdrive.di.iosModule
import org.koin.core.context.GlobalContext.getKoinApplicationOrNull
import org.koin.core.context.startKoin

/**
 * Main entry point for iOS. Creates the default App UIViewController.
 */
fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}

private fun initKoin() {
    if (getKoinApplicationOrNull() == null) {
        startKoin {
            modules(commonModule, iosModule)
        }
    }
}

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
