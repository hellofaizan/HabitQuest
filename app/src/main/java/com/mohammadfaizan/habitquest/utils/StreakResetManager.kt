package com.mohammadfaizan.habitquest.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.data.repository.HabitCompletionRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitManagementRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class StreakResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.mohammadfaizan.habitquest.STREAK_RESET" || 
            intent.action == Intent.ACTION_BOOT_COMPLETED) {
            
            // Reschedule on boot
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                StreakResetManager.scheduleMidnightReset(context)
                return
            }
            
            if (intent.action == "com.mohammadfaizan.habitquest.STREAK_RESET") {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getInstance(context)
                        val habitRepo = HabitRepositoryImpl(db.habitDao())
                        val habitCompletionRepo = HabitCompletionRepositoryImpl(db.habitCompletionDao(), db.habitFreezeDao())
                        val habitManagementRepo = HabitManagementRepositoryImpl(habitRepo, habitCompletionRepo, db)
                        habitManagementRepo.checkAndResetStreaksIfNeeded()
                    } finally {
                        pendingResult.finish()
                    }
                }

                // Reschedule for next midnight
                StreakResetManager.scheduleMidnightReset(context)
            }
        }
    }
}

object StreakResetManager {
    
    fun scheduleMidnightReset(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StreakResetReceiver::class.java).apply {
            action = "com.mohammadfaizan.habitquest.STREAK_RESET"
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calculate next midnight
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1) // Next midnight
        }

        // Schedule the alarm. Exact alarms require runtime permission on Android 12+
        // (and are no longer auto-granted on Android 14+ for apps like this one), so fall
        // back to an inexact alarm rather than risk a SecurityException.
        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

        when {
            canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            canScheduleExact -> {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            else -> {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
    
    fun cancelReset(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, StreakResetReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

