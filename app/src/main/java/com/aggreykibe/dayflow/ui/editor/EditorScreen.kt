package com.aggreykibe.dayflow.ui.editor

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.domain.Repeat
import com.aggreykibe.dayflow.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Create ([initial] == null) or edit a reminder. Field values survive screen rotation.
 * [onDelete] is null when creating, so no Delete button is shown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    initial: Reminder?,
    defaultDay: LocalDate,
    now: LocalDateTime,
    onSave: (Reminder) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val suggested = now.plusHours(1).withMinute(0).withSecond(0).withNano(0)
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    val initialDate = initial?.date ?: if (defaultDay <= now.toLocalDate()) suggested.toLocalDate() else defaultDay
    var epochDay by rememberSaveable { mutableLongStateOf(initialDate.toEpochDay()) }
    var minuteOfDay by rememberSaveable { mutableIntStateOf((initial?.time ?: suggested.toLocalTime()).let { it.hour * 60 + it.minute }) }
    var daily by rememberSaveable { mutableStateOf(initial?.repeat == Repeat.DAILY) }
    var triedToSave by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    val date = LocalDate.ofEpochDay(epochDay)
    val time = LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)
    val repeat = if (daily) Repeat.DAILY else Repeat.ONCE

    val titleBlank = title.isBlank()
    val unchanged = initial != null && initial.repeat == Repeat.ONCE && initial.date == date && initial.time == time
    val inPast = repeat == Repeat.ONCE && !date.atTime(time).isAfter(now) && !unchanged
    val titleError = triedToSave && titleBlank
    val pastError = triedToSave && inPast

    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                stringResource(if (initial == null) R.string.editor_title_new else R.string.editor_title_edit),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics { heading() },
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(MAX_TITLE) },
                label = { Text(stringResource(R.string.field_title)) },
                singleLine = true,
                isError = titleError,
                supportingText = if (titleError) {
                    { Text(stringResource(R.string.error_title_required)) }
                } else null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            val dateLabel = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
            val timeLabel = time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
            ) {
                Text(
                    stringResource(if (daily) R.string.field_starts else R.string.field_date, dateLabel),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
            ) {
                Text(stringResource(R.string.field_time, timeLabel), style = MaterialTheme.typography.labelLarge)
            }

            Column(Modifier.selectableGroup()) {
                Text(stringResource(R.string.field_repeat), style = MaterialTheme.typography.titleMedium)
                RepeatOption(stringResource(R.string.repeat_once), selected = !daily) { daily = false }
                RepeatOption(stringResource(R.string.repeat_daily), selected = daily) { daily = true }
            }

            if (pastError) {
                Text(
                    stringResource(R.string.error_in_past),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Button(
                onClick = {
                    triedToSave = true
                    if (!titleBlank && !inPast) {
                        val base = initial ?: Reminder(id = 0L, title = "", date = date, time = time)
                        onSave(base.copy(title = title.trim(), date = date, time = time, repeat = repeat))
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
            ) { Text(stringResource(R.string.action_save), style = MaterialTheme.typography.labelLarge) }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
            ) { Text(stringResource(R.string.action_cancel), style = MaterialTheme.typography.labelLarge) }

            if (onDelete != null) {
                TextButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget),
                ) {
                    Text(
                        stringResource(R.string.action_delete),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        epochDay = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        ) { DatePicker(state = state) }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = DateFormat.is24HourFormat(LocalContext.current),
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    minuteOfDay = state.hour * 60 + state.minute
                    showTimePicker = false
                }) { Text(stringResource(R.string.action_ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.action_cancel)) }
            },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) { TimePicker(state = state) }
            },
        )
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_body, initial?.title.orEmpty())) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Composable
private fun RepeatOption(label: String, selected: Boolean, onSelect: () -> Unit) {
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

private const val MAX_TITLE = 100
