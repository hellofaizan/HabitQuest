package com.mohammadfaizan.habitquest.domain.usecase

import org.junit.Test
import org.junit.Assert.*

class UseCaseDataTest {
    
    @Test
    fun `test AddHabitRequest data class`() {
        val request = AddHabitRequest(
            name = "Test Habit",
            description = "Test Description",
            color = "#FF0000",
            icon = "fitness",
            targetCount = 3,
            frequency = "WEEKLY",
            category = "Health",
            reminderTime = "09:00",
            reminderEnabled = true
        )
        
        assertEquals("Test Habit", request.name)
        assertEquals("Test Description", request.description)
        assertEquals("#FF0000", request.color)
        assertEquals("fitness", request.icon)
        assertEquals(3, request.targetCount)
        assertEquals("WEEKLY", request.frequency)
        assertEquals("Health", request.category)
        assertEquals("09:00", request.reminderTime)
        assertTrue(request.reminderEnabled)
    }
    
    @Test
    fun `test AddHabitResult data class`() {
        val successResult = AddHabitResult(success = true, habitId = 1L)
        val errorResult = AddHabitResult(success = false, error = "Test error")
        
        assertTrue(successResult.success)
        assertEquals(1L, successResult.habitId)
        assertNull(successResult.error)
        
        assertFalse(errorResult.success)
        assertNull(errorResult.habitId)
        assertEquals("Test error", errorResult.error)
    }
    
    @Test
    fun `test GetHabitsRequest data class`() {
        val request = GetHabitsRequest(
            includeInactive = true,
            category = "Health",
            searchQuery = "exercise",
            limit = 10
        )
        
        assertTrue(request.includeInactive)
        assertEquals("Health", request.category)
        assertEquals("exercise", request.searchQuery)
        assertEquals(10, request.limit)
    }
    
    @Test
    fun `test GetHabitsResult data class`() {
        val result = GetHabitsResult(
            success = true,
            habits = emptyList(),
            error = null
        )
        
        assertTrue(result.success)
        assertTrue(result.habits.isEmpty())
        assertNull(result.error)
    }
    
    @Test
    fun `test CompleteHabitRequest data class`() {
        val request = CompleteHabitRequest(
            habitId = 1L,
            notes = "Great workout!",
            dateKey = "2024-01-01"
        )
        
        assertEquals(1L, request.habitId)
        assertEquals("Great workout!", request.notes)
        assertEquals("2024-01-01", request.dateKey)
    }
    
    @Test
    fun `test CompleteHabitResult data class`() {
        val result = CompleteHabitResult(
            success = true,
            wasAlreadyCompleted = false,
            newStreak = 5
        )
        
        assertTrue(result.success)
        assertFalse(result.wasAlreadyCompleted)
        assertEquals(5, result.newStreak)
        assertNull(result.error)
    }
    
    @Test
    fun `test GetHabitStatsRequest and Result data classes`() {
        val request = GetHabitStatsRequest(habitId = 1L)
        val result = GetHabitStatsResult(success = true, stats = null)
        
        assertEquals(1L, request.habitId)
        assertTrue(result.success)
        assertNull(result.stats)
        assertNull(result.error)
    }
    
    @Test
    fun `test DeleteHabitRequest and Result data classes`() {
        val request = DeleteHabitRequest(habitId = 1L)
        val result = DeleteHabitResult(success = true)
        
        assertEquals(1L, request.habitId)
        assertTrue(result.success)
        assertNull(result.error)
    }
    
    @Test
    fun `test GetHabitsWithCompletionStatusRequest and Result data classes`() {
        val request = GetHabitsWithCompletionStatusRequest(dateKey = "2024-01-01")
        val result = GetHabitsWithCompletionStatusResult(success = true, habits = emptyList())
        
        assertEquals("2024-01-01", request.dateKey)
        assertTrue(result.success)
        assertTrue(result.habits.isEmpty())
        assertNull(result.error)
    }
    
    @Test
    fun `test GetAnalyticsRequest and Result data classes`() {
        val request = GetAnalyticsRequest(
            habitId = 1L,
            weekStart = "2024-01-01",
            month = "2024-01"
        )
        val result = GetAnalyticsResult(success = true, analytics = null)
        
        assertEquals(1L, request.habitId)
        assertEquals("2024-01-01", request.weekStart)
        assertEquals("2024-01", request.month)
        assertTrue(result.success)
        assertNull(result.analytics)
        assertNull(result.error)
    }
} 