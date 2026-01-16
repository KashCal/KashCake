package org.onekash.kashcake

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import org.onekash.kashcake.ui.screens.home.HomeScreen
import org.onekash.kashcake.ui.screens.settings.SettingsSheet
import org.onekash.kashcake.ui.theme.KashCakeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            KashCakeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KashCakeApp()
                }
            }
        }
    }
}

@Composable
fun KashCakeApp() {
    var showSettings by remember { mutableStateOf(false) }

    HomeScreen(
        onOpenSettings = { showSettings = true }
    )

    if (showSettings) {
        SettingsSheet(onDismiss = { showSettings = false })
    }
}
