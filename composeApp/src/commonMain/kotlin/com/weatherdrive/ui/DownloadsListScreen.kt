package com.weatherdrive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.weatherdrive.download.DownloadProgress
import com.weatherdrive.download.DownloadProgressState
import com.weatherdrive.model.FileItem
import com.weatherdrive.player.PlaybackUiState
import com.weatherdrive.util.formatInfo
import com.weatherdrive.util.formatSpeed
import com.weatherdrive.viewmodel.DownloadsListViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsListScreen(
    viewModel: DownloadsListViewModel = koinViewModel<DownloadsListViewModel>(),
    onBack: (() -> Unit)? = null,
    showTopBar: Boolean = true
) {
    val downloads by viewModel.downloads.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val persistedProgress by viewModel.persistedProgress.collectAsState()

    LaunchedEffect(downloads, playbackState.currentFileId) {
        viewModel.refreshProgress()
    }

    val activeDownloads = downloads.values.filter {
        it.state == DownloadProgressState.Downloading ||
        it.state == DownloadProgressState.Pending ||
        it.state == DownloadProgressState.Paused
    }
    val completedDownloads = downloads.values.filter { 
        it.state == DownloadProgressState.Completed 
    }
    val failedDownloads = downloads.values.filter {
        it.state is DownloadProgressState.Failed
    }
    val hasAnyDownloads = activeDownloads.isNotEmpty() || completedDownloads.isNotEmpty() || failedDownloads.isNotEmpty()
    
    var itemToDelete by remember { mutableStateOf<FileItem?>(null) }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text(
                            "Downloads",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black
                        )
                    },
                    navigationIcon = if (onBack != null) {
                        {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    } else {
                        {}
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }
    ) { paddingValues ->
        if (!hasAnyDownloads) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "⬇",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No downloads yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Downloaded files will appear here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Active downloads section
                if (activeDownloads.isNotEmpty()) {
                    item {
                        Text(
                            text = "Downloading",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(
                        items = activeDownloads,
                        key = { "active-${it.fileItem.googleDriveId}" }
                    ) { downloadProgress ->
                        ActiveDownloadCard(downloadProgress = downloadProgress)
                    }
                }

                // Failed downloads section
                if (failedDownloads.isNotEmpty()) {
                    item {
                        Text(
                            text = "Failed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    items(
                        items = failedDownloads,
                        key = { "failed-${it.fileItem.googleDriveId}" }
                    ) { downloadProgress ->
                        ActiveDownloadCard(downloadProgress = downloadProgress)
                    }
                }

                // Completed downloads section
                if (completedDownloads.isNotEmpty()) {
                    item {
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }

                    items(
                        items = completedDownloads,
                        key = { it.fileItem.googleDriveId }
                    ) { downloadProgress ->
                        val fileId = downloadProgress.fileItem.googleDriveId
                        DownloadItemCard(
                            downloadProgress = downloadProgress,
                            playbackState = playbackState,
                            persistedProgressSeconds = persistedProgress[fileId],
                            onPlayPauseClick = {
                                if (playbackState.currentFileId == fileId) {
                                    viewModel.togglePlayPause()
                                } else {
                                    viewModel.playFile(downloadProgress)
                                }
                            },
                            onDeleteClick = { itemToDelete = downloadProgress.fileItem }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
    
    // Delete confirmation dialog
    itemToDelete?.let { fileItem ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            title = { Text("Delete Download", fontWeight = FontWeight.Bold) },
            text = { 
                Text("Are you sure you want to delete \"${fileItem.title}\"? This will remove the downloaded file from your device.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDownload(fileItem)
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun DownloadItemCard(
    downloadProgress: DownloadProgress,
    playbackState: PlaybackUiState,
    persistedProgressSeconds: Double?,
    onPlayPauseClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val fileItem = downloadProgress.fileItem
    val isCurrentItem = playbackState.currentFileId == fileItem.googleDriveId
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentItem) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayPauseProgressButton(
                fileId = fileItem.googleDriveId,
                playbackState = playbackState,
                persistedProgressSeconds = persistedProgressSeconds,
                durationSeconds = fileItem.timeInSeconds,
                onClick = onPlayPauseClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                val displayTitle = fileItem.title.ifBlank { downloadProgress.show?.title ?: "" }
                Text(
                    text = displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (downloadProgress.show?.title?.isNotBlank() == true && fileItem.title.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = downloadProgress.show.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = fileItem.formatInfo(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayPauseProgressButton(
    fileId: String,
    playbackState: PlaybackUiState,
    persistedProgressSeconds: Double?,
    durationSeconds: Int,
    onClick: () -> Unit
) {
    val isCurrentItem = playbackState.currentFileId == fileId
    val isPlaying = isCurrentItem && playbackState.isPlaying
    val progress = if (isCurrentItem) playbackState.progress else null
    val progressFraction = if (progress != null && progress.duration > 0) {
        (progress.elapsed / progress.duration).toFloat().coerceIn(0f, 1f)
    } else if (!isCurrentItem && persistedProgressSeconds != null && durationSeconds > 0) {
        (persistedProgressSeconds / durationSeconds).toFloat().coerceIn(0f, 1f)
    } else null

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(48.dp)
    ) {
        if (progressFraction != null) {
            CircularProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.size(44.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .clip(CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ActiveDownloadCard(downloadProgress: DownloadProgress) {
    val fileItem = downloadProgress.fileItem
    val isFailed = downloadProgress.state is DownloadProgressState.Failed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isFailed) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileItem.title.ifBlank { downloadProgress.show?.title ?: "" },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = fileItem.formatInfo(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isFailed && (downloadProgress.state == DownloadProgressState.Downloading ||
                downloadProgress.state == DownloadProgressState.Paused)) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(downloadProgress.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = downloadProgress.bytesPerSecond.formatSpeed(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isFailed && downloadProgress.error != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = downloadProgress.error ?: "Download failed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
