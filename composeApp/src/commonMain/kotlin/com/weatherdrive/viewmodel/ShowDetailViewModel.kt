package com.weatherdrive.viewmodel

import com.weatherdrive.model.FileItem
import com.weatherdrive.model.Show
import dev.markturnip.radioplayer.MediaPlayerItem
import dev.markturnip.radioplayer.PlaybackState
import dev.markturnip.radioplayer.PlatformMediaPlayer
import dev.markturnip.radioplayer.Progress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UI state for the currently playing item.
 */
data class PlaybackUiState(
    val isPlaying: Boolean = false,
    val currentFileId: String? = null,
    val currentTitle: String? = null,
    val playbackState: PlaybackState = PlaybackState.STOPPED,
    val progress: Progress? = null
)

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
    private val mediaPlayer: PlatformMediaPlayer
) {
    private val _playbackState = MutableStateFlow(PlaybackUiState())
    val playbackState: StateFlow<PlaybackUiState> = _playbackState.asStateFlow()
    
    init {
        mediaPlayer.subscribeState { state ->
            _playbackState.value = _playbackState.value.copy(
                playbackState = state,
                isPlaying = state == PlaybackState.PLAYING
            )
        }
        
        mediaPlayer.subscribeProgress { progress ->
            _playbackState.value = _playbackState.value.copy(progress = progress)
        }
    }
    
    /**
     * Play a file item.
     */
    fun playFile(fileItem: FileItem) {
        val mediaItem = FileItemMediaPlayer(fileItem, show)
        _playbackState.value = _playbackState.value.copy(
            currentFileId = fileItem.googleDriveId,
            currentTitle = fileItem.title
        )
        mediaPlayer.playItem(mediaItem)
    }
    
    /**
     * Toggle play/pause.
     */
    fun togglePlayPause() {
        when (_playbackState.value.playbackState) {
            PlaybackState.PLAYING -> mediaPlayer.pause()
            PlaybackState.PAUSED -> mediaPlayer.play()
            else -> { /* No-op for other states */ }
        }
    }
    
    /**
     * Stop playback.
     */
    fun stop() {
        mediaPlayer.stop()
        _playbackState.value = PlaybackUiState()
    }
    
    /**
     * Seek to a position in seconds.
     */
    fun seekTo(position: Double) {
        mediaPlayer.seekWithPosition(position)
    }
    
    /**
     * Skip forward or backward by delta seconds.
     */
    fun skip(deltaSeconds: Double) {
        mediaPlayer.skip(deltaSeconds)
    }
}
