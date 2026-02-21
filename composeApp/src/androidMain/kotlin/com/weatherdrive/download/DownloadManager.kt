package com.weatherdrive.download

import com.linroid.ketch.core.DownloadConfig
import com.linroid.ketch.core.DownloadRequest
import com.linroid.ketch.core.DownloadState
import com.linroid.ketch.core.DownloadTask
import com.linroid.ketch.core.Ketch
import com.linroid.ketch.core.QueueConfig
import com.linroid.ketch.ktor.KtorHttpEngine
import com.weatherdrive.model.FileItem
import com.weatherdrive.network.FileAccessResponse
import com.weatherdrive.network.WeatherdriveApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DownloadProgress(
    val fileItem: FileItem,
    val state: DownloadProgressState = DownloadProgressState.Idle,
    val progress: Float = 0f,
    val bytesPerSecond: Long = 0,
    val downloadedBytes: Long = 0,
    val totalBytes: Long = 0,
    val error: String? = null
)

sealed class DownloadProgressState {
    data object Idle : DownloadProgressState()
    data object Pending : DownloadProgressState()
    data object Downloading : DownloadProgressState()
    data object Paused : DownloadProgressState()
    data object Completed : DownloadProgressState()
    data object Failed : DownloadProgressState()
}

class DownloadManager(
    private val downloadDirectory: String
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val api = WeatherdriveApi()

    private val ketch = Ketch(
        httpEngine = KtorHttpEngine(),
        config = DownloadConfig(
            maxConnections = 4,
            queueConfig = QueueConfig(maxConcurrentDownloads = 2)
        )
    )

    private val _downloads = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    val downloads: StateFlow<Map<String, DownloadProgress>> = _downloads.asStateFlow()

    private val activeTasks = mutableMapOf<String, DownloadTask>()

    fun startDownload(fileItem: FileItem) {
        scope.launch {
            try {
                // Update state to pending
                updateDownloadState(fileItem, DownloadProgressState.Pending, progress = 0f)

                // Fetch the file access token
                val fileAccess: FileAccessResponse = api.fetchFileAccess(fileItem.googleDriveId)

                // Build the download URL with authorization
                val downloadUrl = fileAccess.url
                val accessToken = fileAccess.credentials.accessToken

                // Generate filename with sanitized title
                val sanitizedTitle = fileItem.title.replace(Regex("[^a-zA-Z0-9\\s-]"), "").trim()
                val filename = "${fileItem.googleDriveId}_$sanitizedTitle.mp3"

                // Create download request with authorization header
                val request = DownloadRequest(
                    url = downloadUrl,
                    directory = downloadDirectory,
                    filename = filename,
                    headers = mapOf("Authorization" to "Bearer $accessToken")
                )

                val task = ketch.download(request)
                activeTasks[fileItem.googleDriveId] = task

                // Observe task state
                scope.launch {
                    task.state.collect { state ->
                        handleTaskState(fileItem, state)
                    }
                }
            } catch (e: Exception) {
                updateDownloadState(
                    fileItem,
                    DownloadProgressState.Failed,
                    progress = 0f,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun handleTaskState(fileItem: FileItem, state: DownloadState) {
        when (state) {
            is DownloadState.Pending -> {
                updateDownloadState(fileItem, DownloadProgressState.Pending)
            }
            is DownloadState.Downloading -> {
                val progress = state.progress
                _downloads.value = _downloads.value + (fileItem.googleDriveId to DownloadProgress(
                    fileItem = fileItem,
                    state = DownloadProgressState.Downloading,
                    progress = progress.percent,
                    bytesPerSecond = progress.bytesPerSecond,
                    downloadedBytes = progress.downloadedBytes,
                    totalBytes = progress.totalBytes
                ))
            }
            is DownloadState.Paused -> {
                updateDownloadState(fileItem, DownloadProgressState.Paused)
            }
            is DownloadState.Completed -> {
                updateDownloadState(fileItem, DownloadProgressState.Completed, progress = 1f)
                activeTasks.remove(fileItem.googleDriveId)
            }
            is DownloadState.Failed -> {
                updateDownloadState(
                    fileItem,
                    DownloadProgressState.Failed,
                    error = state.error.message
                )
                activeTasks.remove(fileItem.googleDriveId)
            }
            else -> {}
        }
    }

    private fun updateDownloadState(
        fileItem: FileItem,
        state: DownloadProgressState,
        progress: Float? = null,
        error: String? = null
    ) {
        val currentProgress = _downloads.value[fileItem.googleDriveId]
        // For failed or cancelled states, reset progress to 0 if not explicitly provided
        val shouldPreserveProgress = state != DownloadProgressState.Failed && 
                                     state != DownloadProgressState.Idle
        val newProgress = progress ?: if (shouldPreserveProgress) {
            currentProgress?.progress ?: 0f
        } else {
            0f
        }
        
        _downloads.value = _downloads.value + (fileItem.googleDriveId to DownloadProgress(
            fileItem = fileItem,
            state = state,
            progress = newProgress,
            downloadedBytes = if (shouldPreserveProgress) currentProgress?.downloadedBytes ?: 0 else 0,
            totalBytes = if (shouldPreserveProgress) currentProgress?.totalBytes ?: 0 else 0,
            error = error
        ))
    }

    fun pauseDownload(fileItem: FileItem) {
        scope.launch {
            activeTasks[fileItem.googleDriveId]?.pause()
        }
    }

    fun resumeDownload(fileItem: FileItem) {
        scope.launch {
            activeTasks[fileItem.googleDriveId]?.resume()
        }
    }

    fun cancelDownload(fileItem: FileItem) {
        scope.launch {
            activeTasks[fileItem.googleDriveId]?.cancel()
            activeTasks.remove(fileItem.googleDriveId)
            _downloads.value = _downloads.value - fileItem.googleDriveId
        }
    }

    fun close() {
        ketch.close()
    }
}
