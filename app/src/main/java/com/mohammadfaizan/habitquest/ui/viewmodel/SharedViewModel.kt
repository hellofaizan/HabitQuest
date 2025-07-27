package com.mohammadfaizan.habitquest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SharedUiState(
    val currentScreen: String = "home",
    val showAddHabitScreen: Boolean = false,
    val showAnalyticsScreen: Boolean = false,
    val selectedHabitId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null
)

data class SharedAction(
    val type: SharedActionType,
    val data: Any? = null
)

enum class SharedActionType {
    NAVIGATE_TO_SCREEN,
    SHOW_ADD_HABIT_SCREEN,
    HIDE_ADD_HABIT_SCREEN,
    SHOW_ANALYTICS_SCREEN,
    HIDE_ANALYTICS_SCREEN,
    SELECT_HABIT,
    SHOW_SNACKBAR,
    HIDE_SNACKBAR,
    SHOW_ERROR,
    HIDE_ERROR
}

class SharedViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(SharedUiState())
    val uiState: StateFlow<SharedUiState> = _uiState.asStateFlow()
    
    private val _actions = MutableStateFlow<SharedAction?>(null)
    val actions: StateFlow<SharedAction?> = _actions.asStateFlow()
    
    fun navigateToScreen(screen: String) {
        _uiState.value = _uiState.value.copy(currentScreen = screen)
        _actions.value = SharedAction(SharedActionType.NAVIGATE_TO_SCREEN, screen)
    }
    
    fun showAddHabitScreen() {
        _uiState.value = _uiState.value.copy(showAddHabitScreen = true)
        _actions.value = SharedAction(SharedActionType.SHOW_ADD_HABIT_SCREEN)
    }
    
    fun hideAddHabitScreen() {
        _uiState.value = _uiState.value.copy(showAddHabitScreen = false)
        _actions.value = SharedAction(SharedActionType.HIDE_ADD_HABIT_SCREEN)
    }
    
    fun showAnalyticsScreen() {
        _uiState.value = _uiState.value.copy(showAnalyticsScreen = true)
        _actions.value = SharedAction(SharedActionType.SHOW_ANALYTICS_SCREEN)
    }
    
    fun hideAnalyticsScreen() {
        _uiState.value = _uiState.value.copy(showAnalyticsScreen = false)
        _actions.value = SharedAction(SharedActionType.HIDE_ANALYTICS_SCREEN)
    }
    
    fun selectHabit(habitId: Long) {
        _uiState.value = _uiState.value.copy(selectedHabitId = habitId)
        _actions.value = SharedAction(SharedActionType.SELECT_HABIT, habitId)
    }
    
    fun showSnackbar(message: String) {
        _uiState.value = _uiState.value.copy(snackbarMessage = message)
        _actions.value = SharedAction(SharedActionType.SHOW_SNACKBAR, message)
    }
    
    fun hideSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
        _actions.value = SharedAction(SharedActionType.HIDE_SNACKBAR)
    }
    
    fun showError(error: String) {
        _uiState.value = _uiState.value.copy(error = error)
        _actions.value = SharedAction(SharedActionType.SHOW_ERROR, error)
    }
    
    fun hideError() {
        _uiState.value = _uiState.value.copy(error = null)
        _actions.value = SharedAction(SharedActionType.HIDE_ERROR)
    }
    
    fun setLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = loading)
    }
    
    fun clearActions() {
        _actions.value = null
    }
    
    fun getCurrentDate(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
    
    fun getCurrentWeekStart(): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
    
    fun getCurrentMonth(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }
}
