package com.mohammadfaizan.habitquest.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_preferences")
data class AppPreferences(
    @PrimaryKey val id: Int = 0,
    val hasSeenOnboarding: Boolean = false
)