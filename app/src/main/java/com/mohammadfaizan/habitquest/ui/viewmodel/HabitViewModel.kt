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
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository
import com.mohammadfaizan.habitquest.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
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
    val weeklyCompletions: Map<String, List<HabitCompletion>> = emptyMap(),
    val error: String? = null,
    val selectedHabit: Habit? = null,
    val dataLoaded: Boolean = false
)

data class HabitAction(
    val type: HabitActionType,
    val data: Any? = null
)

enum class HabitActionType {
    ADD_HABIT,
    COMPLETE_HABIT,
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
    private val habitManagementRepository: HabitManagementRepository
) : ViewModel() {

    // Observe habits Flow directly for instant loading
    // Use Eagerly to start loading immediately when ViewModel is created
    private val habitsFlow = habitRepository.getActiveHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // Start immediately, don't wait for subscription
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    private val _actions = MutableStateFlow<HabitAction?>(null)
    val actions: StateFlow<HabitAction?> = _actions.asStateFlow()

    private val completionsCache = mutableMapOf<Long, List<HabitCompletion>>()

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
        color: String = "#FF0000",
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

    fun completeHabit(habitId: Long, notes: String? = null) {
        viewModelScope.launch {
            try {
                println("Completing habit with ID: $habitId")
                val request = CompleteHabitRequest(habitId = habitId, notes = notes)
                val result = completeHabitUseCase(request)

                if (result.success) {
                    println("Habit completion successful: ${result.currentCompletions}/${result.targetCount}")

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

                    _actions.value = HabitAction(HabitActionType.COMPLETE_HABIT, result.newStreak)
                } else {
                    _uiState.value = _uiState.value.copy(error = result.error)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to complete habit: ${e.message}"
                )
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

    fun searchHabits(query: String) {
        viewModelScope.launch {
            try {
                val result = getHabitsUseCase.searchHabits(query)
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        habits = result.habits
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to search habits: ${e.message}"
                )
            }
        }
    }

    fun filterHabitsByCategory(category: String) {
        viewModelScope.launch {
            try {
                val result = getHabitsUseCase.getHabitsByCategory(category)
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        habits = result.habits
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to filter habits: ${e.message}"
                )
            }
        }
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

                _uiState.value = _uiState.value.copy(
                    habitCompletions = completionsMap,
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