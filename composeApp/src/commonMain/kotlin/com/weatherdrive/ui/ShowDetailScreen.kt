package com.weatherdrive.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.weatherdrive.download.DownloadProgressState
import com.weatherdrive.model.ShowItem
import com.weatherdrive.player.PlaybackUiState
import com.weatherdrive.util.formatDuration
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
    onBack: () -> Unit = {},
    showTopBar: Boolean = true
) {
    val viewModel: ShowDetailViewModel = koinViewModel(
        key = "show_$showId"
    ) { parametersOf(showId) }
    val show by viewModel.show.collectAsState()

    // Show loading state while fetching show data
    if (show == null) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                if (showTopBar) {
                    TopAppBar(
                        title = { Text("Loading...") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        return
    }

    val currentShow = show!!

    val playbackState by viewModel.playbackState.collectAsState()
    val downloads by viewModel.downloadManager.downloads.collectAsState()
    val isFavourite by viewModel.isFavourite.collectAsState()
    val uriHandler = LocalUriHandler.current

    // Map download progress to UI state keyed by stream id
    val downloadStates = currentShow.streams.associate { stream ->
        val downloadProgress = downloads[stream.id]
        stream.id to DownloadUiState(
            status = downloadProgress?.state.toDownloadStatus(),
            progress = downloadProgress?.progress ?: 0f,
            bytesPerSecond = downloadProgress?.bytesPerSecond ?: 0,
            downloadedBytes = downloadProgress?.downloadedBytes ?: 0,
            totalBytes = downloadProgress?.totalBytes ?: 0,
            error = downloadProgress?.error
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.toggleFavourite() }) {
                            Icon(
                                imageVector = if (isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                                tint = if (isFavourite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Hero header
            item {
                ShowDetailHeader(currentShow)
            }

            // Metadata badges
            item {
                ShowMetadataBadges(currentShow)
            }

            // Webpage button
            if (currentShow.webpageUrl != null) {
                item {
                    val label = currentShow.webpageTitle
                        ?.takeIf { it.isNotBlank() }
                        ?: currentShow.webpageUrl
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { uriHandler.openUri(currentShow.webpageUrl) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Streams section
            if (currentShow.streams.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "All episodes",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(currentShow.streams) { stream ->
                    val downloadState = downloadStates[stream.id] ?: DownloadUiState()
                    val isCurrentlyPlaying = playbackState.currentFileId == stream.id
                    StreamCard(
                        stream = stream,
                        downloadState = downloadState,
                        isCurrentlyPlaying = isCurrentlyPlaying,
                        playbackState = if (isCurrentlyPlaying) playbackState else null,
                        onDownloadClick = { viewModel.startDownload(stream.id) },
                        onCancelClick = { viewModel.cancelDownload(stream.id) },
                        onPlayClick = { viewModel.playStream(stream.id) },
                        onPauseClick = { viewModel.togglePlayPause() }
                    )
                }
            }

            // Tracklisting
            if (currentShow.tracklisting.isNotBlank()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Tracklisting",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            SelectionContainer {
                                TracklistingContent(currentShow.tracklisting)
                            }
                        }
                    }
                }
            }

            // Bottom spacing for player
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
private fun ShowDetailHeader(show: ShowItem) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Thumbnail with gradient overlay
        if (!show.thumbnail.isNullOrBlank()) {
            Box {
                AsyncImage(
                    model = show.thumbnail,
                    contentDescription = show.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
                // Gradient overlay fading to background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                )
            }
        }

        // Title overlaid at bottom of hero
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!show.thumbnail.isNullOrBlank()) {
                        Modifier.aspectRatio(16f / 9f)
                    } else {
                        Modifier.padding(top = 8.dp)
                    }
                )
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = show.title.uppercase(),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShowMetadataBadges(show: ShowItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            show.category?.let { category ->
                MetadataBadge(
                    text = category.formattedName,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            show.date?.formatted?.let { dateStr ->
                if (dateStr.isNotBlank()) {
                    MetadataBadge(
                        text = dateStr,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (show.totalDuration > 0) {
                MetadataBadge(
                    text = show.totalDuration.formatDuration(),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (show.streams.isNotEmpty()) {
                MetadataBadge(
                    text = "${show.streams.size} file${if (show.streams.size != 1) "s" else ""}",
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetadataBadge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StreamCard(
    stream: ShowItem.Stream,
    downloadState: DownloadUiState,
    isCurrentlyPlaying: Boolean,
    playbackState: PlaybackUiState?,
    onDownloadClick: () -> Unit,
    onCancelClick: () -> Unit,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentlyPlaying) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stream.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stream.formatInfo(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Download & play controls — must download before playback
                when (downloadState.status) {
                    DownloadStatus.DOWNLOADING, DownloadStatus.PENDING -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = onCancelClick, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    DownloadStatus.COMPLETED -> {
                        // Play button — only shown when downloaded
                        PlaybackControlButton(
                            isCurrentlyPlaying = isCurrentlyPlaying,
                            playbackState = playbackState,
                            onPlayClick = onPlayClick,
                            onPauseClick = onPauseClick
                        )
                    }
                    DownloadStatus.FAILED -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Failed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            // Retry download
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    .clip(CircleShape)
                                    .clickable { onDownloadClick() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⬇",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    DownloadStatus.IDLE, DownloadStatus.PAUSED -> {
                        // Download button — must download before playback
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .clip(CircleShape)
                                .clickable { onDownloadClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⬇",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
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
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { downloadState.progress },
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

@Composable
private fun PlaybackControlButton(
    isCurrentlyPlaying: Boolean,
    playbackState: PlaybackUiState?,
    onPlayClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    when {
        isCurrentlyPlaying && playbackState?.playbackState == PlaybackState.BUFFERING -> {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        isCurrentlyPlaying && playbackState?.playbackState == PlaybackState.PLAYING -> {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .clip(CircleShape)
                    .clickable { onPauseClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        else -> {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .clip(CircleShape)
                    .clickable { onPlayClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun PlaybackProgressIndicator(progress: Progress) {
    if (progress.duration > 0) {
        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { (progress.elapsed / progress.duration).toFloat() },
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

private data class TrackEntry(
    val timestamp: String,
    val title: String,
    val artist: String?
)

private val timestampRegex = Regex("""^\[(\d{1,2}:\d{2}(?::\d{2})?)](.*)$""")

private fun parseTracklisting(raw: String): List<TrackEntry> {
    return raw.lines()
        .filter { it.isNotBlank() }
        .mapNotNull { line ->
            val match = timestampRegex.find(line.trim())
            if (match != null) {
                val timestamp = "[${match.groupValues[1]}]"
                val rest = match.groupValues[2].trim()
                val separatorIndex = rest.indexOfFirst { it == '-' || it == '–' || it == '—' }
                if (separatorIndex > 0) {
                    val title = rest.substring(0, separatorIndex).trim()
                    val artist = rest.substring(separatorIndex + 1).trim()
                    TrackEntry(timestamp, title, artist.ifBlank { null })
                } else {
                    TrackEntry(timestamp, rest, null)
                }
            } else {
                null
            }
        }
}

@Composable
private fun TracklistingContent(tracklisting: String) {
    val tracks = parseTracklisting(tracklisting)

    if (tracks.isEmpty()) {
        // No timestamps found — render as plain text
        Text(
            text = tracklisting,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        tracks.forEach { track ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = track.timestamp,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(88.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                    track.artist?.let { artist ->
                        Text(
                            text = "by $artist",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
