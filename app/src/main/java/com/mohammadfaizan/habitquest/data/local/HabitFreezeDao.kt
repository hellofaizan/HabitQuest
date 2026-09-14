package com.mohammadfaizan.habitquest.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HabitFreezeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFreeze(freeze: HabitFreeze)

    @Query("SELECT dateKey FROM habit_freezes WHERE habitId = :habitId")
    suspend fun getFreezeDates(habitId: Long): List<String>

    @Query("SELECT COUNT(*) FROM habit_freezes WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun isFrozen(habitId: Long, dateKey: String): Int

    @Query("DELETE FROM habit_freezes WHERE habitId = :habitId")
    suspend fun deleteAllFreezesForHabit(habitId: Long)
}
