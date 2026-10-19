package com.aggreykibe.dayflow.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.domain.DayPart
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.domain.Repeat
import com.aggreykibe.dayflow.domain.dayPartFor
import com.aggreykibe.dayflow.domain.nextUpcoming
import com.aggreykibe.dayflow.domain.progressFor
import com.aggreykibe.dayflow.domain.remindersForDay
import com.aggreykibe.dayflow.ui.theme.DayFlowTheme
import com.aggreykibe.dayflow.ui.theme.Spacing
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val FabClearance = 88.dp

/** Stateless dashboard: everything it shows comes in as parameters, so it is easy to preview and test. */
@Composable
fun DashboardScreen(
    reminders: List<Reminder>,
    selectedDay: LocalDate,
    now: LocalDateTime,
    notificationsOk: Boolean,
    exactAlarmsOk: Boolean,
    onSelectDay: (LocalDate) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Reminder) -> Unit,
    onToggleDone: (Reminder, Boolean) -> Unit,
    onFixNotifications: () -> Unit,
    onFixExactAlarms: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = now.toLocalDate()
    val dateText = now.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    val timeText = now.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    val greeting = when (dayPartFor(now.hour)) {
        DayPart.MORNING -> R.string.greeting_morning
        DayPart.AFTERNOON -> R.string.greeting_afternoon
        DayPart.EVENING -> R.string.greeting_evening
    }
    val dayReminders = remindersForDay(reminders, selectedDay)
    val progress = progressFor(reminders, selectedDay, now)
    val upcoming = nextUpcoming(reminders, now)
    val dayTitle = when (selectedDay) {
        today -> stringResource(R.string.day_today)
        today.plusDays(1) -> stringResource(R.string.day_tomorrow)
        today.minusDays(1) -> stringResource(R.string.day_yesterday)
        else -> selectedDay.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.heightIn(min = Spacing.minTouchTarget),
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(
                    stringResource(R.string.add_reminder),
                    modifier = Modifier.padding(start = Spacing.sm),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.md)
                .padding(bottom = FabClearance),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(stringResource(greeting), style = MaterialTheme.typography.headlineLarge)
            Text("$dateText · $timeText", style = MaterialTheme.typography.bodyLarge)

            if (!notificationsOk) {
                NoticeCard(
                    text = stringResource(R.string.notice_notifications_off),
                    action = stringResource(R.string.notice_notifications_action),
                    onAction = onFixNotifications,
                )
            }
            if (!exactAlarmsOk) {
                NoticeCard(
                    text = stringResource(R.string.notice_exact_off),
                    action = stringResource(R.string.notice_exact_action),
                    onAction = onFixExactAlarms,
                )
            }

            InfoCard(stringResource(R.string.next_reminder)) {
                if (upcoming == null) {
                    Text(stringResource(R.string.next_reminder_empty), style = MaterialTheme.typography.bodyMedium)
                } else {
                    val (reminder, at) = upcoming
                    Text(reminder.title, style = MaterialTheme.typography.bodyLarge)
                    Text(whenText(at, today), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(dayTitle, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { onSelectDay(selectedDay.minusDays(1)) },
                        modifier = Modifier.weight(1f).heightIn(min = Spacing.minTouchTarget),
                    ) { Text(stringResource(R.string.previous_day), style = MaterialTheme.typography.labelLarge) }
                    OutlinedButton(
                        onClick = { onSelectDay(today) },
                        enabled = selectedDay != today,
                        modifier = Modifier.weight(1f).heightIn(min = Spacing.minTouchTarget),
                    ) { Text(stringResource(R.string.day_today), style = MaterialTheme.typography.labelLarge) }
                    OutlinedButton(
                        onClick = { onSelectDay(selectedDay.plusDays(1)) },
                        modifier = Modifier.weight(1f).heightIn(min = Spacing.minTouchTarget),
                    ) { Text(stringResource(R.string.next_day), style = MaterialTheme.typography.labelLarge) }
                }
            }

            InfoCard(stringResource(R.string.today_timeline)) {
                if (dayReminders.isEmpty()) {
                    Text(
                        stringResource(if (selectedDay == today) R.string.today_empty else R.string.day_empty),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = onAdd, modifier = Modifier.heightIn(min = Spacing.minTouchTarget)) {
                        Text(stringResource(R.string.add_reminder), style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    dayReminders.forEach { reminder ->
                        TimelineRow(
                            reminder = reminder,
                            day = selectedDay,
                            now = now,
                            onEdit = { onEdit(reminder) },
                            onToggleDone = { done -> onToggleDone(reminder, done) },
                        )
                    }
                }
            }

            InfoCard(stringResource(R.string.progress_title)) {
                Text(
                    stringResource(R.string.progress_summary, progress.completed, progress.upcoming, progress.missed),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun TimelineRow(
    reminder: Reminder,
    day: LocalDate,
    now: LocalDateTime,
    onEdit: () -> Unit,
    onToggleDone: (Boolean) -> Unit,
) {
    val done = reminder.isCompletedOn(day)
    val missed = !done && !day.atTime(reminder.time).isAfter(now)
    val timeText = reminder.time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    val markDesc = stringResource(R.string.mark_done_description, reminder.title)
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Checkbox(
            checked = done,
            onCheckedChange = onToggleDone,
            modifier = Modifier.semantics { contentDescription = markDesc },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = Spacing.minTouchTarget)
                .clickable(onClickLabel = stringResource(R.string.edit_reminder), onClick = onEdit)
                .padding(vertical = Spacing.xs),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                reminder.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (done) TextDecoration.LineThrough else null,
            )
            val detail = buildString {
                append(timeText)
                if (reminder.repeat == Repeat.DAILY) append(" · ").append(stringResource(R.string.repeat_daily_short))
            }
            Text(detail, style = MaterialTheme.typography.bodyMedium)
            if (done) {
                Text(stringResource(R.string.status_done), style = MaterialTheme.typography.bodyMedium)
            } else if (missed) {
                Text(stringResource(R.string.status_missed), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun whenText(at: LocalDateTime, today: LocalDate): String {
    val time = at.toLocalTime().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
    return when (at.toLocalDate()) {
        today -> stringResource(R.string.when_today, time)
        today.plusDays(1) -> stringResource(R.string.when_tomorrow, time)
        else -> stringResource(R.string.when_on, at.toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)), time)
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            content()
        }
    }
}

@Composable
private fun NoticeCard(text: String, action: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(text, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onAction, modifier = Modifier.heightIn(min = Spacing.minTouchTarget)) {
                Text(action, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() {
    val today = LocalDate.of(2026, 10, 19)
    val sample = listOf(
        Reminder(1, "Take medicine", today, LocalTime.of(8, 0), Repeat.DAILY),
        Reminder(2, "Call Mama", today, LocalTime.of(18, 30), Repeat.ONCE),
    )
    DayFlowTheme {
        DashboardScreen(
            reminders = sample, selectedDay = today, now = today.atTime(12, 0),
            notificationsOk = true, exactAlarmsOk = true,
            onSelectDay = {}, onAdd = {}, onEdit = {}, onToggleDone = { _, _ -> },
            onFixNotifications = {}, onFixExactAlarms = {},
        )
    }
}
