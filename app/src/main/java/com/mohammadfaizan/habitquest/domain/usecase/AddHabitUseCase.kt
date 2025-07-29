package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitFrequency
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import javax.inject.Inject

data class AddHabitRequest(
    val name: String,
    val description: String? = null,
    val color: String,
    val icon: String? = null,
    val targetCount: Int = 1,
    val frequency: String = "DAILY",
    val category: String? = null,
    val reminderTime: String? = null,
    val reminderEnabled: Boolean = false
)

data class AddHabitResult(
    val success: Boolean,
    val habitId: Long? = null,
    val error: String? = null
)

class AddHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {

    suspend operator fun invoke(request: AddHabitRequest): AddHabitResult {
        return try {
            if (request.name.isBlank()) {
                return AddHabitResult(success = false, error = "Habit name cannot be empty")
            }

            if (request.targetCount <= 0) {
                return AddHabitResult(
                    success = false,
                    error = "Target count must be greater than 0"
                )
            }

            val habit = Habit(
                name = request.name.trim(),
                description = request.description?.trim(),
                color = request.color,
                icon = request.icon,
                targetCount = request.targetCount,
                frequency = HabitFrequency.valueOf(request.frequency.uppercase()),
                category = request.category?.trim(),
                reminderTime = request.reminderTime,
                reminderEnabled = request.reminderEnabled
            )

            val habitId = habitRepository.insertHabit(habit)

            AddHabitResult(success = true, habitId = habitId)

        } catch (e: Exception) {
            AddHabitResult(success = false, error = "Failed to add habit: ${e.message}")
        }
    }
} 