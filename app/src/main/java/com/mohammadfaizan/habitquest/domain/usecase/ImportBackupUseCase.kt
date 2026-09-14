package com.mohammadfaizan.habitquest.domain.usecase

import com.mohammadfaizan.habitquest.domain.repository.BackupRepository
import com.mohammadfaizan.habitquest.domain.repository.BackupSummary
import com.mohammadfaizan.habitquest.domain.repository.HabitManagementRepository

data class ImportBackupResult(
    val success: Boolean,
    val summary: BackupSummary? = null,
    val error: String? = null
)

class ImportBackupUseCase(
    private val backupRepository: BackupRepository,
    private val habitManagementRepository: HabitManagementRepository
) {
    suspend operator fun invoke(json: String): ImportBackupResult {
        return try {
            val summary = backupRepository.importBackup(json)
            // The backup's stored streak values are only as fresh as the day it was taken;
            // recompute against today's date so a week-old backup doesn't show a stale streak.
            habitManagementRepository.recalculateAllStreaks()
            ImportBackupResult(success = true, summary = summary)
        } catch (e: Exception) {
            ImportBackupResult(success = false, error = e.message ?: "Failed to restore backup")
        }
    }
}
