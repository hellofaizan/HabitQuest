package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import com.mohammadfaizan.habitquest.utils.DateUtils
import javax.inject.Inject

data class CompleteHabitRequest(
    val habitId: Long,
    val notes: String? = null,
    val dateKey: String? = null
)

data class CompleteHabitResult(
    val success: Boolean,
    val wasAlreadyCompleted: Boolean = false,
    val newStreak: Int = 0,
    val currentCompletions: Int = 0,
    val targetCount: Int = 0,
    val error: String? = null
)

class CompleteHabitUseCase @Inject constructor(
    private val habitManagementRepository: HabitManagementRepository
) {

    suspend operator fun invoke(request: CompleteHabitRequest): CompleteHabitResult {
        return try {
            val habit = habitManagementRepository.getHabitWithCompletions(request.habitId)
            if (habit == null) {
                return CompleteHabitResult(success = false, error = "Habit not found")
            }

            if (!habit.habit.isActive) {
                return CompleteHabitResult(
                    success = false,
                    error = "Cannot complete inactive habit"
                )
            }

            val completionSuccess =
                habitManagementRepository.completeHabit(request.habitId, request.notes)

            if (!completionSuccess) {
                return CompleteHabitResult(success = false, error = "Failed to complete habit")
            }

            val newStreak = habitManagementRepository.calculateAndUpdateStreak(request.habitId)

            val todayCompletions =
                habitManagementRepository.getHabitsWithCompletionStatus(DateUtils.getCurrentDateKey())
                    .find { it.habit.id == request.habitId }?.completionCount ?: 0

            CompleteHabitResult(
                success = true,
                wasAlreadyCompleted = false,
                newStreak = newStreak,
                currentCompletions = todayCompletions,
                targetCount = habit.habit.targetCount
            )

        } catch (e: Exception) {
            CompleteHabitResult(success = false, error = "Failed to complete habit: ${e.message}")
        }
    }

    suspend fun completeHabitForToday(habitId: Long, notes: String? = null): CompleteHabitResult {
        return invoke(CompleteHabitRequest(habitId = habitId, notes = notes))
    }

    private fun getCurrentDateKey(): String {
        return DateUtils.getCurrentDateKey()
    }
}