package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.HabitCompletionRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import kotlinx.coroutines.flow.first

data class CalendarDayData(
    val dateKey: String,
    // 0f..1f — fraction of active habits completed (aggregate mode) or fraction of a single
    // habit's target count reached (single-habit mode). Absent dates (no key in the result map)
    // mean zero activity, not "no data" — every date in range is worth a muted cell either way.
    val completionRate: Float
)

data class GetCalendarHeatmapResult(
    val success: Boolean,
    val days: Map<String, CalendarDayData> = emptyMap(),
    val error: String? = null
)

class GetCalendarHeatmapUseCase(
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository
) {

    // Aggregate across every active habit: each day's rate is how many distinct habits got at
    // least one completion that day, out of how many habits are active right now. Habits
    // created/deleted partway through the range aren't accounted for — same simplification the
    // rest of the app's rate calculations already make.
    suspend fun getAggregateHeatmap(startDateKey: String, endDateKey: String): GetCalendarHeatmapResult {
        return try {
            val activeHabits = habitRepository.getActiveHabits().first()
            if (activeHabits.isEmpty()) {
                return GetCalendarHeatmapResult(success = true)
            }

            val habitIds = activeHabits.map { it.id }
            val completions = habitCompletionRepository.getCompletionsForHabits(habitIds)
                .filter { it.dateKey in startDateKey..endDateKey }

            val distinctHabitsByDate = completions
                .groupBy({ it.dateKey }) { it.habitId }
                .mapValues { (_, habitIds) -> habitIds.distinct().size }

            val totalHabits = activeHabits.size
            val days = distinctHabitsByDate.mapValues { (dateKey, completedCount) ->
                CalendarDayData(
                    dateKey = dateKey,
                    completionRate = (completedCount.toFloat() / totalHabits).coerceIn(0f, 1f)
                )
            }

            GetCalendarHeatmapResult(success = true, days = days)
        } catch (e: Exception) {
            GetCalendarHeatmapResult(success = false, error = "Failed to load calendar: ${e.message}")
        }
    }

    suspend fun getHabitHeatmap(habitId: Long, startDateKey: String, endDateKey: String): GetCalendarHeatmapResult {
        return try {
            val habit = habitRepository.getHabitById(habitId)
                ?: return GetCalendarHeatmapResult(success = false, error = "Habit not found")

            val completions = habitCompletionRepository
                .getCompletionsInDateRange(habitId, startDateKey, endDateKey)
                .first()

            val countByDate = completions.groupingBy { it.dateKey }.eachCount()
            val days = countByDate.mapValues { (dateKey, count) ->
                CalendarDayData(
                    dateKey = dateKey,
                    completionRate = (count.toFloat() / habit.targetCount).coerceIn(0f, 1f)
                )
            }

            GetCalendarHeatmapResult(success = true, days = days)
        } catch (e: Exception) {
            GetCalendarHeatmapResult(success = false, error = "Failed to load calendar: ${e.message}")
        }
    }
}
