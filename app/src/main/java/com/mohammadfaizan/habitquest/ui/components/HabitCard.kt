package com.mohammadfaizan.habitquest.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.view.HapticFeedbackConstantsCompat
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitCard(
    habit: Habit,
    completions: List<HabitCompletion> = emptyList(),
    onHabitLongClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current
    val habitColor = Color(habit.color.toColorInt())
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    val isCompletedToday = completions.any { completion ->
        completion.dateKey == today
    }

    val todayCompletionCount = completions.count { completion ->
        completion.dateKey == today
    }

    val isFullyCompleted = todayCompletionCount >= habit.targetCount

    val canCompleteMore = todayCompletionCount < habit.targetCount

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(clip = true)
            .combinedClickable(
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                    } catch (e: Exception) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                onLongClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                    } catch (e: Exception) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    onHabitLongClick()
                }
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(habitColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (habit.description?.isNotBlank() == true) {
                        Text(
                            text = habit.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (habit.targetCount > 1) {
                        Text(
                            text = "$todayCompletionCount/${habit.targetCount} completed today",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isFullyCompleted)
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Display streaks - positioned to the left of the completion button
                if (habit.currentStreak > 0 || habit.longestStreak > 0) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (habit.currentStreak > 0) {
                            Text(
                                text = "🔥 ${habit.currentStreak}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (habit.longestStreak > habit.currentStreak) {
                                Text(
                                    text = "Best: ${habit.longestStreak}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.85f
                                )
                            }
                        } else if (habit.longestStreak > 0) {
                            Text(
                                text = "Best: ${habit.longestStreak}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isFullyCompleted -> habitColor.copy(alpha = 0.9f)
                                isCompletedToday -> habitColor.copy(alpha = 0.7f)
                                else -> habitColor.copy(alpha = 0.1f)
                            }
                        )
                        .clickable {
                            if (canCompleteMore) {
                                try {
                                    view.performHapticFeedback(HapticFeedbackConstantsCompat.KEYBOARD_PRESS)
                                } catch (e: Exception) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onCompleteClick()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFullyCompleted) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = if (isFullyCompleted) "Completed" else "Mark as complete",
                        tint = if (isFullyCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            ContributionGraph(
                habit = habit,
                completions = completions,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

        }
    }
}