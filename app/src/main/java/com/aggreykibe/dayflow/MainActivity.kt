package com.aggreykibe.dayflow

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import com.aggreykibe.dayflow.notifications.NotificationHelper
import com.aggreykibe.dayflow.ui.DayFlowApp
import com.aggreykibe.dayflow.ui.ReminderViewModel
import com.aggreykibe.dayflow.ui.theme.DayFlowTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ReminderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.ensureChannel(this)
        NotificationHelper.ensureAlarmChannel(this)
        setContent {
            val settings = viewModel.settings
            val dark = settings.themeMode.isDark(isSystemInDarkTheme())
            // Keep the status-bar and navigation-bar icons readable when the app's chosen mode
            // differs from the phone's own light/dark setting.
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                )
                onDispose { }
            }
            DayFlowTheme(theme = settings.theme, mode = settings.themeMode) {
                DayFlowApp(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}
