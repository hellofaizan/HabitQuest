package com.mohammadfaizan.habitquest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohammadfaizan.habitquest.domain.usecase.ExportBackupResult
import com.mohammadfaizan.habitquest.domain.usecase.ExportBackupUseCase
import com.mohammadfaizan.habitquest.domain.usecase.ExportCsvResult
import com.mohammadfaizan.habitquest.domain.usecase.ExportCsvUseCase
import com.mohammadfaizan.habitquest.domain.usecase.ImportBackupResult
import com.mohammadfaizan.habitquest.domain.usecase.ImportBackupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackupViewModel(
    private val exportBackupUseCase: ExportBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
    private val exportCsvUseCase: ExportCsvUseCase
) : ViewModel() {

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking.asStateFlow()

    fun exportBackup(onResult: (ExportBackupResult) -> Unit) {
        viewModelScope.launch {
            _isWorking.value = true
            val result = exportBackupUseCase()
            _isWorking.value = false
            onResult(result)
        }
    }

    fun importBackup(json: String, onResult: (ImportBackupResult) -> Unit) {
        viewModelScope.launch {
            _isWorking.value = true
            val result = importBackupUseCase(json)
            _isWorking.value = false
            onResult(result)
        }
    }

    fun exportCsv(onResult: (ExportCsvResult) -> Unit) {
        viewModelScope.launch {
            _isWorking.value = true
            val result = exportCsvUseCase()
            _isWorking.value = false
            onResult(result)
        }
    }
}
