package com.weatherdrive.viewmodel

import androidx.lifecycle.ViewModel
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.download.DownloadProgress
import com.weatherdrive.download.DownloadProgressState
import com.weatherdrive.model.FileItem
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * ViewModel for the DownloadsListScreen managing the list of completed downloads.
 */
class DownloadsListViewModel(
    private val downloadManager: DownloadManager
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
}
