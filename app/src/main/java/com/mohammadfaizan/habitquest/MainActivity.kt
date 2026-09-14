package com.mohammadfaizan.habitquest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mohammadfaizan.habitquest.data.local.AppDatabase
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.repository.BackupRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitCompletionRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitManagementRepositoryImpl
import com.mohammadfaizan.habitquest.data.repository.HabitRepositoryImpl
import com.mohammadfaizan.habitquest.domain.repository.PreferencesRepositoryImpl
import com.mohammadfaizan.habitquest.domain.usecase.AddHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.ArchiveHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.CompleteHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.DeleteHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.ExportBackupUseCase
import com.mohammadfaizan.habitquest.domain.usecase.FreezeStreakUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GenerateRandomDataUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GetAnalyticsUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitsUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitStatsUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitsWithCompletionStatusUseCase
import com.mohammadfaizan.habitquest.domain.usecase.ImportBackupUseCase
import com.mohammadfaizan.habitquest.domain.usecase.ReorderHabitsUseCase
import com.mohammadfaizan.habitquest.domain.usecase.UncompleteHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.UpdateHabitResult
import com.mohammadfaizan.habitquest.domain.usecase.UpdateHabitUseCase
import com.mohammadfaizan.habitquest.ui.components.TopAppBarComponent
import com.mohammadfaizan.habitquest.ui.screens.AddHabitScreen
import com.mohammadfaizan.habitquest.ui.screens.BackupRestoreScreen
import com.mohammadfaizan.habitquest.ui.screens.GeneralSettingsScreen
import com.mohammadfaizan.habitquest.ui.screens.AnalyticsScreen
import com.mohammadfaizan.habitquest.ui.screens.ArchivedHabitsScreen
import com.mohammadfaizan.habitquest.ui.screens.HabitDetailScreen
import com.mohammadfaizan.habitquest.ui.screens.HomeScreen
import com.mohammadfaizan.habitquest.ui.screens.ProUpgradeSheet
import com.mohammadfaizan.habitquest.ui.screens.ReorderHabitsScreen
import com.mohammadfaizan.habitquest.ui.screens.SettingsDrawerContent
import com.mohammadfaizan.habitquest.ui.screens.SplashScreen
import com.mohammadfaizan.habitquest.ui.screens.onbording.OnboardingScreen
import com.mohammadfaizan.habitquest.ui.theme.HabitQuestTheme
import com.mohammadfaizan.habitquest.ui.viewmodel.AddHabitActionType
import com.mohammadfaizan.habitquest.ui.viewmodel.AddHabitViewModel
import com.mohammadfaizan.habitquest.ui.viewmodel.AVAILABLE_HABIT_ICONS
import com.mohammadfaizan.habitquest.ui.viewmodel.AnalyticsViewModel
import com.mohammadfaizan.habitquest.ui.viewmodel.BackupViewModel
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitDetailViewModel
import com.mohammadfaizan.habitquest.ui.viewmodel.HabitViewModel
import com.mohammadfaizan.habitquest.utils.DateUtils
import com.mohammadfaizan.habitquest.utils.HabitNotificationManager
import com.mohammadfaizan.habitquest.utils.NotificationScheduler
import com.mohammadfaizan.habitquest.utils.PermissionUtils
import com.mohammadfaizan.habitquest.utils.StreakResetManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class AppState {
    object Splash : AppState()
    object Onboarding : AppState()
    object Main : AppState()
}

private object Routes {
    const val HOME = "home"
    const val ADD_HABIT = "add_habit"
    const val GENERAL_SETTINGS = "general_settings"
    const val REORDER_HABITS = "reorder_habits"
    const val HABIT_DETAIL = "habit_detail"
    const val ANALYTICS = "analytics"
    const val ARCHIVED_HABITS = "archived_habits"
    const val BACKUP_RESTORE = "backup_restore"
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HabitQuestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = this
                    val db = remember { AppDatabase.getInstance(context) }

