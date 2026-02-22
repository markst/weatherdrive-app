package com.weatherdrive.navigation

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
import com.weatherdrive.ui.DownloadStatus
import com.weatherdrive.ui.DownloadUiState
import com.weatherdrive.ui.HomeScreen
import com.weatherdrive.ui.ShowDetailScreen
import com.weatherdrive.viewmodel.ShowDetailViewModel
import dev.markturnip.radioplayer.PlatformMediaPlayer
import org.koin.mp.KoinPlatform.getKoin

/**
 * Android implementation of AppCoordinator.
 * Uses Jetpack Compose Navigation with NavHost and NavController.
 */
actual class AppCoordinator actual constructor() {
    private var navController: NavHostController? by mutableStateOf(null)
    private val downloadManager: DownloadManager = getKoin().get()
    private val mediaPlayer: PlatformMediaPlayer = getKoin().get()

    /**
     * Composable content that renders the navigation host.
     * Embed this in your App composable.
     */
    @Composable
    actual fun Content() {
        val controller = rememberNavController()
        navController = controller

        DisposableEffect(Unit) {
            onDispose {
                downloadManager.close()
            }
        }

        val downloads by downloadManager.downloads.collectAsState()

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
                
                val viewModel = remember(show.id) { ShowDetailViewModel(show, mediaPlayer) }
                val playbackState by viewModel.playbackState.collectAsState()

                DisposableEffect(show.id) {
                    onDispose {
                        viewModel.stop()
                    }
                }

                val downloadStates = show.filelist.associate { fileItem ->
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

                ShowDetailScreen(
                    show = show,
                    downloadStates = downloadStates,
                    playbackState = playbackState,
                    onBack = { navigateBack() },
                    onDownloadClick = { fileItem ->
                        downloadManager.startDownload(fileItem)
                    },
                    onCancelClick = { fileItem ->
                        downloadManager.cancelDownload(fileItem)
                    },
                    onPlayClick = { fileItem ->
                        viewModel.playFile(fileItem)
                    },
                    onPauseClick = {
                        viewModel.togglePlayPause()
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
