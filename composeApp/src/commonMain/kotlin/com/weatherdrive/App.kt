package com.weatherdrive

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.weatherdrive.navigation.AppCoordinator

@Composable
fun App() {
    val coordinator = remember { AppCoordinator() }

    MaterialTheme {
        coordinator.Content()
    }
}
