package com.mohammadfaizan.habitquest.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AppPreferences::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appPreferencesDao(): AppPreferencesDao
}