package com.mohammadfaizan.habitquest.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.ui.components.HabitCard
import com.mohammadfaizan.habitquest.ui.components.HabitList
import com.mohammadfaizan.habitquest.ui.components.EmptyHabitState
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitViewModel
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitActionType
import com.mohammadfaizan.habitquest.domain.repository.HabitWithCompletionStatus
import androidx.compose.runtime.collectAsState

@Composable
fun HomeScreen(
    habitViewModel: HabitViewModel,
    onAddHabitClick: () -> Unit = {},
    onHabitClick: (Habit) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by habitViewModel.uiState.collectAsState()
    val actions by habitViewModel.actions.collectAsState()
    
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
        // Content
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
    onHabitClick: (Habit) -> Unit,
    onCompleteClick: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    // Convert habitsWithCompletionStatus to a map for easy lookup
    val completionsMap = remember(habitsWithCompletionStatus) {
        habitsWithCompletionStatus.associate { habitWithStatus ->
            habitWithStatus.habit.id to emptyList<HabitCompletion>() // We'll need to fetch actual completions
        }
    }
    
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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

@Composable
fun HomeScreenPreview() {
    // Preview for development
    val mockHabits = listOf(
        Habit(
            id = 1,
            name = "Morning Exercise",
            description = "30 minutes of cardio",
            color = "#FF0000",
            category = "Health",
            targetCount = 1
        ),
        Habit(
            id = 2,
            name = "Read Books",
            description = "Read 20 pages daily",
            color = "#00FF00",
            category = "Learning",
            targetCount = 3
        )
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(mockHabits) { habit ->
            HabitCard(
                habit = habit,
                completions = emptyList(),
                onHabitClick = {},
                onCompleteClick = {}
            )
        }
    }
}
