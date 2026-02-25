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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.weatherdrive.player.PlaybackUiState
import com.weatherdrive.util.formatDuration
import com.weatherdrive.viewmodel.PlayerViewModel
import dev.markturnip.expandable.MinimizableHandler

/**
 * PlayerView composable that displays playback information.
 * Uses the expandable library to show minimized/expanded states.
 *
 * @param modifier The modifier for the composable
 * @param viewModel The PlayerViewModel providing playback state and controls
 * @param miniHandler The MinimizableHandler for expand/collapse functionality
 */
@Composable
fun PlayerView(
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel,
    miniHandler: MinimizableHandler
) {
    val playbackState by viewModel.playbackState.collectAsState()
    
    // Only show player when there's content to display
    if (playbackState.currentTitle == null) {
        return
    }
    
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Expanded view content
        Column(
            modifier = Modifier
                .wrapContentHeight(unbounded = true, align = Alignment.Top)
                .height(miniHandler.settings.maximizedHeight)
        ) {
            // Top expanded section
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(4.0f * miniHandler.fraction.value)
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    playbackState.currentTitle?.let { title ->
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = playbackState.playbackState.name,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // Down chevron button to collapse
                IconButton(onClick = { miniHandler.toggle(animated = true) }) {
                    Text(
                        text = "▼",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            // Center controls section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(4.0f * miniHandler.fraction.value)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                
                // Playback controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Skip backward
                    IconButton(onClick = { viewModel.skip(-15.0) }) {
                        Text(
                            text = "⏪",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    
                    // Play/Pause button
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (playbackState.isPlaying) "⏸" else "▶",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // Skip forward
                    IconButton(onClick = { viewModel.skip(15.0) }) {
                        Text(
                            text = "⏩",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Progress bar
                ProgressSection(playbackState)
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        
        // Minimized player view
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(miniHandler.settings.minimizedHeight)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .alpha(1.0f - (4.0f * miniHandler.fraction.value))
                .fillMaxWidth()
        ) {
            // Mini play/pause button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { viewModel.togglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (playbackState.isPlaying) "⏸" else "▶",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Column(
                verticalArrangement = Arrangement.spacedBy(-4.dp),
                modifier = Modifier.weight(1f)
            ) {
                playbackState.currentTitle?.let { title ->
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = playbackState.playbackState.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ProgressSection(playbackState: PlaybackUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        val progress = playbackState.progress
        val progressFraction = if (progress != null && progress.duration > 0) {
            (progress.elapsed / progress.duration).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
        
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(MaterialTheme.shapes.small),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = (progress?.elapsed ?: 0.0).formatDuration(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = (progress?.duration ?: 0.0).formatDuration(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
