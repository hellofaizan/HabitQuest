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
interface HabitCompletionDao {

    // Basic CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion): Long

    @Update
    suspend fun updateCompletion(completion: HabitCompletion)

    @Delete
    suspend fun deleteCompletion(completion: HabitCompletion)

    // Query operations
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY completedAt DESC")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun getCompletionForDate(habitId: Long, dateKey: String): HabitCompletion?

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND dateKey >= :startDate AND dateKey <= :endDate ORDER BY completedAt DESC")
    fun getCompletionsInDateRange(
        habitId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<HabitCompletion>>

    // Check if habit is completed for a specific date
    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun isHabitCompletedForDate(habitId: Long, dateKey: String): Int

    // Statistics queries
    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId")
    suspend fun getTotalCompletionsForHabit(habitId: Long): Int

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND dateKey >= :startDate")
    suspend fun getCompletionsSinceDate(habitId: Long, startDate: String): Int

    // Date-based statistics
    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND completedAt >= :startDate AND completedAt <= :endDate")
    suspend fun getCompletionsInDateRange(habitId: Long, startDate: Date, endDate: Date): Int

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun getCompletionsForSpecificDate(habitId: Long, dateKey: String): Int

    // Streak calculation
    @Query(
        """
        SELECT COUNT(*) FROM (
            SELECT dateKey FROM habit_completions 
            WHERE habitId = :habitId 
            ORDER BY dateKey DESC
        ) t1
        WHERE dateKey >= (
            SELECT dateKey FROM habit_completions 
            WHERE habitId = :habitId 
            ORDER BY dateKey DESC 
            LIMIT 1
        )
    """
    )
    suspend fun getCurrentStreak(habitId: Long): Int

    // Bulk operations
    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteAllCompletionsForHabit(habitId: Long)

    @Query("DELETE FROM habit_completions WHERE dateKey < :dateKey")
    suspend fun deleteCompletionsBeforeDate(dateKey: String)

    // Delete old completions (for cleanup)
    @Query("DELETE FROM habit_completions WHERE completedAt < :cutoffDate")
    suspend fun deleteOldCompletions(cutoffDate: Date)

    // Recent completions
    @Query("SELECT * FROM habit_completions ORDER BY completedAt DESC LIMIT :limit")
    fun getRecentCompletions(limit: Int): Flow<List<HabitCompletion>>

    // Completion patterns
    @Query("SELECT dateKey, COUNT(*) as count FROM habit_completions WHERE habitId = :habitId GROUP BY dateKey ORDER BY dateKey DESC LIMIT :limit")
    fun getCompletionPattern(habitId: Long, limit: Int): Flow<List<CompletionPattern>>

    @Query("SELECT * FROM habit_completions WHERE habitId IN (:habitIds)")
    suspend fun getCompletionsForHabits(habitIds: List<Long>): List<HabitCompletion>
}

data class CompletionPattern(
    val dateKey: String,
    val count: Int
) 