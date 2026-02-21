package com.weatherdrive

import androidx.compose.ui.window.ComposeUIViewController
import com.weatherdrive.model.Show
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen
import platform.UIKit.UIViewController

/**
 * Main entry point for iOS. Creates the default App UIViewController.
 */
fun MainViewController() = ComposeUIViewController { App() }

/**
 * Creates a UIViewController for the HomeScreen.
 * Use this to integrate with UINavigationController.
 * 
 * @param onShowClick Callback invoked when a show is tapped
 */
fun HomeViewController(onShowClick: (Show) -> Unit): UIViewController {
    return ComposeUIViewController {
        HomeScreen(onShowClick = onShowClick)
    }
}

/**
 * Creates a UIViewController for the ShowDetailScreen.
 * Use this to integrate with UINavigationController.
 * 
 * @param show The show to display details for
 */
fun ShowDetailViewController(show: Show): UIViewController {
    return ComposeUIViewController {
        ShowDetailScreen(show = show)
    }
}
