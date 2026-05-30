package com.weatherdrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.weatherdrive.player.PlayerService
import com.weatherdrive.util.NotificationPermissionHandler
import org.koin.java.KoinJavaComponent.get

class MainActivity : ComponentActivity() {

    private val notificationPermissionHandler =
        NotificationPermissionHandler(this, get(PlayerService::class.java))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationPermissionHandler.observe()
        setContent {
            App()
        }
    }
}
