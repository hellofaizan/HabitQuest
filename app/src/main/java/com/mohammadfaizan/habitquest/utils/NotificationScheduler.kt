package com.mohammadfaizan.habitquest.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitFrequency
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private fun parseTime(timeString: String?): Pair<Int, Int>? {
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

    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
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

    fun scheduleHabitReminder(context: Context, habit: Habit) {
        if (!habit.reminderEnabled || !habit.isActive || habit.reminderTime.isNullOrBlank()) {
            return
        }

        val timePair = parseTime(habit.reminderTime) ?: return
        val (hour, minute) = timePair

        val initialDelay = calculateInitialDelay(hour, minute)

        val workRequest = when (habit.frequency) {
            HabitFrequency.DAILY -> {
                PeriodicWorkRequestBuilder<HabitReminderWorker>(
                    1, TimeUnit.DAYS
                )
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setInputData(
                        androidx.work.Data.Builder()
                            .putLong("habit_id", habit.id)
                            .build()
                    )
                    .addTag("habit_reminder_${habit.id}")
                    .build()
            }
            HabitFrequency.WEEKLY -> {
                PeriodicWorkRequestBuilder<HabitReminderWorker>(
                    7, TimeUnit.DAYS
                )
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setInputData(
                        androidx.work.Data.Builder()
                            .putLong("habit_id", habit.id)
                            .build()
                    )
                    .addTag("habit_reminder_${habit.id}")
                    .build()
            }
            HabitFrequency.MONTHLY -> {
                PeriodicWorkRequestBuilder<HabitReminderWorker>(
                    30, TimeUnit.DAYS
                )
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setInputData(
                        androidx.work.Data.Builder()
                            .putLong("habit_id", habit.id)
                            .build()
                    )
                    .addTag("habit_reminder_${habit.id}")
                    .build()
            }
            HabitFrequency.CUSTOM -> {
                // For custom frequency, schedule as daily for now
                // You can enhance this later based on custom frequency logic
                PeriodicWorkRequestBuilder<HabitReminderWorker>(
                    1, TimeUnit.DAYS
                )
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setInputData(
                        androidx.work.Data.Builder()
                            .putLong("habit_id", habit.id)
                            .build()
                    )
                    .addTag("habit_reminder_${habit.id}")
                    .build()
            }
        }

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "habit_reminder_${habit.id}",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelHabitReminder(context: Context, habitId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("habit_reminder_$habitId")
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

