package com.mohammadfaizan.habitquest.domain.repository

import com.mohammadfaizan.habitquest.data.local.Habit
import com.mohammadfaizan.habitquest.data.local.HabitCompletion
import org.junit.Test
import org.junit.Assert.*

class HabitManagementDataTest {
    
    @Test
    fun `test HabitWithCompletions data class`() {
        val habit = Habit(name = "Test Habit", color = "#FF0000")
        val completions = listOf(
            HabitCompletion(habitId = 1L, dateKey = "2024-01-01"),
            HabitCompletion(habitId = 1L, dateKey = "2024-01-02")
        )
        
        val habitWithCompletions = HabitWithCompletions(habit, completions)
        
        assertEquals(habit, habitWithCompletions.habit)
        assertEquals(completions, habitWithCompletions.completions)
        assertEquals(2, habitWithCompletions.completions.size)
    }
    
    @Test
    fun `test HabitWithCompletionStatus data class`() {
        val habit = Habit(name = "Test Habit", color = "#FF0000")
        val habitWithStatus = HabitWithCompletionStatus(habit, true, 1)
        
        assertEquals(habit, habitWithStatus.habit)
        assertTrue(habitWithStatus.isCompleted)
        assertEquals(1, habitWithStatus.completionCount)
    }
    
    @Test
    fun `test HabitStats data class`() {
        val stats = HabitStats(
            totalCompletions = 10,
            currentStreak = 5,
            longestStreak = 15,
            completionRate = 75.5f,
            averageCompletionsPerDay = 2.5f
        )
        
        assertEquals(10, stats.totalCompletions)
        assertEquals(5, stats.currentStreak)
        assertEquals(15, stats.longestStreak)
        assertEquals(75.5f, stats.completionRate, 0.01f)
        assertEquals(2.5f, stats.averageCompletionsPerDay, 0.01f)
    }
    
    @Test
    fun `test WeeklyProgress data class`() {
        val weeklyProgress = WeeklyProgress(
            habitId = 1L,
            weekStart = "2024-01-01",
            daysCompleted = 5,
            totalDays = 7,
            completionRate = 71.4f,
            streak = 3
        )
        
        assertEquals(1L, weeklyProgress.habitId)
        assertEquals("2024-01-01", weeklyProgress.weekStart)
        assertEquals(5, weeklyProgress.daysCompleted)
        assertEquals(7, weeklyProgress.totalDays)
        assertEquals(71.4f, weeklyProgress.completionRate, 0.01f)
        assertEquals(3, weeklyProgress.streak)
    }
    
    @Test
    fun `test MonthlyProgress data class`() {
        val monthlyProgress = MonthlyProgress(
            habitId = 1L,
            month = "2024-01",
            daysCompleted = 20,
            totalDays = 31,
            completionRate = 64.5f,
            averageCompletionsPerDay = 1.2f
        )
        
        assertEquals(1L, monthlyProgress.habitId)
        assertEquals("2024-01", monthlyProgress.month)
        assertEquals(20, monthlyProgress.daysCompleted)
        assertEquals(31, monthlyProgress.totalDays)
        assertEquals(64.5f, monthlyProgress.completionRate, 0.01f)
        assertEquals(1.2f, monthlyProgress.averageCompletionsPerDay, 0.01f)
    }
} 