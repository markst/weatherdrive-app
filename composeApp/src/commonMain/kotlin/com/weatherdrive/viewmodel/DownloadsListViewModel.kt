package com.weatherdrive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weatherdrive.database.DownloadDatabase
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.download.DownloadProgress
import com.weatherdrive.download.DownloadProgressState
import com.weatherdrive.model.FileItem
import com.weatherdrive.player.PlaybackUiState
import com.weatherdrive.player.PlayerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the DownloadsListScreen managing the list of completed downloads.
 */
class DownloadsListViewModel(
    private val downloadManager: DownloadManager,
    private val playerService: PlayerService,
    private val database: DownloadDatabase
) : ViewModel() {
    
    /**
     * StateFlow of all downloads tracked by the DownloadManager.
     */
    val downloads: StateFlow<Map<String, DownloadProgress>> = downloadManager.downloads

    /**
     * The current playback state exposed for UI to show play/pause/progress per item.
     */
    val playbackState: StateFlow<PlaybackUiState> = playerService.playbackState

    private val _persistedProgress = MutableStateFlow<Map<String, Double>>(emptyMap())
    val persistedProgress: StateFlow<Map<String, Double>> = _persistedProgress.asStateFlow()

    init {
        loadPersistedProgress()
    }

    private fun loadPersistedProgress() {
        viewModelScope.launch {
            val progress = mutableMapOf<String, Double>()
            downloads.value.values
                .filter { it.state == DownloadProgressState.Completed }
                .forEach { download ->
                    val id = download.fileItem.googleDriveId
                    database.getProgress(id)?.let { position ->
                        progress[id] = position
                    }
                }
            _persistedProgress.value = progress
        }
    }

    /**
     * Refresh persisted progress — call after downloads change.
     */
    fun refreshProgress() {
        loadPersistedProgress()
    }
    
    /**
     * Deletes a download by removing both the file from disk and the database entry.
     */
    fun deleteDownload(fileItem: FileItem) {
        downloadManager.deleteDownload(fileItem)
    }

    /**
     * Play a downloaded file using its local file path.
     */
    fun playFile(downloadProgress: DownloadProgress) {
        val fileItem = downloadProgress.fileItem
        val localPath = downloadManager.getLocalFilePath(fileItem) ?: return
        val mediaItem = FileItemMediaPlayer(
            id = fileItem.googleDriveId,
            title = fileItem.title.ifBlank { downloadProgress.showTitle },
            artist = downloadProgress.showTitle,
            url = localPath,
            isLive = false,
            artworkUrl = downloadProgress.artworkUrl
        )
        playerService.playItem(mediaItem)
    }

    /**
     * Toggle play/pause for the currently active item.
     */
    fun togglePlayPause() {
        playerService.togglePlayPause()
    }
}
