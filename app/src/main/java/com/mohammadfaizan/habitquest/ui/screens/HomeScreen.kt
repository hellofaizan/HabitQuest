package com.mohammadfaizan.habitquest.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.domain.repository.HabitWithCompletionStatus
import com.mohammadfaizan.habitquest.ui.components.EmptyHabitState
import com.mohammadfaizan.habitquest.ui.components.HabitCard
import com.mohammadfaizan.habitquest.ui.components.WeeklyCalendarWithData
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitActionType
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitViewModel

@Composable
fun HomeScreen(
    habitViewModel: HabitViewModel,
    onAddHabitClick: () -> Unit = {},
    onHabitClick: (Habit) -> Unit = {},
    onHabitLongClick: (Habit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by habitViewModel.uiState.collectAsState()
    val actions by habitViewModel.actions.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(actions) {
        actions?.let { action ->
            when (action.type) {
                HabitActionType.COMPLETE_HABIT -> {
                    Toast.makeText(context, "Habit checked!", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
            habitViewModel.clearActions()
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.habits.isNotEmpty() -> {
                WeeklyCalendarWithData(
                    activeHabits = uiState.habits,
                    habitCompletions = uiState.weeklyCompletions,
                    onWeekChange = { weekOffset ->
                        // Load data for the selected week
                        habitViewModel.loadWeeklyCompletions(weekOffset)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                HabitListContent(
                    habits = uiState.habits,
                    habitsWithCompletionStatus = uiState.habitsWithCompletionStatus,
                    habitCompletions = uiState.habitCompletions,
                    onHabitLongClick = onHabitLongClick,
                    onCompleteClick = { habit ->
                        habitViewModel.completeHabit(habit.id)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            uiState.dataLoaded -> {
                EmptyHabitState(
                    onAddHabit = onAddHabitClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            !uiState.dataLoaded -> {
                EmptyHabitState(
                    onAddHabit = onAddHabitClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun HabitListContent(
    habits: List<Habit>,
    habitsWithCompletionStatus: List<HabitWithCompletionStatus>,
    habitCompletions: Map<Long, List<HabitCompletion>>,
    onHabitLongClick: (Habit) -> Unit,
    onCompleteClick: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    val completionsMap = remember(habitsWithCompletionStatus, habitCompletions) {
        habitCompletions
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
    ) {
        items(habits) { habit ->
            HabitCard(
                habit = habit,
                completions = completionsMap[habit.id] ?: emptyList(),
                onHabitLongClick = { onHabitLongClick(habit) },
                onCompleteClick = { onCompleteClick(habit) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
