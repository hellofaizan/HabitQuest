package com.mohammadfaizan.habitquest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.graphics.toColorInt
import androidx.core.view.HapticFeedbackConstantsCompat
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitViewModel
import kotlin.math.roundToInt

@Composable
fun ReorderHabitsScreen(
    habitViewModel: HabitViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by habitViewModel.uiState.collectAsState()
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    var orderedHabits by remember { mutableStateOf(uiState.habits) }
    LaunchedEffect(uiState.habits) {
        // Only resync from upstream when the set of habits changed (add/delete) — an echo of
        // our own reorder write shouldn't clobber the in-progress local drag order.
        val currentIds = orderedHabits.map { it.id }.toSet()
        val newIds = uiState.habits.map { it.id }.toSet()
        if (currentIds != newIds) {
            orderedHabits = uiState.habits
        }
    }

    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    var itemHeightPx by remember { mutableStateOf(0f) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Reorder Habits",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Long-press and drag a habit to reorder it",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(orderedHabits, key = { _, habit -> habit.id }) { index, habit ->
                val isDragged = index == draggedIndex
                val habitColor = Color(habit.color.toColorInt())

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            if (itemHeightPx == 0f) {
                                itemHeightPx = coordinates.size.height.toFloat()
                            }
                        }
                        .graphicsLayer {
                            translationY = if (isDragged) dragOffsetY else 0f
                        }
                        .zIndex(if (isDragged) 1f else 0f)
                        .then(
                            if (isDragged) {
                                Modifier.shadow(4.dp, RoundedCornerShape(12.dp))
                            } else {
                                Modifier
                            }
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isDragged) {
                                MaterialTheme.colorScheme.surfaceVariant
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                        .pointerInput(habit.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    try {
                                        view.performHapticFeedback(HapticFeedbackConstantsCompat.LONG_PRESS)
                                    } catch (e: Exception) {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    draggedIndex = index
                                    dragOffsetY = 0f
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetY += dragAmount.y
                                    val currentIndex = draggedIndex
                                    if (currentIndex == null || itemHeightPx <= 0f) return@detectDragGesturesAfterLongPress
                                    val moveBy = (dragOffsetY / itemHeightPx).roundToInt()
                                    if (moveBy != 0) {
                                        val targetIndex = (currentIndex + moveBy)
                                            .coerceIn(0, orderedHabits.lastIndex)
                                        if (targetIndex != currentIndex) {
                                            orderedHabits = orderedHabits.toMutableList().apply {
                                                add(targetIndex, removeAt(currentIndex))
                                            }
                                            draggedIndex = targetIndex
                                            dragOffsetY -= moveBy * itemHeightPx
                                        }
                                    }
                                },
                                onDragEnd = {
                                    draggedIndex = null
                                    dragOffsetY = 0f
                                    habitViewModel.reorderHabits(orderedHabits.map { it.id })
                                },
                                onDragCancel = {
                                    draggedIndex = null
                                    dragOffsetY = 0f
                                }
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(habitColor)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    DragHandleGlyph(tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DragHandleGlyph(tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(tint)
            )
        }
    }
}
