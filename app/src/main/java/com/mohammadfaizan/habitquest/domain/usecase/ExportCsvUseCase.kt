package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.BackupRepository

data class ExportCsvResult(
    val success: Boolean,
    val csv: String? = null,
    val error: String? = null
)

class ExportCsvUseCase(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(): ExportCsvResult {
        return try {
            ExportCsvResult(success = true, csv = backupRepository.exportCsv())
        } catch (e: Exception) {
            ExportCsvResult(success = false, error = "Failed to create CSV: ${e.message}")
        }
    }
}
