package com.aggreykibe.dayflow.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.domain.AppSettings
import com.aggreykibe.dayflow.domain.ThemeCatalog
import com.aggreykibe.dayflow.domain.ThemeId
import com.aggreykibe.dayflow.domain.ThemeMode
import com.aggreykibe.dayflow.domain.ThemeSpec
import com.aggreykibe.dayflow.ui.components.AlarmOptions
import com.aggreykibe.dayflow.ui.components.ChipChoice
import com.aggreykibe.dayflow.ui.components.modeLabel
import com.aggreykibe.dayflow.ui.theme.DayFlowTypography
import com.aggreykibe.dayflow.ui.theme.LocalDayFlowExtras
import com.aggreykibe.dayflow.ui.theme.Spacing
import com.aggreykibe.dayflow.ui.theme.colorSchemeFor
import com.aggreykibe.dayflow.ui.theme.extrasFor

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

            SectionCard(stringResource(R.string.settings_appearance)) {
                ChipChoice(
                    label = stringResource(R.string.field_mode),
                    options = ThemeMode.values().toList(),
                    selected = settings.themeMode,
                    optionText = { modeLabel(it) },
                    onSelect = { onSettingsChange(settings.copy(themeMode = it)) },
                )
                ThemeGallery(
                    appliedTheme = settings.theme,
                    mode = settings.themeMode,
                    onApply = { onSettingsChange(settings.copy(theme = it)) },
                )
            }

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
                Text(stringResource(R.string.perm_exact_hint), style = MaterialTheme.typography.bodyMedium)
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

/** Tap a theme to preview it in the panel below; nothing changes in the app until "Apply theme" is pressed. */
@Composable
private fun ThemeGallery(appliedTheme: ThemeId, mode: ThemeMode, onApply: (ThemeId) -> Unit) {
    var previewId by rememberSaveable { mutableStateOf(appliedTheme) }
    val dark = mode.isDark(isSystemInDarkTheme())

    Text(stringResource(R.string.theme_gallery_hint), style = MaterialTheme.typography.bodyMedium)
    ThemeCatalog.all.chunked(2).forEach { rowSpecs ->
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
            rowSpecs.forEach { spec ->
                ThemeCard(
                    spec = spec,
                    dark = dark,
                    selected = spec.id == previewId,
                    inUse = spec.id == appliedTheme,
                    onClick = { previewId = spec.id },
                    modifier = Modifier.weight(1f),
                )
            }
            if (rowSpecs.size == 1) Spacer(Modifier.weight(1f))
        }
    }
    ThemePreview(previewId, dark)
    Button(
        onClick = { onApply(previewId) },
        enabled = previewId != appliedTheme,
        modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
    ) {
        Text(
            stringResource(if (previewId == appliedTheme) R.string.theme_applied else R.string.theme_apply),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun ThemeCard(spec: ThemeSpec, dark: Boolean, selected: Boolean, inUse: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val p = if (dark) spec.dark else spec.light
    Card(
        modifier = modifier.selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (selected) 3.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
        colors = CardDefaults.cardColors(containerColor = Color(p.background), contentColor = Color(p.onBackground)),
    ) {
        Column(Modifier.padding(Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(Modifier.fillMaxWidth().height(28.dp).clip(RoundedCornerShape(8.dp))) {
                listOf(p.primary, p.secondary, p.primaryContainer, p.surfaceVariant).forEach { swatch ->
                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(swatch)))
                }
            }
            Text(spec.displayName, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(if (inUse) R.string.theme_in_use else if (selected) R.string.theme_previewing else R.string.theme_tap_to_preview),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** A small picture of the home screen in the chosen theme. Its pieces are labels, not buttons. */
@Composable
private fun ThemePreview(id: ThemeId, dark: Boolean) {
    val scheme = remember(id, dark) { colorSchemeFor(id, dark) }
    val extras = remember(dark) { extrasFor(dark) }
    CompositionLocalProvider(LocalDayFlowExtras provides extras) {
        MaterialTheme(colorScheme = scheme, typography = DayFlowTypography) {
            Surface(
                color = scheme.background,
                shape = RoundedCornerShape(Spacing.cardRadius),
                border = BorderStroke(1.dp, scheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(stringResource(R.string.greeting_morning), style = MaterialTheme.typography.headlineMedium)
                    Surface(color = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Spacing.md)) {
                            Text(stringResource(R.string.next_reminder), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.preview_sample_title), style = MaterialTheme.typography.headlineMedium)
                            Text(stringResource(R.string.preview_sample_when), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Surface(color = scheme.surfaceVariant, contentColor = scheme.onSurfaceVariant, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).background(LocalDayFlowExtras.current.categoryColor(com.aggreykibe.dayflow.domain.Category.SCHOOL), CircleShape))
                            Text(
                                stringResource(R.string.preview_sample_row),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = Spacing.sm),
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Surface(color = scheme.primary, contentColor = scheme.onPrimary, shape = RoundedCornerShape(24.dp)) {
                            Text(stringResource(R.string.preview_primary), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm))
                        }
                        Surface(color = scheme.secondary, contentColor = scheme.onSecondary, shape = RoundedCornerShape(24.dp)) {
                            Text(stringResource(R.string.preview_secondary), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm))
                        }
                    }
                }
            }
        }
    }
}
