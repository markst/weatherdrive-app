package com.weatherdrive.di

import com.weatherdrive.database.DatabaseDriverFactory
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.network.WeatherdriveApi
import com.weatherdrive.player.PlayerService
import com.weatherdrive.viewmodel.DownloadsListViewModel
import com.weatherdrive.viewmodel.PlayerViewModel
import com.weatherdrive.viewmodel.ShowDetailViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Android-specific Koin module providing platform dependencies.
 */
val androidModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { DownloadManager(get<WeatherdriveApi>(), get()) }
    single { PlayerService() }
    single { PlayerViewModel(get()) }
    
    viewModel { (showId: Long) ->
        ShowDetailViewModel(
            showId = showId,
            repository = get(),
            playerService = get(),
            downloadManager = get(),
            favouriteDatabase = get()
        )
    }
    
    viewModel { DownloadsListViewModel(get(), get()) }
}
