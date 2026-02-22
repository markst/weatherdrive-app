package com.weatherdrive.di

import com.weatherdrive.model.Show
import com.weatherdrive.player.PlayerService
import com.weatherdrive.viewmodel.ShowDetailViewModel
import dev.markturnip.radioplayer.PlatformMediaPlayer
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * iOS-specific Koin module providing platform dependencies.
 */
val iosModule = module {
    single { PlatformMediaPlayer() }
    single { PlayerService(get()) }
    
    viewModel { (show: Show) ->
        ShowDetailViewModel(
            show = show,
            playerService = get(),
            getLocalFilePath = { null } // iOS doesn't have download manager yet
        )
    }
}
