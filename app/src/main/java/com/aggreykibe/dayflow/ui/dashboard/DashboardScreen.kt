package com.aggreykibe.dayflow.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.aggreykibe.dayflow.R
import com.aggreykibe.dayflow.domain.DayPart
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.domain.Repeat
import com.aggreykibe.dayflow.domain.dayPartFor
import com.aggreykibe.dayflow.domain.minutesUntil
import com.aggreykibe.dayflow.domain.nextUpcoming
import com.aggreykibe.dayflow.domain.progressFor
import com.aggreykibe.dayflow.domain.remindersForDay
import com.aggreykibe.dayflow.ui.components.categoryLabel
import com.aggreykibe.dayflow.ui.components.rememberAnimationsEnabled
import com.aggreykibe.dayflow.ui.theme.DayFlowTheme
import com.aggreykibe.dayflow.ui.theme.LocalDayFlowExtras
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
    fullScreenOk: Boolean,
    onSelectDay: (LocalDate) -> Unit,
    onAdd: () -> Unit,
    onEdit: (Reminder) -> Unit,
    onToggleDone: (Reminder, Boolean) -> Unit,
    onFixNotifications: () -> Unit,
    onFixExactAlarms: () -> Unit,
    onFixFullScreen: () -> Unit,
    onOpenSettings: () -> Unit,
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(greeting), style = MaterialTheme.typography.headlineLarge)
                    Text("$dateText · $timeText", style = MaterialTheme.typography.bodyLarge)
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_title))
                }
            }

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
            if (!fullScreenOk) {
                NoticeCard(
                    text = stringResource(R.string.notice_fullscreen_off),
                    action = stringResource(R.string.notice_fullscreen_action),
                    onAction = onFixFullScreen,
                )
            }

            NextReminderCard(upcoming = upcoming, now = now, today = today)

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

            ProgressCard(completed = progress.completed, upcoming = progress.upcoming, missed = progress.missed)

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
        }
    }
}

/** The hero card: what is coming next, when, and how long from now. */
@Composable
private fun NextReminderCard(upcoming: Pair<Reminder, LocalDateTime>?, now: LocalDateTime, today: LocalDate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                stringResource(R.string.next_reminder),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            if (upcoming == null) {
                Text(stringResource(R.string.next_reminder_empty), style = MaterialTheme.typography.bodyLarge)
            } else {
                val (reminder, at) = upcoming
                Text(reminder.title, style = MaterialTheme.typography.headlineMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    whenText(at, today) + " · " + countdownText(minutesUntil(now, at)),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun ProgressCard(completed: Int, upcoming: Int, missed: Int) {
    val total = completed + upcoming + missed
    InfoCard(stringResource(R.string.progress_title)) {
        if (total == 0) {
            Text(stringResource(R.string.progress_none), style = MaterialTheme.typography.bodyMedium)
        } else {
            val animated by animateFloatAsState(
                targetValue = completed.toFloat() / total,
                animationSpec = if (rememberAnimationsEnabled()) tween(durationMillis = 600) else snap(),
                label = "dayProgress",
            )
            Text(stringResource(R.string.progress_of_total, completed, total), style = MaterialTheme.typography.titleMedium)
            LinearProgressIndicator(
                progress = { animated },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
            )
            Text(
                stringResource(R.string.progress_summary, completed, upcoming, missed),
                style = MaterialTheme.typography.bodyMedium,
            )
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
    val dotColor = LocalDayFlowExtras.current.categoryColor(reminder.category)
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
            val repeatText = when (reminder.repeat) {
                Repeat.DAILY -> " · " + stringResource(R.string.repeat_daily_short)
                Repeat.WEEKLY -> " · " + stringResource(R.string.repeat_weekly_short)
                Repeat.ONCE -> ""
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(dotColor, CircleShape))
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    categoryLabel(reminder.category) + " · " + timeText + repeatText,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (reminder.message.isNotBlank()) {
                Text(reminder.message, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
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
private fun countdownText(minutes: Long): String = when {
    minutes <= 0L -> stringResource(R.string.countdown_now)
    minutes < 60L -> stringResource(R.string.countdown_minutes, minutes.toInt())
    minutes < 24L * 60L -> {
        val hours = (minutes / 60).toInt()
        val rest = (minutes % 60).toInt()
        if (rest == 0) stringResource(R.string.countdown_hours, hours)
        else stringResource(R.string.countdown_hours_minutes, hours, rest)
    }
    else -> stringResource(R.string.countdown_days, (minutes / (24L * 60L)).toInt())
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacing.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
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
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
        Reminder(2, "Call Mama", today, LocalTime.of(18, 30), Repeat.ONCE, message = "Ask about the weekend plans."),
    )
    DayFlowTheme {
        DashboardScreen(
            reminders = sample, selectedDay = today, now = today.atTime(12, 0),
            notificationsOk = true, exactAlarmsOk = true, fullScreenOk = true,
            onSelectDay = {}, onAdd = {}, onEdit = {}, onToggleDone = { _, _ -> },
            onFixNotifications = {}, onFixExactAlarms = {}, onFixFullScreen = {}, onOpenSettings = {},
        )
    }
}
