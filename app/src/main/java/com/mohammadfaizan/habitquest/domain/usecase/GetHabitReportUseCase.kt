package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.HabitCompletionRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import com.mohammadfaizan.habitquest.utils.DateUtils
import kotlinx.coroutines.flow.first

data class HabitPeriodStat(
    val habitId: Long,
    val name: String,
    val icon: String?,
    val color: String,
    val completedDays: Int,
    val totalDays: Int,
    val rawCompletions: Int,
    // 0f..100f
    val completionRate: Float
)

data class PeriodReport(
    val startDateKey: String,
    val endDateKey: String,
    val totalDays: Int,
    val habitStats: List<HabitPeriodStat>,
    val overallCompletionRate: Float,
    val previousOverallCompletionRate: Float?,
    val totalCompletions: Int,
    val bestHabits: List<HabitPeriodStat>,
    val needsAttentionHabits: List<HabitPeriodStat>
)

// Below this rate a habit shows up under "Needs Attention" rather than being left off entirely.
private const val NEEDS_ATTENTION_THRESHOLD = 50f

class GetHabitReportUseCase(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository
) {

    suspend fun getReport(
        startDateKey: String,
        endDateKey: String,
        previousStartDateKey: String,
        previousEndDateKey: String
    ): PeriodReport {
        val activeHabits = habitRepository.getActiveHabits().first()
        val totalDays = (DateUtils.getDaysBetween(startDateKey, endDateKey) + 1).coerceAtLeast(1)

        val habitStats = activeHabits.map { habit ->
            statFor(habit.id, habit.name, habit.icon, habit.color, habit.targetCount, startDateKey, endDateKey, totalDays)
        }

        val overallRate = habitStats.averageRateOrZero()
        val totalCompletions = habitStats.sumOf { it.rawCompletions }

        val previousTotalDays = (DateUtils.getDaysBetween(previousStartDateKey, previousEndDateKey) + 1).coerceAtLeast(1)
        val previousOverallRate = if (activeHabits.isEmpty()) {
            null
        } else {
            activeHabits.map { habit ->
                statFor(
                    habit.id, habit.name, habit.icon, habit.color, habit.targetCount,
                    previousStartDateKey, previousEndDateKey, previousTotalDays
                ).completionRate
            }.average().toFloat()
        }

        val sortedByRateDesc = habitStats.sortedByDescending { it.completionRate }
        val bestHabits = sortedByRateDesc.filter { it.completionRate > 0f }.take(3)
        val needsAttention = habitStats
            .filter { it.completionRate < NEEDS_ATTENTION_THRESHOLD }
            .sortedBy { it.completionRate }
            .take(3)

        return PeriodReport(
            startDateKey = startDateKey,
            endDateKey = endDateKey,
            totalDays = totalDays,
            habitStats = habitStats,
            overallCompletionRate = overallRate,
            previousOverallCompletionRate = previousOverallRate,
            totalCompletions = totalCompletions,
            bestHabits = bestHabits,
            needsAttentionHabits = needsAttention
        )
    }

    private suspend fun statFor(
        habitId: Long,
        name: String,
        icon: String?,
        color: String,
        targetCount: Int,
        startDateKey: String,
        endDateKey: String,
        totalDays: Int
    ): HabitPeriodStat {
        val completions = habitCompletionRepository
            .getCompletionsInDateRange(habitId, startDateKey, endDateKey)
            .first()
        val countByDate = completions.groupingBy { it.dateKey }.eachCount()
        val completedDays = countByDate.count { (_, count) -> count >= targetCount }

        return HabitPeriodStat(
            habitId = habitId,
            name = name,
            icon = icon,
            color = color,
            completedDays = completedDays,
            totalDays = totalDays,
            rawCompletions = completions.size,
            completionRate = (completedDays.toFloat() / totalDays * 100f).coerceIn(0f, 100f)
        )
    }

    private fun List<HabitPeriodStat>.averageRateOrZero(): Float {
        return if (isEmpty()) 0f else map { it.completionRate }.average().toFloat()
    }
}
