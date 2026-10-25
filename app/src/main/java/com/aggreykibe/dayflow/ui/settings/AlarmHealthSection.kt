package com.aggreykibe.dayflow.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.alarm.AlarmHealth
import com.aggreykibe.dayflow.data.AlarmLog
import com.aggreykibe.dayflow.data.TestAlarm
import com.aggreykibe.dayflow.domain.AlarmLogCodec
import com.aggreykibe.dayflow.domain.HealthCheck
import com.aggreykibe.dayflow.domain.HealthReport
import com.aggreykibe.dayflow.notifications.NotificationHelper
import com.aggreykibe.dayflow.permissions.OemSettings
import com.aggreykibe.dayflow.permissions.PermissionHelper
import com.aggreykibe.dayflow.ui.theme.Spacing
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Lets the person (and the developer) see exactly where an alarm stops: what Android allows, a real test alarm,
 * and a step-by-step record of what each alarm did. [refreshKey] changes when the app comes back to the screen.
 */
@Composable
fun AlarmHealthSection(refreshKey: Int) {
    val context = LocalContext.current
    var tick by remember { mutableStateOf(0) }
    var testMessage by remember { mutableStateOf<String?>(null) }
    var copied by remember { mutableStateOf(false) }
    val checks = remember(refreshKey, tick) { AlarmHealth.collect(context) }
    val events = remember(refreshKey, tick) { AlarmLog.entries(context) }
    val zone = ZoneId.systemDefault()

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(stringResource(R.string.health_intro), style = MaterialTheme.typography.bodyMedium)

        checks.forEach { CheckRow(it) }

        if (checks.any { it.label == "Alarm notification channel" && it.ok == false }) {
            Button(
                onClick = { PermissionHelper.openChannelSettings(context, NotificationHelper.ALARM_CHANNEL_ID) },
                modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
            ) { Text(stringResource(R.string.health_fix_channel), style = MaterialTheme.typography.labelLarge) }
        }

        Button(
            onClick = {
                val at = TestAlarm.schedule(context, 20)
                val clock = at.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))
                testMessage = context.getString(R.string.health_test_scheduled, clock)
                copied = false
                tick++
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
        ) { Text(stringResource(R.string.health_test_button), style = MaterialTheme.typography.labelLarge) }
        testMessage?.let { Text(it, style = MaterialTheme.typography.bodyLarge) }

        OutlinedButton(
            onClick = {
                tick++
                copied = false
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
        ) { Text(stringResource(R.string.health_refresh), style = MaterialTheme.typography.labelLarge) }

        Text(
            stringResource(R.string.health_events_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        if (events.isEmpty()) {
            Text(stringResource(R.string.health_events_empty), style = MaterialTheme.typography.bodyMedium)
        } else {
            events.takeLast(12).reversed().forEach {
                Text(
                    "${AlarmLogCodec.formatTime(it.atMillis, zone)}  ${it.stage}  ${it.detail}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        OutlinedButton(
            onClick = {
                val report = HealthReport.build(
                    header = AlarmHealth.header(context),
                    checks = checks,
                    exits = AlarmHealth.recentExits(context),
                    events = events,
                    zone = zone,
                    nowMillis = System.currentTimeMillis(),
                )
                context.getSystemService(ClipboardManager::class.java)
                    .setPrimaryClip(ClipData.newPlainText("Aggrey DayFlow alarm report", report))
                copied = true
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
        ) { Text(stringResource(R.string.health_copy), style = MaterialTheme.typography.labelLarge) }
        if (copied) Text(stringResource(R.string.health_copied), style = MaterialTheme.typography.bodyMedium)

        if (OemSettings.isTranssion()) TecnoHelp()
    }
}

@Composable
private fun CheckRow(check: HealthCheck) {
    val color = when (check.ok) {
        true -> MaterialTheme.colorScheme.primary
        false -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.Top) {
        Text(
            HealthReport.statusWord(check.ok),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            modifier = Modifier.widthIn(min = 84.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(check.label, style = MaterialTheme.typography.titleMedium)
            Text(check.detail, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TecnoHelp() {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            stringResource(R.string.tecno_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(stringResource(R.string.tecno_intro), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.tecno_step1), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.tecno_step2), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.tecno_step3), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.tecno_step4), style = MaterialTheme.typography.bodyMedium)
        Button(
            onClick = { PermissionHelper.openBatterySettings(context) },
            modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
        ) { Text(stringResource(R.string.perm_battery_action), style = MaterialTheme.typography.labelLarge) }
        Button(
            onClick = { OemSettings.openAutostart(context) },
            modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
        ) { Text(stringResource(R.string.tecno_open_autostart), style = MaterialTheme.typography.labelLarge) }
        OutlinedButton(
            onClick = { OemSettings.openAppDetails(context) },
            modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
        ) { Text(stringResource(R.string.tecno_open_app), style = MaterialTheme.typography.labelLarge) }
    }
}
