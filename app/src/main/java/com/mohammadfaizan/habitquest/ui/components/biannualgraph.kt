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
    modifier: Modifier = Modifier
) {
    val habitColor = Color(habit.color.toColorInt())
    Calendar.getInstance()
    val graphDays = 182 // 182 days (around 6 months)

    val completionMap = completions.groupBy { it.dateKey }

    val days = remember {
        List(graphDays) { dayOffset ->
            val date = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -dayOffset)
            }
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
            val dayCompletions = completionMap[dateKey] ?: emptyList()
            DayData(
                dateKey = dateKey,
                completionCount = dayCompletions.size,
                targetCount = habit.targetCount
            )
        }.reversed() // reverse it
    }

    val gridData = remember(days) {
        val columns = 26
        val rows = 7
        val grid = Array(rows) { row ->
            Array(columns) { col ->
                val dayIndex = row + col * rows
                if (dayIndex < days.size) {
                    days[dayIndex]
                } else {
                    DayData("", 0, habit.targetCount)
                }
            }
        }
        grid
    }

    Column(
        modifier = modifier
            .padding(horizontal = 5.dp)
    ) {
        // 26 columns × 7 rows
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
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        /*
        // Less is more component
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(habitColor.copy(alpha = 0.3f))
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(habitColor)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "More",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        */
    }
}

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
            .height(10.dp)
            .clip(RoundedCornerShape(2.dp))
            .border(1.dp, habitColor.copy(alpha = 0.02f), RoundedCornerShape(2.dp))
            .background(
                if (day.completionCount > 0) {
                    habitColor.copy(alpha = alpha)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            )
    )
}

data class DayData(
    val dateKey: String,
    val completionCount: Int,
    val targetCount: Int
)