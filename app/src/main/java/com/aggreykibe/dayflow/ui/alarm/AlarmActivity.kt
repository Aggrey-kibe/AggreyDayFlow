package com.aggreykibe.dayflow.ui.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.alarm.AlarmService
import com.aggreykibe.dayflow.data.AlarmLog
import com.aggreykibe.dayflow.data.ReminderLookup
import com.aggreykibe.dayflow.data.SettingsStore
import com.aggreykibe.dayflow.domain.AlarmPalette
import com.aggreykibe.dayflow.domain.AlarmStage
import com.aggreykibe.dayflow.domain.ThemeCatalog
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.domain.Repeat
import com.aggreykibe.dayflow.ui.theme.DayFlowTheme
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Full-screen alert shown over the lock screen while an alarm rings. */
class AlarmActivity : ComponentActivity() {
    private var receiverRegistered = false
    private val stoppedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish() // the service stopped ringing (Dismiss, Done, Snooze or time ran out)
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
        val reminder = ReminderLookup.find(this, id)
        if (reminder == null) {
            finish()
            return
        }

        AlarmLog.record(this, AlarmStage.SCREEN_SHOWN, "id=$id")

        // Opened from the fallback notification (Android would not let the service ring from the background)?
        // An open screen is allowed to start the ringing service, so start it now.
        if (!AlarmService.isRinging) AlarmService.tryStart(this, id)

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

        // The alarm uses the theme the person chose, read straight from storage because this screen can
        // open when the main app is not running.
        val settings = SettingsStore(this).load()
        val palette = ThemeCatalog.get(settings.theme).alarm

        setContent {
            DayFlowTheme(theme = settings.theme, mode = settings.themeMode) {
                BackHandler { send(AlarmService.ACTION_STOP) }
                AlarmScreen(
                    reminder = reminder,
                    palette = palette,
                    onDismiss = { send(AlarmService.ACTION_STOP) },
                    onSnooze = { send(AlarmService.ACTION_SNOOZE) },
                    onDone = { send(AlarmService.ACTION_DONE) },
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
    palette: AlarmPalette,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    // Respect the system "remove animations" setting: no pulsing when the person has turned motion off.
    val animate = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
    }
    val clock by produceState(LocalTime.now()) {
        while (true) {
            value = LocalTime.now()
            delay(10_000)
        }
    }
    val clockText = clock.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    val scheduledText = reminder.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    val kind = stringResource(
        when (reminder.repeat) {
            Repeat.ONCE -> R.string.alarm_kind_once
            Repeat.DAILY -> R.string.alarm_kind_daily
            Repeat.WEEKLY -> R.string.alarm_kind_weekly
        },
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(palette.top), Color(palette.bottom))))
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
            AlarmGlow(animate)
            Text(clockText, style = MaterialTheme.typography.displayMedium, color = Color.White)
            Text(
                "$kind · " + stringResource(R.string.alarm_scheduled_for, scheduledText),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                reminder.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            if (reminder.message.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    // A darker panel, never a lighter one: it can only raise the contrast of the white text.
                    color = Color.Black.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        reminder.message,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(palette.accent), contentColor = Color(palette.onAccent)),
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            ) { Text(stringResource(R.string.alarm_dismiss), style = MaterialTheme.typography.titleMedium) }
            OutlinedButton(
                onClick = onSnooze,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.5.dp, Color.White),
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            ) {
                Text(
                    stringResource(R.string.alarm_snooze_minutes, reminder.snoozeMinutes),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            TextButton(
                onClick = onDone,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            ) { Text(stringResource(R.string.alarm_mark_done), style = MaterialTheme.typography.titleMedium) }
        }
    }
}

/** A soft glowing ring around the alarm icon. It only pulses when animations are enabled on the phone. */
@Composable
private fun AlarmGlow(animate: Boolean) {
    val scale = if (animate) {
        val transition = rememberInfiniteTransition(label = "glow")
        val pulse by transition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.10f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 900), RepeatMode.Reverse),
            label = "glowScale",
        )
        pulse
    } else {
        1f
    }
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(168.dp)) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(scale)
                .background(Color.White.copy(alpha = 0.09f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .background(Color.White.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("⏰", fontSize = 56.sp)
        }
    }
}
