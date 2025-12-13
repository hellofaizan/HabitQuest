package com.mohammadfaizan.habitquest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.repository.HabitCompletionRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitManagementRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitRepositoryImpl
import com.mohammadfaizan.habitquest.domain.repository.PreferencesRepositoryImpl
import com.mohammadfaizan.habitquest.domain.usecase.AddHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.CompleteHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.DeleteHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GenerateRandomDataUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitsUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitsWithCompletionStatusUseCase
import com.mohammadfaizan.habitquest.domain.usecase.UpdateHabitResult
import com.mohammadfaizan.habitquest.domain.usecase.UpdateHabitUseCase
import com.mohammadfaizan.habitquest.ui.components.TopAppBarComponent
import com.mohammadfaizan.habitquest.ui.screens.AddHabitScreen
import com.mohammadfaizan.habitquest.ui.screens.GeneralSettingsScreen
import com.mohammadfaizan.habitquest.ui.screens.HomeScreen
import com.mohammadfaizan.habitquest.ui.screens.SettingsScreen
import com.mohammadfaizan.habitquest.ui.screens.SplashScreen
import com.mohammadfaizan.habitquest.ui.screens.onbording.OnboardingScreen
import com.mohammadfaizan.habitquest.ui.theme.HabitQuestTheme
import com.mohammadfaizan.habitquest.ui.viewmodel.AddHabitActionType
import com.mohammadfaizan.habitquest.ui.viewmodel.AddHabitViewModel
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitViewModel
import kotlinx.coroutines.launch

