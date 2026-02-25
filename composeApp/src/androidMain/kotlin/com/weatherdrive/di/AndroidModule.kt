package com.weatherdrive.di

import com.weatherdrive.download.DownloadManager
import com.weatherdrive.network.WeatherdriveApi
import com.weatherdrive.player.PlayerService
import com.weatherdrive.viewmodel.PlayerViewModel
import com.weatherdrive.viewmodel.ShowDetailViewModel
import dev.markturnip.radioplayer.PlatformMediaPlayer
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Android-specific Koin module providing platform dependencies.
 */
val androidModule = module {
    single { DownloadManager(get<WeatherdriveApi>()) }
    single { PlatformMediaPlayer() }
    single { PlayerService(get()) }
    single { PlayerViewModel(get()) }
    
    viewModel { (showId: Long) ->
        ShowDetailViewModel(
            showId = showId,
            repository = get(),
            playerService = get(),
            downloadManager = get()
        )
    }
}
