package com.weatherdrive.di

import app.cash.sqldelight.db.SqlDriver
import com.weatherdrive.database.DatabaseDriverFactory
import com.weatherdrive.database.DownloadDatabase
import com.weatherdrive.database.FavouriteDatabase
import com.weatherdrive.network.WeatherdriveApi
import com.weatherdrive.repository.ShowRepository
import com.weatherdrive.viewmodel.HomeViewModel
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
    viewModel { HomeViewModel(get()) }
}
