package com.weatherdrive.download

import com.linroid.ketch.core.DownloadConfig
import com.linroid.ketch.core.DownloadRequest
import com.linroid.ketch.core.DownloadState
import com.linroid.ketch.core.DownloadTask
import com.linroid.ketch.core.Ketch
import com.linroid.ketch.core.QueueConfig
import com.linroid.ketch.ktor.KtorHttpEngine
import com.weatherdrive.model.FileItem
import com.weatherdrive.network.WeatherdriveApi
import com.weatherdrive.util.sanitizeForFilename
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

/**
 * Manages file downloads using Ketch library.
 * Tracks download progress for multiple files identified by googleDriveId.
 */
class DownloadManager(
    private val api: WeatherdriveApi = WeatherdriveApi()
) {
    private val downloadDirectory: String = getDownloadDirectory()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

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
                setDownloadPending(fileItem)
                val (downloadUrl, accessToken) = fetchFileAccessInfo(fileItem.googleDriveId)
                val filename = generateFilename(fileItem)
                startKetchDownload(fileItem, downloadUrl, accessToken, filename)
            } catch (e: Exception) {
                setDownloadFailed(fileItem, e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun fetchFileAccessInfo(googleDriveId: String): Pair<String, String> {
        val fileAccess = api.fetchFileAccess(googleDriveId)
        // Append alt=media parameter for direct file download from Google Drive
        val downloadUrl = if (fileAccess.url.contains("?")) {
            "${fileAccess.url}&alt=media"
        } else {
            "${fileAccess.url}?alt=media"
        }
        return downloadUrl to fileAccess.credentials.accessToken
    }

    private fun generateFilename(fileItem: FileItem): String {
        val sanitizedTitle = fileItem.title.sanitizeForFilename()
        return "${fileItem.googleDriveId}_$sanitizedTitle.mp3"
    }

    private suspend fun startKetchDownload(
        fileItem: FileItem,
        downloadUrl: String,
        accessToken: String,
        filename: String
    ) {
        val request = DownloadRequest(
            url = downloadUrl,
            directory = downloadDirectory,
            filename = filename,
            headers = mapOf("Authorization" to "Bearer $accessToken")
        )

        val task = ketch.download(request)
        activeTasks[fileItem.googleDriveId] = task

        scope.launch {
            task.state.collect { state ->
                handleTaskState(fileItem, state)
            }
        }
    }

    private fun setDownloadPending(fileItem: FileItem) {
        updateDownload(fileItem.googleDriveId) {
            DownloadProgress(
                fileItem = fileItem,
                state = DownloadProgressState.Pending,
                progress = 0f
            )
        }
    }

    private fun setDownloadFailed(fileItem: FileItem, error: String) {
        updateDownload(fileItem.googleDriveId) {
            DownloadProgress(
                fileItem = fileItem,
                state = DownloadProgressState.Failed,
                progress = 0f,
                error = error
            )
        }
    }

    /**
     * Transforms internal Ketch DownloadState into our own DownloadProgressState.
     * This allows us to decouple our UI state from the library's internal state representation.
     */
    private fun handleTaskState(fileItem: FileItem, state: DownloadState) {
        when (state) {
            is DownloadState.Pending -> {
                updateDownload(fileItem.googleDriveId) { current ->
                    (current ?: DownloadProgress(fileItem = fileItem)).copy(
                        state = DownloadProgressState.Pending
                    )
                }
            }
            is DownloadState.Downloading -> {
                val progress = state.progress
                updateDownload(fileItem.googleDriveId) {
                    DownloadProgress(
                        fileItem = fileItem,
                        state = DownloadProgressState.Downloading,
                        progress = progress.percent,
                        bytesPerSecond = progress.bytesPerSecond,
                        downloadedBytes = progress.downloadedBytes,
                        totalBytes = progress.totalBytes
                    )
                }
            }
            is DownloadState.Paused -> {
                updateDownload(fileItem.googleDriveId) { current ->
                    (current ?: DownloadProgress(fileItem = fileItem)).copy(
                        state = DownloadProgressState.Paused
                    )
                }
            }
            is DownloadState.Completed -> {
                updateDownload(fileItem.googleDriveId) { current ->
                    (current ?: DownloadProgress(fileItem = fileItem)).copy(
                        state = DownloadProgressState.Completed,
                        progress = 1f
                    )
                }
                activeTasks.remove(fileItem.googleDriveId)
            }
            is DownloadState.Failed -> {
                updateDownload(fileItem.googleDriveId) { current ->
                    (current ?: DownloadProgress(fileItem = fileItem)).copy(
                        state = DownloadProgressState.Failed,
                        error = state.error.message
                    )
                }
                activeTasks.remove(fileItem.googleDriveId)
            }
            else -> {}
        }
    }

    /**
     * Updates the download state for a specific googleDriveId using StateFlow.update
     * for thread-safe state mutations.
     */
    private fun updateDownload(
        googleDriveId: String,
        transform: (DownloadProgress?) -> DownloadProgress
    ) {
        _downloads.update { currentMap ->
            currentMap + (googleDriveId to transform(currentMap[googleDriveId]))
        }
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
            _downloads.update { it - fileItem.googleDriveId }
        }
    }

    fun close() {
        ketch.close()
    }

    /**
     * Gets the local file path for a downloaded file.
     * Returns null if the file is not downloaded or download is not completed.
     */
    fun getLocalFilePath(fileItem: FileItem): String? {
        val downloadProgress = _downloads.value[fileItem.googleDriveId]
        return if (downloadProgress?.state == DownloadProgressState.Completed) {
            val filename = generateFilename(fileItem)
            "$downloadDirectory/$filename"
        } else {
            null
        }
    }
}
