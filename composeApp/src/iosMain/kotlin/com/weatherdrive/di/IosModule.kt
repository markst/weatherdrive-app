package com.weatherdrive.di

import com.weatherdrive.database.DatabaseDriverFactory
import org.koin.dsl.module

/**
 * iOS-specific Koin module providing platform dependencies.
 */
val iosModule = module {
    single { DatabaseDriverFactory() }
}
