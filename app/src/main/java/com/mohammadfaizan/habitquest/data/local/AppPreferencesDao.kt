package com.mohammadfaizan.habitquest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppPreferencesDao {
    @Query("SELECT * FROM app_preferences WHERE id = 0")
    suspend fun getPreferences(): AppPreferences?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(prefs: AppPreferences)
}