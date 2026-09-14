package com.mohammadfaizan.habitquest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.domain.repository.HabitWithCompletionStatus
import com.mohammadfaizan.habitquest.domain.usecase.AddHabitRequest
import com.mohammadfaizan.habitquest.domain.usecase.AddHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.CompleteHabitRequest
import com.mohammadfaizan.habitquest.domain.usecase.CompleteHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.DeleteHabitRequest
import com.mohammadfaizan.habitquest.domain.usecase.DeleteHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitsUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitsWithCompletionStatusUseCase
import com.mohammadfaizan.habitquest.domain.usecase.ReorderHabitsUseCase
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import com.mohammadfaizan.habitquest.utils.Achievements
import com.mohammadfaizan.habitquest.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HabitUiState(
    val habits: List<Habit> = emptyList(),
    val habitsWithCompletionStatus: List<HabitWithCompletionStatus> = emptyList(),
    val habitCompletions: Map<Long, List<HabitCompletion>> = emptyMap(),
    val habitFreezeDates: Map<Long, List<String>> = emptyMap(),
    val weeklyCompletions: Map<String, List<HabitCompletion>> = emptyMap(),
    val error: String? = null,
    val selectedHabit: Habit? = null,
    val dataLoaded: Boolean = false
)

data class HabitAction(
    val type: HabitActionType,
    val data: Any? = null
)

data class HabitCompletionInfo(
    val newStreak: Int,
    val milestoneReached: Int? = null
)

data class FreezeStreakActionResult(
    val success: Boolean,
    val error: String? = null
)

enum class HabitActionType {
    ADD_HABIT,
    COMPLETE_HABIT,
    COMPLETE_ALL_HABITS,
    UNCOMPLETE_HABIT,
    FREEZE_STREAK,
    DELETE_HABIT,
    REFRESH_HABITS,
    SELECT_HABIT,
    SEARCH_HABITS,
    FILTER_BY_CATEGORY
}

