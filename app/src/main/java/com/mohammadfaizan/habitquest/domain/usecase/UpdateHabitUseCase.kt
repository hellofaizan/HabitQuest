package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitFrequency
import javax.inject.Inject

class UpdateHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(
        habitId: Long,
        name: String,
        description: String?,
        color: String,
        category: String?,
        frequency: String,
        targetCount: Int,
        reminderEnabled: Boolean,
        reminderTime: String?
    ): UpdateHabitResult {
        return try {
            val existingHabit = habitRepository.getHabitById(habitId)
            if (existingHabit == null) {
                return UpdateHabitResult.Error("Habit not found")
            }

            val updatedHabit = existingHabit.copy(
                name = name,
                description = description,
                color = color,
                category = category,
                frequency = HabitFrequency.valueOf(frequency),
                targetCount = targetCount,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime
            )

            habitRepository.updateHabit(updatedHabit)
            
            UpdateHabitResult.Success(updatedHabit)
        } catch (e: Exception) {
            UpdateHabitResult.Error("Failed to update habit: ${e.message}")
        }
    }
}

sealed class UpdateHabitResult {
    data class Success(val habit: Habit) : UpdateHabitResult()
    data class Error(val message: String) : UpdateHabitResult()
} 