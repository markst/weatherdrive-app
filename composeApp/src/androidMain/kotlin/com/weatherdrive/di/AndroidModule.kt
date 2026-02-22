package com.weatherdrive.di

import android.os.Environment
import com.weatherdrive.download.DownloadManager
import com.weatherdrive.model.Show
import com.weatherdrive.network.WeatherdriveApi
import com.weatherdrive.player.PlayerService
import com.weatherdrive.viewmodel.ShowDetailViewModel
import dev.markturnip.radioplayer.PlatformMediaPlayer
import org.koin.core.module.dsl.viewModel
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
    single { PlatformMediaPlayer() }
    single { PlayerService(get()) }
    
    viewModel { (show: Show) ->
        val downloadManager: DownloadManager = get()
        ShowDetailViewModel(
            show = show,
            playerService = get(),
            getLocalFilePath = { fileItem -> downloadManager.getLocalFilePath(fileItem) }
        )
    }
}
