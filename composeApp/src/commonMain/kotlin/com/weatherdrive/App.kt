package com.weatherdrive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.weatherdrive.navigation.AppCoordinator
import com.weatherdrive.ui.PlayerView
import com.weatherdrive.viewmodel.PlayerViewModel
import dev.markturnip.expandable.ExpandableSettings
import dev.markturnip.expandable.MinimizableHandler
import dev.markturnip.expandable.expandable
import org.koin.compose.koinInject

@Composable
fun App() {
    val coordinator = remember { AppCoordinator() }
    val scope = rememberCoroutineScope()
    val settings = ExpandableSettings(
        minimizedHeight = 70.dp,
        maximizedHeight = 650.dp,
        bottomPadding = 100.dp,
        expandedBottomPadding = 8.dp,
        cornerRadius = 35.dp
    )
    val handler = remember { MinimizableHandler(scope, settings) }
    val playerViewModel: PlayerViewModel = koinInject()

    MaterialTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            coordinator.Content()
            
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                viewModel = playerViewModel,
                miniHandler = handler
            )
        }
    }
}
