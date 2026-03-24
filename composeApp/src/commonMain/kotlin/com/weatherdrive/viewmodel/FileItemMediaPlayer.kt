package com.weatherdrive.viewmodel

import dev.markturnip.radioplayer.MediaPlayerItem

/**
 * Adapter to convert FileItem to MediaPlayerItem.
 */
internal class FileItemMediaPlayer(
    override val id: String,
    override val title: String,
    override val artist: String,
    override val url: String,
    override val isLive: Boolean,
    override val artworkUrl: String?
) : MediaPlayerItem
