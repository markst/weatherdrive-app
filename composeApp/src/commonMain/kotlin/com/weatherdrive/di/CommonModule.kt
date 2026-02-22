package com.weatherdrive.di

import com.weatherdrive.network.WeatherdriveApi
import org.koin.dsl.module

/**
 * Common Koin module providing shared dependencies.
 */
val commonModule = module {
    single { WeatherdriveApi() }
}
