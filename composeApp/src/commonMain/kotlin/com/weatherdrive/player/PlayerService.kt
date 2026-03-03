package com.weatherdrive.player

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
 * Intermediary player service that owns the PlatformMediaPlayer and manages subscriptions.
 * Can be shared across multiple view models for audio playback.
 */
class PlayerService(
    private val mediaPlayer: PlatformMediaPlayer = PlatformMediaPlayer()
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
     * Play a media item.
     */
    fun playItem(mediaItem: MediaPlayerItem) {
        _playbackState.value = _playbackState.value.copy(
            currentFileId = mediaItem.id,
            currentTitle = mediaItem.title
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
