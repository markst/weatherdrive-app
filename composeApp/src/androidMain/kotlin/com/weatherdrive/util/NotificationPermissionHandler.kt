package com.weatherdrive.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.weatherdrive.player.PlayerService
import dev.markturnip.radioplayer.PlatformMediaPlayer
import kotlinx.coroutines.launch

/**
 * Handles requesting POST_NOTIFICATIONS permission in response to playback,
 * and starts the foreground media service once granted.
 *
 * Must be created before the activity reaches STARTED (i.e. in onCreate),
 * because [registerForActivityResult] requires it.
 */
class NotificationPermissionHandler(
    private val activity: ComponentActivity,
    private val playerService: PlayerService
) {
    private var hasRequestedPermission = false

    private val permissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            PlatformMediaPlayer.startForegroundServiceIfAllowed()
        }
    }

    /**
     * Start observing playback state. Requests notification permission the
     * first time content begins playing, if not already granted.
     */
    fun observe() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        activity.lifecycleScope.launch {
            playerService.playbackState.collect { state ->
                if (!hasRequestedPermission && state.isPlaying && !isPermissionGranted()) {
                    hasRequestedPermission = true
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun isPermissionGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
}
