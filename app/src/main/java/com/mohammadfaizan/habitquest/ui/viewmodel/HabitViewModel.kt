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
import com.mohammadfaizan.habitquest.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val habitCompletionRepository: com.mohammadfaizan.habitquest.domain.repository.HabitCompletionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitUiState())
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    private val _actions = MutableStateFlow<HabitAction?>(null)
    val actions: StateFlow<HabitAction?> = _actions.asStateFlow()

    private val completionsCache = mutableMapOf<Long, List<HabitCompletion>>()

    private var lastWeekResetTime: Long = 0

    init {
        loadHabits()
    }

    fun loadHabits() {
        viewModelScope.launch {
            try {
                val result = getHabitsUseCase.getActiveHabits()
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        habits = result.habits,
                        dataLoaded = true
                    )
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
                    loadHabits()
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

                    loadHabits()
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
        loadHabits()
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

    fun loadWeeklyCompletions() {
        viewModelScope.launch {
            try {
                val habits = _uiState.value.habits
                val weeklyCompletionsMap = mutableMapOf<String, List<HabitCompletion>>()

                checkAndHandleWeekReset()

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

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

                _uiState.value = _uiState.value.copy(
                    weeklyCompletions = weeklyCompletionsMap
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
            loadHabits()
        }
    }
}