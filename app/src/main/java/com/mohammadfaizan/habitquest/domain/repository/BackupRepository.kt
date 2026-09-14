package com.mohammadfaizan.habitquest.domain.repository

data class BackupSummary(
    val habitCount: Int,
    val completionCount: Int,
    val freezeCount: Int
)

interface BackupRepository {
    suspend fun exportBackup(): String
    suspend fun importBackup(json: String): BackupSummary
}
