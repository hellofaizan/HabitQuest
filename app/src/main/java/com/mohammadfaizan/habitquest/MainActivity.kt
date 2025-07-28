package com.mohammadfaizan.habitquest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.data.repository.HabitCompletionRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitManagementRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitRepositoryImpl
import com.mohammadfaizan.habitquest.domain.repository.PreferencesRepositoryImpl
import com.mohammadfaizan.habitquest.domain.usecase.AddHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.CompleteHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.DeleteHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitsUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitsWithCompletionStatusUseCase
import com.mohammadfaizan.habitquest.ui.components.TopAppBarComponent
import com.mohammadfaizan.habitquest.ui.screens.AddHabitScreen
import com.mohammadfaizan.habitquest.ui.screens.HomeScreen
import com.mohammadfaizan.habitquest.ui.screens.onbording.OnboardingScreen
import com.mohammadfaizan.habitquest.ui.theme.HabitQuestTheme
import com.mohammadfaizan.habitquest.ui.viewmodel.AddHabitActionType
import com.mohammadfaizan.habitquest.ui.viewmodel.AddHabitViewModel
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitQuestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = this
                    val db = remember {
                        Room.databaseBuilder(
                            context,
                            AppDatabase::class.java,
                            "app-db"
                        ).build()
                    }

                    // Initialize repositories and use cases
                    val preferencesRepo =
                        remember { PreferencesRepositoryImpl(db.appPreferencesDao()) }
                    val habitRepo = remember { HabitRepositoryImpl(db.habitDao()) }
                    val habitCompletionRepo =
                        remember { HabitCompletionRepositoryImpl(db.habitCompletionDao()) }
                    val habitManagementRepo =
                        remember { HabitManagementRepositoryImpl(habitRepo, habitCompletionRepo) }
                    val addHabitUseCase = remember { AddHabitUseCase(habitRepo) }

                    val addHabitViewModel = remember { AddHabitViewModel(addHabitUseCase) }
                    val habitViewModel = remember {
                        HabitViewModel(
                            addHabitUseCase,
                            GetHabitsUseCase(habitRepo, habitManagementRepo),
                            CompleteHabitUseCase(habitManagementRepo),
                            DeleteHabitUseCase(habitManagementRepo),
                            GetHabitsWithCompletionStatusUseCase(habitManagementRepo),
                            habitCompletionRepo
                        )
                    }

                    var hasSeenOnboarding by remember { mutableStateOf<Boolean?>(null) }
                    var showAddHabitScreen by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(Unit) {
                        hasSeenOnboarding = preferencesRepo.hasSeenOnboarding()
                    }

                    when (hasSeenOnboarding) {
                        null -> {
                            // Loading state (optional)
                            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                                Text(
                                    text = "Loading...",
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        false -> {
                            OnboardingScreen(
                                onContinue = {
                                    scope.launch {
                                        preferencesRepo.setOnboardingSeen()
                                        hasSeenOnboarding = true
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        else -> {
                            if (showAddHabitScreen) {
                                AddHabitScreen(
                                    onBack = {
                                        showAddHabitScreen = false
                                        addHabitViewModel.resetForm()
                                    },
                                    onCreateHabit = { name, description, color, category, frequency, targetCount, reminderEnabled, reminderTime ->
                                        // Create the habit using ViewModel
                                        scope.launch {
                                            addHabitViewModel.createHabit()
                                        }
                                    },
                                    modifier = Modifier.padding(innerPadding),
                                    viewModel = addHabitViewModel
                                )

                                // Handle ViewModel actions
                                LaunchedEffect(addHabitViewModel.actions) {
                                    addHabitViewModel.actions.collect { action ->
                                        action?.let {
                                            when (it.type) {
                                                AddHabitActionType.HABIT_CREATED -> {
                                                    Toast.makeText(
                                                        context,
                                                        "Habit created successfully!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    showAddHabitScreen = false
                                                    addHabitViewModel.resetForm()
                                                }

                                                AddHabitActionType.VALIDATION_ERROR -> {
                                                    Toast.makeText(
                                                        context,
                                                        "Validation error: ${it.data}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }

                                                AddHabitActionType.NETWORK_ERROR -> {
                                                    Toast.makeText(
                                                        context,
                                                        "Error creating habit: ${it.data}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                            addHabitViewModel.clearActions()
                                        }
                                    }
                                }

                                LaunchedEffect(addHabitViewModel.formState) {
                                    addHabitViewModel.formState.collect { formState ->
                                        formState.error?.let { error ->
                                            Toast.makeText(
                                                context,
                                                error,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                Scaffold(
                                    topBar = {
                                        TopAppBarComponent(
                                            title = "Habit Quest",
                                            onMenuClick = {
                                                Toast.makeText(
                                                    context, "Menu Clicked",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onStatsClick = {
                                                Toast.makeText(
                                                    context, "STats Clicked",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onAddClick = {
                                                showAddHabitScreen = true
                                            }
                                        )
                                    },
                                    floatingActionButton = {
                                        FloatingActionButton(
                                            onClick = {
                                                showAddHabitScreen = true
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                contentDescription = "Add Habit"
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                ) { innerPadding ->
                                    HomeScreen(
                                        habitViewModel = habitViewModel,
                                        onAddHabitClick = {
                                            showAddHabitScreen = true
                                        },
                                        onHabitClick = { habit ->
                                            // Handle habit click - could open detail screen
                                            Toast.makeText(
                                                context,
                                                "Clicked on: ${habit.name}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
