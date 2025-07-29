package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import javax.inject.Inject

data class DeleteHabitRequest(
    val habitId: Long
)

data class DeleteHabitResult(
    val success: Boolean,
    val error: String? = null
)

class DeleteHabitUseCase @Inject constructor(
    private val habitManagementRepository: HabitManagementRepository
) {

    suspend operator fun invoke(request: DeleteHabitRequest): DeleteHabitResult {
        return try {
            // Check if habit exists
            val habit = habitManagementRepository.getHabitWithCompletions(request.habitId)
            if (habit == null) {
                return DeleteHabitResult(success = false, error = "Habit not found")
            }

            // Delete habit and all its completions
            val deleted = habitManagementRepository.deleteHabitAndCompletions(request.habitId)

            if (deleted) {
                DeleteHabitResult(success = true)
            } else {
                DeleteHabitResult(success = false, error = "Failed to delete habit")
            }

        } catch (e: Exception) {
            DeleteHabitResult(success = false, error = "Failed to delete habit: ${e.message}")
        }
    }

    suspend fun deleteHabit(habitId: Long): DeleteHabitResult {
        return invoke(DeleteHabitRequest(habitId = habitId))
    }
} 