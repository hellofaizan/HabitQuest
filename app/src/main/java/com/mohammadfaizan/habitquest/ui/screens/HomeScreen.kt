package com.mohammadfaizan.habitquest.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.domain.repository.HabitWithCompletionStatus
import com.mohammadfaizan.habitquest.ui.components.EmptyHabitState
import com.mohammadfaizan.habitquest.ui.components.HabitCard
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitActionType
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitViewModel

@Composable
fun HomeScreen(
    habitViewModel: HabitViewModel,
    onAddHabitClick: () -> Unit = {},
    onHabitClick: (Habit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by habitViewModel.uiState.collectAsState()
    val actions by habitViewModel.actions.collectAsState()
    val context = LocalContext.current

    // Handle ViewModel actions
    LaunchedEffect(actions) {
        actions?.let { action ->
            when (action.type) {
                HabitActionType.ADD_HABIT -> {
                    // Habit was added, refresh the list
                    habitViewModel.loadHabits()
                    habitViewModel.loadHabitsWithCompletionStatus()
                }

                HabitActionType.COMPLETE_HABIT -> {
                    // Habit was completed, refresh the list
                    habitViewModel.loadHabitsWithCompletionStatus()
                    Toast.makeText(context, "Habit completed!", Toast.LENGTH_SHORT).show()
                }

                HabitActionType.DELETE_HABIT -> {
                    // Habit was deleted, refresh the list
                    habitViewModel.loadHabits()
                    habitViewModel.loadHabitsWithCompletionStatus()
                }

                else -> {}
            }
            habitViewModel.clearActions()
        }
    }

    // Load habits on first launch
    LaunchedEffect(Unit) {
        habitViewModel.loadHabits()
        habitViewModel.loadHabitsWithCompletionStatus()
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.habits.isEmpty() -> {
                EmptyHabitState(
                    onAddHabit = onAddHabitClick,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                HabitListContent(
                    habits = uiState.habits,
                    habitsWithCompletionStatus = uiState.habitsWithCompletionStatus,
                    habitCompletions = uiState.habitCompletions,
                    onHabitClick = onHabitClick,
                    onCompleteClick = { habit ->
                        habitViewModel.completeHabit(habit.id)
                    },
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
    onHabitClick: (Habit) -> Unit,
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
                onHabitClick = { onHabitClick(habit) },
                onCompleteClick = { onCompleteClick(habit) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
