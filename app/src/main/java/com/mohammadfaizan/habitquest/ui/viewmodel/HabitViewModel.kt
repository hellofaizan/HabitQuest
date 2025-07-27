package com.mohammadfaizan.habitquest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import com.mohammadfaizan.habitquest.domain.repository.HabitWithCompletionStatus
import com.mohammadfaizan.habitquest.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HabitUiState(
    val isLoading: Boolean = false,
    val habits: List<Habit> = emptyList(),
    val habitsWithCompletionStatus: List<HabitWithCompletionStatus> = emptyList(),
    val habitCompletions: Map<Long, List<com.mohammadfaizan.habitquest.data.local.HabitCompletion>> = emptyMap(),
    val error: String? = null,
    val selectedHabit: Habit? = null,
    val isRefreshing: Boolean = false
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
    private val habitCompletionRepository: com.mohammadfaizan.habitquest.domain.repository.HabitCompletionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()
    
    private val _actions = MutableStateFlow<HabitAction?>(null)
    val actions: StateFlow<HabitAction?> = _actions.asStateFlow()
    
    init {
        loadHabits()
    }
    
    fun loadHabits() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = getHabitsUseCase.getActiveHabits()
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        habits = result.habits,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load habits: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun loadHabitsWithCompletionStatus(dateKey: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = if (dateKey != null) {
                    getHabitsWithCompletionStatusUseCase.getHabitsForDate(dateKey)
                } else {
                    getHabitsWithCompletionStatusUseCase.getHabitsForToday()
                }
                
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        habitsWithCompletionStatus = result.habits,
                        isLoading = false
                    )
                    // Load completion data for the contribution graph
                    loadHabitCompletions()
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load habits with completion status: ${e.message}",
                    isLoading = false
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
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
                    // Reload habits after successful addition
                    loadHabits()
                    _actions.value = HabitAction(HabitActionType.ADD_HABIT, result.habitId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to add habit: ${e.message}",
                    isLoading = false
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
                    println("Habit completion successful: ${result.currentCompletions}/${result.targetCount}, reloading data...")
                    // Reload habits with completion status
                    loadHabitsWithCompletionStatus()
                    // Also reload completion data for the contribution graph
                    loadHabitCompletions()
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val request = DeleteHabitRequest(habitId = habitId)
                val result = deleteHabitUseCase(request)
                
                if (result.success) {
                    // Reload habits after successful deletion
                    loadHabits()
                    loadHabitsWithCompletionStatus()
                    _actions.value = HabitAction(HabitActionType.DELETE_HABIT, habitId)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete habit: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun searchHabits(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = getHabitsUseCase.searchHabits(query)
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        habits = result.habits,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to search habits: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun filterHabitsByCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = getHabitsUseCase.getHabitsByCategory(category)
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        habits = result.habits,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to filter habits: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun selectHabit(habit: Habit) {
        _uiState.value = _uiState.value.copy(selectedHabit = habit)
        _actions.value = HabitAction(HabitActionType.SELECT_HABIT, habit)
    }
    
    fun refreshHabits() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadHabits()
        loadHabitsWithCompletionStatus()
        _uiState.value = _uiState.value.copy(isRefreshing = false)
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
                
                println("Loading completions for ${habits.size} habits...")

                habits.forEach { habit ->
                    val completions = habitCompletionRepository.getCompletionsForHabit(habit.id).first()
                    completionsMap[habit.id] = completions
                    println("Habit ${habit.name}: ${completions.size} completions")
                }
                
                _uiState.value = _uiState.value.copy(habitCompletions = completionsMap)
                println("Updated habitCompletions map with ${completionsMap.size} entries")
            } catch (e: Exception) {
                println("Error loading habit completions: ${e.message}")
            }
        }
    }
}
