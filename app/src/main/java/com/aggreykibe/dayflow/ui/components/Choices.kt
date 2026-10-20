package com.aggreykibe.dayflow.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.alarm.SoundPlayer
import com.aggreykibe.dayflow.domain.RingStyle
import com.aggreykibe.dayflow.domain.RING_CHOICES_SECONDS
import com.aggreykibe.dayflow.domain.SNOOZE_CHOICES_MINUTES
import com.aggreykibe.dayflow.domain.SoundIds
import com.aggreykibe.dayflow.ui.theme.Spacing

/** A labelled row of choices that scrolls sideways on small screens. */
@Composable
fun <T> ChipChoice(
    label: String,
    options: List<T>,
    selected: T,
    optionText: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(optionText(option)) },
                )
            }
        }
    }
}

@Composable
fun RadioRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.minTouchTarget)
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = Spacing.sm))
    }
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.minTouchTarget)
            .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Switch),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** Sound choices plus a button that plays a sample so people can hear what they are picking. */
@Composable
fun SoundPicker(selected: String, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val player = remember { SoundPlayer(context) }
    var playing by remember { mutableStateOf(false) }
    DisposableEffect(Unit) { onDispose { player.stop() } }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        ChipChoice(
            label = stringResource(R.string.field_sound),
            options = SoundIds.all,
            selected = selected,
            optionText = { soundLabel(it) },
            onSelect = {
                player.stop()
                playing = false
                onSelect(it)
            },
        )
        OutlinedButton(
            onClick = {
                if (playing) {
                    player.stop()
                    playing = false
                } else {
                    playing = true
                    player.start(selected, loop = false, gradual = false, onFinished = { playing = false })
                }
            },
            modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
        ) {
            Text(
                stringResource(if (playing) R.string.sound_stop_sample else R.string.sound_play_sample),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

/**
 * The alarm controls shared by the reminder editor and the Settings screen.
 * Alarm-only controls are hidden when "quiet notification" is chosen. The "get louder" switch
 * appears only where [gradualVolume] is provided (Settings).
 */
@Composable
fun AlarmOptions(
    ringStyle: RingStyle,
    onRingStyle: (RingStyle) -> Unit,
    soundId: String,
    onSoundId: (String) -> Unit,
    vibrate: Boolean,
    onVibrate: (Boolean) -> Unit,
    snoozeMinutes: Int,
    onSnoozeMinutes: (Int) -> Unit,
    ringSeconds: Int,
    onRingSeconds: (Int) -> Unit,
    gradualVolume: Boolean? = null,
    onGradualVolume: ((Boolean) -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Column(Modifier.selectableGroup()) {
            Text(stringResource(R.string.field_ring_style), style = MaterialTheme.typography.titleMedium)
            RadioRow(stringResource(R.string.ring_style_alarm), ringStyle == RingStyle.ALARM) { onRingStyle(RingStyle.ALARM) }
            RadioRow(stringResource(R.string.ring_style_notification), ringStyle == RingStyle.NOTIFICATION) { onRingStyle(RingStyle.NOTIFICATION) }
        }
        if (ringStyle == RingStyle.ALARM) {
            SoundPicker(selected = soundId, onSelect = onSoundId)
            SwitchRow(stringResource(R.string.field_vibrate), vibrate, onVibrate)
            ChipChoice(
                label = stringResource(R.string.field_snooze),
                options = SNOOZE_CHOICES_MINUTES,
                selected = snoozeMinutes,
                optionText = { durationLabelMinutes(it) },
                onSelect = onSnoozeMinutes,
            )
            ChipChoice(
                label = stringResource(R.string.field_ring_duration),
                options = RING_CHOICES_SECONDS,
                selected = ringSeconds,
                optionText = { durationLabelSeconds(it) },
                onSelect = onRingSeconds,
            )
            if (gradualVolume != null && onGradualVolume != null) {
                SwitchRow(stringResource(R.string.field_gradual), gradualVolume, onGradualVolume)
            }
        }
    }
}
