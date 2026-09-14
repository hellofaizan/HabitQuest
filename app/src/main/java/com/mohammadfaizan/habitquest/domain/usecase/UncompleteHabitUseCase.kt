package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import javax.inject.Inject

data class UncompleteHabitResult(
    val success: Boolean,
    val error: String? = null
)

class UncompleteHabitUseCase @Inject constructor(
    private val habitManagementRepository: HabitManagementRepository
) {
    suspend operator fun invoke(habitId: Long, dateKey: String): UncompleteHabitResult {
        return try {
            val didUndo = habitManagementRepository.uncompleteHabit(habitId, dateKey)
            if (didUndo) {
                UncompleteHabitResult(success = true)
            } else {
                UncompleteHabitResult(success = false, error = "No completion to undo")
            }
        } catch (e: Exception) {
            UncompleteHabitResult(success = false, error = "Failed to undo completion: ${e.message}")
        }
    }
}
