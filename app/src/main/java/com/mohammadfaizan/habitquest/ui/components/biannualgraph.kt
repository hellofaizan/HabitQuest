package com.mohammadfaizan.habitquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ContributionGraph(
    habit: Habit,
    completions: List<HabitCompletion>,
    freezeDates: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    val habitColor = Color(habit.color.toColorInt())
    val graphDays = 182
    // 182 days (~6 months); reuses one Calendar/formatter instead of allocating 182 of each.
    val days = remember(habit.id, completions, freezeDates) {
        val completionMap = completions.groupBy { it.dateKey }
        val freezeSet = freezeDates.toSet()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        List(graphDays) { dayOffset ->
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
            val dateKey = dateFormat.format(calendar.time)
            val dayCompletions = completionMap[dateKey] ?: emptyList()
            DayData(
                dateKey = dateKey,
                completionCount = dayCompletions.size,
                targetCount = habit.targetCount,
                isFrozen = dateKey in freezeSet
            )
        }.reversed()
    }

    // Create 7x26 grid (7 rows, 26 columns = 182 days)
    val gridData = remember(days) {
        val columns = 26
        val rows = 7
        val grid = Array(rows) { row ->
            Array(columns) { col ->
                val dayIndex = row + col * rows
                if (dayIndex < days.size) {
                    days[dayIndex]
                } else {
                    DayData("", 0, habit.targetCount, isFrozen = false)
                }
            }
        }
        grid
    }

    Column(
        modifier = modifier
            .padding(horizontal = 5.dp)
    ) {
        // Render 7x26 grid
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (row in 0 until 7) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (col in 0 until 26) {
                        val day = gridData[row][col]
                        ContributionDay(
                            day = day,
                            habitColor = habitColor,
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                        )
                    }
                }
            }
        }
    }
}

// A fixed icy blue rather than a habit-color tint — a frozen day is a distinct *state*
// (protected, not actually done), so it should read the same way across every habit.
// Not private: HabitDetailScreen reuses it for the freeze-streak card so the color stays consistent.
val FrozenDayColor = Color(0xFF7EC8E3)

@Composable
fun ContributionDay(
    day: DayData,
    habitColor: Color,
    modifier: Modifier = Modifier
) {
    val alpha = when {
        day.completionCount == 0 -> 0.1f
        day.completionCount >= day.targetCount -> 1.0f
        else -> (day.completionCount.toFloat() / day.targetCount.toFloat()) * 0.8f + 0.2f
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .border(
                width = 1.dp,
                color = if (day.isFrozen && day.completionCount == 0) {
                    FrozenDayColor.copy(alpha = 0.5f)
                } else {
                    habitColor.copy(alpha = 0.03f)
                },
                shape = RoundedCornerShape(2.dp)
            )
            .background(
                when {
                    day.completionCount > 0 -> habitColor.copy(alpha = alpha)
                    day.isFrozen -> FrozenDayColor.copy(alpha = 0.55f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            )
    )
}

data class DayData(
    val dateKey: String,
    val completionCount: Int,
    val targetCount: Int,
    val isFrozen: Boolean = false
)