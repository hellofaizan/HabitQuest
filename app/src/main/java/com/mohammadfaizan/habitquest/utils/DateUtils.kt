package com.mohammadfaizan.habitquest.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    /**
     * Get the current date key in YYYY-MM-DD format
     */
    fun getCurrentDateKey(): String {
        return dateFormat.format(Date())
    }
    
    /**
     * Get the date key for a specific date
     */
    fun getDateKey(date: Date): String {
        return dateFormat.format(date)
    }
    
    /**
     * Get the date key for a specific number of days ago
     */
    fun getDateKeyForDaysAgo(daysAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(calendar.time)
    }
    
    /**
     * Check if two date keys represent the same day
     */
    fun isSameDay(dateKey1: String, dateKey2: String): Boolean {
        return dateKey1 == dateKey2
    }
    
    /**
     * Check if a date key represents today
     */
    fun isToday(dateKey: String): Boolean {
        return dateKey == getCurrentDateKey()
    }
    
    /**
     * Get the number of days between two date keys
     */
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
    
    /**
     * Get the start of the current week (Monday)
     */
    fun getCurrentWeekStart(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return dateFormat.format(calendar.time)
    }
    
    /**
     * Get the current month in YYYY-MM format
     */
    fun getCurrentMonth(): String {
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return monthFormat.format(Date())
    }
    
    /**
     * Check if it's a new day (useful for midnight reset)
     */
    fun isNewDay(lastDateKey: String?): Boolean {
        if (lastDateKey == null) return true
        return !isToday(lastDateKey)
    }
    
    /**
     * Get the time until midnight in milliseconds
     */
    fun getTimeUntilMidnight(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis - System.currentTimeMillis()
    }
} 