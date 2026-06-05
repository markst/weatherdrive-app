package com.weatherdrive

import com.weatherdrive.di.commonModule
import com.weatherdrive.di.iosModule
import org.koin.core.context.startKoin

/**
 * Initialize Koin for iOS. Call this before using any Koin-injected dependencies.
 */
fun initKoin() {
    startKoin {
        modules(commonModule, iosModule)
    }
}
