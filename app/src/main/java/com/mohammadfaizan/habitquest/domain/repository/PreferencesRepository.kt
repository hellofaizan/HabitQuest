package com.mohammadfaizan.habitquest.domain.repository

import com.mohammadfaizan.habitquest.data.local.AppPreferences
import com.mohammadfaizan.habitquest.data.local.AppPreferencesDao

interface PreferencesRepository {
    suspend fun hasSeenOnboarding(): Boolean
    suspend fun setOnboardingSeen()
}

class PreferencesRepositoryImpl(
    private val dao: AppPreferencesDao
) : PreferencesRepository {
    override suspend fun hasSeenOnboarding(): Boolean {
        return dao.getPreferences()?.hasSeenOnboarding ?: false
    }

    override suspend fun setOnboardingSeen() {
        dao.insertPreferences(AppPreferences(id = 0, hasSeenOnboarding = true))
    }
}