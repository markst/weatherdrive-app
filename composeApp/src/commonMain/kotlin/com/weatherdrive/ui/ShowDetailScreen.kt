package com.weatherdrive.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.weatherdrive.download.DownloadProgressState
import com.weatherdrive.model.FileItem
import com.weatherdrive.player.PlaybackUiState
import com.weatherdrive.util.formatInfo
import com.weatherdrive.util.formatSpeed
import com.weatherdrive.util.formatTime
import com.weatherdrive.viewmodel.ShowDetailViewModel
import dev.markturnip.radioplayer.PlaybackState
import dev.markturnip.radioplayer.Progress
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Represents the current state of a download operation for UI display.
 */
enum class DownloadStatus {
    IDLE,
    PENDING,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED
}

/**
 * UI state for displaying download progress.
 */
data class DownloadUiState(
    val status: DownloadStatus = DownloadStatus.IDLE,
    val progress: Float = 0f,
    val bytesPerSecond: Long = 0,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val error: String? = null
)

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDetailScreen(
    showId: Long,
    onBack: () -> Unit = {}
) {
    val viewModel: ShowDetailViewModel = koinViewModel { parametersOf(showId) }
    val show by viewModel.show.collectAsState()

    // Show loading state while fetching show data
    if (show == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Loading...") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val currentShow = show!!

    DisposableEffect(showId) {
        onDispose {
            viewModel.stop()
        }
    }
    
    val playbackState by viewModel.playbackState.collectAsState()
    val downloads by viewModel.downloadManager.downloads.collectAsState()
    
    // Map download progress to UI state
    val downloadStates = currentShow.filelist.associate { fileItem ->
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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentShow.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                if (!currentShow.thumbnail.isNullOrBlank()) {
                    AsyncImage(
                        model = currentShow.thumbnail,
                        contentDescription = currentShow.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(MaterialTheme.shapes.medium),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = currentShow.title,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Year: ${currentShow.year}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Category: ${currentShow.category.replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (currentShow.filelist.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Available Files",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(currentShow.filelist) { fileItem ->
                val downloadState = downloadStates[fileItem.googleDriveId] ?: DownloadUiState()
                val isCurrentlyPlaying = playbackState.currentFileId == fileItem.googleDriveId
                FileItemCard(
                    fileItem = fileItem,
                    downloadState = downloadState,
                    isCurrentlyPlaying = isCurrentlyPlaying,
                    playbackState = if (isCurrentlyPlaying) playbackState else null,
                    onDownloadClick = { viewModel.startDownload(fileItem) },
                    onCancelClick = { viewModel.cancelDownload(fileItem) },
                    onPlayClick = { viewModel.playFile(fileItem) },
                    onPauseClick = { viewModel.togglePlayPause() }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun FileItemCard(
    fileItem: FileItem,
    downloadState: DownloadUiState,
    isCurrentlyPlaying: Boolean,
    playbackState: PlaybackUiState?,
    onDownloadClick: () -> Unit,
    onCancelClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (isCurrentlyPlaying) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileItem.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = fileItem.formatInfo(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                PlaybackControlButton(
                    isCurrentlyPlaying = isCurrentlyPlaying,
                    playbackState = playbackState,
                    onPlayClick = onPlayClick,
                    onPauseClick = onPauseClick
                )

                // Download controls
                when (downloadState.status) {
                    DownloadStatus.DOWNLOADING, DownloadStatus.PENDING -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onCancelClick) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel"
                                )
                            }
                        }
                    }
                    DownloadStatus.COMPLETED -> {
                        Text(
                            text = "✓ Downloaded",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    DownloadStatus.FAILED -> {
                        Text(
                            text = "✗ Failed",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    DownloadStatus.IDLE, DownloadStatus.PAUSED -> {
                        IconButton(onClick = onDownloadClick) {
                            Text(
                                text = "⬇",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }

            // Playback progress
            if (isCurrentlyPlaying && playbackState?.progress != null) {
                PlaybackProgressIndicator(progress = playbackState.progress)
            }

            // Download progress
            if (downloadState.status == DownloadStatus.DOWNLOADING || 
                downloadState.status == DownloadStatus.PAUSED) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { downloadState.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(downloadState.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = downloadState.bytesPerSecond.formatSpeed(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (downloadState.status == DownloadStatus.FAILED && downloadState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Error: ${downloadState.error}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Composable displaying playback control button (play/pause/buffering).
 */
@Composable
private fun PlaybackControlButton(
    isCurrentlyPlaying: Boolean,
    playbackState: PlaybackUiState?,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    when {
        isCurrentlyPlaying && playbackState?.playbackState == PlaybackState.BUFFERING -> {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
        isCurrentlyPlaying && playbackState?.playbackState == PlaybackState.PLAYING -> {
            IconButton(onClick = onPauseClick) {
                Icon(
                    imageVector = Icons.Default.Close, // TODO: Replace with pause icon when available
                    contentDescription = "Pause",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        else -> {
            IconButton(onClick = onPlayClick) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play"
                )
            }
        }
    }
}

/**
 * Composable displaying playback progress with elapsed and duration times.
 */
@Composable
private fun PlaybackProgressIndicator(progress: Progress) {
    if (progress.duration > 0) {
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { (progress.elapsed / progress.duration).toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = progress.elapsed.formatTime(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = progress.duration.formatTime(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
