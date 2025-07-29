package com.mohammadfaizan.habitquest.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getCurrentDateKey(): String {
        return dateFormat.format(Date())
    }

    fun getDateKey(date: Date): String {
        return dateFormat.format(date)
    }

    fun getDateKeyForDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(calendar.time)
    }

    fun isSameDay(dateKey1: String, dateKey2: String): Boolean {
        return dateKey1 == dateKey2
    }

    fun isToday(dateKey: String): Boolean {
        return dateKey == getCurrentDateKey()
    }

    fun getDaysBetween(dateKey1: String, dateKey2: String): Int {
        try {
            val date1 = dateFormat.parse(dateKey1)
            val date2 = dateFormat.parse(dateKey2)
            if (date1 != null && date2 != null) {
                val diffInMillis = date2.time - date1.time
                return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
            }
            return 0
        } catch (e: Exception) {
            return 0
        }
    }

    fun getCurrentWeekStart(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return dateFormat.format(calendar.time)
    }

    fun getCurrentMonth(): String {
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return monthFormat.format(Date())
    }

    fun isNewDay(lastDateKey: String?): Boolean {
        if (lastDateKey == null) return true
        return !isToday(lastDateKey)
    }

    fun getTimeUntilMidnight(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis - System.currentTimeMillis()
    }
    
    fun isWeekResetTime(): Boolean {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        return dayOfWeek == Calendar.SUNDAY && hour == 23 && minute >= 59
    }
    
    fun getTimeUntilWeekReset(): Long {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        val daysUntilSunday = if (currentDayOfWeek == Calendar.SUNDAY) 0 else 7 - currentDayOfWeek
        
        calendar.add(Calendar.DAY_OF_YEAR, daysUntilSunday)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        
        return calendar.timeInMillis - System.currentTimeMillis()
    }
} 