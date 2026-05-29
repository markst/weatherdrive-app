package com.weatherdrive.di

import com.weatherdrive.database.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin module providing platform dependencies.
 */
val androidModule = module {
    single { DatabaseDriverFactory(androidContext()) }
}
