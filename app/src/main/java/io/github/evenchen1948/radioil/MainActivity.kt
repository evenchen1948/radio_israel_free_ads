package io.github.evenchen1948.radioil

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.evenchen1948.radioil.player.PlayerViewModel
import io.github.evenchen1948.radioil.ui.RadioScreen
import io.github.evenchen1948.radioil.ui.theme.RadioTheme

class MainActivity : ComponentActivity() {

    // Playback works either way; the permission only controls whether the notification is shown.
    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Transparent bars. Their icons start light and then follow the theme (see SystemBarIcons).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        if (savedInstanceState == null) requestNotificationPermission()
        setContent {
            // The app is Hebrew-only, so the layout is RTL whatever the device language.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val viewModel: PlayerViewModel = viewModel()
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                // Nothing is drawn until the saved theme is known, so a pearl
                // launch never shows the dark theme first.
                state.theme?.let { theme ->
                    RadioTheme(theme) {
                        Surface(color = MaterialTheme.colorScheme.background) {
                            RadioScreen(viewModel)
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
