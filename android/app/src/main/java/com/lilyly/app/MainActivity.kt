package com.lilyly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.DisposableEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = AppStore(this)
        setContent {
            DisposableEffect(store.darkTheme) {
                val style = if (store.darkTheme) SystemBarStyle.dark(android.graphics.Color.rgb(16, 15, 22)) else SystemBarStyle.light(android.graphics.Color.rgb(245, 235, 221), android.graphics.Color.rgb(16, 15, 22))
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                onDispose { }
            }
            LilylyApp(store)
        }
    }
}
