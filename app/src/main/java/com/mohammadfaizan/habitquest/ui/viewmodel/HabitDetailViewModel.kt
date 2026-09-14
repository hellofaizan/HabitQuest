package com.mohammadfaizan.habitquest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammadfaizan.habitquest.domain.repository.HabitStats
import com.mohammadfaizan.habitquest.domain.usecase.GetHabitStatsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HabitDetailViewModel(
    private val getHabitStatsUseCase: GetHabitStatsUseCase
) : ViewModel() {

    private val _stats = MutableStateFlow<HabitStats?>(null)
    val stats: StateFlow<HabitStats?> = _stats.asStateFlow()

    private var loadedForHabitId: Long? = null

    fun loadStats(habitId: Long) {
        loadedForHabitId = habitId
        viewModelScope.launch {
            val result = getHabitStatsUseCase.getStats(habitId)
            // Ignore a stale response if the screen moved on to a different habit meanwhile.
            if (loadedForHabitId == habitId && result.success) {
                _stats.value = result.stats
            }
        }
    }
}
