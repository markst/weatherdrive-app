package com.weatherdrive.di

import app.cash.sqldelight.db.SqlDriver
import com.weatherdrive.database.DatabaseDriverFactory
import com.weatherdrive.database.DownloadDatabase
import com.weatherdrive.database.FavouriteDatabase
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.network.WeatherdriveApi
import com.weatherdrive.player.PlayerService
import com.weatherdrive.repository.ShowRepository
import com.weatherdrive.viewmodel.DownloadsListViewModel
import com.weatherdrive.viewmodel.HomeViewModel
import com.weatherdrive.viewmodel.PlayerViewModel
import com.weatherdrive.viewmodel.ShowDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Common Koin module providing shared dependencies.
 */
val commonModule = module {
    single { WeatherdriveApi() }
    single { ShowRepository(get()) }
    single<SqlDriver> { get<DatabaseDriverFactory>().createDriver() }
    single { DownloadDatabase(get()) }
    single { FavouriteDatabase(get()) }
    single { PlayerService(database = get()) }
    single { DownloadManager(get<WeatherdriveApi>(), get(), get()) }
    single { PlayerViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { (showId: Long) ->
        ShowDetailViewModel(
            showId = showId,
            repository = get(),
            playerService = get(),
            downloadManager = get(),
            favouriteDatabase = get()
        )
    }
    viewModel { DownloadsListViewModel(get(), get(), get()) }
}
