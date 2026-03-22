package com.weatherdrive.player

import com.weatherdrive.database.DownloadDatabase
import dev.markturnip.radioplayer.MediaPlayerItem
import dev.markturnip.radioplayer.PlaybackState
import dev.markturnip.radioplayer.PlatformMediaPlayer
import dev.markturnip.radioplayer.Progress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

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
 *
 * @param mediaPlayer The underlying platform media player.
 * @param database The database used to persist playback progress.
 * @param persistIntervalSeconds How often (in seconds) progress is written to the database.
 */
class PlayerService(
    private val mediaPlayer: PlatformMediaPlayer = PlatformMediaPlayer(),
    private val database: DownloadDatabase,
    private val persistIntervalSeconds: Int = 10
) {
    private val _playbackState = MutableStateFlow(PlaybackUiState())
    val playbackState: StateFlow<PlaybackUiState> = _playbackState.asStateFlow()

    private var lastPersistedTime: Long = 0L

    init {
        mediaPlayer.subscribeState { state ->
            _playbackState.value = _playbackState.value.copy(
                playbackState = state,
                isPlaying = state == PlaybackState.PLAYING
            )
            if (state == PlaybackState.STOPPED || state == PlaybackState.COMPLETED) {
                val currentFileId = _playbackState.value.currentFileId
                if (currentFileId != null) {
                    database.clearProgress(currentFileId)
                }
            }
        }
        
        mediaPlayer.subscribeProgress { progress ->
            _playbackState.value = _playbackState.value.copy(progress = progress)
            val currentFileId = _playbackState.value.currentFileId ?: return@subscribeProgress
            val now = Clock.System.now().toEpochMilliseconds()
            if (now - lastPersistedTime >= persistIntervalSeconds * 1000L) {
                lastPersistedTime = now
                database.saveProgress(currentFileId, progress.elapsed)
            }
        }
    }
    
    /**
     * Play a media item, resuming from a previously saved position if available.
     */
    fun playItem(mediaItem: MediaPlayerItem) {
        _playbackState.value = _playbackState.value.copy(
            currentFileId = mediaItem.id,
            currentTitle = mediaItem.title
        )
        mediaPlayer.playItem(mediaItem)
        val savedPosition = database.getProgress(mediaItem.id)
        if (savedPosition != null && savedPosition > 0.0) {
            mediaPlayer.seekWithPosition(savedPosition)
        }
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
