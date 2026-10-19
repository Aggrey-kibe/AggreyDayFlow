package com.aggreykibe.dayflow.ui.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.alarm.AlarmService
import com.aggreykibe.dayflow.data.ReminderStore
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.domain.Repeat
import com.aggreykibe.dayflow.ui.theme.DayFlowTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Full-screen alert shown over the lock screen while an alarm rings. */
class AlarmActivity : ComponentActivity() {
    private var receiverRegistered = false
    private val stoppedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish() // the service stopped ringing (Stop, Done, Snooze or timeout)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        val id = intent.getLongExtra(AlarmService.EXTRA_REMINDER_ID, -1L)
        val reminder = ReminderStore(this).load().firstOrNull { it.id == id }
        if (reminder == null) {
            finish()
            return
        }

        val filter = IntentFilter(AlarmService.ACTION_ALARM_STOPPED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(stoppedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(stoppedReceiver, filter)
        }
        receiverRegistered = true

        fun send(action: String) {
            AlarmService.command(this, action, id)
            finish()
        }

        setContent {
            DayFlowTheme {
                BackHandler { send(AlarmService.ACTION_STOP) }
                AlarmScreen(
                    reminder = reminder,
                    onDone = { send(AlarmService.ACTION_DONE) },
                    onSnooze = { send(AlarmService.ACTION_SNOOZE) },
                    onStop = { send(AlarmService.ACTION_STOP) },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recreate() // a different reminder started ringing; show it
    }

    override fun onDestroy() {
        if (receiverRegistered) unregisterReceiver(stoppedReceiver)
        super.onDestroy()
    }
}

@Composable
fun AlarmScreen(
    reminder: Reminder,
    onDone: () -> Unit,
    onSnooze: () -> Unit,
    onStop: () -> Unit,
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800), RepeatMode.Reverse),
        label = "bell",
    )
    val timeText = reminder.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    val kind = stringResource(if (reminder.repeat == Repeat.DAILY) R.string.alarm_kind_daily else R.string.alarm_kind_once)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F766E), Color(0xFF042F2E))))
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .scale(scale)
                    .background(Color.White.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("⏰", fontSize = 60.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(timeText, style = MaterialTheme.typography.displayLarge, color = Color.White)
            Text(kind, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.85f))
            Spacer(Modifier.height(8.dp))
            Text(
                reminder.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onDone,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B), contentColor = Color(0xFF1F2937)),
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            ) { Text(stringResource(R.string.alarm_done), style = MaterialTheme.typography.titleMedium) }
            OutlinedButton(
                onClick = onSnooze,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.5.dp, Color.White),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) { Text(stringResource(R.string.alarm_snooze), style = MaterialTheme.typography.titleMedium) }
            TextButton(
                onClick = onStop,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(stringResource(R.string.alarm_stop), style = MaterialTheme.typography.titleMedium) }
        }
    }
}
