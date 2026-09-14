package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.BackupRepository

data class ExportBackupResult(
    val success: Boolean,
    val json: String? = null,
    val error: String? = null
)

class ExportBackupUseCase(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(): ExportBackupResult {
        return try {
            ExportBackupResult(success = true, json = backupRepository.exportBackup())
        } catch (e: Exception) {
            ExportBackupResult(success = false, error = "Failed to create backup: ${e.message}")
        }
    }
}
