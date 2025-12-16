package com.mohammadfaizan.habitquest.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.data.repository.HabitCompletionRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitManagementRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitRepositoryImpl
import com.mohammadfaizan.habitquest.domain.usecase.CompleteHabitUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_COMPLETE_HABIT) return

        val habitId = intent.getLongExtra("habit_id", -1L)
        if (habitId == -1L) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "app-db"
            ).build()

            val habitRepo = HabitRepositoryImpl(db.habitDao())
            val habitCompletionRepo = HabitCompletionRepositoryImpl(db.habitCompletionDao())
            val habitManagementRepo = HabitManagementRepositoryImpl(habitRepo, habitCompletionRepo)
            val completeHabitUseCase = CompleteHabitUseCase(habitManagementRepo)

            try {
                val result = completeHabitUseCase.completeHabitForToday(habitId)
                if (result.success) {
                    HabitNotificationManager.cancelNotification(context, habitId.toInt())
                }
            } finally {
                db.close()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE_HABIT =
            "com.mohammadfaizan.habitquest.ACTION_COMPLETE_HABIT"
    }
}


