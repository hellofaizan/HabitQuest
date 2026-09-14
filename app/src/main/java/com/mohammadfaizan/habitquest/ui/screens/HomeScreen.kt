package com.mohammadfaizan.habitquest.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.ui.components.CategoryChips
import com.mohammadfaizan.habitquest.ui.components.EmptyHabitState
import com.mohammadfaizan.habitquest.ui.components.HabitCard
import com.mohammadfaizan.habitquest.ui.components.WeeklyCalendarWithData
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitActionType
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitCompletionInfo
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitViewModel
import com.mohammadfaizan.habitquest.utils.Achievements
import com.mohammadfaizan.habitquest.utils.DateUtils
import com.mohammadfaizan.habitquest.widget.HabitWidgetUpdater

@Composable
fun HomeScreen(
    habitViewModel: HabitViewModel,
    showSearchBar: Boolean = false,
    onAddHabitClick: () -> Unit = {},
    onHabitClick: (Habit) -> Unit = {},
    onHabitLongClick: (Habit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by habitViewModel.uiState.collectAsState()
    val visibleHabits by habitViewModel.visibleHabits.collectAsState()
    val searchQuery by habitViewModel.searchQuery.collectAsState()
    val selectedCategory by habitViewModel.selectedCategory.collectAsState()
    val actions by habitViewModel.actions.collectAsState()
    val context = LocalContext.current

    // 0 = current week, -1 = previous week, etc. Hoisted here (rather than owned inside
    // WeeklyCalendarWithData) so the "back to today" button can float above the FAB instead
    // of living inline above the calendar.
    var weekOffset by remember { mutableStateOf(0) }

    val todayKey = DateUtils.getCurrentDateKey()
    val anyIncompleteToday = uiState.habits.any { habit ->
        val completedToday = uiState.habitCompletions[habit.id]
            ?.count { it.dateKey == todayKey } ?: 0
        completedToday < habit.targetCount
    }

    LaunchedEffect(actions) {
        actions?.let { action ->
            when (action.type) {
                HabitActionType.COMPLETE_HABIT -> {
                    val info = action.data as? HabitCompletionInfo
                    val message = when {
                        info?.milestoneReached != null -> Achievements.milestoneMessage(info.milestoneReached)
                        else -> Achievements.randomMotivationalMessage()
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                HabitActionType.UNCOMPLETE_HABIT -> {
                    Toast.makeText(context, "Completion undone", Toast.LENGTH_SHORT).show()
                }
                HabitActionType.COMPLETE_ALL_HABITS -> {
                    val count = action.data as? Int ?: 0
                    val message = if (count > 0) {
                        "Completed $count habit${if (count == 1) "" else "s"}!"
                    } else {
                        "Everything's already done for today"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                HabitActionType.FREEZE_STREAK -> {
                    val result = action.data as? com.mohammadfaizan.habitquest.ui.viewmodel.FreezeStreakActionResult
                    val message = if (result?.success == true) {
                        "🧊 Streak frozen for today & tomorrow"
                    } else {
                        result?.error ?: "Couldn't freeze streak"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
            when (action.type) {
                HabitActionType.COMPLETE_HABIT,
                HabitActionType.UNCOMPLETE_HABIT,
                HabitActionType.COMPLETE_ALL_HABITS,
                HabitActionType.FREEZE_STREAK -> HabitWidgetUpdater.updateAll(context)
                else -> {}
            }
            habitViewModel.clearActions()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // The outer Scaffold (MainActivity's TopAppBar) already accounts for the status bar;
        // without this, this nested Scaffold applies its own systemBars inset on top of that
        // and leaves a double gap between the app bar and the weekly calendar.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (weekOffset < 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                weekOffset = 0
                                habitViewModel.loadWeeklyCompletions(0)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "Back to Today",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (uiState.habits.isNotEmpty() && anyIncompleteToday) {
                    ExtendedFloatingActionButton(
                        onClick = { habitViewModel.completeAllForToday() },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        text = { Text("Complete All") }
                    )
                }
            }
        }
    ) { fabPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(fabPadding)
        ) {
            when {
                uiState.habits.isNotEmpty() -> {
                    WeeklyCalendarWithData(
                        activeHabits = uiState.habits,
                        habitCompletions = uiState.weeklyCompletions,
                        weekOffset = weekOffset,
                        onWeekChange = { newOffset ->
                            weekOffset = newOffset
                            habitViewModel.loadWeeklyCompletions(newOffset)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    HabitSearchAndFilter(
                        showSearchBar = showSearchBar,
                        query = searchQuery,
                        onQueryChange = habitViewModel::updateSearchQuery,
                        categories = uiState.habits.mapNotNull { it.category }.distinct().sorted(),
                        selectedCategory = selectedCategory,
                        onCategorySelected = habitViewModel::updateCategoryFilter,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (visibleHabits.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "No habits match your search",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        HabitListContent(
                            habits = visibleHabits,
                            habitCompletions = uiState.habitCompletions,
                            habitFreezeDates = uiState.habitFreezeDates,
                            onHabitClick = onHabitClick,
                            onHabitLongClick = onHabitLongClick,
                            onCompleteClick = { habit ->
                                habitViewModel.completeHabit(habit.id)
                            },
                            onUndoClick = { habit ->
                                habitViewModel.uncompleteHabit(habit.id)
                            },
                            onNoteSave = { habit, note ->
                                habitViewModel.updateTodayNote(habit.id, note)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                !uiState.dataLoaded -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    EmptyHabitState(
                        onAddHabit = onAddHabitClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitSearchAndFilter(
    showSearchBar: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        if (showSearchBar) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search habits") },
                singleLine = true,
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        Text(
                            text = "✕",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable { onQueryChange("") }
                        )
                    }
                }
            )

            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (categories.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    CategoryChips(
                        category = category,
                        isSelected = category == selectedCategory,
                        onCategorySelected = { onCategorySelected(category) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitListContent(
    habits: List<Habit>,
    habitCompletions: Map<Long, List<HabitCompletion>>,
    habitFreezeDates: Map<Long, List<String>> = emptyMap(),
    onHabitClick: (Habit) -> Unit,
    onHabitLongClick: (Habit) -> Unit,
    onCompleteClick: (Habit) -> Unit,
    onUndoClick: (Habit) -> Unit,
    onNoteSave: (Habit, String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        items(habits, key = { it.id }) { habit ->
            HabitCard(
                habit = habit,
                completions = habitCompletions[habit.id] ?: emptyList(),
                freezeDates = habitFreezeDates[habit.id] ?: emptyList(),
                onHabitClick = { onHabitClick(habit) },
                onHabitLongClick = { onHabitLongClick(habit) },
                onCompleteClick = { onCompleteClick(habit) },
                onUndoClick = { onUndoClick(habit) },
                onNoteSave = { note -> onNoteSave(habit, note) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
