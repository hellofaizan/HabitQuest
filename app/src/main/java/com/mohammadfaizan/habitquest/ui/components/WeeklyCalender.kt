package com.mohammadfaizan.habitquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohammadfaizan.habitquest.ui.theme.DarkBackground
import com.mohammadfaizan.habitquest.ui.theme.Success
import com.mohammadfaizan.habitquest.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

data class DayProgress(
    val date: String,
    val dayOfWeek: String,
    val dayNumber: Int,
    val completedHabits: Int,
    val totalHabits: Int,
    val isToday: Boolean = false
)

@Composable
fun WeeklyCalendar(
    modifier: Modifier = Modifier,
    dayProgressList: List<DayProgress>
) {
    val isDarkTheme = MaterialTheme.colorScheme.surface == DarkBackground
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "This Week",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            dayProgressList.forEach { dayProgress ->
                DayProgressCircle(
                    dayProgress = dayProgress,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )
                if (dayProgress != dayProgressList.last()) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dayProgressList.forEach { dayProgress ->
                Text(
                    text = dayProgress.dayOfWeek,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DayProgressCircle(
    dayProgress: DayProgress,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = if (dayProgress.totalHabits > 0) {
        dayProgress.completedHabits.toFloat() / dayProgress.totalHabits.toFloat()
    } else {
        0f
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )
    
    val progressColor = when {
        progress >= 1f -> Success // Full completion - green
        progress > 0f -> Color(0xFF172DF6) // Partial completion - blue
        else -> MaterialTheme.colorScheme.secondary // No completion
    }
    
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val todayBorderColor = when {
        progress >= 1f -> Success
        else -> Color(0xFF172DF6)
    }
    
    Box(
        modifier = modifier
            .size(50.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .size(68.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = CircleShape,
                    spotColor = if (dayProgress.isToday) todayBorderColor else Color.Transparent
                ),
            color = progressColor,
            trackColor = trackColor,
            strokeWidth = 5.dp
        )
        
        Text(
            text = dayProgress.dayNumber.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (dayProgress.isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (dayProgress.isToday) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
        
        if (dayProgress.totalHabits > 0) {
            val completionStatus = when {
                dayProgress.completedHabits >= dayProgress.totalHabits -> "✓"
                dayProgress.completedHabits > 0 -> ""
                else -> ""
            }
            
            if (completionStatus.isNotEmpty()) {
                Text(
                    text = completionStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        dayProgress.completedHabits >= dayProgress.totalHabits -> Success
                        dayProgress.completedHabits > 0 -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                )
            }
        }

        /*// Today indicator border
        if (dayProgress.isToday) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(
                        width = 2.dp,
                        color = todayBorderColor,
                        shape = CircleShape
                    )
            )
        }*/
    }
}

@Composable
fun WeeklyCalendarWithData(
    modifier: Modifier = Modifier,
    activeHabits: List<com.mohammadfaizan.habitquest.data.local.Habit>,
    habitCompletions: Map<String, List<com.mohammadfaizan.habitquest.data.local.HabitCompletion>>
) {
    val currentWeekDays = remember {
        generateCurrentWeekDays()
    }
    
    val dayProgressList = remember(activeHabits, habitCompletions) {
        currentWeekDays.map { date ->
            val completionsForDay = habitCompletions[date] ?: emptyList()
            val totalHabitsForDay = activeHabits.sumOf { habit ->
                // Count each habit based on its frequency/target count
                habit.targetCount
            }
            val completedHabitsForDay = completionsForDay.size
            
            DayProgress(
                date = date,
                dayOfWeek = getDayOfWeekAbbreviation(date),
                dayNumber = getDayNumber(date),
                completedHabits = completedHabitsForDay,
                totalHabits = totalHabitsForDay,
                isToday = DateUtils.isToday(date)
            )
        }
    }
    
    WeeklyCalendar(
        modifier = modifier,
        dayProgressList = dayProgressList
    )
}

private fun generateCurrentWeekDays(): List<String> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    
    val weekDays = mutableListOf<String>()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    for (i in 0..6) {
        val date = calendar.time
        weekDays.add(dateFormat.format(date))
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    
    return weekDays
}

private fun getDayOfWeekAbbreviation(dateKey: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    
    return try {
        val date = dateFormat.parse(dateKey)
        dayFormat.format(date!!)
    } catch (e: Exception) {
        ""
    }
}

private fun getDayNumber(dateKey: String): Int {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    return try {
        val date = dateFormat.parse(dateKey)
        val calendar = Calendar.getInstance()
        calendar.time = date!!
        calendar.get(Calendar.DAY_OF_MONTH)
    } catch (e: Exception) {
        0
    }
}

