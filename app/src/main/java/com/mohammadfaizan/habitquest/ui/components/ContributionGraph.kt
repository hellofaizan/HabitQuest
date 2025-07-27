package com.mohammadfaizan.habitquest.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.chunked

@Composable
fun ContributionGraph(
    habit: Habit,
    completions: List<HabitCompletion>,
    modifier: Modifier = Modifier
) {
    val habitColor = Color(habit.color.toColorInt())
    val today = Calendar.getInstance()
    val graphDays = 70 // Show last 70 days (10 weeks)

    // Create a map of completion dates for quick lookup
    val completionMap = completions.associateBy { it.dateKey }

    // Generate the last 70 days
    val days = remember {
        List(graphDays) { dayOffset ->
            val date = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -dayOffset)
            }
            val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
            val completion = completionMap[dateKey]
            DayData(
                dateKey = dateKey,
                completionCount = completion?.let { 1 } ?: 0,
                targetCount = habit.targetCount
            )
        }.reversed() // Reverse to show oldest to newest
    }

    // Group days into weeks (7 days per row)
    val weeks = remember(days) {
        days.chunked(7)
    }

    Column(
        modifier = modifier
    ) {
        // Week labels (optional - can be hidden)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(10) { weekIndex ->
                Text(
                    text = if (weekIndex == 0) "10w" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Contribution grid
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            weeks.forEach { week ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    week.forEach { day ->
                        ContributionDay(
                            day = day,
                            habitColor = habitColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
