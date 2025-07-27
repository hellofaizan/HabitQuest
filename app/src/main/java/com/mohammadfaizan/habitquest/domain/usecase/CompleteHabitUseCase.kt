package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import javax.inject.Inject

data class CompleteHabitRequest(
    val habitId: Long,
    val notes: String? = null,
    val dateKey: String? = null // If null, uses current date
)

data class CompleteHabitResult(
    val success: Boolean,
    val wasAlreadyCompleted: Boolean = false,
    val newStreak: Int = 0,
    val error: String? = null
)

class CompleteHabitUseCase @Inject constructor(
    private val habitManagementRepository: HabitManagementRepository
) {
    
    suspend operator fun invoke(request: CompleteHabitRequest): CompleteHabitResult {
        return try {
            // Check if habit exists and is active
            val habit = habitManagementRepository.getHabitWithCompletions(request.habitId)
            if (habit == null) {
                return CompleteHabitResult(success = false, error = "Habit not found")
            }
            
            if (!habit.habit.isActive) {
                return CompleteHabitResult(success = false, error = "Cannot complete inactive habit")
            }
            
            // Complete the habit (repository handles duplicate checking)
            val completionSuccess = habitManagementRepository.completeHabit(request.habitId, request.notes)
            
            if (!completionSuccess) {
                return CompleteHabitResult(success = false, error = "Failed to complete habit")
            }
            
            // Get updated streak
            val newStreak = habitManagementRepository.calculateAndUpdateStreak(request.habitId)
            
            CompleteHabitResult(
                success = true,
                wasAlreadyCompleted = false,
                newStreak = newStreak
            )
            
        } catch (e: Exception) {
            CompleteHabitResult(success = false, error = "Failed to complete habit: ${e.message}")
        }
    }
    
    // Convenience method for completing habit for today
    suspend fun completeHabitForToday(habitId: Long, notes: String? = null): CompleteHabitResult {
        return invoke(CompleteHabitRequest(habitId = habitId, notes = notes))
    }
    
    // Helper method to get current date key
    private fun getCurrentDateKey(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
} 