package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import javax.inject.Inject

class ReorderHabitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(orderedHabitIds: List<Long>) {
        habitRepository.updateHabitsOrder(orderedHabitIds)
    }
}
