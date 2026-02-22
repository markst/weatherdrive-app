package com.weatherdrive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.weatherdrive.di.androidModule
import com.weatherdrive.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.getKoinApplicationOrNull
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (getKoinApplicationOrNull() == null) {
            startKoin {
                androidContext(this@MainActivity)
                modules(commonModule, androidModule)
            }
        }
        setContent {
            App()
        }
    }
}
