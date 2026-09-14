package com.mohammadfaizan.habitquest.utils

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mohammadfaizan.habitquest.data.local.Habit
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    internal fun parseTime(timeString: String?): Pair<Int, Int>? {
        if (timeString.isNullOrBlank()) return null
        return try {
            val parts = timeString.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                if (hour in 0..23 && minute in 0..59) {
                    Pair(hour, minute)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
    }

    internal fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val targetCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If the time has passed today, schedule for tomorrow
        if (targetCalendar.timeInMillis <= System.currentTimeMillis()) {
            targetCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return targetCalendar.timeInMillis - System.currentTimeMillis()
    }

    // Delay until the next occurrence (today or up to 6 days out) of targetDayOfWeek at hour:minute.
    // targetDayOfWeek uses java.util.Calendar.DAY_OF_WEEK values (1=Sunday..7=Saturday).
    internal fun calculateInitialDelayForDay(targetDayOfWeek: Int, hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val daysUntilTarget = (targetDayOfWeek - currentDayOfWeek + 7) % 7

        val candidate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysUntilTarget)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (candidate.timeInMillis <= now.timeInMillis) {
            candidate.add(Calendar.DAY_OF_YEAR, 7)
        }

        return candidate.timeInMillis - now.timeInMillis
    }

    private fun uniqueWorkName(habitId: Long, dayOfWeek: Int) = "habit_reminder_${habitId}_day_$dayOfWeek"

    // WorkManager has no native "specific days of the week" trigger, so each selected day gets
    // its own weekly-repeating request, individually named so it can be replaced/cancelled alone.
    fun scheduleHabitReminder(context: Context, habit: Habit) {
        cancelHabitReminder(context, habit.id)

        if (!habit.reminderEnabled || !habit.isActive || habit.reminderTime.isNullOrBlank()) {
            return
        }

        val (hour, minute) = parseTime(habit.reminderTime) ?: return
        val selectedDays = DateUtils.parseReminderDays(habit.reminderDays)
        if (selectedDays.isEmpty()) return

        val workManager = WorkManager.getInstance(context)

        selectedDays.forEach { dayOfWeek ->
            val initialDelay = calculateInitialDelayForDay(dayOfWeek, hour, minute)

            val workRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setInputData(
                    Data.Builder()
                        .putLong("habit_id", habit.id)
                        .build()
                )
                .addTag("habit_reminder_${habit.id}")
                .build()

            workManager.enqueueUniquePeriodicWork(
                uniqueWorkName(habit.id, dayOfWeek),
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
        }
    }

    fun cancelHabitReminder(context: Context, habitId: Long) {
        val workManager = WorkManager.getInstance(context)
        for (dayOfWeek in Calendar.SUNDAY..Calendar.SATURDAY) {
            workManager.cancelUniqueWork(uniqueWorkName(habitId, dayOfWeek))
        }
        // Also cancel any notifications that might be showing
        HabitNotificationManager.cancelNotification(context, habitId.toInt())
    }

    fun rescheduleAllReminders(context: Context, habits: List<Habit>) {
        habits.forEach { habit ->
            if (habit.reminderEnabled && habit.isActive) {
                scheduleHabitReminder(context, habit)
            } else {
                cancelHabitReminder(context, habit.id)
            }
        }
    }
}
