package com.weatherdrive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.model.FileItem
import com.weatherdrive.model.Show
import com.weatherdrive.player.PlayerService
import com.weatherdrive.player.PlaybackUiState
import com.weatherdrive.repository.ShowRepository
import dev.markturnip.radioplayer.MediaPlayerItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
 * ViewModel for the ShowDetailScreen managing playback and download state.
 */
class ShowDetailViewModel(
    private val showId: Long,
    private val repository: ShowRepository,
    private val playerService: PlayerService,
    val downloadManager: DownloadManager
) : ViewModel() {
    private val _show = MutableStateFlow<Show?>(null)
    val show: StateFlow<Show?> = _show.asStateFlow()

    val playbackState: StateFlow<PlaybackUiState> = playerService.playbackState
    
    init {
        loadShow()
    }

    private fun loadShow() {
        viewModelScope.launch {
            _show.value = repository.getShowById(showId)
        }
    }

    /**
     * Play a file item using its local file path.
     * Only plays if the file has been downloaded.
     */
    fun playFile(fileItem: FileItem) {
        val currentShow = _show.value ?: return
        val localPath = downloadManager.getLocalFilePath(fileItem) ?: return
        val mediaItem = FileItemMediaPlayer(
            id = fileItem.googleDriveId,
            title = fileItem.title,
            artist = currentShow.title,
            url = localPath,
            isLive = false,
            artworkUrl = currentShow.thumbnail
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
    
    /**
     * Start downloading a file.
     */
    fun startDownload(fileItem: FileItem) {
        downloadManager.startDownload(fileItem)
    }
    
    /**
     * Cancel downloading a file.
     */
    fun cancelDownload(fileItem: FileItem) {
        downloadManager.cancelDownload(fileItem)
    }
}
