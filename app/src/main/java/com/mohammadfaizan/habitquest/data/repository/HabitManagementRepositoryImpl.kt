package com.mohammadfaizan.habitquest.data.repository

import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.domain.repository.HabitCompletionRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitStats
import com.mohammadfaizan.habitquest.domain.repository.HabitWithCompletionStatus
import com.mohammadfaizan.habitquest.domain.repository.HabitWithCompletions
import com.mohammadfaizan.habitquest.domain.repository.MonthlyProgress
import com.mohammadfaizan.habitquest.domain.repository.WeeklyProgress
import com.mohammadfaizan.habitquest.utils.DateUtils
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitManagementRepositoryImpl(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository
) : HabitManagementRepository {

    override suspend fun completeHabit(habitId: Long, notes: String?): Boolean {
        return try {
            val habit = habitRepository.getHabitById(habitId) ?: return false
            val dateKey = DateUtils.getCurrentDateKey()

            val todayCompletions =
                habitCompletionRepository.getCompletionsForSpecificDate(habitId, dateKey)

            if (todayCompletions >= habit.targetCount) {
                return true
            }

            val completion = HabitCompletion(
                habitId = habitId,
                notes = notes,
                dateKey = dateKey
            )
            habitCompletionRepository.insertCompletion(completion)

            habitRepository.incrementCompletions(habitId)

            calculateAndUpdateStreak(habitId)

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun uncompleteHabit(habitId: Long, dateKey: String): Boolean {
        return try {
            val completion = habitCompletionRepository.getCompletionForDate(habitId, dateKey)
            completion?.let {
                habitCompletionRepository.deleteCompletion(it)
                calculateAndUpdateStreak(habitId)
            }
            completion != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getHabitWithCompletions(habitId: Long): HabitWithCompletions? {
        val habit = habitRepository.getHabitById(habitId) ?: return null
        val completions = habitCompletionRepository.getCompletionsForHabit(habitId).first()
        return HabitWithCompletions(habit, completions)
    }

    override suspend fun getHabitsWithCompletionStatus(dateKey: String): List<HabitWithCompletionStatus> {
        val habits = habitRepository.getActiveHabits().first()
        return habits.map { habit ->
            val isCompleted =
                habitCompletionRepository.isHabitCompletedForDate(habit.id, dateKey) > 0
            val completionCount =
                habitCompletionRepository.getCompletionsForSpecificDate(habit.id, dateKey)
            HabitWithCompletionStatus(habit, isCompleted, completionCount)
        }
    }

    override suspend fun calculateAndUpdateStreak(habitId: Long): Int {
        val currentStreak = habitCompletionRepository.getCurrentStreak(habitId)
        habitRepository.updateStreak(habitId, currentStreak)
        return currentStreak
    }

    override suspend fun getHabitStats(habitId: Long): HabitStats {
        val habit = habitRepository.getHabitById(habitId)
            ?: throw IllegalArgumentException("Habit not found")
        val totalCompletions = habitCompletionRepository.getTotalCompletionsForHabit(habitId)
        val currentStreak = habit.currentStreak
        val longestStreak = habit.longestStreak

        val thirtyDaysAgo = DateUtils.getDateKeyForDaysAgo(30)
        val recentCompletions =
            habitCompletionRepository.getCompletionsSinceDate(habitId, thirtyDaysAgo)
        val completionRate = if (recentCompletions > 0) (recentCompletions / 30.0f) * 100 else 0f

        val averageCompletionsPerDay = if (totalCompletions > 0) {
            val daysSinceCreation = getDaysSinceCreation(habit.createdAt)
            if (daysSinceCreation > 0) totalCompletions.toFloat() / daysSinceCreation else 0f
        } else 0f

        return HabitStats(
            totalCompletions = totalCompletions,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            completionRate = completionRate,
            averageCompletionsPerDay = averageCompletionsPerDay
        )
    }

    override suspend fun completeHabitsForDate(habitIds: List<Long>, dateKey: String): Int {
        var completedCount = 0
        for (habitId in habitIds) {
            val isCompleted = habitCompletionRepository.isHabitCompletedForDate(habitId, dateKey)
            if (isCompleted == 0) {
                val completion = HabitCompletion(
                    habitId = habitId,
                    dateKey = dateKey
                )
                habitCompletionRepository.insertCompletion(completion)
                habitRepository.incrementCompletions(habitId)
                completedCount++
            }
        }
        return completedCount
    }

    override suspend fun deleteHabitAndCompletions(habitId: Long): Boolean {
        return try {
            habitCompletionRepository.deleteAllCompletionsForHabit(habitId)
            habitRepository.deleteHabitById(habitId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getWeeklyProgress(habitId: Long, weekStart: String): WeeklyProgress {
        val weekEnd = getWeekEnd(weekStart)
        val completions =
            habitCompletionRepository.getCompletionsInDateRange(habitId, weekStart, weekEnd).first()
        val daysCompleted = completions.size
        val totalDays = 7
        val completionRate = (daysCompleted.toFloat() / totalDays) * 100
        val streak = habitCompletionRepository.getCurrentStreak(habitId)

        return WeeklyProgress(
            habitId = habitId,
            weekStart = weekStart,
            daysCompleted = daysCompleted,
            totalDays = totalDays,
            completionRate = completionRate,
            streak = streak
        )
    }

    override suspend fun getMonthlyProgress(habitId: Long, month: String): MonthlyProgress {
        val monthStart = "$month-01"
        val monthEnd = getMonthEnd(month)
        val completions =
            habitCompletionRepository.getCompletionsInDateRange(habitId, monthStart, monthEnd)
                .first()
        val daysCompleted = completions.size
        val totalDays = getDaysInMonth(month)
        val completionRate = (daysCompleted.toFloat() / totalDays) * 100
        val averageCompletionsPerDay =
            if (totalDays > 0) daysCompleted.toFloat() / totalDays else 0f

        return MonthlyProgress(
            habitId = habitId,
            month = month,
            daysCompleted = daysCompleted,
            totalDays = totalDays,
            completionRate = completionRate,
            averageCompletionsPerDay = averageCompletionsPerDay
        )
    }

    private fun getCurrentDateKey(): String {
        return DateUtils.getCurrentDateKey()
    }

    private fun getDateKeyForDaysAgo(days: Int): String {
        return DateUtils.getDateKeyForDaysAgo(days)
    }

    private fun getDaysSinceCreation(createdAt: Date): Int {
        val now = Date()
        val diffInMillis = now.time - createdAt.time
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
    }

    private fun getWeekEnd(weekStart: String): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        calendar.time = dateFormat.parse(weekStart) ?: Date()
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        return dateFormat.format(calendar.time)
    }

    private fun getMonthEnd(month: String): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        calendar.time = dateFormat.parse(month) ?: Date()
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val endDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return endDateFormat.format(calendar.time)
    }

    private fun getDaysInMonth(month: String): Int {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        calendar.time = dateFormat.parse(month) ?: Date()
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}