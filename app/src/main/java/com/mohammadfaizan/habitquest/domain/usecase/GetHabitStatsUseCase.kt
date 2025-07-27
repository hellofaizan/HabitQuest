package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitStats
import javax.inject.Inject

data class GetHabitStatsRequest(
    val habitId: Long
)

data class GetHabitStatsResult(
    val success: Boolean,
    val stats: HabitStats? = null,
    val error: String? = null
)

class GetHabitStatsUseCase @Inject constructor(
    private val habitManagementRepository: HabitManagementRepository
) {
    
    suspend operator fun invoke(request: GetHabitStatsRequest): GetHabitStatsResult {
        return try {
            val stats = habitManagementRepository.getHabitStats(request.habitId)
            GetHabitStatsResult(success = true, stats = stats)
            
        } catch (e: Exception) {
            GetHabitStatsResult(success = false, error = "Failed to get habit stats: ${e.message}")
        }
    }
    
    // Convenience method
    suspend fun getStats(habitId: Long): GetHabitStatsResult {
        return invoke(GetHabitStatsRequest(habitId = habitId))
    }
} 