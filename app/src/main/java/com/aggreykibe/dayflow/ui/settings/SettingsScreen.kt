package com.aggreykibe.dayflow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.domain.AppSettings
import com.aggreykibe.dayflow.ui.components.AlarmOptions
import com.aggreykibe.dayflow.ui.theme.Spacing

/** App-wide alarm defaults and a plain-language view of which Android permissions are on. */
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    notificationsOk: Boolean,
    exactAlarmsOk: Boolean,
    fullScreenOk: Boolean,
    onFixNotifications: () -> Unit,
    onFixExactAlarms: () -> Unit,
    onFixFullScreen: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
            )

            SectionCard(stringResource(R.string.settings_alarm_defaults)) {
                Text(stringResource(R.string.settings_alarm_defaults_hint), style = MaterialTheme.typography.bodyMedium)
                AlarmOptions(
                    ringStyle = settings.ringStyle,
                    onRingStyle = { onSettingsChange(settings.copy(ringStyle = it)) },
                    soundId = settings.soundId,
                    onSoundId = { onSettingsChange(settings.copy(soundId = it)) },
                    vibrate = settings.vibrate,
                    onVibrate = { onSettingsChange(settings.copy(vibrate = it)) },
                    snoozeMinutes = settings.snoozeMinutes,
                    onSnoozeMinutes = { onSettingsChange(settings.copy(snoozeMinutes = it)) },
                    ringSeconds = settings.ringSeconds,
                    onRingSeconds = { onSettingsChange(settings.copy(ringSeconds = it)) },
                    gradualVolume = settings.gradualVolume,
                    onGradualVolume = { onSettingsChange(settings.copy(gradualVolume = it)) },
                )
            }

            SectionCard(stringResource(R.string.settings_permissions)) {
                PermissionRow(stringResource(R.string.perm_notifications), notificationsOk, stringResource(R.string.notice_notifications_action), onFixNotifications)
                PermissionRow(stringResource(R.string.perm_exact), exactAlarmsOk, stringResource(R.string.notice_exact_action), onFixExactAlarms)
                PermissionRow(stringResource(R.string.perm_fullscreen), fullScreenOk, stringResource(R.string.notice_fullscreen_action), onFixFullScreen)
                Text(stringResource(R.string.perm_battery_tip), style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(
                    onClick = onOpenBatterySettings,
                    modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
                ) { Text(stringResource(R.string.perm_battery_action), style = MaterialTheme.typography.labelLarge) }
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
            ) { Text(stringResource(R.string.action_back), style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
            content()
        }
    }
}

@Composable
private fun PermissionRow(label: String, ok: Boolean, fixLabel: String, onFix: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(
                stringResource(if (ok) R.string.status_on else R.string.status_off),
                style = MaterialTheme.typography.titleMedium,
                color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
        if (!ok) {
            Button(onClick = onFix, modifier = Modifier.heightIn(min = Spacing.minTouchTarget)) {
                Text(fixLabel, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
