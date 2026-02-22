package com.weatherdrive.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.interop.UIKitViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.download.DownloadProgressState
import com.weatherdrive.model.Show
import com.weatherdrive.ui.DownloadStatus
import com.weatherdrive.ui.DownloadUiState
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.compose.koinInject
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
            val downloadManager: DownloadManager = koinInject()
            val downloads by downloadManager.downloads.collectAsState()
            
            val downloadStates = remember(downloads, show.filelist) {
                show.filelist.associate { fileItem ->
                    val downloadProgress = downloads[fileItem.googleDriveId]
                    fileItem.googleDriveId to DownloadUiState(
                        status = downloadProgress?.state.toDownloadStatus(),
                        progress = downloadProgress?.progress ?: 0f,
                        bytesPerSecond = downloadProgress?.bytesPerSecond ?: 0,
                        downloadedBytes = downloadProgress?.downloadedBytes ?: 0,
                        totalBytes = downloadProgress?.totalBytes ?: 0,
                        error = downloadProgress?.error
                    )
                }
            }
            
            ShowDetailScreen(
                show = show,
                downloadStates = downloadStates,
                onBack = { navigateBack() },
                onDownloadClick = { fileItem ->
                    downloadManager.startDownload(fileItem)
                },
                onCancelClick = { fileItem ->
                    downloadManager.cancelDownload(fileItem)
                }
            )
        }
        navigationController.pushViewController(detailVC, animated = true)
    }

    actual fun navigateBack() {
        navigationController.popViewControllerAnimated(animated = true)
    }
}

/**
 * Maps internal DownloadProgressState to UI DownloadStatus enum.
 */
private fun DownloadProgressState?.toDownloadStatus(): DownloadStatus {
    return when (this) {
        is DownloadProgressState.Idle -> DownloadStatus.IDLE
        is DownloadProgressState.Pending -> DownloadStatus.PENDING
        is DownloadProgressState.Downloading -> DownloadStatus.DOWNLOADING
        is DownloadProgressState.Paused -> DownloadStatus.PAUSED
        is DownloadProgressState.Completed -> DownloadStatus.COMPLETED
        is DownloadProgressState.Failed -> DownloadStatus.FAILED
        null -> DownloadStatus.IDLE
    }
}
