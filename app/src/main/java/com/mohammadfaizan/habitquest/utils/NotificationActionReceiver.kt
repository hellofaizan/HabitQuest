package com.mohammadfaizan.habitquest.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)

                val habitRepo = HabitRepositoryImpl(db.habitDao())
                val habitCompletionRepo = HabitCompletionRepositoryImpl(db.habitCompletionDao(), db.habitFreezeDao())
                val habitManagementRepo = HabitManagementRepositoryImpl(habitRepo, habitCompletionRepo, db)
                val completeHabitUseCase = CompleteHabitUseCase(habitManagementRepo)

                val result = completeHabitUseCase.completeHabitForToday(habitId)
                if (result.success) {
                    HabitNotificationManager.cancelNotification(context, habitId.toInt())
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE_HABIT =
            "com.mohammadfaizan.habitquest.ACTION_COMPLETE_HABIT"
    }
}


