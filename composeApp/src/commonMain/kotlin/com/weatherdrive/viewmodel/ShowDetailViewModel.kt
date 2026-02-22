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
    override val id: String,
    override val title: String,
    override val artist: String,
    override val url: String,
    override val isLive: Boolean,
    override val artworkUrl: String?
) : MediaPlayerItem

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
     * Only plays if the file has been downloaded.
     */
    fun playFile(fileItem: FileItem) {
        val localPath = getLocalFilePath(fileItem) ?: return
        val mediaItem = FileItemMediaPlayer(
            id = fileItem.googleDriveId,
            title = fileItem.title,
            artist = show.title,
            url = localPath,
            isLive = false,
            artworkUrl = show.thumbnail
        )
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
