package com.weatherdrive.di

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
    viewModel { HomeViewModel(get()) }
}
