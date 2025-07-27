package com.mohammadfaizan.habitquest.data.repository

import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.data.local.HabitCompletionDao
import com.mohammadfaizan.habitquest.data.local.CompletionPattern
import com.mohammadfaizan.habitquest.domain.repository.HabitCompletionRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

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
    
    override fun getCompletionsInDateRange(habitId: Long, startDate: String, endDate: String): Flow<List<HabitCompletion>> {
        return habitCompletionDao.getCompletionsInDateRange(habitId, startDate, endDate)
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
    
    override suspend fun getCompletionsInDateRange(habitId: Long, startDate: Date, endDate: Date): Int {
        return habitCompletionDao.getCompletionsInDateRange(habitId, startDate, endDate)
    }
    
    override suspend fun getCompletionsForSpecificDate(habitId: Long, dateKey: String): Int {
        return habitCompletionDao.getCompletionsForSpecificDate(habitId, dateKey)
    }
    
    // Streak Calculations
    override suspend fun getCurrentStreak(habitId: Long): Int {
        return habitCompletionDao.getCurrentStreak(habitId)
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
} 