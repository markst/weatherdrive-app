package com.weatherdrive.di

import com.weatherdrive.database.DatabaseDriverFactory
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.model.Show
import com.weatherdrive.network.WeatherdriveApi
import com.weatherdrive.player.PlayerService
import com.weatherdrive.viewmodel.PlayerViewModel
import com.weatherdrive.viewmodel.ShowDetailViewModel
import dev.markturnip.radioplayer.PlatformMediaPlayer
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * iOS-specific Koin module providing platform dependencies.
 */
val iosModule = module {
    single { DatabaseDriverFactory() }
    single { DownloadManager(get<WeatherdriveApi>(), get()) }
    single { PlatformMediaPlayer() }
    single { PlayerService(get()) }
    single { PlayerViewModel(get()) }
    
    viewModel { (show: Show) ->
        ShowDetailViewModel(
            show = show,
            playerService = get(),
            downloadManager = get()
        )
    }
}
