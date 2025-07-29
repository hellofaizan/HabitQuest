package com.mohammadfaizan.habitquest.domain.repository

import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion

interface HabitManagementRepository {

    suspend fun completeHabit(habitId: Long, notes: String? = null): Boolean
    suspend fun uncompleteHabit(habitId: Long, dateKey: String): Boolean
    suspend fun getHabitWithCompletions(habitId: Long): HabitWithCompletions?
    suspend fun getHabitsWithCompletionStatus(dateKey: String): List<HabitWithCompletionStatus>
    suspend fun calculateAndUpdateStreak(habitId: Long): Int
    suspend fun getHabitStats(habitId: Long): HabitStats

    suspend fun completeHabitsForDate(habitIds: List<Long>, dateKey: String): Int
    suspend fun deleteHabitAndCompletions(habitId: Long): Boolean

    suspend fun getWeeklyProgress(habitId: Long, weekStart: String): WeeklyProgress
    suspend fun getMonthlyProgress(habitId: Long, month: String): MonthlyProgress
}

data class HabitWithCompletions(
    val habit: Habit,
    val completions: List<HabitCompletion>
)

data class HabitWithCompletionStatus(
    val habit: Habit,
    val isCompleted: Boolean,
    val completionCount: Int
)

data class HabitStats(
    val totalCompletions: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Float,
    val averageCompletionsPerDay: Float
)

data class WeeklyProgress(
    val habitId: Long,
    val weekStart: String,
    val daysCompleted: Int,
    val totalDays: Int,
    val completionRate: Float,
    val streak: Int
)

data class MonthlyProgress(
    val habitId: Long,
    val month: String,
    val daysCompleted: Int,
    val totalDays: Int,
    val completionRate: Float,
    val averageCompletionsPerDay: Float
) 