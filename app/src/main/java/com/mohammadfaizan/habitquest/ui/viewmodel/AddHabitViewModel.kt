package com.mohammadfaizan.habitquest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammadfaizan.habitquest.domain.usecase.AddHabitRequest
import com.mohammadfaizan.habitquest.domain.usecase.AddHabitUseCase
import com.mohammadfaizan.habitquest.utils.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

val DEFAULT_REMINDER_DAYS = setOf(
    Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
    Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
)

// Emoji rather than a vector icon set — no icon-resource dependency needed, renders
// identically everywhere, and gives genuine per-habit visual customization.
val AVAILABLE_HABIT_ICONS = listOf(
    "❤️", "🏃", "💪", "🧘", "📚", "💧", "🥗", "😴",
    "🚭", "💰", "🎨", "🎵", "🧹", "🌱", "☀️", "🦷",
    "🚴", "🏊", "✍️", "🙏"
)

data class AddHabitFormState(
    val name: String = "",
    val description: String = "",
    val color: String = "#2a78d6",
    val icon: String = AVAILABLE_HABIT_ICONS.first(),
    val category: String = "",
    val frequency: String = "DAILY",
    val targetCount: Int = 1,
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "09:00",
    val reminderDays: Set<Int> = DEFAULT_REMINDER_DAYS,
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

    fun updateIcon(icon: String) {
        _formState.value = _formState.value.copy(icon = icon)
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

    fun updateReminderDays(days: Set<Int>) {
        _formState.value = _formState.value.copy(reminderDays = days)
    }

    fun toggleReminderDay(day: Int) {
        val current = _formState.value.reminderDays
        val updated = if (day in current) current - day else current + day
        _formState.value = _formState.value.copy(reminderDays = updated)
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
                    icon = _formState.value.icon,
                    category = _formState.value.category.trim().takeIf { it.isNotBlank() },
                    frequency = _formState.value.frequency,
                    targetCount = _formState.value.targetCount,
                    reminderTime = if (_formState.value.reminderEnabled) _formState.value.reminderTime else null,
                    reminderEnabled = _formState.value.reminderEnabled,
                    reminderDays = DateUtils.formatReminderDays(_formState.value.reminderDays)
                )

                val result = addHabitUseCase(request)
                if (result.success) {
                    _formState.value = _formState.value.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                    _actions.value =
                        AddHabitAction(AddHabitActionType.HABIT_CREATED, result.habitId)
                } else {
                    _formState.value = _formState.value.copy(
                        error = result.error,
                        isLoading = false
                    )
                    _actions.value =
                        AddHabitAction(AddHabitActionType.VALIDATION_ERROR, result.error)
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
        return listOf("DAILY", "WEEKLY", "MONTHLY")
    }

    fun getAvailableColors(): List<String> {
        // Muted, graph-friendly hues (validated categorical palette) instead of
        // harsh neon primaries — these read well both as UI accents and blended
        // at varying alpha in the contribution graph.
        return listOf(
            "#2a78d6", // Blue
            "#eb6834", // Orange
            "#1baf7a", // Aqua
            "#eda100", // Yellow
            "#e87ba4", // Magenta
            "#008300", // Green
            "#4a3aa7", // Violet
            "#e34948"  // Red
        )
    }

    fun getAvailableIcons(): List<String> {
        return AVAILABLE_HABIT_ICONS
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
