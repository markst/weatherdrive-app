package com.weatherdrive.di

import android.content.Context
import android.os.Environment
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.network.WeatherdriveApi

/**
 * Simple service locator for dependency injection.
 * Can be initialized with Application context for future context-dependent services.
 */
object ServiceLocator {
    private val api: WeatherdriveApi by lazy { WeatherdriveApi() }
    
    private val downloadManager: DownloadManager by lazy {
        val downloadDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).absolutePath
        DownloadManager(downloadDir, api)
    }
    
    /**
     * Initialize the service locator. Call this before accessing any services.
     * Currently a no-op but kept for future extensibility.
     */
    fun init(context: Context) {
        // Reserved for future context-dependent initialization
    }
    
    fun provideDownloadManager(): DownloadManager = downloadManager
    
    fun provideApi(): WeatherdriveApi = api
}
