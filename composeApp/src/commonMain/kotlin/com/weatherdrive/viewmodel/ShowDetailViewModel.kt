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
    private val show: Show
) : MediaPlayerItem {
    override val id: String = fileItem.googleDriveId
    override val title: String = fileItem.title
    override val artist: String = show.title
    override val url: String = "https://drive.google.com/uc?export=download&id=${fileItem.googleDriveId}"
    override val isLive: Boolean = false
    override val artworkUrl: String? = show.thumbnail
}

/**
 * ViewModel for the ShowDetailScreen managing playback state.
 */
class ShowDetailViewModel(
    val show: Show,
    private val playerService: PlayerService
) {
    val playbackState: StateFlow<PlaybackUiState> = playerService.playbackState
    
    /**
     * Play a file item.
     */
    fun playFile(fileItem: FileItem) {
        val mediaItem = FileItemMediaPlayer(fileItem, show)
        playerService.playItem(mediaItem)
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