sealed class AppState {
    object Splash : AppState()
    object Onboarding : AppState()
    object Main : AppState()
}

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

                    val preferencesRepo =
                        remember { PreferencesRepositoryImpl(db.appPreferencesDao()) }
                    val habitRepo = remember { HabitRepositoryImpl(db.habitDao()) }
                    val habitCompletionRepo =
                        remember { HabitCompletionRepositoryImpl(db.habitCompletionDao()) }
                    val habitManagementRepo =
                        remember { HabitManagementRepositoryImpl(habitRepo, habitCompletionRepo) }
                    val addHabitUseCase = remember { AddHabitUseCase(habitRepo) }
                    val updateHabitUseCase = remember { UpdateHabitUseCase(habitRepo) }

                    val addHabitViewModel = remember { AddHabitViewModel(addHabitUseCase) }
                    val generateRandomDataUseCase = remember { GenerateRandomDataUseCase(habitRepo, habitCompletionRepo) }
                    val habitViewModel = remember {
                        HabitViewModel(
                            addHabitUseCase,
                            GetHabitsUseCase(habitRepo, habitManagementRepo),
                            CompleteHabitUseCase(habitManagementRepo),
                            DeleteHabitUseCase(habitManagementRepo),
                            GetHabitsWithCompletionStatusUseCase(habitManagementRepo),
                            habitCompletionRepo,
                            habitRepo,
                            generateRandomDataUseCase
                        )
                    }

                    var appState by remember { mutableStateOf<AppState>(AppState.Splash) }
                    var showAddHabitScreen by remember { mutableStateOf(false) }
                    var showSettingsScreen by remember { mutableStateOf(false) }
                    var showGeneralSettingsScreen by remember { mutableStateOf(false) }
                    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
                    val scope = rememberCoroutineScope()

                    when (appState) {
                        AppState.Splash -> {
                            SplashScreen(
                                onSplashComplete = {
                                    scope.launch {
                                        // Transition after splash - data is already loading via Flow
                                        val hasSeenOnboarding = preferencesRepo.hasSeenOnboarding()
                                        appState =
                                            if (hasSeenOnboarding) AppState.Main else AppState.Onboarding
                                    }
                                },
                                habitViewModel = habitViewModel,
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppState.Onboarding -> {
                            OnboardingScreen(
                                onContinue = {
                                    scope.launch {
                                        preferencesRepo.setOnboardingSeen()
                                        appState = AppState.Main
                                    }
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }

                        AppState.Main -> {
                            // Settings Screen
                            if (showSettingsScreen && !showGeneralSettingsScreen) {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = tween(durationMillis = 200)
                                    ),
                                    exit = slideOutVertically(
                                        targetOffsetY = { it },
                                        animationSpec = tween(durationMillis = 200)
                                    )
                                ) {
                                    SettingsScreen(
                                        onBackClick = {
                                            showSettingsScreen = false
                                        },
                                        onNavigateToGeneral = {
                                            showGeneralSettingsScreen = true
                                        },
                                        habitViewModel = habitViewModel,
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }

                            if (showGeneralSettingsScreen) {
                                AnimatedVisibility(
                                    visible = true,
                                    enter = slideInVertically(
                                        initialOffsetY = { it },
                                        animationSpec = tween(durationMillis = 200)
                                    ),
                                    exit = slideOutVertically(
                                        targetOffsetY = { it },
                                        animationSpec = tween(durationMillis = 200)
                                    )
                                ) {
                                    GeneralSettingsScreen(
                                        onBackClick = {
                                            showGeneralSettingsScreen = false
                                        },
                                        modifier = Modifier.padding(innerPadding)
                                    )
                                }
                            }

                            // Add Habit Screen
                            AnimatedVisibility(
                                visible = showAddHabitScreen,
                                enter = slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(durationMillis = 100)
                                ),
                                exit = slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(durationMillis = 100)
                                )
                            ) {
                                AddHabitScreen(
                                    onBack = {
                                        showAddHabitScreen = false
                                        habitToEdit = null
                                        addHabitViewModel.resetForm()
                                    },
                                    onCreateHabit = { name, description, color, category, frequency, targetCount, reminderEnabled, reminderTime ->
                                        scope.launch {
                                            addHabitViewModel.createHabit()
                                        }
                                    },
                                    onUpdateHabit = { habitId, name, description, color, category, frequency, targetCount, reminderEnabled, reminderTime ->
                                        scope.launch {
                                            val result = updateHabitUseCase(
                                                habitId,
                                                name,
                                                description,
                                                color,
                                                category,
                                                frequency,
                                                targetCount,
                                                reminderEnabled,
                                                reminderTime
                                            )
                                            when (result) {
                                                is UpdateHabitResult.Success -> {
                                                    Toast.makeText(
                                                        context,
                                                        "Habit updated successfully!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    showAddHabitScreen = false
                                                    habitToEdit = null
                                                    addHabitViewModel.resetForm()
                                                    scope.launch {
                                                        kotlinx.coroutines.delay(100)
                                                        habitViewModel.refreshHabits()
                                                    }
                                                }

                                                is UpdateHabitResult.Error -> {
                                                    Toast.makeText(
                                                        context,
                                                        "Error updating habit: ${result.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                        }
                                    },
                                    onDeleteHabit = { habitId ->
                                        habitViewModel.deleteHabit(habitId)
                                        Toast.makeText(
                                            context,
                                            "Habit deleted successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showAddHabitScreen = false
                                        habitToEdit = null
                                        addHabitViewModel.resetForm()
                                    },
                                    modifier = Modifier.padding(innerPadding),
                                    viewModel = addHabitViewModel,
                                    habitToEdit = habitToEdit
                                )
                                                        }

                            // LaunchedEffect blocks moved outside AnimatedVisibility
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
                                                scope.launch {
                                                    kotlinx.coroutines.delay(100)
                                                    habitViewModel.refreshHabits()
                                                }
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

                            val openHabitEditor: (Habit) -> Unit = { habit ->
                                habitToEdit = habit
                                addHabitViewModel.updateName(habit.name)
                                addHabitViewModel.updateDescription(
                                    habit.description ?: ""
                                )
                                addHabitViewModel.updateColor(habit.color)
                                addHabitViewModel.updateCategory(habit.category ?: "")
                                addHabitViewModel.updateFrequency(habit.frequency.name)
                                addHabitViewModel.updateTargetCount(habit.targetCount)
                                addHabitViewModel.updateReminderEnabled(habit.reminderEnabled)
                                addHabitViewModel.updateReminderTime(
                                    habit.reminderTime ?: "09:00"
                                )
                                showAddHabitScreen = true
                            }

                            // Main content when screens are not shown
                            if (!showAddHabitScreen && !showSettingsScreen && !showGeneralSettingsScreen) {
                                Scaffold(
                                    topBar = {
                                        TopAppBarComponent(
                                            title = "Habit Quest",
                                            onMenuClick = {
                                                showSettingsScreen = true
                                            },
                                            onStatsClick = {
                                                Toast.makeText(
                                                    context, "Work in progress",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onAddClick = {
                                                showAddHabitScreen = true
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxSize()
                                ) { innerPadding ->
                                    HomeScreen(
                                        habitViewModel = habitViewModel,
                                        onAddHabitClick = {
                                            showAddHabitScreen = true
                                        },
                                            onHabitClick = openHabitEditor,
                                            onHabitLongClick = openHabitEditor,
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
