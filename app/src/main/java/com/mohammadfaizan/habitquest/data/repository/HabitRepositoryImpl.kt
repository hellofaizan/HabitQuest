package com.mohammadfaizan.habitquest.data.repository

import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitDao
import com.mohammadfaizan.habitquest.domain.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

class HabitRepositoryImpl(
    private val habitDao: HabitDao
) : HabitRepository {

    // Habit Operations
    override fun getAllHabits(): Flow<List<Habit>> {
        return habitDao.getAllHabits()
    }

    override fun getActiveHabits(): Flow<List<Habit>> {
        return habitDao.getActiveHabits()
    }

    override suspend fun getHabitById(habitId: Long): Habit? {
        return habitDao.getHabitById(habitId)
    }

    override suspend fun insertHabit(habit: Habit): Long {
        return habitDao.insertHabit(habit)
    }

    override suspend fun updateHabit(habit: Habit) {
        habitDao.updateHabit(habit)
    }

    override suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteHabit(habit)
    }

    override suspend fun deleteHabitById(habitId: Long) {
        habitDao.deleteHabitById(habitId)
    }

    // Search and Filter
    override fun searchHabits(query: String): Flow<List<Habit>> {
        return habitDao.searchHabits(query)
    }

    override fun getHabitsByCategory(category: String): Flow<List<Habit>> {
        return habitDao.getHabitsByCategory(category)
    }

    override fun getHabitsCreatedSince(startDate: Date): Flow<List<Habit>> {
        return habitDao.getHabitsCreatedSince(startDate)
    }

    // Progress and Statistics
    override suspend fun updateStreak(habitId: Long, streak: Int) {
        habitDao.updateStreak(habitId, streak)
    }

    override suspend fun incrementCompletions(habitId: Long) {
        habitDao.incrementCompletions(habitId)
    }

    override suspend fun getActiveHabitCount(): Int {
        return habitDao.getActiveHabitCount()
    }

    override suspend fun getTotalHabitCount(): Int {
        return habitDao.getTotalHabitCount()
    }

    override suspend fun getHabitCountByCategory(category: String): Int {
        return habitDao.getHabitCountByCategory(category)
    }

    // Categories
    override fun getAllCategories(): Flow<List<String>> {
        return habitDao.getAllCategories()
    }

    // Top Performing Habits
    override fun getTopStreakHabits(limit: Int): Flow<List<Habit>> {
        return habitDao.getTopStreakHabits(limit)
    }

    override fun getTopCompletionHabits(limit: Int): Flow<List<Habit>> {
        return habitDao.getTopCompletionHabits(limit)
    }

    // Bulk Operations
    override suspend fun deactivateHabitsByCategory(category: String) {
        habitDao.deactivateHabitsByCategory(category)
    }

    override suspend fun deleteHabitsByCategory(category: String) {
        habitDao.deleteHabitsByCategory(category)
    }
} 