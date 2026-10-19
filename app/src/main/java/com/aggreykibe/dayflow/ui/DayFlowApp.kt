package com.aggreykibe.dayflow.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.aggreykibe.dayflow.permissions.PermissionHelper
import com.aggreykibe.dayflow.ui.dashboard.DashboardScreen
import com.aggreykibe.dayflow.ui.editor.EditorScreen
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime

/** Owns navigation (dashboard <-> editor), the clock, and permission prompts. */
@Composable
fun DayFlowApp(viewModel: ReminderViewModel) {
    val context = LocalContext.current
    val now by produceState(LocalDateTime.now()) {
        while (true) {
            value = LocalDateTime.now()
            delay(15_000)
        }
    }

    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableLongStateOf(0L) } // 0 = a new reminder
    var askedForNotifications by rememberSaveable { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.onPermissionStateMayHaveChanged()
    }

    fun requestNotificationsOrOpenSettings() {
        if (PermissionHelper.canRequestNotificationPermission() && !askedForNotifications) {
            askedForNotifications = true
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            PermissionHelper.openNotificationSettings(context)
        }
    }

    // Reading statusTick makes these re-evaluate after the app resumes or a permission dialog closes.
    @Suppress("UNUSED_VARIABLE")
    val tick = viewModel.statusTick
    val notificationsOk = PermissionHelper.notificationsEnabled(context)
    val exactOk = PermissionHelper.exactAlarmsAllowed(context)

    val editing = viewModel.reminders.firstOrNull { it.id == editingId }
    // If the reminder being edited no longer exists, fall back to the dashboard instead of an empty editor.
    val showEditor = editorOpen && (editingId == 0L || editing != null)

    BackHandler(enabled = showEditor) { editorOpen = false }

    if (showEditor) {
        EditorScreen(
            initial = editing,
            defaultDay = maxOf(viewModel.selectedDay, LocalDate.now()),
            now = now,
            onSave = { reminder ->
                viewModel.save(reminder)
                editorOpen = false
                // Ask at the moment it becomes useful: the first time someone saves a reminder.
                if (!PermissionHelper.notificationsEnabled(context) &&
                    PermissionHelper.canRequestNotificationPermission() && !askedForNotifications
                ) {
                    askedForNotifications = true
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onDelete = editing?.let { target ->
                {
                    viewModel.delete(target.id)
                    editorOpen = false
                }
            },
            onCancel = { editorOpen = false },
        )
    } else {
        DashboardScreen(
            reminders = viewModel.reminders,
            selectedDay = viewModel.selectedDay,
            now = now,
            notificationsOk = notificationsOk,
            exactAlarmsOk = exactOk,
            onSelectDay = viewModel::selectDay,
            onAdd = {
                editingId = 0L
                editorOpen = true
            },
            onEdit = { reminder ->
                editingId = reminder.id
                editorOpen = true
            },
            onToggleDone = { reminder, done -> viewModel.setCompleted(reminder.id, viewModel.selectedDay, done) },
            onFixNotifications = { requestNotificationsOrOpenSettings() },
            onFixExactAlarms = { PermissionHelper.openExactAlarmSettings(context) },
        )
    }
}
