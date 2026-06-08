package com.weatherdrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.weatherdrive.ui.theme.DarkSurface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(DarkSurface.value.toInt()),
            navigationBarStyle = SystemBarStyle.dark(DarkSurface.value.toInt())
        )

        setContent {
            App()
        }
    }
}
