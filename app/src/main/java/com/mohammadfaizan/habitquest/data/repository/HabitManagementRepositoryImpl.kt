package com.mohammadfaizan.habitquest.data.repository

import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.domain.repository.CompleteHabitOutcome
import com.mohammadfaizan.habitquest.domain.repository.HabitCompletionRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitStats
import com.mohammadfaizan.habitquest.domain.repository.HabitWithCompletionStatus
import com.mohammadfaizan.habitquest.domain.repository.HabitWithCompletions
import com.mohammadfaizan.habitquest.domain.repository.MonthlyProgress
import com.mohammadfaizan.habitquest.domain.repository.WeeklyProgress
import com.mohammadfaizan.habitquest.utils.DateUtils
import androidx.room.withTransaction
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitManagementRepositoryImpl(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository,
    private val database: AppDatabase
) : HabitManagementRepository {

    override suspend fun completeHabit(habitId: Long, notes: String?): CompleteHabitOutcome {
        return try {
            // The target-count check and the insert must be one atomic unit — otherwise two
            // near-simultaneous taps can both read "not yet at target" before either commits,
            // and both insert, double-counting a single logical tap.
            // Returns null = habit not found, false = already at target (no-op), true = newly inserted.
            val didInsert = database.withTransaction {
                val habit = habitRepository.getHabitById(habitId) ?: return@withTransaction null

                val todayCompletions =
                    habitCompletionRepository.getCompletionsForSpecificDate(habitId, DateUtils.getCurrentDateKey())

                if (todayCompletions >= habit.targetCount) {
                    return@withTransaction false
                }

                val completion = HabitCompletion(
                    habitId = habitId,
                    notes = notes,
                    dateKey = DateUtils.getCurrentDateKey()
                )
                habitCompletionRepository.insertCompletion(completion)
                habitRepository.incrementCompletions(habitId)
                true
            } ?: return CompleteHabitOutcome.FAILED

            if (didInsert) {
                calculateAndUpdateStreak(habitId)
                CompleteHabitOutcome.COMPLETED
            } else {
                CompleteHabitOutcome.ALREADY_AT_TARGET
            }
        } catch (e: Exception) {
            CompleteHabitOutcome.FAILED
        }
    }

    override suspend fun uncompleteHabit(habitId: Long, dateKey: String): Boolean {
        return try {
            val didDelete = database.withTransaction {
                val completion = habitCompletionRepository.getCompletionForDate(habitId, dateKey)
                if (completion != null) {
                    habitCompletionRepository.deleteCompletion(completion)
                    true
                } else {
                    false
                }
            }
            if (didDelete) {
                calculateAndUpdateStreak(habitId)
            }
            didDelete
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateCompletionNote(habitId: Long, dateKey: String, note: String?): Boolean {
        return try {
            // Same "most recent" convention as undo — for a multi-times-a-day habit, the note
            // attaches to the latest completion, not an arbitrary one.
            val completion = habitCompletionRepository.getCompletionForDate(habitId, dateKey) ?: return false
            habitCompletionRepository.updateCompletion(completion.copy(notes = note))
            true
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
        val habit = habitRepository.getHabitById(habitId)
        if (habit != null) {
            // Update current streak and longest streak if current is higher
            val longestStreak = if (currentStreak > habit.longestStreak) currentStreak else habit.longestStreak
            // Skip the write when nothing changed — an unconditional write here re-triggers the habits Flow on every launch.
            if (currentStreak != habit.currentStreak || longestStreak != habit.longestStreak) {
                val updatedHabit = habit.copy(
                    currentStreak = currentStreak,
                    longestStreak = longestStreak
                )
                habitRepository.updateHabit(updatedHabit)
            }
        } else {
            habitRepository.updateStreak(habitId, currentStreak)
        }
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
        // Normalize by target*days, not just days, or a targetCount>1 habit can read past 100%.
        val expectedCompletions = 30 * habit.targetCount
        val completionRate = if (expectedCompletions > 0) {
            ((recentCompletions.toFloat() / expectedCompletions) * 100).coerceIn(0f, 100f)
        } else 0f

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
        // Delegates to completeHabit() per habit (today-only, same as its single-habit
        // counterpart) so it gets the same race-safe transaction, target-count awareness
        // (fills a multi-times-a-day habit all the way to target, not just one tick), and
        // streak recalculation — instead of duplicating that logic with a naive count check.
        var completedCount = 0
        for (habitId in habitIds) {
            var completedThisHabit = false
            while (completeHabit(habitId, null) == CompleteHabitOutcome.COMPLETED) {
                completedThisHabit = true
            }
            if (completedThisHabit) {
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
    
    override suspend fun recalculateAllStreaks() {
        val habits = habitRepository.getActiveHabits().first()
        coroutineScope {
            habits.forEach { habit ->
                launch { calculateAndUpdateStreak(habit.id) }
            }
        }
    }
    
    override suspend fun checkAndResetStreaksIfNeeded() {
        // Check if we need to recalculate streaks (e.g., new day has started)
        // This should be called on app start or at midnight
        recalculateAllStreaks()
    }
}