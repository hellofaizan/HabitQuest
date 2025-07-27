package com.mohammadfaizan.habitquest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammadfaizan.habitquest.domain.usecase.GetAnalyticsUseCase
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitStatsUseCase
import com.mohammadfaizan.habitquest.domain.usecase.AnalyticsData
import com.mohammadfaizan.habitquest.domain.repository.HabitStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val analytics: AnalyticsData? = null,
    val habitStats: HabitStats? = null,
    val selectedHabitId: Long? = null,
    val selectedWeekStart: String? = null,
    val selectedMonth: String? = null,
    val error: String? = null,
    val isRefreshing: Boolean = false
)

data class AnalyticsAction(
    val type: AnalyticsActionType,
    val data: Any? = null
)

enum class AnalyticsActionType {
    LOAD_OVERALL_ANALYTICS,
    LOAD_HABIT_ANALYTICS,
    LOAD_WEEKLY_ANALYTICS,
    LOAD_MONTHLY_ANALYTICS,
    SELECT_HABIT,
    SELECT_WEEK,
    SELECT_MONTH
}

class AnalyticsViewModel @Inject constructor(
    private val getAnalyticsUseCase: GetAnalyticsUseCase,
    private val getHabitStatsUseCase: GetHabitStatsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()
    
    private val _actions = MutableStateFlow<AnalyticsAction?>(null)
    val actions: StateFlow<AnalyticsAction?> = _actions.asStateFlow()
    
    init {
        loadOverallAnalytics()
    }
    
    fun loadOverallAnalytics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = getAnalyticsUseCase.getOverallAnalytics()
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        analytics = result.analytics,
                        isLoading = false
                    )
                    _actions.value = AnalyticsAction(AnalyticsActionType.LOAD_OVERALL_ANALYTICS, result.analytics)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load overall analytics: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun loadHabitAnalytics(habitId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedHabitId = habitId)
            
            try {
                val result = getAnalyticsUseCase.getHabitAnalytics(habitId)
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        analytics = result.analytics,
                        isLoading = false
                    )
                    _actions.value = AnalyticsAction(AnalyticsActionType.LOAD_HABIT_ANALYTICS, result.analytics)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load habit analytics: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun loadWeeklyAnalytics(habitId: Long, weekStart: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null, 
                selectedHabitId = habitId,
                selectedWeekStart = weekStart
            )
            
            try {
                val result = getAnalyticsUseCase.getWeeklyAnalytics(habitId, weekStart)
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        analytics = result.analytics,
                        isLoading = false
                    )
                    _actions.value = AnalyticsAction(AnalyticsActionType.LOAD_WEEKLY_ANALYTICS, result.analytics)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load weekly analytics: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun loadMonthlyAnalytics(habitId: Long, month: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                error = null, 
                selectedHabitId = habitId,
                selectedMonth = month
            )
            
            try {
                val result = getAnalyticsUseCase.getMonthlyAnalytics(habitId, month)
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        analytics = result.analytics,
                        isLoading = false
                    )
                    _actions.value = AnalyticsAction(AnalyticsActionType.LOAD_MONTHLY_ANALYTICS, result.analytics)
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load monthly analytics: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun loadHabitStats(habitId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = getHabitStatsUseCase.getStats(habitId)
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        habitStats = result.stats,
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
                    error = "Failed to load habit stats: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun selectHabit(habitId: Long) {
        _uiState.value = _uiState.value.copy(selectedHabitId = habitId)
        _actions.value = AnalyticsAction(AnalyticsActionType.SELECT_HABIT, habitId)
        loadHabitAnalytics(habitId)
        loadHabitStats(habitId)
    }
    
    fun selectWeek(weekStart: String) {
        val habitId = _uiState.value.selectedHabitId
        if (habitId != null) {
            _uiState.value = _uiState.value.copy(selectedWeekStart = weekStart)
            _actions.value = AnalyticsAction(AnalyticsActionType.SELECT_WEEK, weekStart)
            loadWeeklyAnalytics(habitId, weekStart)
        }
    }
    
    fun selectMonth(month: String) {
        val habitId = _uiState.value.selectedHabitId
        if (habitId != null) {
            _uiState.value = _uiState.value.copy(selectedMonth = month)
            _actions.value = AnalyticsAction(AnalyticsActionType.SELECT_MONTH, month)
            loadMonthlyAnalytics(habitId, month)
        }
    }
    
    fun refreshAnalytics() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        
        val habitId = _uiState.value.selectedHabitId
        if (habitId != null) {
            loadHabitAnalytics(habitId)
            loadHabitStats(habitId)
        } else {
            loadOverallAnalytics()
        }
        
        _uiState.value = _uiState.value.copy(isRefreshing = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearActions() {
        _actions.value = null
    }
    
    fun getCurrentWeekStart(): String {
        // Helper method to get current week start (Monday)
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    fun getCurrentMonth(): String {
        // Helper method to get current month in yyyy-MM format
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
}
