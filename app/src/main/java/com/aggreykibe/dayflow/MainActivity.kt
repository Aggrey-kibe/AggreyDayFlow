package com.aggreykibe.dayflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
        setContent {
            DayFlowTheme {
                DayFlowApp(viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}
