package com.mohammadfaizan.habitquest.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.data.repository.HabitRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Initialize notification channel
            HabitNotificationManager.createNotificationChannel(context)
            
            // Reschedule all habit reminders after boot
            CoroutineScope(Dispatchers.IO).launch {
                val db = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "app-db"
                ).build()

                val habitRepo = HabitRepositoryImpl(db.habitDao())
                
                try {
                    val allHabits = habitRepo.getAllHabits().first()
                    NotificationScheduler.rescheduleAllReminders(context, allHabits)
                } catch (e: Exception) {
                    // Handle error silently
                } finally {
                    db.close()
                }
            }
        }
    }
}