class HabitViewModel @Inject constructor(
    private val addHabitUseCase: AddHabitUseCase,
    private val getHabitsUseCase: GetHabitsUseCase,
    private val completeHabitUseCase: CompleteHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val getHabitsWithCompletionStatusUseCase: GetHabitsWithCompletionStatusUseCase,
    private val habitCompletionRepository: com.mohammadfaizan.habitquest.domain.repository.HabitCompletionRepository,
    private val habitRepository: HabitRepository,
    private val generateRandomDataUseCase: com.mohammadfaizan.habitquest.domain.usecase.GenerateRandomDataUseCase,
    private val habitManagementRepository: HabitManagementRepository,
    private val reorderHabitsUseCase: ReorderHabitsUseCase,
    private val uncompleteHabitUseCase: com.mohammadfaizan.habitquest.domain.usecase.UncompleteHabitUseCase,
    private val archiveHabitUseCase: com.mohammadfaizan.habitquest.domain.usecase.ArchiveHabitUseCase,
    private val freezeStreakUseCase: com.mohammadfaizan.habitquest.domain.usecase.FreezeStreakUseCase
) : ViewModel() {

    // Observe habits Flow directly for instant loading
    // Use Eagerly to start loading immediately when ViewModel is created
    private val habitsFlow = habitRepository.getActiveHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // Start immediately, don't wait for subscription
            initialValue = emptyList()
        )

    // Includes inactive habits too, so the Archived Habits screen has something to show.
    val archivedHabits: StateFlow<List<Habit>> = habitRepository.getAllHabits()
        .map { habits -> habits.filter { !it.isActive } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Derived from the live habits Flow rather than a one-off snapshot, so search/filter
    // results stay correct as habits are completed, added, or streak-recalculated in the
    // background — instead of getting silently clobbered by the next reactive update.
    val visibleHabits: StateFlow<List<Habit>> = combine(
        habitsFlow, _searchQuery, _selectedCategory
    ) { habits, query, category ->
        habits.filter { habit ->
            (query.isBlank() || habit.name.contains(query, ignoreCase = true)) &&
                (category == null || habit.category == category)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    private val _actions = MutableStateFlow<HabitAction?>(null)
    val actions: StateFlow<HabitAction?> = _actions.asStateFlow()

    private val completionsCache = mutableMapOf<Long, List<HabitCompletion>>()
    private val habitsBeingCompleted = mutableSetOf<Long>()

    private var lastWeekResetTime: Long = 0

    init {
        // Check and reset streaks on app start
        checkAndResetStreaks()
        
        // Observe habits Flow reactively for instant updates
        viewModelScope.launch {
            habitsFlow.collect { habits ->
                _uiState.value = _uiState.value.copy(
                    habits = habits,
                    dataLoaded = true
                )
                // Load completions when habits change
                if (habits.isNotEmpty()) {
                    loadHabitCompletions()
                    loadWeeklyCompletions()
                    // Load completion status reactively when habits are available
                    loadHabitsWithCompletionStatus()
                }
            }
        }
    }
    
    private fun checkAndResetStreaks() {
        viewModelScope.launch {
            try {
                habitManagementRepository.checkAndResetStreaksIfNeeded()
            } catch (e: Exception) {
                // Silently fail, will retry next time
            }
        }
    }

    fun loadHabits() {
        // This method is kept for backward compatibility but is no longer needed
        // as habits are now loaded reactively via Flow
        // However, we still call it for refresh operations
        viewModelScope.launch {
            try {
                // Force refresh by accessing the flow
                // The flow will automatically emit updated data
                val result = getHabitsUseCase.getActiveHabits()
                if (result.success) {
                    // State is already updated via Flow, just ensure completions are loaded
                    loadWeeklyCompletions()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error,
                        dataLoaded = true
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load habits: ${e.message}",
                    dataLoaded = true
                )
            }
        }
    }

    fun loadHabitsWithCompletionStatus(dateKey: String? = null) {
        viewModelScope.launch {
            try {
                val result = if (dateKey != null) {
                    getHabitsWithCompletionStatusUseCase.getHabitsForDate(dateKey)
                } else {
                    getHabitsWithCompletionStatusUseCase.getHabitsForToday()
                }

                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        habitsWithCompletionStatus = result.habits
                    )
                    loadHabitCompletions()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load habits with completion status: ${e.message}"
                )
            }
        }
    }

    fun addHabit(
        name: String,
        description: String? = null,
        color: String = "#2a78d6",
        category: String? = null,
        frequency: String = "DAILY",
        targetCount: Int = 1
    ) {
        viewModelScope.launch {
            try {
                val request = AddHabitRequest(
                    name = name,
                    description = description,
                    color = color,
                    category = category,
                    frequency = frequency,
                    targetCount = targetCount
                )

                val result = addHabitUseCase(request)
                if (result.success) {
                    // Habits will update automatically via Flow, no need to call loadHabits()
                    _actions.value = HabitAction(HabitActionType.ADD_HABIT, result.habitId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add habit: ${e.message}"
                )
            }
        }
    }

    fun completeAllForToday() {
        viewModelScope.launch {
            try {
                val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val habitIds = _uiState.value.habits.map { it.id }
                val completedCount = habitManagementRepository.completeHabitsForDate(habitIds, dateKey)
                // A bulk action, not the hot single-tap path — a full reload is simplest here
                // rather than hand-patching the optimistic cache for many habits at once.
                refreshHabits()
                _actions.value = HabitAction(HabitActionType.COMPLETE_ALL_HABITS, completedCount)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to complete all habits: ${e.message}"
                )
            }
        }
    }

    fun completeHabit(habitId: Long, notes: String? = null) {
        // Guards against a single tap somehow firing this twice (double-invocation, fast
        // double-tap) launching two concurrent completion attempts for the same habit.
        if (!habitsBeingCompleted.add(habitId)) return

        val previousStreak = _uiState.value.habits.find { it.id == habitId }?.currentStreak ?: 0

        viewModelScope.launch {
            try {
                val request = CompleteHabitRequest(habitId = habitId, notes = notes)
                val result = completeHabitUseCase(request)

                if (result.success) {
                    // Only append locally when a completion was actually newly recorded —
                    // otherwise (already at target) this would double-count in the UI even
                    // though nothing new was written to the database.
                    if (!result.wasAlreadyCompleted) {
                        val currentCompletions = completionsCache[habitId] ?: emptyList()
                        val newCompletion = HabitCompletion(
                            habitId = habitId,
                            completedAt = Date(),
                            notes = notes,
                            dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        )
                        val updatedCompletions = currentCompletions + newCompletion
                        completionsCache[habitId] = updatedCompletions

                        val updatedCompletionsMap = _uiState.value.habitCompletions.toMutableMap()
                        updatedCompletionsMap[habitId] = updatedCompletions
                        _uiState.value = _uiState.value.copy(habitCompletions = updatedCompletionsMap)

                        loadWeeklyCompletions()
                    }

                    val milestoneReached = if (result.wasAlreadyCompleted) {
                        null
                    } else {
                        Achievements.streakMilestoneCrossed(previousStreak, result.newStreak)
                    }
                    _actions.value = HabitAction(
                        HabitActionType.COMPLETE_HABIT,
                        HabitCompletionInfo(newStreak = result.newStreak, milestoneReached = milestoneReached)
                    )
                } else {
                    _uiState.value = _uiState.value.copy(error = result.error)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to complete habit: ${e.message}"
                )
            } finally {
                habitsBeingCompleted.remove(habitId)
            }
        }
    }

    fun updateTodayNote(habitId: Long, note: String) {
        viewModelScope.launch {
            try {
                val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val trimmedNote = note.trim().takeIf { it.isNotBlank() }
                val success = habitManagementRepository.updateCompletionNote(habitId, dateKey, trimmedNote)
                if (success) {
                    // Simplest correct option here: re-pull from the DB rather than hand-patch
                    // the optimistic cache — note edits aren't on the hot tap-to-complete path.
                    loadHabitCompletions()
                } else {
                    _uiState.value = _uiState.value.copy(error = "No completion found for today")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update note: ${e.message}"
                )
            }
        }
    }

    fun uncompleteHabit(habitId: Long) {
        // Same in-flight guard as completeHabit — also blocks a stray double-tap racing an
        // in-progress complete/uncomplete for the same habit.
        if (!habitsBeingCompleted.add(habitId)) return

        viewModelScope.launch {
            try {
                val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val result = uncompleteHabitUseCase(habitId, dateKey)

                if (result.success) {
                    val currentCompletions = completionsCache[habitId] ?: emptyList()
                    val mostRecentToday = currentCompletions
                        .filter { it.dateKey == dateKey }
                        .maxByOrNull { it.completedAt }

                    if (mostRecentToday != null) {
                        val updatedCompletions = currentCompletions - mostRecentToday
                        completionsCache[habitId] = updatedCompletions

                        val updatedCompletionsMap = _uiState.value.habitCompletions.toMutableMap()
                        updatedCompletionsMap[habitId] = updatedCompletions
                        _uiState.value = _uiState.value.copy(habitCompletions = updatedCompletionsMap)

                        loadWeeklyCompletions()
                    }

                    _actions.value = HabitAction(HabitActionType.UNCOMPLETE_HABIT)
                } else {
                    _uiState.value = _uiState.value.copy(error = result.error)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to undo completion: ${e.message}"
                )
            } finally {
                habitsBeingCompleted.remove(habitId)
            }
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                val request = DeleteHabitRequest(habitId = habitId)
                val result = deleteHabitUseCase(request)

                if (result.success) {
                    completionsCache.remove(habitId)

                    // Habits will update automatically via Flow, no need to call loadHabits()
                    loadHabitsWithCompletionStatus()
                    _actions.value = HabitAction(HabitActionType.DELETE_HABIT, habitId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete habit: ${e.message}"
                )
            }
        }
    }

    fun freezeStreak(habitId: Long) {
        viewModelScope.launch {
            try {
                val result = freezeStreakUseCase(habitId)
                if (result.success) {
                    // habitsFlow reflects the updated freezesAvailable/streak reactively; also
                    // refresh the freeze-dates map so the icy highlight shows immediately.
                    loadHabitCompletions()
                }
                _actions.value = HabitAction(
                    HabitActionType.FREEZE_STREAK,
                    FreezeStreakActionResult(success = result.success, error = result.error)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to freeze streak: ${e.message}"
                )
            }
        }
    }

    fun archiveHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                // habitsFlow (active only) and archivedHabits (inactive only) both react
                // automatically via their own Flows once isActive flips — caller shows its
                // own confirmation (MainActivity's onArchiveHabit), so no action to emit here.
                archiveHabitUseCase(habitId, isActive = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to archive habit: ${e.message}"
                )
            }
        }
    }

    fun restoreHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                archiveHabitUseCase(habitId, isActive = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to restore habit: ${e.message}"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategoryFilter(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun selectHabit(habit: Habit) {
        _uiState.value = _uiState.value.copy(selectedHabit = habit)
        _actions.value = HabitAction(HabitActionType.SELECT_HABIT, habit)
    }

    fun refreshHabits() {
        completionsCache.clear()
        // Habits Flow will automatically update, just refresh completions
        loadHabitsWithCompletionStatus()
        loadWeeklyCompletions()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearActions() {
        _actions.value = null
    }

    fun loadHabitCompletions() {
        viewModelScope.launch {
            try {
                val habits = _uiState.value.habits
                val completionsMap = mutableMapOf<Long, List<HabitCompletion>>()
                val habitIds = habits.map { it.id }
                if (habitIds.isNotEmpty()) {
                    val allCompletions = habitCompletionRepository.getCompletionsForHabits(habitIds)
                    habitIds.forEach { habitId ->
                        val habitCompletions = allCompletions.filter { it.habitId == habitId }
                        completionsMap[habitId] = habitCompletions
                        completionsCache[habitId] = habitCompletions
                    }
                }

                // No batch query for freezes (they're rare — a handful per habit at most),
                // so a small per-habit query here is fine rather than adding one.
                val freezeDatesMap = habitIds.associateWith { habitId ->
                    habitCompletionRepository.getFreezeDates(habitId)
                }

                _uiState.value = _uiState.value.copy(
                    habitCompletions = completionsMap,
                    habitFreezeDates = freezeDatesMap,
                    dataLoaded = true
                )

                loadWeeklyCompletions()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load habit completions: ${e.message}",
                    dataLoaded = true
                )
            }
        }
    }

    fun loadWeeklyCompletions(weekOffset: Int = 0) {
        viewModelScope.launch {
            try {
                val habits = _uiState.value.habits
                val weeklyCompletionsMap = mutableMapOf<String, List<HabitCompletion>>()

                if (weekOffset == 0) {
                    checkAndHandleWeekReset()
                }

                val calendar = Calendar.getInstance()
                // Calculate days to subtract to get to Monday of current week
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val daysToSubtract = when (dayOfWeek) {
                    Calendar.SUNDAY -> 6  // Go back 6 days to get Monday
                    Calendar.MONDAY -> 0  // Already Monday
                    else -> dayOfWeek - Calendar.MONDAY  // Subtract to get to Monday
                }
                calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
                
                // Add week offset (negative = go back in weeks)
                calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
                
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val weekDates = mutableListOf<String>()

                for (i in 0..6) {
                    val date = calendar.time
                    weekDates.add(dateFormat.format(date))
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                val habitIds = habits.map { it.id }
                if (habitIds.isNotEmpty()) {
                    val allCompletions = habitCompletionRepository.getCompletionsForHabits(habitIds)

                    weekDates.forEach { date ->
                        val completionsForDate = allCompletions.filter { it.dateKey == date }
                        weeklyCompletionsMap[date] = completionsForDate
                    }
                }

                // Merge with existing completions (keep all weeks data)
                val updatedCompletions = _uiState.value.weeklyCompletions.toMutableMap()
                updatedCompletions.putAll(weeklyCompletionsMap)

                _uiState.value = _uiState.value.copy(
                    weeklyCompletions = updatedCompletions
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load weekly completions: ${e.message}"
                )
            }
        }
    }

    private fun checkAndHandleWeekReset() {
        val currentTime = System.currentTimeMillis()
        if (DateUtils.isWeekResetTime() && currentTime > lastWeekResetTime + 60000) { // 1 minute cooldown
            lastWeekResetTime = currentTime
            // Habits Flow will automatically update, just refresh completions
            loadWeeklyCompletions()
        }
    }

    fun reorderHabits(orderedHabitIds: List<Long>) {
        viewModelScope.launch {
            try {
                reorderHabitsUseCase(orderedHabitIds)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to reorder habits: ${e.message}"
                )
            }
        }
    }

    fun generateRandomData(days: Int = 182, completionProbability: Float = 0.7f) {
        viewModelScope.launch {
            try {
                val result = generateRandomDataUseCase(days, completionProbability)
                if (result.success) {
                    // Refresh all completions and weekly data
                    loadHabitCompletions()
                    loadWeeklyCompletions()
                    loadHabitsWithCompletionStatus()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error ?: "Failed to generate random data"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to generate random data: ${e.message}"
                )
            }
        }
    }
}