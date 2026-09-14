package com.mohammadfaizan.habitquest.widget

import android.content.Context
import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.utils.DateUtils
import kotlinx.coroutines.flow.first

data class WidgetDayState(
    val dateKey: String,
    // Mirrors HabitCompletionRepositoryImpl.getCurrentStreak: a completion or a freeze both
    // count as "covered" for streak-continuity purposes, regardless of target count.
    val isCovered: Boolean
)

data class HabitWidgetData(
    val habitId: Long,
    val name: String,
    val icon: String?,
    val colorHex: String,
    val currentStreak: Int,
    val isFullyCompletedToday: Boolean,
    // Oldest to newest, most recent entry is today.
    val days: List<WidgetDayState>
)

// Manual DAO wiring, same pattern as StreakResetReceiver — widgets and their action callbacks
// run outside any Activity/ViewModel scope, so there's no existing repository instance to reuse.
suspend fun loadHabitWidgetData(context: Context, habitId: Long, days: Int): HabitWidgetData? {
    val db = AppDatabase.getInstance(context)
    val habit = db.habitDao().getHabitById(habitId) ?: return null

    val todayKey = DateUtils.getCurrentDateKey()
    val startKey = DateUtils.getDateKeyForDaysAgo(days - 1)
    val completions = db.habitCompletionDao()
        .getCompletionsInDateRange(habitId, startKey, todayKey)
        .first()
    val freezeDates = db.habitFreezeDao().getFreezeDates(habitId).toSet()
    val completionCountByDate = completions.groupingBy { it.dateKey }.eachCount()

    val dayStates = (days - 1 downTo 0).map { daysAgo ->
        val dateKey = DateUtils.getDateKeyForDaysAgo(daysAgo)
        WidgetDayState(
            dateKey = dateKey,
            isCovered = (completionCountByDate[dateKey] ?: 0) > 0 || dateKey in freezeDates
        )
    }

    return HabitWidgetData(
        habitId = habit.id,
        name = habit.name,
        icon = habit.icon,
        colorHex = habit.color,
        currentStreak = habit.currentStreak,
        isFullyCompletedToday = (completionCountByDate[todayKey] ?: 0) >= habit.targetCount,
        days = dayStates
    )
}
