package com.mohammadfaizan.habitquest.ui.screens.onbording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.mohammadfaizan.habitquest.domain.repository.PreferencesRepository

class OnboardingViewModel(
    private val repository: PreferencesRepository
) : ViewModel() {
    private val _navigateToMain = MutableStateFlow(false)
    val navigateToMain: StateFlow<Boolean> = _navigateToMain

    fun onContinueClicked() {
        viewModelScope.launch {
            repository.setOnboardingSeen()
            _navigateToMain.value = true
        }
    }
}