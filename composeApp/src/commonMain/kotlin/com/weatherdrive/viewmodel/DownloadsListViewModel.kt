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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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

    val persistedProgress: StateFlow<Map<String, Double>> = combine(
        downloads,
        playerService.playbackState.map { it.currentFileId }.distinctUntilChanged()
    ) { currentDownloads, _ ->
        currentDownloads.values
            .filter { it.state == DownloadProgressState.Completed }
            .mapNotNull { download ->
                val id = download.fileItem.googleDriveId
                database.getProgress(id)?.let { id to it }
            }.toMap()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    
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
            title = fileItem.title.ifBlank { downloadProgress.show?.title ?: "" },
            artist = downloadProgress.show?.title ?: "",
            url = localPath,
            isLive = false,
            artworkUrl = downloadProgress.show?.thumbnail
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
