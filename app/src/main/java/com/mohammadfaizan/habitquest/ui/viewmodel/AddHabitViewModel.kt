package com.mohammadfaizan.habitquest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammadfaizan.habitquest.domain.usecase.AddHabitUseCase
import com.mohammadfaizan.habitquest.domain.usecase.AddHabitRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddHabitFormState(
    val name: String = "",
    val description: String = "",
    val color: String = "#FF0000",
    val category: String = "",
    val frequency: String = "DAILY",
    val targetCount: Int = 1,
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "09:00",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

data class AddHabitFormValidation(
    val isNameValid: Boolean = true,
    val isTargetCountValid: Boolean = true,
    val isColorValid: Boolean = true,
    val isFormValid: Boolean = false
)

data class AddHabitAction(
    val type: AddHabitActionType,
    val data: Any? = null
)

enum class AddHabitActionType {
    HABIT_CREATED,
    VALIDATION_ERROR,
    NETWORK_ERROR
}

class AddHabitViewModel @Inject constructor(
    private val addHabitUseCase: AddHabitUseCase
) : ViewModel() {
    
    private val _formState = MutableStateFlow(AddHabitFormState())
    val formState: StateFlow<AddHabitFormState> = _formState.asStateFlow()
    
    private val _validation = MutableStateFlow(AddHabitFormValidation())
    val validation: StateFlow<AddHabitFormValidation> = _validation.asStateFlow()
    
    private val _actions = MutableStateFlow<AddHabitAction?>(null)
    val actions: StateFlow<AddHabitAction?> = _actions.asStateFlow()
    
    fun updateName(name: String) {
        _formState.value = _formState.value.copy(name = name)
        validateForm()
    }
    
    fun updateDescription(description: String) {
        _formState.value = _formState.value.copy(description = description)
    }
    
    fun updateColor(color: String) {
        _formState.value = _formState.value.copy(color = color)
        validateForm()
    }
    
    fun updateCategory(category: String) {
        _formState.value = _formState.value.copy(category = category)
    }
    
    fun updateFrequency(frequency: String) {
        _formState.value = _formState.value.copy(frequency = frequency)
    }
    
    fun updateTargetCount(targetCount: Int) {
        _formState.value = _formState.value.copy(targetCount = targetCount)
        validateForm()
    }
    
    fun updateReminderEnabled(enabled: Boolean) {
        _formState.value = _formState.value.copy(reminderEnabled = enabled)
    }
    
    fun updateReminderTime(time: String) {
        _formState.value = _formState.value.copy(reminderTime = time)
    }
    
    private fun validateForm() {
        val currentState = _formState.value
        val isNameValid = currentState.name.trim().isNotBlank()
        val isTargetCountValid = currentState.targetCount > 0
        val isColorValid = currentState.color.matches(Regex("^#[0-9A-Fa-f]{6}$"))
        val isFormValid = isNameValid && isTargetCountValid && isColorValid
        
        _validation.value = AddHabitFormValidation(
            isNameValid = isNameValid,
            isTargetCountValid = isTargetCountValid,
            isColorValid = isColorValid,
            isFormValid = isFormValid
        )
    }
    
    fun createHabit() {
        if (!_validation.value.isFormValid) {
            _actions.value = AddHabitAction(AddHabitActionType.VALIDATION_ERROR)
            return
        }
        
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isLoading = true, error = null)
            
            try {
                val request = AddHabitRequest(
                    name = _formState.value.name.trim(),
                    description = _formState.value.description.trim().takeIf { it.isNotBlank() },
                    color = _formState.value.color,
                    category = _formState.value.category.trim().takeIf { it.isNotBlank() },
                    frequency = _formState.value.frequency,
                    targetCount = _formState.value.targetCount,
                    reminderTime = if (_formState.value.reminderEnabled) _formState.value.reminderTime else null,
                    reminderEnabled = _formState.value.reminderEnabled
                )
                
                val result = addHabitUseCase(request)
                if (result.success) {
                    _formState.value = _formState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    _actions.value = AddHabitAction(AddHabitActionType.HABIT_CREATED, result.habitId)
                } else {
                    _formState.value = _formState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                    _actions.value = AddHabitAction(AddHabitActionType.VALIDATION_ERROR, result.error)
                }
            } catch (e: Exception) {
                _formState.value = _formState.value.copy(
                    error = "Failed to create habit: ${e.message}",
                    isLoading = false
                )
                _actions.value = AddHabitAction(AddHabitActionType.NETWORK_ERROR, e.message)
            }
        }
    }
    
    fun resetForm() {
        _formState.value = AddHabitFormState()
        _validation.value = AddHabitFormValidation()
        _actions.value = null
    }
    
    fun clearError() {
        _formState.value = _formState.value.copy(error = null)
    }
    
    fun clearActions() {
        _actions.value = null
    }
    
    fun getAvailableFrequencies(): List<String> {
        return listOf("DAILY", "WEEKLY", "MONTHLY", "CUSTOM")
    }
    
    fun getAvailableColors(): List<String> {
        return listOf(
            "#FF0000", // Red
            "#00FF00", // Green
            "#0000FF", // Blue
            "#FFFF00", // Yellow
            "#FF00FF", // Magenta
            "#00FFFF", // Cyan
            "#FFA500", // Orange
            "#800080", // Purple
            "#008000", // Dark Green
            "#FFC0CB"  // Pink
        )
    }
    
    fun getAvailableCategories(): List<String> {
        return listOf(
            "Health",
            "Fitness",
            "Learning",
            "Productivity",
            "Mindfulness",
            "Social",
            "Finance",
            "Creative",
            "Other"
        )
    }
}
