package com.weatherdrive.viewmodel

import com.weatherdrive.player.PlaybackUiState
import com.weatherdrive.player.PlayerService
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the PlayerView, providing playback state and controls.
 * This ViewModel injects the PlayerService to display and control playback.
 */
class PlayerViewModel(
    private val playerService: PlayerService
) {
    /**
     * The current playback state exposed as a StateFlow.
     */
    val playbackState: StateFlow<PlaybackUiState> = playerService.playbackState
    
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
     * Seek to a position in seconds.
     */
    fun seekTo(position: Double) {
        playerService.seekTo(position)
    }
    
    /**
     * Skip forward or backward by delta seconds.
     */
    fun skip(deltaSeconds: Double) {
        playerService.skip(deltaSeconds)
    }
}
