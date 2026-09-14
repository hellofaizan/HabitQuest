package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import javax.inject.Inject

class ArchiveHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habitId: Long, isActive: Boolean) {
        habitRepository.updateHabitStatus(habitId, isActive)
    }
}
