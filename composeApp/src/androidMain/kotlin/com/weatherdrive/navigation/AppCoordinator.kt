package com.weatherdrive.navigation

import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.download.DownloadProgressState
import com.weatherdrive.model.Show
import com.weatherdrive.navigation.routes.HomeRoute
import com.weatherdrive.navigation.routes.ShowDetailRoute
import com.weatherdrive.navigation.routes.toRoute
import com.weatherdrive.navigation.routes.toShow
import com.weatherdrive.ui.DownloadUiState
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen

/**
 * Android implementation of AppCoordinator.
 * Uses Jetpack Compose Navigation with NavHost and NavController.
 */
actual class AppCoordinator actual constructor() {
    private var navController: NavHostController? by mutableStateOf(null)
    private var downloadManager: DownloadManager? = null

    /**
     * Composable content that renders the navigation host.
     * Embed this in your App composable.
     */
    @Composable
    actual fun Content() {
        val controller = rememberNavController()
        navController = controller

        val downloadDirectory = remember {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        }
        val manager = remember { DownloadManager(downloadDirectory) }
        downloadManager = manager

        DisposableEffect(Unit) {
            onDispose {
                manager.close()
            }
        }

        val downloads by manager.downloads.collectAsState()

        NavHost(
            navController = controller,
            startDestination = HomeRoute
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onShowClick = { show -> navigateToShowDetail(show) }
                )
            }
            composable<ShowDetailRoute> { backStackEntry ->
                val route: ShowDetailRoute = backStackEntry.toRoute()
                val show = route.toShow()

                val downloadStates = show.filelist.associate { fileItem ->
                    val downloadProgress = downloads[fileItem.googleDriveId]
                    fileItem.googleDriveId to DownloadUiState(
                        progress = downloadProgress?.progress ?: 0f,
                        isDownloading = downloadProgress?.state == DownloadProgressState.Downloading ||
                                downloadProgress?.state == DownloadProgressState.Pending,
                        isCompleted = downloadProgress?.state == DownloadProgressState.Completed,
                        isFailed = downloadProgress?.state == DownloadProgressState.Failed,
                        isPaused = downloadProgress?.state == DownloadProgressState.Paused,
                        bytesPerSecond = downloadProgress?.bytesPerSecond ?: 0,
                        downloadedBytes = downloadProgress?.downloadedBytes ?: 0,
                        totalBytes = downloadProgress?.totalBytes ?: 0,
                        error = downloadProgress?.error
                    )
                }

                ShowDetailScreen(
                    show = show,
                    downloadStates = downloadStates,
                    onBack = { navigateBack() },
                    onDownloadClick = { fileItem ->
                        manager.startDownload(fileItem)
                    },
                    onCancelClick = { fileItem ->
                        manager.cancelDownload(fileItem)
                    }
                )
            }
        }
    }

    actual fun navigateToShowDetail(show: Show) {
        navController?.navigate(show.toRoute())
    }

    actual fun navigateBack() {
        navController?.popBackStack()
    }
}
