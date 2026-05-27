package com.weatherdrive

import android.app.Application
import com.weatherdrive.di.androidModule
import com.weatherdrive.di.commonModule
import dev.markturnip.radioplayer.PlatformMediaPlayer
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WeatherdriveApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PlatformMediaPlayer.initialize(this)
        startKoin {
            androidContext(this@WeatherdriveApp)
            modules(commonModule, androidModule)
        }
    }
}
