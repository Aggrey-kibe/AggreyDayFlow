package com.aggreykibe.dayflow.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.aggreykibe.dayflow.data.ReminderStore
import com.aggreykibe.dayflow.domain.Reminder
import com.aggreykibe.dayflow.scheduling.ReminderScheduler
import java.time.LocalDate

/** Single source of truth for the UI. Every change is saved first, then the alarms are brought in line. */
class ReminderViewModel(app: Application) : AndroidViewModel(app) {
    private val store = ReminderStore(app)
    private val scheduler = ReminderScheduler(app)

    var reminders by mutableStateOf(store.load())
        private set
    var selectedDay by mutableStateOf(LocalDate.now())
        private set

    /** Bumped when outside state (permissions) may have changed, so the UI re-reads it. */
    var statusTick by mutableIntStateOf(0)
        private set

    /** Reload from disk and re-arm every alarm. Called whenever the app comes to the foreground. */
    fun refresh() {
        reminders = store.load()
        scheduler.rescheduleAll(reminders)
        statusTick++
    }

    fun onPermissionStateMayHaveChanged() {
        statusTick++
    }

    fun selectDay(day: LocalDate) {
        selectedDay = day
    }

    /** Adds the reminder (id 0 means "new") or replaces the one with the same id. */
    fun save(reminder: Reminder) {
        val saved = if (reminder.id == 0L) reminder.copy(id = nextId()) else reminder
        val updated = if (reminders.any { it.id == saved.id }) {
            reminders.map { if (it.id == saved.id) saved else it }
        } else {
            reminders + saved
        }
        persist(updated)
        scheduler.schedule(saved)
    }

    fun delete(id: Long) {
        persist(reminders.filterNot { it.id == id })
        scheduler.cancel(id)
        scheduler.cancelSnooze(id)
    }

    fun setCompleted(id: Long, day: LocalDate, completed: Boolean) {
        val target = reminders.firstOrNull { it.id == id } ?: return
        val changed = target.withCompletion(day, completed)
        persist(reminders.map { if (it.id == id) changed else it })
        scheduler.schedule(changed) // skips completed days, so a ticked reminder will not ring
    }

    private fun persist(updated: List<Reminder>) {
        store.save(updated)
        reminders = updated
    }

    private fun nextId(): Long = (reminders.maxOfOrNull { it.id } ?: 0L) + 1L
}
