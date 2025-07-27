package com.mohammadfaizan.habitquest.domain.repository

import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.data.local.CompletionPattern
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface HabitCompletionRepository {
    
    // Basic CRUD Operations
    suspend fun insertCompletion(completion: HabitCompletion): Long
    suspend fun updateCompletion(completion: HabitCompletion)
    suspend fun deleteCompletion(completion: HabitCompletion)
    
    // Query Operations
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>
    suspend fun getCompletionForDate(habitId: Long, dateKey: String): HabitCompletion?
    fun getCompletionsInDateRange(habitId: Long, startDate: String, endDate: String): Flow<List<HabitCompletion>>
    
    // Completion Status
    suspend fun isHabitCompletedForDate(habitId: Long, dateKey: String): Int
    
    // Statistics
    suspend fun getTotalCompletionsForHabit(habitId: Long): Int
    suspend fun getCompletionsSinceDate(habitId: Long, startDate: String): Int
    suspend fun getCompletionsInDateRange(habitId: Long, startDate: Date, endDate: Date): Int
    suspend fun getCompletionsForSpecificDate(habitId: Long, dateKey: String): Int
    
    // Streak Calculations
    suspend fun getCurrentStreak(habitId: Long): Int
    
    // Bulk Operations
    suspend fun deleteAllCompletionsForHabit(habitId: Long)
    suspend fun deleteCompletionsBeforeDate(dateKey: String)
    suspend fun deleteOldCompletions(cutoffDate: Date)
    
    // Recent Activity
    fun getRecentCompletions(limit: Int): Flow<List<HabitCompletion>>
    
    // Patterns and Analytics
    fun getCompletionPattern(habitId: Long, limit: Int): Flow<List<CompletionPattern>>
} 