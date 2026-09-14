package com.mohammadfaizan.habitquest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import com.mohammadfaizan.habitquest.domain.usecase.CalendarDayData
import com.mohammadfaizan.habitquest.domain.usecase.GetCalendarHeatmapUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class CalendarViewMode { MONTH, YEAR }

data class CalendarOverviewUiState(
    val habits: List<Habit> = emptyList(),
    val selectedHabitId: Long? = null, // null = "All Habits" aggregate
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH), // Calendar.MONTH: 0-11
    val days: Map<String, CalendarDayData> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

class CalendarOverviewViewModel(
    private val habitRepository: HabitRepository,
    private val getCalendarHeatmapUseCase: GetCalendarHeatmapUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarOverviewUiState())
    val uiState: StateFlow<CalendarOverviewUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            habitRepository.getActiveHabits().collect { habits ->
                _uiState.value = _uiState.value.copy(habits = habits)
            }
        }
        loadHeatmap()
    }

    fun selectHabit(habitId: Long?) {
        _uiState.value = _uiState.value.copy(selectedHabitId = habitId)
        loadHeatmap()
    }

    fun setViewMode(mode: CalendarViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
        loadHeatmap()
    }

    fun goToPreviousPeriod() {
        val state = _uiState.value
        _uiState.value = if (state.viewMode == CalendarViewMode.MONTH) {
            val calendar = Calendar.getInstance().apply {
                set(state.year, state.month, 1)
                add(Calendar.MONTH, -1)
            }
            state.copy(year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH))
        } else {
            state.copy(year = state.year - 1)
        }
        loadHeatmap()
    }

    fun goToNextPeriod() {
        val state = _uiState.value
        _uiState.value = if (state.viewMode == CalendarViewMode.MONTH) {
            val calendar = Calendar.getInstance().apply {
                set(state.year, state.month, 1)
                add(Calendar.MONTH, 1)
            }
            state.copy(year = calendar.get(Calendar.YEAR), month = calendar.get(Calendar.MONTH))
        } else {
            state.copy(year = state.year + 1)
        }
        loadHeatmap()
    }

    fun goToToday() {
        val calendar = Calendar.getInstance()
        _uiState.value = _uiState.value.copy(
            year = calendar.get(Calendar.YEAR),
            month = calendar.get(Calendar.MONTH)
        )
        loadHeatmap()
    }

    fun jumpToMonth(year: Int, month: Int) {
        _uiState.value = _uiState.value.copy(
            viewMode = CalendarViewMode.MONTH,
            year = year,
            month = month
        )
        loadHeatmap()
    }

    private fun loadHeatmap() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true, error = null)

            val (startKey, endKey) = if (state.viewMode == CalendarViewMode.MONTH) {
                monthRange(state.year, state.month)
            } else {
                yearRange(state.year)
            }

            val result = state.selectedHabitId?.let { habitId ->
                getCalendarHeatmapUseCase.getHabitHeatmap(habitId, startKey, endKey)
            } ?: getCalendarHeatmapUseCase.getAggregateHeatmap(startKey, endKey)

            _uiState.value = _uiState.value.copy(
                days = if (result.success) result.days else _uiState.value.days,
                isLoading = false,
                error = if (!result.success) result.error else null
            )
        }
    }

    private fun monthRange(year: Int, month: Int): Pair<String, String> {
        val calendar = Calendar.getInstance().apply {
            set(year, month, 1, 0, 0, 0)
        }
        val start = dateKeyFormat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = dateKeyFormat.format(calendar.time)
        return start to end
    }

    private fun yearRange(year: Int): Pair<String, String> {
        val calendar = Calendar.getInstance().apply { set(year, Calendar.JANUARY, 1, 0, 0, 0) }
        val start = dateKeyFormat.format(calendar.time)
        calendar.set(year, Calendar.DECEMBER, 31, 0, 0, 0)
        val end = dateKeyFormat.format(calendar.time)
        return start to end
    }
}