                    val preferencesRepo =
                        remember { PreferencesRepositoryImpl(db.appPreferencesDao()) }
                    val habitRepo = remember { HabitRepositoryImpl(db.habitDao()) }
                    val habitCompletionRepo =
                        remember { HabitCompletionRepositoryImpl(db.habitCompletionDao(), db.habitFreezeDao()) }
                    val habitManagementRepo =
                        remember { HabitManagementRepositoryImpl(habitRepo, habitCompletionRepo, db) }
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
                            generateRandomDataUseCase,
                            habitManagementRepo,
                            ReorderHabitsUseCase(habitRepo),
                            UncompleteHabitUseCase(habitManagementRepo),
                            ArchiveHabitUseCase(habitRepo),
                            FreezeStreakUseCase(habitManagementRepo)
                        )
                    }
                    val habitDetailViewModel = remember {
                        HabitDetailViewModel(GetHabitStatsUseCase(habitManagementRepo))
                    }
                    val analyticsViewModel = remember {
                        AnalyticsViewModel(
                            GetAnalyticsUseCase(habitRepo, habitManagementRepo, habitCompletionRepo),
                            GetHabitStatsUseCase(habitManagementRepo)
                        )
                    }
                    val backupRepo = remember {
                        BackupRepositoryImpl(
                            db,
                            db.habitDao(),
                            db.habitCompletionDao(),
                            db.habitFreezeDao(),
                            db.appPreferencesDao()
                        )
                    }
                    val backupViewModel = remember {
                        BackupViewModel(
                            ExportBackupUseCase(backupRepo),
                            ImportBackupUseCase(backupRepo, habitManagementRepo)
                        )
                    }

                    // Initialize notification channel
                    LaunchedEffect(Unit) {
                        HabitNotificationManager.createNotificationChannel(context)
                    }

                    // Request POST_NOTIFICATIONS at runtime (required on Android 13+;
                    // reminders would otherwise silently never show).
                    val notificationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = {}
                    )
                    LaunchedEffect(Unit) {
                        if (PermissionUtils.isNotificationPermissionRequired() &&
                            !PermissionUtils.hasNotificationPermission(context)
                        ) {
                            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    // Schedule midnight streak reset and reschedule all habit reminders
                    LaunchedEffect(Unit) {
                        StreakResetManager.scheduleMidnightReset(context)
                        // Reschedule all habit reminders on app startup
                        val allHabits = habitRepo.getAllHabits().first()
                        NotificationScheduler.rescheduleAllReminders(context, allHabits)
                    }

                    var appState by remember { mutableStateOf<AppState>(AppState.Splash) }
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
                            val navController = rememberNavController()
                            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                            var habitToEdit by remember { mutableStateOf<Habit?>(null) }
                            var selectedHabitForDetail by remember { mutableStateOf<Habit?>(null) }
                            val currentRoute by navController.currentBackStackEntryAsState()

                            // ModalNavigationDrawer computes its closed offset from the drawer
                            // sheet's measured width, which isn't known on the very first frame —
                            // so it can render open for a frame before snapping shut. Force a
                            // no-animation snap once layout settles so that flash never shows.
                            LaunchedEffect(Unit) {
                                drawerState.snapTo(DrawerValue.Closed)
                            }

                            BackHandler(enabled = drawerState.isOpen) {
                                scope.launch { drawerState.close() }
                            }

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
                                                navController.popBackStack(Routes.HOME, inclusive = false)
                                                scope.launch {
                                                    delay(100)
                                                    habitViewModel.refreshHabits()
                                                    // Schedule notification for the newly created habit
                                                    val habitId = it.data as? Long
                                                    if (habitId != null) {
                                                        val habit = habitRepo.getHabitById(habitId)
                                                        if (habit != null && habit.reminderEnabled) {
                                                            NotificationScheduler.scheduleHabitReminder(context, habit)
                                                        }
                                                    }
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
                                addHabitViewModel.updateIcon(habit.icon ?: AVAILABLE_HABIT_ICONS.first())
                                addHabitViewModel.updateCategory(habit.category ?: "")
                                addHabitViewModel.updateFrequency(habit.frequency.name)
                                addHabitViewModel.updateTargetCount(habit.targetCount)
                                addHabitViewModel.updateReminderEnabled(habit.reminderEnabled)
                                addHabitViewModel.updateReminderTime(
                                    habit.reminderTime ?: "09:00"
                                )
                                addHabitViewModel.updateReminderDays(
                                    DateUtils.parseReminderDays(habit.reminderDays)
                                )
                                navController.navigate(Routes.ADD_HABIT)
                            }

                            val openHabitDetail: (Habit) -> Unit = { habit ->
                                selectedHabitForDetail = habit
                                habitDetailViewModel.loadStats(habit.id)
                                navController.navigate(Routes.HABIT_DETAIL)
                            }

                            ModalNavigationDrawer(
                                drawerState = drawerState,
                                gesturesEnabled = currentRoute?.destination?.route == Routes.HOME,
                                drawerContent = {
                                    ModalDrawerSheet {
                                        SettingsDrawerContent(
                                            onNavigateToGeneral = {
                                                scope.launch { drawerState.close() }
                                                navController.navigate(Routes.GENERAL_SETTINGS)
                                            },
                                            onNavigateToReorder = {
                                                scope.launch { drawerState.close() }
                                                navController.navigate(Routes.REORDER_HABITS)
                                            },
                                            onNavigateToAnalytics = {
                                                scope.launch { drawerState.close() }
                                                navController.navigate(Routes.ANALYTICS)
                                            },
                                            onNavigateToArchived = {
                                                scope.launch { drawerState.close() }
                                                navController.navigate(Routes.ARCHIVED_HABITS)
                                            },
                                            onNavigateToBackupRestore = {
                                                scope.launch { drawerState.close() }
                                                navController.navigate(Routes.BACKUP_RESTORE)
                                            },
                                            habitViewModel = habitViewModel
                                        )
                                    }
                                }
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = Routes.HOME,
                                    enterTransition = {
                                        slideInHorizontally(initialOffsetX = { it }) + fadeIn()
                                    },
                                    exitTransition = {
                                        slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut()
                                    },
                                    popEnterTransition = {
                                        slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()
                                    },
                                    popExitTransition = {
                                        slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                                    }
                                ) {
                                    composable(
                                        Routes.HOME,
                                        // Add Habit is a modal "create" sheet, not a drill-down
                                        // destination, so Home just fades behind it (and fades
                                        // back in on dismiss) instead of sliding sideways.
                                        exitTransition = {
                                            if (targetState.destination.route == Routes.ADD_HABIT) {
                                                fadeOut(animationSpec = tween(150))
                                            } else {
                                                slideOutHorizontally(targetOffsetX = { -it / 4 }) + fadeOut()
                                            }
                                        },
                                        popEnterTransition = {
                                            if (initialState.destination.route == Routes.ADD_HABIT) {
                                                fadeIn(animationSpec = tween(150))
                                            } else {
                                                slideInHorizontally(initialOffsetX = { -it / 4 }) + fadeIn()
                                            }
                                        }
                                    ) {
                                        var showProSheet by remember { mutableStateOf(false) }
                                        var showSearchBar by remember { mutableStateOf(false) }

                                        Scaffold(
                                            topBar = {
                                                TopAppBarComponent(
                                                    title = "Habit Quest",
                                                    onMenuClick = {
                                                        scope.launch { drawerState.open() }
                                                    },
                                                    onStatsClick = {
                                                        Toast.makeText(
                                                            context, "Work in progress",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    onAddClick = {
                                                        habitToEdit = null
                                                        addHabitViewModel.resetForm()
                                                        navController.navigate(Routes.ADD_HABIT)
                                                    },
                                                    onCrownClick = {
                                                        showProSheet = true
                                                    },
                                                    onSearchClick = {
                                                        showSearchBar = !showSearchBar
                                                    },
                                                    isSearchActive = showSearchBar
                                                )
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        ) { homeInnerPadding ->
                                            HomeScreen(
                                                habitViewModel = habitViewModel,
                                                showSearchBar = showSearchBar,
                                                onAddHabitClick = {
                                                    habitToEdit = null
                                                    addHabitViewModel.resetForm()
                                                    navController.navigate(Routes.ADD_HABIT)
                                                },
                                                onHabitClick = openHabitDetail,
                                                onHabitLongClick = openHabitEditor,
                                                modifier = Modifier.padding(homeInnerPadding)
                                            )
                                        }

                                        if (showProSheet) {
                                            ProUpgradeSheet(onDismiss = { showProSheet = false })
                                        }
                                    }

                                    composable(
                                        Routes.ADD_HABIT,
                                        // Modal "create" sheet: rises up from the bottom instead
                                        // of the lateral push used for drill-down navigation.
                                        enterTransition = {
                                            slideInVertically(
                                                initialOffsetY = { it },
                                                animationSpec = tween(280)
                                            ) + fadeIn(animationSpec = tween(200))
                                        },
                                        popExitTransition = {
                                            slideOutVertically(
                                                targetOffsetY = { it },
                                                animationSpec = tween(220)
                                            ) + fadeOut(animationSpec = tween(150))
                                        }
                                    ) {
                                        // Reset only after this screen leaves composition, not on tap — else the form clears mid slide-out.
                                        DisposableEffect(Unit) {
                                            onDispose {
                                                habitToEdit = null
                                                addHabitViewModel.resetForm()
                                            }
                                        }

                                        AddHabitScreen(
                                            onBack = {
                                                navController.popBackStack()
                                            },
                                            onCreateHabit = { _, _, _, _, _, _, _, _ ->
                                                scope.launch {
                                                    addHabitViewModel.createHabit()
                                                }
                                            },
                                            onUpdateHabit = { habitId, name, description, color, category, frequency, targetCount, reminderEnabled, reminderTime, reminderDays, icon ->
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
                                                        reminderTime,
                                                        reminderDays,
                                                        icon
                                                    )
                                                    when (result) {
                                                        is UpdateHabitResult.Success -> {
                                                            Toast.makeText(
                                                                context,
                                                                "Habit updated successfully!",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            navController.popBackStack()
                                                            scope.launch {
                                                                delay(100)
                                                                habitViewModel.refreshHabits()
                                                                // Reschedule notification for the updated habit
                                                                val updatedHabit = result.habit
                                                                if (updatedHabit.reminderEnabled) {
                                                                    NotificationScheduler.scheduleHabitReminder(context, updatedHabit)
                                                                } else {
                                                                    NotificationScheduler.cancelHabitReminder(context, updatedHabit.id)
                                                                }
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
                                                // Cancel notification before deleting
                                                NotificationScheduler.cancelHabitReminder(context, habitId)
                                                habitViewModel.deleteHabit(habitId)
                                                Toast.makeText(
                                                    context,
                                                    "Habit deleted successfully!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.popBackStack()
                                            },
                                            onArchiveHabit = { habitId ->
                                                // Cancel reminders — an archived habit shouldn't keep buzzing
                                                NotificationScheduler.cancelHabitReminder(context, habitId)
                                                habitViewModel.archiveHabit(habitId)
                                                Toast.makeText(
                                                    context,
                                                    "Habit archived",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.popBackStack()
                                            },
                                            modifier = Modifier.padding(innerPadding),
                                            viewModel = addHabitViewModel,
                                            habitToEdit = habitToEdit
                                        )
                                    }

                                    composable(Routes.GENERAL_SETTINGS) {
                                        GeneralSettingsScreen(
                                            onBackClick = {
                                                navController.popBackStack()
                                            },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }

                                    composable(Routes.REORDER_HABITS) {
                                        ReorderHabitsScreen(
                                            habitViewModel = habitViewModel,
                                            onBackClick = {
                                                navController.popBackStack()
                                            },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }

                                    composable(Routes.HABIT_DETAIL) {
                                        val habit = selectedHabitForDetail
                                        if (habit == null) {
                                            LaunchedEffect(Unit) { navController.popBackStack() }
                                        } else {
                                            val detailUiState by habitViewModel.uiState.collectAsState()
                                            val stats by habitDetailViewModel.stats.collectAsState()
                                            // Re-derive from the live list so streak/freeze count reflect
                                            // reactively (e.g. right after freezing), not a stale snapshot.
                                            val liveHabit = detailUiState.habits.find { it.id == habit.id } ?: habit

                                            HabitDetailScreen(
                                                habit = liveHabit,
                                                completions = detailUiState.habitCompletions[habit.id] ?: emptyList(),
                                                freezeDates = detailUiState.habitFreezeDates[habit.id] ?: emptyList(),
                                                stats = stats,
                                                onBackClick = {
                                                    navController.popBackStack()
                                                },
                                                onEditClick = {
                                                    navController.popBackStack()
                                                    openHabitEditor(liveHabit)
                                                },
                                                onFreezeStreakClick = {
                                                    habitViewModel.freezeStreak(habit.id)
                                                },
                                                modifier = Modifier.padding(innerPadding)
                                            )
                                        }
                                    }

                                    composable(Routes.ANALYTICS) {
                                        LaunchedEffect(Unit) {
                                            analyticsViewModel.loadOverallAnalytics()
                                        }
                                        AnalyticsScreen(
                                            analyticsViewModel = analyticsViewModel,
                                            onBackClick = {
                                                navController.popBackStack()
                                            },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }

                                    composable(Routes.ARCHIVED_HABITS) {
                                        ArchivedHabitsScreen(
                                            habitViewModel = habitViewModel,
                                            onBackClick = {
                                                navController.popBackStack()
                                            },
                                            onRestoreClick = { habit ->
                                                habitViewModel.restoreHabit(habit.id)
                                                // Resume reminders for the restored habit, if it had any.
                                                if (habit.reminderEnabled) {
                                                    NotificationScheduler.scheduleHabitReminder(
                                                        context,
                                                        habit.copy(isActive = true)
                                                    )
                                                }
                                                Toast.makeText(
                                                    context,
                                                    "Habit restored",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }

                                    composable(Routes.BACKUP_RESTORE) {
                                        BackupRestoreScreen(
                                            viewModel = backupViewModel,
                                            onBackClick = {
                                                navController.popBackStack()
                                            },
                                            onImportComplete = {
                                                // A restore bypasses the normal add/update paths that
                                                // usually (re)schedule reminders, so do it once here
                                                // for every habit the backup brought in.
                                                scope.launch {
                                                    habitRepo.getAllHabits().first().forEach { habit ->
                                                        NotificationScheduler.scheduleHabitReminder(context, habit)
                                                    }
                                                }
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
}
