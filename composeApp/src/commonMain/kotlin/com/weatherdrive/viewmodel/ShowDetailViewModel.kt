package com.weatherdrive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.model.FileItem
import com.weatherdrive.model.ShowItem
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
    private val _descriptor = MutableStateFlow<ShowItem?>(null)
    val descriptor: StateFlow<ShowItem?> = _descriptor.asStateFlow()

    /** Cached lookup of FileItem by googleDriveId for O(1) stream operations. */
    private var fileItemIndex: Map<String, FileItem> = emptyMap()

    val playbackState: StateFlow<PlaybackUiState> = playerService.playbackState

    init {
        loadShow()
    }

    private fun loadShow() {
        viewModelScope.launch {
            val show = repository.getShowById(showId)
            fileItemIndex = show?.filelist?.associateBy { it.googleDriveId } ?: emptyMap()
            _descriptor.value = show?.let { ShowItem.from(it) }
        }
    }

    /**
     * Play the stream identified by [streamId] using its local file path.
     * Only plays if the file has been downloaded.
     */
    fun playStream(streamId: String) {
        val fileItem = fileItemIndex[streamId] ?: return
        val localPath = downloadManager.getLocalFilePath(fileItem) ?: return
        val mediaItem = FileItemMediaPlayer(
            id = fileItem.googleDriveId,
            title = fileItem.title,
            artist = _descriptor.value?.title ?: "",
            url = localPath,
            isLive = false,
            artworkUrl = _descriptor.value?.thumbnail
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
     * Start downloading the stream identified by [streamId].
     */
    fun startDownload(streamId: String) {
        val fileItem = fileItemIndex[streamId] ?: return
        downloadManager.startDownload(fileItem)
    }

    /**
     * Cancel downloading the stream identified by [streamId].
     */
    fun cancelDownload(streamId: String) {
        val fileItem = fileItemIndex[streamId] ?: return
        downloadManager.cancelDownload(fileItem)
    }
}

