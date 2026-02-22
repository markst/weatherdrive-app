package com.weatherdrive.di

import android.os.Environment
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.network.WeatherdriveApi
import org.koin.dsl.module

/**
 * Android-specific Koin module providing platform dependencies.
 */
val androidModule = module {
    single {
        val downloadDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).absolutePath
        DownloadManager(downloadDir, get<WeatherdriveApi>())
    }
}

