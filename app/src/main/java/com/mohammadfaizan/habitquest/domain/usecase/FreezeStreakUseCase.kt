package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.FreezeStreakOutcome
import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import javax.inject.Inject

data class FreezeStreakResult(
    val success: Boolean,
    val error: String? = null
)

class FreezeStreakUseCase @Inject constructor(
    private val habitManagementRepository: HabitManagementRepository
) {
    suspend operator fun invoke(habitId: Long): FreezeStreakResult {
        return when (habitManagementRepository.freezeStreak(habitId)) {
            FreezeStreakOutcome.FROZEN -> FreezeStreakResult(success = true)
            FreezeStreakOutcome.NO_FREEZES_LEFT -> FreezeStreakResult(success = false, error = "No freezes left")
            FreezeStreakOutcome.HABIT_NOT_FOUND -> FreezeStreakResult(success = false, error = "Habit not found")
        }
    }
}
