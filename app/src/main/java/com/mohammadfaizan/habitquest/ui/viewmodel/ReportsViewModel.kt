package com.mohammadfaizan.habitquest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitReportUseCase
import com.mohammadfaizan.habitquest.domain.usecase.PeriodReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ReportPeriod { WEEK, MONTH }

data class ReportsUiState(
    val period: ReportPeriod = ReportPeriod.WEEK,
    // 0 = current period, -1 = one period back, etc. Never lets you go past 0 (into the future).
    val periodOffset: Int = 0,
    val report: PeriodReport? = null,
    val isLoading: Boolean = false
)

private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

class ReportsViewModel(
    private val getHabitReportUseCase: GetHabitReportUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReport()
    }

    fun setPeriod(period: ReportPeriod) {
        _uiState.value = _uiState.value.copy(period = period, periodOffset = 0)
        loadReport()
    }

    fun goToPrevious() {
        _uiState.value = _uiState.value.copy(periodOffset = _uiState.value.periodOffset - 1)
        loadReport()
    }

    fun goToNext() {
        val current = _uiState.value
        if (current.periodOffset < 0) {
            _uiState.value = current.copy(periodOffset = current.periodOffset + 1)
            loadReport()
        }
    }

    private fun loadReport() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.value = state.copy(isLoading = true)

            val (start, end) = periodRange(state.period, state.periodOffset)
            val (prevStart, prevEnd) = periodRange(state.period, state.periodOffset - 1)
            val report = getHabitReportUseCase.getReport(start, end, prevStart, prevEnd)

            _uiState.value = _uiState.value.copy(report = report, isLoading = false)
        }
    }

    private fun periodRange(period: ReportPeriod, offset: Int): Pair<String, String> {
        return if (period == ReportPeriod.WEEK) weekRange(offset) else monthRange(offset)
    }

    // Monday-first, matching the rest of the app's week convention (see WeeklyCalender.kt).
    private fun weekRange(offset: Int): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = when (dayOfWeek) {
            Calendar.SUNDAY -> 6
            Calendar.MONDAY -> 0
            else -> dayOfWeek - Calendar.MONDAY
        }
        calendar.add(Calendar.DAY_OF_YEAR, -daysToSubtract)
        calendar.add(Calendar.WEEK_OF_YEAR, offset)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = dateKeyFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 6)
        val end = dateKeyFormat.format(calendar.time)
        return start to end
    }

    private fun monthRange(offset: Int): Pair<String, String> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            add(Calendar.MONTH, offset)
        }
        val start = dateKeyFormat.format(calendar.time)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val end = dateKeyFormat.format(calendar.time)
        return start to end
    }
}
