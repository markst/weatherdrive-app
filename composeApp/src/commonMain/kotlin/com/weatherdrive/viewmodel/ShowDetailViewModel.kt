package com.weatherdrive.viewmodel

import com.weatherdrive.model.FileItem
import com.weatherdrive.model.Show
import com.weatherdrive.player.PlayerService
import com.weatherdrive.player.PlaybackUiState
import dev.markturnip.radioplayer.MediaPlayerItem
import kotlinx.coroutines.flow.StateFlow

/**
 * Adapter to convert FileItem to MediaPlayerItem.
 */
private class FileItemMediaPlayer(
    private val fileItem: FileItem,
    private val show: Show,
    private val localFilePath: String?
) : MediaPlayerItem {
    override val id: String = fileItem.googleDriveId
    override val title: String = fileItem.title
    override val artist: String = show.title
    override val url: String = localFilePath ?: ""
    override val isLive: Boolean = false
    override val artworkUrl: String? = show.thumbnail
}

/**
 * ViewModel for the ShowDetailScreen managing playback state.
 */
class ShowDetailViewModel(
    val show: Show,
    private val playerService: PlayerService,
    private val getLocalFilePath: (FileItem) -> String?
) {
    val playbackState: StateFlow<PlaybackUiState> = playerService.playbackState
    
    /**
     * Play a file item using its local file path.
     */
    fun playFile(fileItem: FileItem) {
        val localPath = getLocalFilePath(fileItem)
        if (localPath != null) {
            val mediaItem = FileItemMediaPlayer(fileItem, show, localPath)
            playerService.playItem(mediaItem)
        }
    }
    
    /**
     * Toggle play/pause.
     */
    fun togglePlayPause() {
        playerService.togglePlayPause()
    }
    
    /**
     * Stop playback.
     */
    fun stop() {
        playerService.stop()
    }
}
