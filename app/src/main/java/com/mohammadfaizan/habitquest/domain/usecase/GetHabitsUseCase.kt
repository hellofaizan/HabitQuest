package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class GetHabitsRequest(
    val includeInactive: Boolean = false,
    val category: String? = null,
    val searchQuery: String? = null,
    val limit: Int? = null
)

data class GetHabitsResult(
    val success: Boolean,
    val habits: List<Habit> = emptyList(),
    val error: String? = null
)

class GetHabitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val habitManagementRepository: HabitManagementRepository
) {
    
    suspend operator fun invoke(request: GetHabitsRequest): GetHabitsResult {
        return try {
            val habits = when {
                request.category != null -> {
                    habitRepository.getHabitsByCategory(request.category).first()
                }
                request.searchQuery != null -> {
                    habitRepository.searchHabits(request.searchQuery).first()
                }
                !request.includeInactive -> {
                    habitRepository.getActiveHabits().first()
                }
                else -> {
                    habitRepository.getAllHabits().first()
                }
            }
            
            val filteredHabits = request.limit?.let { limit ->
                habits.take(limit)
            } ?: habits
            
            GetHabitsResult(success = true, habits = filteredHabits)
            
        } catch (e: Exception) {
            GetHabitsResult(success = false, error = "Failed to get habits: ${e.message}")
        }
    }
    
    // Convenience method for getting active habits
    suspend fun getActiveHabits(): GetHabitsResult {
        return invoke(GetHabitsRequest(includeInactive = false))
    }
    
    // Convenience method for getting habits by category
    suspend fun getHabitsByCategory(category: String): GetHabitsResult {
        return invoke(GetHabitsRequest(category = category))
    }
    
    // Convenience method for searching habits
    suspend fun searchHabits(query: String): GetHabitsResult {
        return invoke(GetHabitsRequest(searchQuery = query))
    }
} 