package com.mohammadfaizan.habitquest.data.repository

import com.mohammadfaizan.habitquest.data.local.CompletionPattern
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.data.local.HabitCompletionDao
import com.mohammadfaizan.habitquest.domain.repository.HabitCompletionRepository
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitCompletionRepositoryImpl(
    private val habitCompletionDao: HabitCompletionDao
) : HabitCompletionRepository {

    // Basic CRUD Operations
    override suspend fun insertCompletion(completion: HabitCompletion): Long {
        return habitCompletionDao.insertCompletion(completion)
    }

    override suspend fun updateCompletion(completion: HabitCompletion) {
        habitCompletionDao.updateCompletion(completion)
    }

    override suspend fun deleteCompletion(completion: HabitCompletion) {
        habitCompletionDao.deleteCompletion(completion)
    }

    // Query Operations
    override fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletionsForHabit(habitId)
    }

    override suspend fun getCompletionForDate(habitId: Long, dateKey: String): HabitCompletion? {
        return habitCompletionDao.getCompletionForDate(habitId, dateKey)
    }

    override fun getCompletionsInDateRange(
        habitId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletionsInDateRange(habitId, startDate, endDate)
    }

    override suspend fun getCompletionsForHabits(habitIds: List<Long>): List<HabitCompletion> {
        return habitCompletionDao.getCompletionsForHabits(habitIds)
    }

    // Completion Status
    override suspend fun isHabitCompletedForDate(habitId: Long, dateKey: String): Int {
        return habitCompletionDao.isHabitCompletedForDate(habitId, dateKey)
    }

    // Statistics
    override suspend fun getTotalCompletionsForHabit(habitId: Long): Int {
        return habitCompletionDao.getTotalCompletionsForHabit(habitId)
    }

    override suspend fun getCompletionsSinceDate(habitId: Long, startDate: String): Int {
        return habitCompletionDao.getCompletionsSinceDate(habitId, startDate)
    }

    override suspend fun getCompletionsInDateRange(
        habitId: Long,
        startDate: Date,
        endDate: Date
    ): Int {
        return habitCompletionDao.getCompletionsInDateRange(habitId, startDate, endDate)
    }

    override suspend fun getCompletionsForSpecificDate(habitId: Long, dateKey: String): Int {
        return habitCompletionDao.getCompletionsForSpecificDate(habitId, dateKey)
    }

    // Streak Calculations
    override suspend fun getCurrentStreak(habitId: Long): Int {
        // Get all completion dates sorted descending
        val completionDates = habitCompletionDao.getCompletionDates(habitId)
        if (completionDates.isEmpty()) return 0
        
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        var streak = 0
        var expectedDate = dateFormat.format(calendar.time) // Today
        
        // Check if today is completed, if not start from yesterday
        val todayCompleted = completionDates.contains(expectedDate)
        if (!todayCompleted) {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            expectedDate = dateFormat.format(calendar.time)
        }
        
        // Count consecutive days backwards
        val completionSet = completionDates.toSet()
        while (completionSet.contains(expectedDate)) {
            streak++
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            expectedDate = dateFormat.format(calendar.time)
        }
        
        return streak
    }
    
    override suspend fun getCompletionDates(habitId: Long): List<String> {
        return habitCompletionDao.getCompletionDates(habitId)
    }
    
    override suspend fun hasCompletionForDate(habitId: Long, dateKey: String): Boolean {
        return habitCompletionDao.hasCompletionForDate(habitId, dateKey) > 0
    }

    // Bulk Operations
    override suspend fun deleteAllCompletionsForHabit(habitId: Long) {
        habitCompletionDao.deleteAllCompletionsForHabit(habitId)
    }

    override suspend fun deleteCompletionsBeforeDate(dateKey: String) {
        habitCompletionDao.deleteCompletionsBeforeDate(dateKey)
    }

    override suspend fun deleteOldCompletions(cutoffDate: Date) {
        habitCompletionDao.deleteOldCompletions(cutoffDate)
    }

    // Recent Activity
    override fun getRecentCompletions(limit: Int): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getRecentCompletions(limit)
    }

    // Patterns and Analytics
    override fun getCompletionPattern(habitId: Long, limit: Int): Flow<List<CompletionPattern>> {
        return habitCompletionDao.getCompletionPattern(habitId, limit)
    }

    // Random data generation for screenshots/demos
    override suspend fun generateRandomCompletions(habitId: Long, days: Int, completionProbability: Float) {
        val random = kotlin.random.Random.Default
        val completions = mutableListOf<HabitCompletion>()
        val calendar = Calendar.getInstance()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        // Generate completions for the past N days
        for (dayOffset in 0 until days) {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
            val date = calendar.time
            val dateKey = dateFormat.format(date)

            // Randomly decide if this day should have completions
            if (random.nextFloat() < completionProbability) {
                // Random number of completions (1 to targetCount, but we'll use a default of 1-3 for now)
                // This will be handled by the use case which knows the targetCount
                completions.add(
                    HabitCompletion(
                        habitId = habitId,
                        completedAt = date,
                        dateKey = dateKey,
                        notes = null
                    )
                )
            }
        }

        // Insert all completions at once
        if (completions.isNotEmpty()) {
            habitCompletionDao.insertCompletions(completions)
        }
    }

    override suspend fun insertCompletions(completions: List<HabitCompletion>) {
        if (completions.isNotEmpty()) {
            habitCompletionDao.insertCompletions(completions)
        }
    }
}