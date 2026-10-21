package com.aggreykibe.dayflow.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.alarm.SoundPlayer
import com.aggreykibe.dayflow.domain.RingStyle
import com.aggreykibe.dayflow.domain.RING_CHOICES_SECONDS
import com.aggreykibe.dayflow.domain.SNOOZE_CHOICES_MINUTES
import com.aggreykibe.dayflow.domain.SoundCatalog
import com.aggreykibe.dayflow.domain.SoundIds
import com.aggreykibe.dayflow.domain.SoundVibe
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

/**
 * Shows the chosen sound on a button. Tapping it opens a list grouped by mood, where every sound has its own
 * Play button, so people can listen before choosing. Picking a row selects it; Play only previews.
 */
@Composable
fun SoundPicker(selected: String, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    val player = remember { SoundPlayer(context) }
    var playingId by remember { mutableStateOf<String?>(null) }
    var open by rememberSaveable { mutableStateOf(false) }
    DisposableEffect(Unit) { onDispose { player.stop() } }

    fun stopSample() {
        player.stop()
        playingId = null
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(stringResource(R.string.field_sound), style = MaterialTheme.typography.titleMedium)
        OutlinedButton(
            onClick = { open = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
        ) {
            Text(stringResource(R.string.sound_selected, soundLabel(selected)), style = MaterialTheme.typography.labelLarge)
        }
    }

    if (open) {
        val toggleSample: (String) -> Unit = { id ->
            if (playingId == id) {
                stopSample()
            } else {
                playingId = id
                player.start(id, loop = false, gradual = false, onFinished = { playingId = null })
            }
        }
        AlertDialog(
            onDismissRequest = {
                stopSample()
                open = false
            },
            title = { Text(stringResource(R.string.sound_dialog_title)) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    item(key = SoundIds.SYSTEM) {
                        SoundRow(SoundIds.SYSTEM, selected == SoundIds.SYSTEM, playingId == SoundIds.SYSTEM, onSelect, toggleSample)
                    }
                    SoundVibe.values().forEach { vibe ->
                        item(key = "header_" + vibe.name) {
                            Text(
                                vibeLabel(vibe),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.xs).semantics { heading() },
                            )
                        }
                        items(SoundCatalog.bundled.filter { it.vibe == vibe }, key = { it.id }) { info ->
                            SoundRow(info.id, selected == info.id, playingId == info.id, onSelect, toggleSample)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    stopSample()
                    open = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
        )
    }
}

@Composable
private fun SoundRow(
    id: String,
    selected: Boolean,
    playing: Boolean,
    onSelect: (String) -> Unit,
    onTogglePlay: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Spacing.minTouchTarget)
            .selectable(selected = selected, onClick = { onSelect(id) }, role = Role.RadioButton),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            soundLabel(id),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(start = Spacing.sm),
        )
        TextButton(onClick = { onTogglePlay(id) }, modifier = Modifier.heightIn(min = Spacing.minTouchTarget)) {
            Text(
                stringResource(if (playing) R.string.sound_stop else R.string.sound_play),
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
