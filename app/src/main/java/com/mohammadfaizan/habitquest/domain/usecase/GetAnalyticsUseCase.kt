package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.data.local.DayOfWeekCount
import com.mohammadfaizan.habitquest.domain.repository.HabitCompletionRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import com.mohammadfaizan.habitquest.domain.repository.MonthlyProgress
import com.mohammadfaizan.habitquest.domain.repository.WeeklyProgress
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class GetAnalyticsRequest(
    val habitId: Long? = null, // If null, gets overall analytics
    val weekStart: String? = null,
    val month: String? = null
)

data class AnalyticsData(
    val totalHabits: Int,
    val activeHabits: Int,
    val totalCompletions: Int,
    val averageCompletionRate: Float,
    val topPerformingHabits: List<String>,
    val weeklyProgress: WeeklyProgress? = null,
    val monthlyProgress: MonthlyProgress? = null,
    val completionsByDayOfWeek: List<DayOfWeekCount> = emptyList()
)

data class GetAnalyticsResult(
    val success: Boolean,
    val analytics: AnalyticsData? = null,
    val error: String? = null
)

class GetAnalyticsUseCase @Inject constructor(
    private val habitRepository: HabitRepository,
    private val habitManagementRepository: HabitManagementRepository,
    private val habitCompletionRepository: HabitCompletionRepository
) {

    suspend operator fun invoke(request: GetAnalyticsRequest): GetAnalyticsResult {
        return try {
            if (request.habitId != null) {
                getHabitAnalyticsInternal(request.habitId, request.weekStart, request.month)
            } else {
                getOverallAnalyticsInternal()
            }
        } catch (e: Exception) {
            GetAnalyticsResult(success = false, error = "Failed to get analytics: ${e.message}")
        }
    }

    private suspend fun getHabitAnalyticsInternal(
        habitId: Long,
        weekStart: String?,
        month: String?
    ): GetAnalyticsResult {
        val stats = habitManagementRepository.getHabitStats(habitId)

        val weeklyProgress = weekStart?.let {
            habitManagementRepository.getWeeklyProgress(habitId, it)
        }

        val monthlyProgress = month?.let {
            habitManagementRepository.getMonthlyProgress(habitId, it)
        }

        val analytics = AnalyticsData(
            totalHabits = 1,
            activeHabits = 1,
            totalCompletions = stats.totalCompletions,
            averageCompletionRate = stats.completionRate,
            topPerformingHabits = emptyList(),
            weeklyProgress = weeklyProgress,
            monthlyProgress = monthlyProgress
        )

        return GetAnalyticsResult(success = true, analytics = analytics)
    }

    private suspend fun getOverallAnalyticsInternal(): GetAnalyticsResult {
        val totalHabits = habitRepository.getTotalHabitCount()
        val activeHabits = habitRepository.getActiveHabitCount()

        val topStreakHabits = habitRepository.getTopStreakHabits(5).first()
        val topCompletionHabits = habitRepository.getTopCompletionHabits(5).first()

        val topPerformingHabits = (topStreakHabits + topCompletionHabits)
            .distinctBy { it.id }
            .take(5)
            .map { it.name }

        val totalCompletions = habitCompletionRepository.getTotalCompletionsCount()

        val activeHabitsList = habitRepository.getActiveHabits().first()
        val averageCompletionRate = if (activeHabitsList.isNotEmpty()) {
            coroutineScope {
                activeHabitsList
                    .map { habit -> async { habitManagementRepository.getHabitStats(habit.id).completionRate } }
                    .map { it.await() }
                    .average()
                    .toFloat()
            }
        } else 0.0f

        val completionsByDayOfWeek = habitCompletionRepository.getCompletionCountsByDayOfWeek()

        val analytics = AnalyticsData(
            totalHabits = totalHabits,
            activeHabits = activeHabits,
            totalCompletions = totalCompletions,
            averageCompletionRate = averageCompletionRate,
            topPerformingHabits = topPerformingHabits,
            completionsByDayOfWeek = completionsByDayOfWeek
        )

        return GetAnalyticsResult(success = true, analytics = analytics)
    }

    suspend fun getHabitAnalytics(habitId: Long): GetAnalyticsResult {
        return invoke(GetAnalyticsRequest(habitId = habitId))
    }

    suspend fun getWeeklyAnalytics(habitId: Long, weekStart: String): GetAnalyticsResult {
        return invoke(GetAnalyticsRequest(habitId = habitId, weekStart = weekStart))
    }

    suspend fun getMonthlyAnalytics(habitId: Long, month: String): GetAnalyticsResult {
        return invoke(GetAnalyticsRequest(habitId = habitId, month = month))
    }

    suspend fun getOverallAnalytics(): GetAnalyticsResult {
        return invoke(GetAnalyticsRequest())
    }
} 