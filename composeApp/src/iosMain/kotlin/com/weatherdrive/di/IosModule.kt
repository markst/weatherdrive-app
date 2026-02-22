package com.weatherdrive.di

import com.weatherdrive.player.PlayerService
import dev.markturnip.radioplayer.PlatformMediaPlayer
import org.koin.dsl.module

/**
 * iOS-specific Koin module providing platform dependencies.
 */
val iosModule = module {
    single { PlatformMediaPlayer() }
    single { PlayerService(get()) }
}
