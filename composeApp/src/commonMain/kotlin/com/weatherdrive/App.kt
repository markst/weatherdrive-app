package com.weatherdrive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.weatherdrive.navigation.AppCoordinator
import com.weatherdrive.ui.PlayerView
import com.weatherdrive.ui.theme.PlayerDimens
import com.weatherdrive.ui.theme.WeatherDriveTheme
import com.weatherdrive.viewmodel.PlayerViewModel
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.markturnip.expandable.ExpandableSettings
import dev.markturnip.expandable.MinimizableHandler
import dev.markturnip.expandable.expandable
import org.koin.compose.koinInject

@Composable
fun App() {
    val coordinator = remember { AppCoordinator() }
    val scope = rememberCoroutineScope()
    val settings = ExpandableSettings(
        minimizedHeight = PlayerDimens.minimizedHeight,
        maximizedHeight = PlayerDimens.maximizedHeight,
        bottomPadding = PlayerDimens.bottomPadding,
        expandedBottomPadding = PlayerDimens.expandedBottomPadding,
        cornerRadius = PlayerDimens.cornerRadius
    )
    val handler = remember { MinimizableHandler(scope, settings) }
    val playerViewModel: PlayerViewModel = koinInject()
    val hazeState = rememberHazeState()

    WeatherDriveTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Wrap content in a haze source so the player can blur what's behind it
            Box(modifier = Modifier.fillMaxSize().hazeSource(state = hazeState)) {
                coordinator.Content()
            }
            
            // Overlay that blocks interaction when player is expanded
            if (handler.transparency > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = handler.transparency))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            handler.collapse(animated = true)
                        }
                )
            }

            PlayerView(
                modifier = Modifier
                    .fillMaxWidth()
                    .expandable(handler = handler, scope = scope)
                    .hazeBlur(
                        input = HazeInput.Sources(hazeState),
                        style = HazeBlurStyle {
                            blurRadius(18.dp)
                            colorEffects(
                                listOf(HazeColorEffect.tint(Color.Gray.copy(alpha = 0.2f))),
                            )
                        },
                    ),
                viewModel = playerViewModel,
                miniHandler = handler
            )
        }
    }
}
