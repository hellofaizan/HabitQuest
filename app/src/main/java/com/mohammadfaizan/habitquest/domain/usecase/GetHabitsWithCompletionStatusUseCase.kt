package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitWithCompletionStatus
import javax.inject.Inject

data class GetHabitsWithCompletionStatusRequest(
    val dateKey: String? = null // If null, uses current date
)

data class GetHabitsWithCompletionStatusResult(
    val success: Boolean,
    val habits: List<HabitWithCompletionStatus> = emptyList(),
    val error: String? = null
)

class GetHabitsWithCompletionStatusUseCase @Inject constructor(
    private val habitManagementRepository: HabitManagementRepository
) {
    
    suspend operator fun invoke(request: GetHabitsWithCompletionStatusRequest): GetHabitsWithCompletionStatusResult {
        return try {
            val dateKey = request.dateKey ?: getCurrentDateKey()
            val habits = habitManagementRepository.getHabitsWithCompletionStatus(dateKey)
            
            GetHabitsWithCompletionStatusResult(success = true, habits = habits)
            
        } catch (e: Exception) {
            GetHabitsWithCompletionStatusResult(success = false, error = "Failed to get habits with completion status: ${e.message}")
        }
    }
    
    // Convenience method for getting habits for today
    suspend fun getHabitsForToday(): GetHabitsWithCompletionStatusResult {
        return invoke(GetHabitsWithCompletionStatusRequest())
    }
    
    // Convenience method for getting habits for specific date
    suspend fun getHabitsForDate(dateKey: String): GetHabitsWithCompletionStatusResult {
        return invoke(GetHabitsWithCompletionStatusRequest(dateKey = dateKey))
    }
    
    // Helper method to get current date key
    private fun getCurrentDateKey(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
} 