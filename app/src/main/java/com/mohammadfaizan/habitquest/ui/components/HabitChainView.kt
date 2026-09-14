package com.mohammadfaizan.habitquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val chainDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
private val chainDayNumberFormat = SimpleDateFormat("d", Locale.getDefault())

data class ChainDay(
    val dateKey: String,
    val dayNumber: String,
    // "Covered" mirrors HabitCompletionRepositoryImpl.getCurrentStreak: any completion or a
    // freeze keeps the chain unbroken for that day, regardless of target count.
    val isCovered: Boolean,
    val isFrozen: Boolean,
    val isToday: Boolean
)

private const val CHAIN_NODE_SIZE_DP = 30
private const val CHAIN_CONNECTOR_WIDTH_DP = 14

@Composable
fun HabitChainVisualization(
    habit: Habit,
    completions: List<HabitCompletion>,
    freezeDates: List<String> = emptyList(),
    days: Int = 30,
    modifier: Modifier = Modifier
) {
    val habitColor = Color(habit.color.toColorInt())
    val scrollState = rememberScrollState()

    val chainDays = remember(habit.id, completions, freezeDates, days) {
        val completionSet = completions.map { it.dateKey }.toSet()
        val freezeSet = freezeDates.toSet()
        val calendar = Calendar.getInstance()
        val todayKey = chainDateFormat.format(Date())

        List(days) { dayOffset ->
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
            val dateKey = chainDateFormat.format(calendar.time)
            val isFrozen = dateKey in freezeSet
            ChainDay(
                dateKey = dateKey,
                dayNumber = chainDayNumberFormat.format(calendar.time),
                isCovered = dateKey in completionSet || isFrozen,
                isFrozen = isFrozen,
                isToday = dateKey == todayKey
            )
        }.reversed()
    }

    // Land on today's link rather than the oldest day in the window.
    LaunchedEffect(habit.id, chainDays.size) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.horizontalScroll(scrollState),
            verticalAlignment = Alignment.Top
        ) {
            chainDays.forEachIndexed { index, day ->
                ChainNode(day = day, habitColor = habitColor)
                if (index != chainDays.lastIndex) {
                    ChainConnector(
                        linked = day.isCovered && chainDays[index + 1].isCovered,
                        habitColor = habitColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ChainLegendItem(color = habitColor, label = "Completed")
            ChainLegendItem(color = FrozenDayColor, label = "Frozen")
            ChainLegendItem(
                color = MaterialTheme.colorScheme.surfaceVariant,
                label = "Missed"
            )
        }
    }
}

@Composable
private fun ChainNode(
    day: ChainDay,
    habitColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(CHAIN_NODE_SIZE_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(CHAIN_NODE_SIZE_DP.dp)
                .clip(CircleShape)
                .background(
                    when {
                        day.isFrozen -> FrozenDayColor.copy(alpha = if (day.isCovered) 0.85f else 0.55f)
                        day.isCovered -> habitColor
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                )
                .then(
                    if (day.isToday) {
                        Modifier.border(width = 2.dp, color = habitColor, shape = CircleShape)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (day.isCovered) {
                Text(
                    text = if (day.isFrozen) "❄" else "✓",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = day.dayNumber,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (day.isToday) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ChainConnector(
    linked: Boolean,
    habitColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(CHAIN_CONNECTOR_WIDTH_DP.dp)
            .height(CHAIN_NODE_SIZE_DP.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(CHAIN_CONNECTOR_WIDTH_DP.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (linked) {
                        habitColor.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    }
                )
        )
    }
}

@Composable
private fun ChainLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
