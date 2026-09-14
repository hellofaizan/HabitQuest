package com.mohammadfaizan.habitquest.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.core.view.HapticFeedbackConstantsCompat
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitCard(
    habit: Habit,
    completions: List<HabitCompletion> = emptyList(),
    freezeDates: List<String> = emptyList(),
    onHabitClick: () -> Unit = {},
    onHabitLongClick: () -> Unit = {},
    onCompleteClick: () -> Unit = {},
    onUndoClick: () -> Unit = {},
    onNoteSave: (String) -> Unit = {},
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

    val todayNote = completions
        .filter { it.dateKey == today }
        .maxByOrNull { it.completedAt }
        ?.notes

    var confettiTrigger by remember { mutableStateOf(0) }
    var confettiVisible by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

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
                    onHabitClick()
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
                    Text(
                        text = habit.icon ?: "❤️",
                        fontSize = 16.sp
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

                Box(contentAlignment = Alignment.Center) {
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
                                    if (todayCompletionCount + 1 >= habit.targetCount) {
                                        confettiTrigger++
                                        confettiVisible = true
                                    }
                                    onCompleteClick()
                                } else {
                                    // Already at today's target — tap again to undo the last completion.
                                    try {
                                        view.performHapticFeedback(HapticFeedbackConstantsCompat.REJECT)
                                    } catch (e: Exception) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    onUndoClick()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFullyCompleted) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = if (isFullyCompleted) "Completed, tap to undo" else "Mark as complete",
                            tint = if (isFullyCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (confettiVisible) {
                        key(confettiTrigger) {
                            // Reports (0, 0) to its parent no matter how big the burst draws,
                            // so it floats over the card instead of stretching the Row/Card
                            // to fit a 72dp burst around a 40dp button.
                            FloatingOverlay {
                                ConfettiBurst(
                                    modifier = Modifier.size(72.dp),
                                    onFinished = { confettiVisible = false }
                                )
                            }
                        }
                    }
                }
            }

            if (isCompletedToday) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (todayNote.isNullOrBlank()) "📝 Add a note" else "📝 $todayNote",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clickable { showNoteDialog = true }
                        .padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ContributionGraph(
                habit = habit,
                completions = completions,
                freezeDates = freezeDates,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

        }
    }

    if (showNoteDialog) {
        NoteEditDialog(
            initialNote = todayNote ?: "",
            onDismiss = { showNoteDialog = false },
            onSave = { note ->
                onNoteSave(note)
                showNoteDialog = false
            }
        )
    }
}

@Composable
private fun NoteEditDialog(
    initialNote: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var noteText by remember { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Note for today") },
        text = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { Text("How did it go?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(noteText) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Measures its content unconstrained but always reports zero size to its own
// parent, centering the content on top of that zero-size point instead. Lets an
// overlay (like the confetti burst) draw larger than its anchor without ever
// resizing the layout it sits inside.
@Composable
private fun FloatingOverlay(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(content = content, modifier = modifier) { measurables, _ ->
        val placeable = measurables.firstOrNull()?.measure(Constraints())
        layout(0, 0) {
            placeable?.placeRelative(
                x = -(placeable.width / 2),
                y = -(placeable.height / 2)
            )
        }
    }
}

private data class ConfettiParticle(
    val angle: Float,
    val distance: Float,
    val size: Float,
    val color: Color
)

private val ConfettiColors = listOf(
    Color(0xFF2A78D6), // Blue
    Color(0xFFEB6834), // Orange
    Color(0xFF1BAF7A), // Aqua
    Color(0xFFEDA100), // Yellow
    Color(0xFFE87BA4), // Magenta
    Color(0xFF4A3AA7)  // Violet
)

// One-shot burst of dots that pops out from center and fades, fired on completing a habit's daily target.
@Composable
private fun ConfettiBurst(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    val particles = remember {
        List(10) {
            ConfettiParticle(
                angle = Random.nextFloat() * (2f * Math.PI.toFloat()),
                distance = 16f + Random.nextFloat() * 14f,
                size = 3f + Random.nextFloat() * 3f,
                color = ConfettiColors[Random.nextInt(ConfettiColors.size)]
            )
        }
    }

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(durationMillis = 500, easing = LinearOutSlowInEasing))
        onFinished()
    }

    Canvas(modifier = modifier) {
        val p = progress.value
        val alpha = (1f - p).coerceIn(0f, 1f)
        val center = Offset(size.width / 2f, size.height / 2f)
        particles.forEach { particle ->
            val traveled = particle.distance.dp.toPx() * p
            val x = center.x + cos(particle.angle) * traveled
            val y = center.y + sin(particle.angle) * traveled - (6.dp.toPx() * p)
            drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = (particle.size.dp.toPx() / 2f) * (1f - p * 0.3f),
                center = Offset(x, y)
            )
        }
    }
}