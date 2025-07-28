package com.mohammadfaizan.habitquest.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface HabitDao {

    // Basic CRUD operations
    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    suspend fun getHabitById(habitId: Long): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Long)

    // Search and filter operations
    @Query("SELECT * FROM habits WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchHabits(query: String): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE category = :category")
    fun getHabitsByCategory(category: String): Flow<List<Habit>>

    // Date-based queries
    @Query("SELECT * FROM habits WHERE createdAt >= :startDate ORDER BY createdAt DESC")
    fun getHabitsCreatedSince(startDate: Date): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE updatedAt >= :startDate ORDER BY updatedAt DESC")
    fun getHabitsUpdatedSince(startDate: Date): Flow<List<Habit>>

    // Streak and progress operations
    @Query("UPDATE habits SET currentStreak = :streak, longestStreak = CASE WHEN :streak > longestStreak THEN :streak ELSE longestStreak END WHERE id = :habitId")
    suspend fun updateStreak(habitId: Long, streak: Int)

    @Query("UPDATE habits SET totalCompletion = totalCompletion + 1 WHERE id = :habitId")
    suspend fun incrementCompletions(habitId: Long)

    // Status operations
    @Query("UPDATE habits SET isActive = :isActive WHERE id = :habitId")
    suspend fun updateHabitStatus(habitId: Long, isActive: Boolean)

    @Query("UPDATE habits SET reminderEnabled = :enabled WHERE id = :habitId")
    suspend fun updateReminderStatus(habitId: Long, enabled: Boolean)

    // Bulk operations
    @Query("UPDATE habits SET isActive = 0 WHERE category = :category")
    suspend fun deactivateHabitsByCategory(category: String)

    @Query("DELETE FROM habits WHERE category = :category")
    suspend fun deleteHabitsByCategory(category: String)

    // Statistics queries
    @Query("SELECT COUNT(*) FROM habits WHERE isActive = 1")
    suspend fun getActiveHabitCount(): Int

    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getTotalHabitCount(): Int

    @Query("SELECT COUNT(*) FROM habits WHERE category = :category")
    suspend fun getHabitCountByCategory(category: String): Int

    // Category operations
    @Query("SELECT DISTINCT category FROM habits WHERE category IS NOT NULL")
    fun getAllCategories(): Flow<List<String>>

    // Top performing habits
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY currentStreak DESC LIMIT :limit")
    fun getTopStreakHabits(limit: Int): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY totalCompletion DESC LIMIT :limit")
    fun getTopCompletionHabits(limit: Int): Flow<List<Habit>>
} 