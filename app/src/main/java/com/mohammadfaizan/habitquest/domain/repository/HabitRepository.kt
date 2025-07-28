package com.mohammadfaizan.habitquest.domain.repository

import com.mohammadfaizan.habitquest.data.local.Habit
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface HabitRepository {

    // Habit Operations
    fun getAllHabits(): Flow<List<Habit>>
    fun getActiveHabits(): Flow<List<Habit>>
    suspend fun getHabitById(habitId: Long): Habit?
    suspend fun insertHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(habit: Habit)
    suspend fun deleteHabitById(habitId: Long)

    // Search and Filter
    fun searchHabits(query: String): Flow<List<Habit>>
    fun getHabitsByCategory(category: String): Flow<List<Habit>>
    fun getHabitsCreatedSince(startDate: Date): Flow<List<Habit>>

    // Progress and Statistics
    suspend fun updateStreak(habitId: Long, streak: Int)
    suspend fun incrementCompletions(habitId: Long)
    suspend fun getActiveHabitCount(): Int
    suspend fun getTotalHabitCount(): Int
    suspend fun getHabitCountByCategory(category: String): Int

    // Categories
    fun getAllCategories(): Flow<List<String>>

    // Top Performing Habits
    fun getTopStreakHabits(limit: Int): Flow<List<Habit>>
    fun getTopCompletionHabits(limit: Int): Flow<List<Habit>>

    // Bulk Operations
    suspend fun deactivateHabitsByCategory(category: String)
    suspend fun deleteHabitsByCategory(category: String)
} 