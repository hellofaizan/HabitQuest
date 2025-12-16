package com.mohammadfaizan.habitquest.utils

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.data.repository.HabitRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HabitReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val habitId = inputData.getLong("habit_id", -1L)
            if (habitId == -1L) {
                return@withContext Result.failure()
            }

            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "app-db"
            ).build()

            val habitRepo = HabitRepositoryImpl(db.habitDao())
            val habit = habitRepo.getHabitById(habitId)

            db.close()

            if (habit != null && habit.reminderEnabled && habit.isActive) {
                // Show notification
                val notificationId = habitId.toInt()
                HabitNotificationManager.showHabitReminderNotification(
                    applicationContext,
                    habitId,
                    habit.name,
                    notificationId
                )
                Result.success()
            } else {
                Result.success() // Habit might be disabled or deleted, just succeed
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}


