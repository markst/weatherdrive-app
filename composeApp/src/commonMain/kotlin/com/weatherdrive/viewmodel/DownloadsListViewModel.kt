package com.weatherdrive.viewmodel

import androidx.lifecycle.ViewModel
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.download.DownloadProgress
import com.weatherdrive.download.DownloadProgressState
import com.weatherdrive.model.FileItem
import com.weatherdrive.player.PlayerService
import dev.markturnip.radioplayer.MediaPlayerItem
import kotlinx.coroutines.flow.StateFlow

private class FileItemMediaPlayer(
    override val id: String,
    override val title: String,
    override val artist: String,
    override val url: String,
    override val isLive: Boolean,
    override val artworkUrl: String?
) : MediaPlayerItem

/**
 * ViewModel for the DownloadsListScreen managing the list of completed downloads.
 */
class DownloadsListViewModel(
    private val downloadManager: DownloadManager,
    private val playerService: PlayerService
) : ViewModel() {
    
    /**
     * StateFlow of all downloads tracked by the DownloadManager.
     */
    val downloads: StateFlow<Map<String, DownloadProgress>> = downloadManager.downloads
    
    /**
     * Returns a list of completed downloads from the current state.
     */
    fun getCompletedDownloads(): List<DownloadProgress> {
        return downloads.value.values.filter { 
            it.state == DownloadProgressState.Completed 
        }
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
    fun playFile(fileItem: FileItem) {
        val localPath = downloadManager.getLocalFilePath(fileItem) ?: return
        val mediaItem = FileItemMediaPlayer(
            id = fileItem.googleDriveId,
            title = fileItem.title,
            artist = "",
            url = localPath,
            isLive = false,
            artworkUrl = null
        )
        playerService.playItem(mediaItem)
    }
}
