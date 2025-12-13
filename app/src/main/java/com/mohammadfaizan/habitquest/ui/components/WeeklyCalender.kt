package com.mohammadfaizan.habitquest.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohammadfaizan.habitquest.ui.theme.DarkBackground
import com.mohammadfaizan.habitquest.ui.theme.Success
import com.mohammadfaizan.habitquest.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
                    color = if (dayProgress.isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    elevation = 6.dp,
                    shape = CircleShape,
                    spotColor = Color.Transparent
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
    habitCompletions: Map<String, List<com.mohammadfaizan.habitquest.data.local.HabitCompletion>>,
    onWeekChange: ((Int) -> Unit)? = null  // Callback when week changes (offset: -1 = previous week)
) {
    var weekOffset by remember { mutableStateOf(0) } // 0 = current week, -1 = previous week, etc.
    var dragOffset by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val weekDays = remember(weekOffset) {
        generateWeekDays(weekOffset)
    }

    val dayProgressList = remember(activeHabits, habitCompletions, weekDays) {
        weekDays.map { date ->
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
                isToday = DateUtils.isToday(date) && weekOffset == 0
            )
        }
    }

    // Animate the calendar when week changes
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0.7f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "calendar_alpha"
    )

    Column(modifier = modifier) {
        // Show "Back to current week" button when viewing old data
        if (weekOffset < 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = {
                        weekOffset = 0
                        onWeekChange?.invoke(0)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "← Back to Current Week",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            // Check if drag was significant enough to change week
                            if (dragOffset > 150f) {
                                // Swiped right - go to previous week (older data)
                                val newOffset = weekOffset - 1
                                weekOffset = newOffset
                                onWeekChange?.invoke(newOffset)
                            } else if (dragOffset < -150f && weekOffset < 0) {
                                // Swiped left - go forward (toward current week)
                                val newOffset = (weekOffset + 1).coerceAtMost(0)
                                weekOffset = newOffset
                                onWeekChange?.invoke(newOffset)
                            }
                            dragOffset = 0f
                            isDragging = false
                        },
                        onHorizontalDrag = { change: PointerInputChange, dragAmount: Float ->
                            isDragging = true
                            dragOffset += dragAmount
                            change.consume()
                        }
                    )
                }
        ) {
            AnimatedContent(
                targetState = weekOffset,
                transitionSpec = {
                    if (targetState < initialState) {
                        // Going to previous week (right swipe) - new slides in from left, old slides out to right (left to right animation)
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(durationMillis = 300)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { it },
                            animationSpec = tween(durationMillis = 300)
                        )
                    } else {
                        // Going forward to current week (left swipe) - new slides in from right, old slides out to left (right to left animation)
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(durationMillis = 300)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(durationMillis = 300)
                        )
                    }
                },
                label = "week_transition"
            ) { _ ->
                WeeklyCalendar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(animatedAlpha),
                    dayProgressList = dayProgressList
                )
            }
        }
    }
}

private fun generateCurrentWeekDays(): List<String> {
    return generateWeekDays(0)
}

private fun generateWeekDays(weekOffset: Int): List<String> {
    val calendar = Calendar.getInstance()
    // Calculate days to subtract to get to Monday of current week
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val daysToSubtract = when (dayOfWeek) {
        Calendar.SUNDAY -> 6  // Go back 6 days to get Monday
        Calendar.MONDAY -> 0  // Already Monday
        else -> dayOfWeek - Calendar.MONDAY  // Subtract to get to Monday
    }
    calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
    
    // Add week offset (negative = go back in weeks)
    calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
    
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

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

